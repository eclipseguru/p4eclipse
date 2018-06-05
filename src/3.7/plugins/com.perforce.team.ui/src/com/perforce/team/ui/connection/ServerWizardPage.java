/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.AuthTicketsHelper;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerWizardPage extends BaseConnectionWizardPage {

    /**
     * RECENT_SERVER_LENGTH
     */
    public static final int RECENT_SERVER_LENGTH = 10;

    /**
     * SERVERS_SECTION
     */
    public static final String SERVERS_SECTION = "Servers"; //$NON-NLS-1$

    /**
     * RECENT_KEY
     */
    public static final String RECENT_KEY = "recent"; //$NON-NLS-1$

    /**
     * LEGACY_SECTION
     */
    public static final String LEGACY_SECTION = "NewConnection"; //$NON-NLS-1$

    private Composite displayArea;

    private Group locationGroup;
    private Label previousServersLabel;
    private Combo previousServersCombo;
    private Label serverLabel;
    private Text serverText;
    private Label charsetLabel;
    private Combo charsetCombo;

    private Group authGroup;
    private Label userNameLabel;
    private Text userNameText;
    private Label passwordLabel;
    private Text passwordText;
    private Button savePasswordButton;

    private String server = null;
    private String user = null;
    private String password = null;
    private boolean savePassword = false;
    private String charset = null;

    private ModifyListener modify = new ModifyListener() {

        public void modifyText(ModifyEvent e) {
            validatePage();
        }
    };

    /**
     * @param pageName
     */
    public ServerWizardPage(String pageName) {
        super(pageName);
        setImageDescriptor(PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_SHARE_WIZARD));
        setTitle(Messages.ServerWizardPage_AddConnectionTitle);
        setDescription(Messages.ServerWizardPage_AddConnectionMessage);
    }

    private void loadServerHistory() {
        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        String[] serverHistory = null;
        if (settings != null) {
            IDialogSettings section = settings.getSection(SERVERS_SECTION);
            if (section != null) {
                serverHistory = section.getArray(RECENT_KEY);
            } else {
                // Support for legacy connection dialog
                IDialogSettings oldSection = settings
                        .getSection(LEGACY_SECTION);
                if (oldSection != null) {
                    String[] oldHistory = oldSection.getArray(RECENT_KEY);
                    if (oldHistory != null) {
                        List<String> migratedHistory = new ArrayList<String>();
                        for (String old : oldHistory) {
                            ConnectionParameters params = new ConnectionParameters(
                                    old);
                            String port = params.getPort();
                            if (port != null && !migratedHistory.contains(port)) {
                                migratedHistory.add(port);
                            }
                        }
                        serverHistory = migratedHistory.toArray(new String[0]);
                    }
                }
            }
        }
        if (serverHistory != null && serverHistory.length > 0) {
            for (String entry : serverHistory) {
                if (entry != null && entry.trim().length() > 0) {
                    previousServersCombo.add(entry);
                }
            }
        }
    }

    /**
     * Save server history
     */
    public void saveServerHistory() {
        String currentServer = serverText.getText().trim();
        if (currentServer.isEmpty()) {
            return;
        }

        List<String> history = new ArrayList<String>(
                Arrays.asList(previousServersCombo.getItems()));
        for(Iterator<String> iter=history.iterator();iter.hasNext();){
        	if(iter.next().equals(currentServer))
        		iter.remove();
        }
        history.add(0, currentServer);
        if (history.size() > RECENT_SERVER_LENGTH) {
            history.remove(RECENT_SERVER_LENGTH);
        }

        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        if (settings != null) {
            IDialogSettings section = settings.getSection(SERVERS_SECTION);
            if (section == null) {
                section = settings.addNewSection(SERVERS_SECTION);
            }
            section.put(RECENT_KEY, history.toArray(new String[0]));
        }
    }

    private void createCharsetArea(Composite parent) {
        charsetLabel = new Label(parent, SWT.LEFT);
        charsetLabel.setText(Messages.ServerWizardPage_Charset);

        charsetCombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
        charsetCombo.setItems(P4UIUtils.getDisplayCharsets());
        charsetCombo.select(charsetCombo.indexOf("none")); //$NON-NLS-1$
        charsetCombo
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        charset = charsetCombo.getText();
        charsetCombo.addModifyListener(modify);
    }

    private void createLocationGroup(Composite parent) {
        locationGroup = new Group(parent, SWT.NONE);
        locationGroup.setText(Messages.ServerWizardPage_Location);
        locationGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        locationGroup.setLayout(new GridLayout(2, false));

        previousServersLabel = new Label(locationGroup, SWT.LEFT);
        previousServersLabel.setText(Messages.ServerWizardPage_RecentServers);
        previousServersCombo = new Combo(locationGroup, SWT.READ_ONLY
                | SWT.DROP_DOWN);
        previousServersCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        loadServerHistory();
        previousServersCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                serverText.setText(previousServersCombo.getText());
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        serverLabel = new Label(locationGroup, SWT.LEFT);
        serverLabel.setText(Messages.ServerWizardPage_Server);
        serverText = new Text(locationGroup, SWT.SINGLE | SWT.BORDER);
        serverText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        serverText.addModifyListener(modify);

        createCharsetArea(locationGroup);

    }

    private void createAuthGroup(Composite parent) {
        authGroup = new Group(parent, SWT.NONE);
        authGroup.setText(Messages.ServerWizardPage_Authentication);
        authGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        authGroup.setLayout(new GridLayout(2, false));

        userNameLabel = new Label(authGroup, SWT.NONE);
        userNameLabel.setText(Messages.ServerWizardPage_User);
        userNameText = new Text(authGroup, SWT.SINGLE | SWT.BORDER);
        userNameText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        userNameText.addModifyListener(modify);

        passwordLabel = new Label(authGroup, SWT.NONE);
        passwordLabel.setText(Messages.ServerWizardPage_Password);
        passwordText = new Text(authGroup, SWT.SINGLE | SWT.BORDER
                | SWT.PASSWORD);
        passwordText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        passwordText.addModifyListener(modify);

        savePasswordButton = new Button(authGroup, SWT.CHECK);
        savePasswordButton.setText(Messages.ServerWizardPage_SavePassword);
        GridData spbData = new GridData(SWT.FILL, SWT.FILL, true, false);
        spbData.horizontalSpan = 2;
        savePasswordButton.setLayoutData(spbData);
        savePasswordButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                savePassword = savePasswordButton.getSelection();
                validatePage();
            }

        });
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
	public void createControl(Composite parent) {
		displayArea = new Composite(parent, SWT.NONE);
		displayArea.setLayout(new GridLayout(1, true));
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		createLocationGroup(displayArea);
		createAuthGroup(displayArea);

		setControl(displayArea);
		setPageComplete(false);

		if (getContainer() instanceof WizardDialog) {
			WizardDialog wd = (WizardDialog) getContainer();
			wd.addPageChangingListener(new IPageChangingListener() {
				public void handlePageChanging(final PageChangingEvent event) {
					final String[] messages=new String[1];
					if (event.getCurrentPage() == ServerWizardPage.this && event.getTargetPage()!=getPreviousPage()) {
						try {
							getContainer().run(true, true,
									new IRunnableWithProgress() {
										public void run(IProgressMonitor monitor)
												throws InvocationTargetException,
												InterruptedException {
											monitor.beginTask(
													Messages.ServerWizardPage_TestingConnection,
													IProgressMonitor.UNKNOWN);
											event.doit=false;
											IP4Connection conn =null;
											try {
												ConnectionParameters params = new ConnectionParameters();
												params.setPort(getPort());
												params.setClient(getClient());
												params.setUser(getUser());
												params.setCharset(getCharset());
												params.setPassword(getPassword());
												params.setSavePassword(savePassword());

												IErrorHandler handler = new ErrorHandler() {
													
													@Override
													public boolean shouldRetry(IP4Connection connection,
															final P4JavaException exception) {
														boolean retry=P4ConnectionManager.getManager().displayException(
																connection, exception, false, false);
														return retry;
													}
												};
												conn = P4Workspace
														.getWorkspace()
														.tryToConnect(params,handler);
												
												IServerInfo info = conn.getServer().getServerInfo();
												String ticket = null;
												try {
													ticket = AuthTicketsHelper.getTicketValue(ConnectionParameters.getTicketUser(params, conn.getServer()),info.getServerAddress(),
															P4Connection.getP4TicketsOSLocation());
												} catch (Throwable t) {
													PerforceProviderPlugin.logWarning(t);
												}
												
												if(ticket!=null && params.getPasswordNoNull().isEmpty()){
													// try to use authticket
													conn.getServer().setAuthTicket(ticket);
													event.doit=true;
												}else{
													// try login with password
													conn.getServer().login(params.getPassword());
													event.doit=true;
												}
												
												if(event.doit)
													setWizardConnection(conn);
											} catch (Throwable t) {
												messages[0]=t.getLocalizedMessage();    
												PerforceProviderPlugin.logError(t);
												if(conn!=null){
													conn.dispose();
												}
											}
											monitor.done();
										}
									});
						} catch (Exception e) {
							e.printStackTrace();
							event.doit = false;
						}
						if(event.doit==false){
							setErrorMessage(messages[0]);
							getContainer().updateButtons();
						}
					}
				}
			});
		}
    }

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getPort()
     */
    @Override
    public String getPort() {
        return this.server;
    }

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getUser()
     */
    @Override
    public String getUser() {
        return this.user;
    }

    /**
     * @see com.perforce.team.ui.connection.BaseConnectionWizardPage#getPassword()
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the port field
     * 
     * @param port
     */
    public void setPort(String port) {
        if (port != null) {
            this.serverText.setText(port);
        }
    }

    /**
     * Set the user field
     * 
     * @param user
     */
    public void setUser(String user) {
        if (user != null) {
            this.userNameText.setText(user);
        }
    }

    private void validatePage() {
        this.server = serverText.getText().trim();
        this.user = userNameText.getText().trim();
        this.charset = P4UIUtils.getP4Charset(charsetCombo.getText());
        this.password = passwordText.getText();
        if (password.isEmpty()) {
            this.password = null;
        }
        
        String message = null;
        if (!this.server.matches("(ssl:)?[^:]+:[1-9]\\d*")) { //$NON-NLS-1$
            message = Messages.ServerWizardPage_MustSpecifyServer;
        }
        else if (this.user.isEmpty()) {
            message = Messages.ServerWizardPage_MustSpecifyUser;
        }

        setPageComplete(message == null);
        setErrorMessage(message);
        if(getContainer().getCurrentPage()!=null)
        	getContainer().updateButtons();
    }

    /**
     * Is the password configured to be saved?
     * 
     * @return - true to save password
     */
    public boolean savePassword() {
        return this.savePassword;
    }

    /**
     * Set the charset name. The specified charset should a p4 charset name from
     * {@link CharSetApi}. This method will convert it to a displayable charset
     * label before setting the text of the charset combo.
     * 
     * @param charset
     */
    public void setCharset(String charset) {
        if (charset != null) {
            this.charsetCombo.setText(P4UIUtils.getDisplayCharset(charset));
            validatePage();
        }
    }

    /**
     * Get selected charset
     * 
     * @return - charset chosen
     */
    @Override
    public String getCharset() {
        return this.charset;
    }

    @Override
    public boolean canFlipToNextPage() {
    	return super.canFlipToNextPage() && getErrorMessage()==null;
    }
}
