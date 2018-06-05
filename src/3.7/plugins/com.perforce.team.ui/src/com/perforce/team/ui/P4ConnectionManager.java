/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ClientError;
import com.perforce.p4java.exception.ConfigException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.TrustException;
import com.perforce.p4java.server.AuthTicketsHelper;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4JavaCallback;
import com.perforce.team.core.P4LogUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ConnectionMappedException;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IEventObject;
import com.perforce.team.core.p4java.IP4CommandListener;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.ServerNotSupportedException;
import com.perforce.team.ui.actions.ServerPropertiesAction;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.p4java.dialogs.PasswordDialog;
import com.perforce.team.ui.p4java.dialogs.PerforceErrorDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4ConnectionManager implements IErrorHandler, IEventObject {

    /**
     * CONNECTIONS
     */
    public static final String CONNECTIONS = "com.perforce.team.ui.DEPOT_CONNECTIONS"; //$NON-NLS-1$

    /**
     * SUPPRESS_ERROR_DIALOGS
     */
    public static final String SUPPRESS_ERROR_DIALOGS = "com.perforce.team.ui.SUPPRESS_ERROR_DIALOGS"; //$NON-NLS-1$

    /**
     * Is the specified message reporting the need to login with a password?
     * 
     * @param message
     * @return - true if a password needed login error
     */
    public static boolean isLoginError(String message) {
        return message != null
                && (message.indexOf("invalid or unset") > -1
                        || message.indexOf("please login again") > -1
                        || message.indexOf("please %'login'% again") > -1
                        || message.indexOf("The pipe is being closed") > -1);
    }

    /**
     * Is the specified exception message reporting a client non existent error?
     * 
     * @param message
     * @return - true if client non existent error
     */
    public static boolean isClientNonExistentError(String message) {
        return message != null
                && message.indexOf(IP4Connection.CLIENT_NON_EXISTENT_PREFIX) > -1
                && message.indexOf(IP4Connection.CLIENT_NON_EXISTENT_SUFFIX) > -1;
    }

    /**
     * Is the specified exception a {@link ServerNotSupportedException}?
     * 
     * @param exception
     * @return - true if exception is a {@link ServerNotSupportedException}
     */
    public static boolean isServerNotSupportedError(P4JavaException exception) {
        return exception instanceof ServerNotSupportedException;
    }

    /**
     * Is the specified exception either a {@link TrustException} or a
     * {@link ConnectionException} with a {@link TrustException} cause?
     * 
     * @param exception
     * @return - true if exception is either a {@link TrustException}or a
     *         {@link ConnectionException} with {@link TrustException} cause.
     */
    public static boolean isTrustError(P4JavaException exception) {
        TrustException te;
        if (exception instanceof TrustException) {
            te = (TrustException) exception;
        } else if (exception instanceof ConnectionException
                && exception.getCause() instanceof TrustException) {
            te = (TrustException) exception.getCause();
        } else {
            return false;
        }
        if (te.getType() == TrustException.Type.NEW_CONNECTION
                || te.getType() == TrustException.Type.NEW_KEY)
            return true;
        return false;
    }

    /**
     * Is the specified exception a {@link ConnectionException}?
     * 
     * @param exception
     * @return - true if exception is a {@link ConnectionException}
     */
    public static boolean isConnectionError(P4JavaException exception) {
        boolean connectionError = false;
        if (exception instanceof ConnectionException) {
            if (!(exception.getCause() instanceof ClientError)) {
                connectionError = true;
            }
        }
        return connectionError;
    }

    /**
     * Test the specified exception to see if it is caused by the absence of the
     * p4 command line executable
     * 
     * @param exception
     * @return - true if exception is due to missing p4 command line executable
     */
    public static boolean isCommandLineError(P4JavaException exception) {
        boolean error = false;
        if (exception instanceof ConfigException) {
            String message = exception.getMessage();
            error = message != null
                    && message
                            .endsWith("no such command line executable found");
        }
        return error;
    }

    private static final int WORK_OFFLINE = 0;
    private static final int EDIT_SETTINGS = 1;
    private static final int RETRY = 2;

    private static P4ConnectionManager manager = null;

    private boolean suppressErrors = false;
    private IP4Connection selectedConnection = null;

    // test supporessErrors flag, which in test mode for skip the 
    // message dialog to the default answer directly.
    public boolean isSuppressErrors() {
		return suppressErrors;
	}

	/**
     * Gets the connection manager
     * 
     * @return - manager instance
     */
    public static synchronized P4ConnectionManager getManager() {
        if (manager == null) {
            manager = new P4ConnectionManager();
        }
        return manager;
    }

    /**
     * Wrapper class for boolean
     */
    private static class Retry {

        boolean retry = false;
    }

    private P4ConnectionManager() {
        suppressErrors = System.getProperty(SUPPRESS_ERROR_DIALOGS) != null;
    }

    /**
     * Creates a new p4 collection
     * 
     * @return - collection
     */
    public P4Collection createP4Collection() {
        P4Collection collection = new P4Collection();
        collection.setErrorHandler(this);
        return collection;
    }

    /**
     * Open information dialog using the standard {@link MessageDialog} static
     * methods.
     * 
     * @param shell
     * @param title
     * @param message
     */
    public void openInformation(Shell shell, String title, String message) {
        if (!suppressErrors) {
            MessageDialog.openInformation(shell, title, message);
        } else {
            PerforceProviderPlugin.logInfo(message);
        }
    }

    /**
     * Open error dialog using the standard {@link MessageDialog} static
     * methods.
     * 
     * @param shell
     * @param title
     * @param message
     */
    public void openError(Shell shell, String title, String message) {
        PerforceProviderPlugin.logError(message);
        if (!suppressErrors) {
            MessageDialog.openError(shell, title, message);
        }
    }

    /**
     * Open confirm dialog by running a display sync exec
     * 
     * @param title
     * @param message
     * @return - true if confirmed
     */
    public boolean openConfirm(final String title, final String message) {
        final boolean[] confirmed = new boolean[] { false };
        PerforceProviderPlugin.logInfo(message);
        if (!suppressErrors) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    confirmed[0] = openConfirm(P4UIUtils.getShell(), title,
                            message);
                }
            });
        }
        return confirmed[0];
    }

    /**
     * Open confirm dialog using the standard {@link MessageDialog} static
     * methods.
     * 
     * @param shell
     * @param title
     * @param message
     * @return - true if confirmed
     */
    public boolean openConfirm(Shell shell, String title, String message) {
        boolean confirm = false;
        PerforceProviderPlugin.logInfo(message);
        if (!suppressErrors) {
            confirm = MessageDialog.openConfirm(shell, title, message);
        }
        return confirm;
    }

    /**
     * Open question dialolg using the standard {@link MessageDialog} static
     * method.
     * 
     * @param shell
     * @param title
     * @param message
     * @return - true if yes, false if no to question
     */
    public boolean openQuestion(Shell shell, String title, String message) {
        boolean answer = false;
        PerforceProviderPlugin.logInfo(message);
        if (!suppressErrors) {
            answer = MessageDialog.openQuestion(shell, title, message);
        }
        return answer;
    }

    /**
     * Open warning dialog using the standard {@link MessageDialog} static
     * methods.
     * 
     * @param shell
     * @param title
     * @param message
     */
    public void openWarning(Shell shell, String title, String message) {
        PerforceProviderPlugin.logWarning(message);
        if (!suppressErrors) {
            MessageDialog.openWarning(shell, title, message);
        }
    }

    /**
     * Creates a new p4 collection
     * 
     * @param resource
     *            - initial resource
     * @return - collection
     */
    public P4Collection createP4Collection(IP4Resource resource) {
        return createP4Collection(new IP4Resource[] { resource });
    }

    /**
     * Creates a new p4 collection
     * 
     * @param resources
     *            - initial resources
     * @return - collection
     */
    public P4Collection createP4Collection(IP4Resource[] resources) {
        P4Collection collection = new P4Collection(resources);
        collection.setErrorHandler(this);
        return collection;
    }

    private boolean connectionInvalid(IP4Connection connection) {
        return connection == null || connection.isOffline()
                || connection.isDisposed();
    }

    private void goOffline(IP4Connection connection) {
        connection.setOffline(true);
        P4Workspace.getWorkspace().notifyListeners(
                new P4Event(EventType.CHANGED, connection));
    }

    private void editConnection(IP4Connection connection) {
        ServerPropertiesAction action = new ServerPropertiesAction();
        action.execute(connection);
    }

    /**
     * Should login with no password be attempted based on user setting of
     * {@link P4JavaCallback#SSO_CMD_ENV_KEY} on advanced workspace properties
     * object or as a {@link System#getenv(String)} environment variable.
     * 
     * @return - true if empty password login attempt should be made
     */
    private boolean trySSO() {
        return P4Workspace.getWorkspace().getAdvancedProperties()
                .getProperty(P4JavaCallback.SSO_CMD_ENV_KEY) != null
                || System.getenv(P4JavaCallback.SSO_CMD_ENV_KEY) != null;
    }

    private void handleLoginError(final IP4Connection connection,
            final Retry returnCode, final boolean allowOffline,
            final long exceptionTime) {
        if (connection.getParameters().savePassword()) {
            String password = connection.getParameters().getPassword();
            if (password != null) {
                if (connection.login(password)) {
                    returnCode.retry = true;
                    return;
                }
            }
        }

        // Attempt to avoid showing password dialog to users who appear to be
        // using SSO. Instead make a login attempt for them and set retry to
        // true if successful.
        if (trySSO()) {
            // Login with empty string to activate SSO login
            if (connection.login("")) { //$NON-NLS-1$
                returnCode.retry = true;
                return;
            }
        }

		// Use existing auth ticket first then fall back to params
		// auth ticket and lastly attempt to look up from possible
		// tickets file
		try {
			String ticket = null;
			IServer server = connection.getServer();
			IServerInfo info = connection.getServer().getServerInfo();
			ticket = AuthTicketsHelper.getTicketValue(ConnectionParameters.getTicketUser(connection.getParameters(), server),
					info.getServerAddress(), P4Connection.getP4TicketsOSLocation());
			if(ticket!=null && !ticket.equals(server.getAuthTicket())){
				connection.getServer().setAuthTicket(ticket);
				returnCode.retry = true;
				return;
			}
		} catch (Exception e) {
			PerforceProviderPlugin.logError(e);
		}

        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                // If the last successful login time is after or at the same
                // time as the exception was thrown then retry. If a second
                // access exception is thrown then the password dialog will be
                // shown since the exception time will be later than the logged
                // in time.
                if (connection.getLoggedInTime() >= exceptionTime) {
                    returnCode.retry = true;
                    return;
                }
                if (connection.isLoggedIn()) {
                    connection.markLoggedOut();
                    returnCode.retry = true;
                    return;
                }
                PasswordDialog dialog = new PasswordDialog(P4UIUtils
                        .getDialogShell(), connection, allowOffline);
                if (PasswordDialog.OK == dialog.open()) {
                    String password = dialog.getPassword();
                    if (password != null) {
                        if (connection.getParameters().savePassword()) {
                            connection.getParameters().setPassword(password);
                            PerforceUIPlugin.storePasswordInUI(connection
                                    .getParameters());
                        }
                        connection.login(password);
                        returnCode.retry = true;
                    }
                }
            }

        });
    }

    private void handleClientNonExistentError(final IP4Connection connection,
            final Retry returnCode, final boolean showOptions) {
        // Client spec is probably wrong. Give them a chance to repair
        // it and retry...
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                ConnectionParameters params = connection.getParameters();
                String msg = MessageFormat.format(
                        Messages.P4ConnectionManager_ClientUnknown,
                        params.getClientNoNull());
                String[] options = null;
                if (showOptions) {
                    options = new String[] {
                            Messages.P4ConnectionManager_WorkOffline,
                            Messages.P4ConnectionManager_EditSettings,
                            Messages.P4ConnectionManager_Retry, };
                } else {
                    options = new String[] { IDialogConstants.OK_LABEL };
                }
                MessageDialog errdlg = new MessageDialog(P4UIUtils
                        .getDialogShell(),
                        Messages.P4ConnectionManager_ConnectionError, null,
                        msg, MessageDialog.ERROR, options, 0);
                int option = errdlg.open();
                if (showOptions) {
                    if (option == RETRY) {
                        returnCode.retry = true;
                    } else if (option == WORK_OFFLINE) {
                        goOffline(connection);
                    } else if (option == EDIT_SETTINGS) {
                        editConnection(connection);
                    }
                }
            }
        });
    }

    private void handleServerNotSupportedError(final IP4Connection connection,
            final P4JavaException exception, final Retry returnCode,
            final boolean showOptions) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                // ConnectionParameters params = connection.getParameters();
                String msg = exception.getMessage();
                //			+ "\n\n" //$NON-NLS-1$
                // + params.getPortNoNull()
                // + Messages.P4ConnectionManager_P4User
                // + params.getUserNoNull()
                // + Messages.P4ConnectionManager_P4Client
                // + params.getClientNoNull()
                //			+ "\n"; //$NON-NLS-1$
                String[] options = null;
                if (showOptions) {
                    options = new String[] {
                            Messages.P4ConnectionManager_EditSettings,
                            Messages.P4ConnectionManager_Retry, };
                } else {
                    options = new String[] { IDialogConstants.OK_LABEL };
                }
                MessageDialog errdlg = new MessageDialog(P4UIUtils
                        .getDialogShell(),
                        Messages.P4ConnectionManager_ServerNotSupported, null,
                        msg, MessageDialog.ERROR, options, 0);
                int option = errdlg.open();
                if (showOptions) {
                    // Subtract 1 from the indices since there is no
                    // WORK_OFFLINE button
                    if (option == RETRY - 1) {
                        returnCode.retry = true;
                    } else if (option == EDIT_SETTINGS - 1) {
                        editConnection(connection);
                    }
                }
            }
        });
    }

    private void handleTrustError(final IP4Connection connection,
            final Exception exception, final Retry returnCode) {
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                TrustException te;
                if (exception instanceof TrustException)
                    te = (TrustException) exception;
                else
                    te = (TrustException) exception.getCause();

                P4TrustDialog trustDlg = new P4TrustDialog(P4UIUtils
                        .getDialogShell(), te.getServerHostPort(), te
                        .getServerIpPort(), te.getFingerprint(), te.getType());
                int option = trustDlg.open();
                if (option == P4TrustDialog.CONNECT) {
                	try {
                		returnCode.retry = P4Workspace.addTrust(
                            te.getServerIpPort(), te.getFingerprint());
                	} catch (P4JavaException e) {
                		handleConnectionError(connection, e, returnCode, true);
                	}
                } else {
                    goOffline(connection);
                }
            }
        });
    }

    private void handleConnectionError(final IP4Connection connection,
            final Exception exception, final Retry returnCode,
            final boolean showOptions) {
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                final String msg = exception.getLocalizedMessage();
                List<String> causes = new ArrayList<String>();
                String last = msg;
                Throwable cause = exception.getCause();
                while (cause != null) {
                    String nextCause = cause.getLocalizedMessage();
                    if (!StringUtils.equals(nextCause,last)) {
                        causes.add(nextCause);
                        last = nextCause;
                    }
                    if (cause == cause.getCause())
                        break;
                    cause = cause.getCause();
                }
                final String details = StringUtils.join(causes, "\n");

                String[] options = null;
                if (showOptions) {
                    options = new String[] {
                            Messages.P4ConnectionManager_WorkOffline,
                            Messages.P4ConnectionManager_EditSettings,
                            Messages.P4ConnectionManager_Retry, };
                } else {
                    options = new String[] { IDialogConstants.OK_LABEL };
                }

                MessageDialog errdlg = new MessageDialog(P4UIUtils
                        .getDialogShell(),
                        Messages.P4ConnectionManager_ConnectionError, null,
                        msg, MessageDialog.ERROR, options, 0) {

                    // add details area if exception contains cause info

                    @Override
                    protected Control createCustomArea(Composite parent) {
                        if (details.isEmpty())
                            return null;

                        Composite displayArea = new Composite(parent, SWT.NONE);
                        displayArea.setLayout(new GridLayout(2, false));
                        displayArea.setLayoutData(new GridData(SWT.FILL,
                                SWT.FILL, true, true));
                        Label detailsLabel = new Label(displayArea, SWT.NONE);
                        detailsLabel.setLayoutData(new GridData(SWT.LEFT,
                                SWT.TOP, false, false));
                        detailsLabel.setText("Details:");
                        Text detailsText = DialogUtils.createSelectableLabel(
                                displayArea, new GridData(SWT.FILL, SWT.FILL,
                                        true, false));
                        detailsText.setText(details);
                        detailsText.setSelection(0, 0);
                        return displayArea;
                    }

                    @Override
                    protected boolean customShouldTakeFocus() {
                        return false;
                    }
                };

                int option = errdlg.open();
                if (showOptions) {
                    if (option == RETRY) {
                        returnCode.retry = true;
                    } else if (option == WORK_OFFLINE) {
                        goOffline(connection);
                    } else if (option == EDIT_SETTINGS) {
                        editConnection(connection);
                    }
                }
            }
        });
    }

    private void handleCommandLineError(final IP4Connection connection,
            final boolean showOptions) {
        // P4 command line app not found. Tell the user then put it
        // offline.
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                String[] options = null;
                if (showOptions) {
                    options = new String[] { Messages.P4ConnectionManager_WorkOffline, };
                } else {
                    options = new String[] { IDialogConstants.OK_LABEL };
                }
                MessageDialog errdlg = new MessageDialog(P4UIUtils
                        .getDialogShell(),
                        Messages.P4ConnectionManager_P4NotFoundTitle, null,
                        Messages.P4ConnectionManager_P4NotFoundMessage,
                        MessageDialog.ERROR, options, 0);
                int option = errdlg.open();
                if (showOptions) {
                    if (option == WORK_OFFLINE) {
                        goOffline(connection);
                    }
                }
            }
        });
    }

    private void handleStandardError(final IP4Connection connection,
            final P4JavaException exception, final boolean showOptions) {
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                if (!dispatchAndCheck(connection)) {
                    return;
                }
                String[] options = null;
                if (showOptions) {
                    options = new String[] { IDialogConstants.OK_LABEL,
                            Messages.P4ConnectionManager_WorkOffline,
                            Messages.P4ConnectionManager_EditSettings, };
                } else {
                    options = new String[] { IDialogConstants.OK_LABEL };
                }
                MessageDialog errdlg = new MessageDialog(P4UIUtils
                        .getDialogShell(),
                        Messages.P4ConnectionManager_PerforceError, null,
                        Messages.P4ConnectionManager_ErrorHasOccurred
                                + exception.getLocalizedMessage(),
                        MessageDialog.ERROR, options, 0);
                int option = errdlg.open();
                if (showOptions) {
                    // Add 1 to the indices since the OK button is now 0 and the
                    // other two have shifted up 1
                    if (option == WORK_OFFLINE + 1) {
                        goOffline(connection);
                    } else if (option == EDIT_SETTINGS + 1) {
                        editConnection(connection);
                    }
                }
            }
        });
    }

    private boolean suppressException(IP4Connection connection) {
        return connectionInvalid(connection) || suppressErrors;
    }

    /**
     * @see #displayException(IP4Connection, P4JavaException, boolean, boolean)
     * @param connection
     * @param exception
     */
    public void displayException(IP4Connection connection,
            P4JavaException exception) {
        displayException(connection, exception, false, false);
    }

    /**
     * Displays an exception in an error dialog with no options besides an 'OK'
     * button. Should be used to display exceptions that can't fixed by going
     * offline or editing the connection such as editing/creating a client or
     * job. The one case this method can handle is login if handleLogin is
     * specified. The returned value will always be false if handeLogin is
     * false.
     * 
     * @param connection
     * @param exception
     * @param handleLogin
     * @param allowOffline
     * @return - true to retry, false otherwise
     */
    public boolean displayException(IP4Connection connection,
            P4JavaException exception, boolean handleLogin, boolean allowOffline) {
        boolean retry = false;
        if (suppressException(connection)) {
            return false;
        }
        long exceptionTime = System.currentTimeMillis();
        if (PerforceUIPlugin.isUIThread()) {
            retry = internalDisplay(connection, exception, handleLogin,
                    allowOffline, exceptionTime);
        } else {
            synchronized (this) {
                retry = internalDisplay(connection, exception, handleLogin,
                        allowOffline, exceptionTime);
            }
        }
        return retry;
    }

    private boolean internalDisplay(IP4Connection connection,
            P4JavaException exception, boolean handleLogin,
            boolean allowOffline, long exceptionTime) {
        Retry returnCode = new Retry();
        if (!connectionInvalid(connection) && !suppressErrors) {
            String message = exception.getMessage();
            if (handleLogin && isLoginError(message)) {
                handleLoginError(connection, returnCode, allowOffline,
                        exceptionTime);
            } else if (isClientNonExistentError(message)) {
                handleClientNonExistentError(connection, null, false);
            } else if (isServerNotSupportedError(exception)) {
                handleServerNotSupportedError(connection, exception, null,
                        false);
            } else if (isTrustError(exception)) {
                handleTrustError(connection, exception, returnCode);
            } else if (isConnectionError(exception)) {
                handleConnectionError(connection, exception, null, false);
            } else if (isCommandLineError(exception)) {
                handleCommandLineError(connection, false);
            } else {
                handleStandardError(connection, exception, false);
            }
        }
        return returnCode.retry;
    }

    private boolean dispatchAndCheck(IP4Connection connection) {
        // Make sure other dialogs have completed first
        while (P4UIUtils.getDisplay().readAndDispatch()) {
        }
        return !connectionInvalid(connection);
    }

    private boolean internalRetry(IP4Connection connection,
            P4JavaException exception, long exceptionTime) {
        Retry returnCode = new Retry();
        String message = exception.getMessage();
        if (isLoginError(message)) {
            handleLoginError(connection, returnCode, true, exceptionTime);
        } else if (isClientNonExistentError(message)) {
            handleClientNonExistentError(connection, returnCode, true);
        } else if (isServerNotSupportedError(exception)) {
            handleServerNotSupportedError(connection, exception, returnCode,
                    true);
        } else if (isTrustError(exception)) {
            handleTrustError(connection, exception, returnCode);
        } else if (isConnectionError(exception)) {
            handleConnectionError(connection, exception, returnCode, true);
        } else if (isCommandLineError(exception)) {
            handleCommandLineError(connection, true);
        } else {
            handleStandardError(connection, exception, true);
        }
        return returnCode.retry;
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorHandler#shouldRetry(com.perforce.team.core.p4java.IP4Connection,
     *      com.perforce.p4java.exception.P4JavaException)
     */
    public boolean shouldRetry(IP4Connection connection,
            P4JavaException exception) {
        if (suppressException(connection)) {
            return false;
        }
        boolean retry = false;
        long exceptionTime = System.currentTimeMillis();
        if (PerforceUIPlugin.isUIThread()) {
            retry = internalRetry(connection, exception, exceptionTime);
        } else {
            synchronized (this) {
                retry = internalRetry(connection, exception, exceptionTime);
            }
        }
        return retry;
    }

    /**
     * Set selected connection
     * 
     * @param connection
     */
    public void setSelection(IP4Connection connection) {
        this.selectedConnection = connection;
    }

    /**
     * Get selected connection, optionally checking that the connection still
     * exists in the workspace store.
     * 
     * @param checkStore
     * @return - selected connection or null if none exists
     */
    public IP4Connection getSelection(boolean checkStore) {
        IP4Connection connection = this.selectedConnection;
        if (checkStore && connection != null) {
            boolean exists = false;
            for (IP4Connection storedConnection : getConnections()) {
                if (connection.equals(storedConnection)) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                connection = null;
            }
        }
        return connection;
    }

    /**
     * Gets a p4 connection from the manager
     * 
     * @param params
     * @return - connection manager
     */
    public IP4Connection getConnection(ConnectionParameters params) {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                params);
        if (!connection.isConnected() && !connection.isOffline()) {
            connection.connect();
        }
        return connection;
    }

    /**
     * Gets the current connection associated with the project
     * 
     * @param project
     * @param connect
     *            - true to connect if the connection retrieved is not connected
     *            and not offline
     * @return - p4 connection
     */
    public IP4Connection getConnection(IProject project, boolean connect) {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        if (connection != null) {
            if (connect && !connection.isConnected() && !connection.isOffline()) {
                connection.connect();
            }
        }
        return connection;
    }

    /**
     * Gets the current connection associated with the project
     * 
     * @param project
     * @return - p4 connection
     */
    public IP4Connection getConnection(IProject project) {
        return getConnection(project, true);
    }

    /**
     * Removes a connection from the cache
     * 
     * @param parameters
     */
    public void removeConnection(ConnectionParameters parameters) {
        try {
            P4Workspace.getWorkspace().removeConnection(parameters);
        } catch (ConnectionMappedException cme) {
            showMappedException(cme, parameters);
        }
    }

    /**
     * Add a connection to the workspace cache
     * 
     * @param parameters
     */
    public void add(ConnectionParameters parameters) {
        if (parameters != null) {
            if (!containsConnection(parameters)) {
                IP4Connection connection = P4Workspace.getWorkspace()
                        .getConnection(parameters);
                
                boolean showDeletedFiles = PerforceUIPlugin.getPlugin().getPreferenceStore().getBoolean(
                		IPreferenceConstants.SHOW_DELETED_FILES);
                boolean filterClientFiles = PerforceUIPlugin.getPlugin().getPreferenceStore().getBoolean(
                		IPreferenceConstants.FILTER_CLIENT_FILES);                
                connection.setShowFoldersWIthOnlyDeletedFiles(showDeletedFiles);
                connection.setShowClientOnly(filterClientFiles);
                
                if (!connection.isOffline()) {
                    connection.login(parameters.getPassword());
                    connection.connect();
                }
            } else {
                P4UIUtils.getDisplay().syncExec(new Runnable() {

                    public void run() {
                        MessageDialog
                                .openInformation(
                                        P4UIUtils.getShell(),
                                        Messages.P4ConnectionManager_ConnectionAlreadyExistsTitle,
                                        Messages.P4ConnectionManager_ConnectionAlreadyExistsMessage);
                    }

                });
            }
        }
    }

    /**
     * Removes a connection from the cache
     * 
     * @param connection
     */
    public void removeConnection(IP4Connection connection) {
        try {
            P4Workspace.getWorkspace().removeConnection(connection);
        } catch (final ConnectionMappedException cme) {
            showMappedException(cme, connection.getParameters());
        }
    }

    private void showMappedException(final ConnectionMappedException cme,
            ConnectionParameters params) {
        final String message = MessageFormat.format(
                Messages.P4ConnectionManager_CantRemoveConnectionMessage,
                params.getDisplayString());
        P4UIUtils.getDisplay().syncExec(new Runnable() {

            public void run() {
                ErrorDialog.openError(P4UIUtils.getShell(),
                        Messages.P4ConnectionManager_CantRemoveConnectionTitle,
                        message, cme.getStatus());
            }

        });
    }

    /**
     * Gets the connections
     * 
     * @return - array of connections
     */
    public IP4Connection[] getConnections() {
        return P4Workspace.getWorkspace().getConnections();
    }

    /**
     * Does the connection manager contain a connection?
     * 
     * @param parameters
     * @return - true if the connection exists for the parameters, false
     *         otherwise
     */
    public boolean containsConnection(ConnectionParameters parameters) {
        return P4Workspace.getWorkspace().containsConnection(parameters);
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void addListener(IP4Listener listener) {
        P4Workspace.getWorkspace().addListener(listener);
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#clearListeners()
     */
    public void clearListeners() {
        P4Workspace.getWorkspace().clearListeners();
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#notifyListeners(com.perforce.team.core.p4java.P4Event)
     */
    public void notifyListeners(P4Event event) {
        P4Workspace.getWorkspace().notifyListeners(event);
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#removeListener(com.perforce.team.core.p4java.IP4Listener)
     */
    public void removeListener(IP4Listener listener) {
        P4Workspace.getWorkspace().removeListener(listener);
    }

    /**
     * @see com.perforce.team.core.p4java.IEventObject#addListeners(com.perforce.team.core.p4java.IP4Listener[])
     */
    public void addListeners(IP4Listener[] listeners) {
        P4Workspace.getWorkspace().addListeners(listeners);
    }

    /**
     * @param resource
     * @return - p4 resource
     * @see P4Workspace#asyncGetResource(IResource)
     */
    public IP4Resource asyncGetResource(final IResource resource) {
        return P4Workspace.getWorkspace().asyncGetResource(resource);
    }

    /**
     * @param resource
     * @return - p4 resource
     * @see P4Workspace#getResource(IResource)
     */
    public IP4Resource getResource(IResource resource) {
        return P4Workspace.getWorkspace().getResource(resource);
    }

    /**
     * Adds a command listener to the p4 workspace
     * 
     * @param listener
     */
    public void addCommandListener(IP4CommandListener listener) {
        P4Workspace.getWorkspace().addCommandListener(listener);
    }

    /**
     * Removes a command listener from the p4 workspace
     * 
     * @param listener
     */
    public void removeCommandListener(IP4CommandListener listener) {
        P4Workspace.getWorkspace().removeCommandListener(listener);
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorHandler#handleErrorSpecs(com.perforce.p4java.core.file.IFileSpec[])
     */
    public void handleErrorSpecs(final IFileSpec[] specs) {
        if (!suppressErrors) {
            UIJob job = new UIJob(
                    Messages.P4ConnectionManager_DisplayingPerforceErrors) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    // Make sure other dialogs have completed first
                    while (getDisplay()!=null && !getDisplay().isDisposed() && getDisplay().readAndDispatch()) {
                    }
                    PerforceErrorDialog.showErrors(P4UIUtils.getDialogShell(),
                            specs);
                    return Status.OK_STATUS;
                }

            };
            job.schedule();
        } else {
            P4LogUtils.logError(specs);
        }
    }

    /**
     * Edits a connection by removing the connection, updating the mapped
     * projects and adding back in the new connection
     * 
     * @param connection
     * @param newParameters
     */
    public void editConnection(IP4Connection connection,
            ConnectionParameters newParameters) {
        IP4Connection newConnection = P4Workspace.getWorkspace()
                .editConnection(connection, newParameters);
        if (newConnection != null && !newConnection.isConnected()
                && !newConnection.isOffline()) {
            newConnection.connect();
        }
    }

    /**
     * @return number of connections
     * @see P4Workspace#size()
     */
    public int size() {
        return P4Workspace.getWorkspace().size();
    }

    public static boolean retryAfterLogin(IP4Connection connection,
            AccessException exception) {
        if (connection == null || exception == null) {
            return false;
        }

        IErrorHandler handler = connection.getErrorHandler();
        if (handler == null) {
            handler = P4Workspace.getWorkspace().getErrorHandler();
        }
        if (handler != null) {
            return handler.shouldRetry(connection, exception);
        } else {
            return false;
        }
    }

}
