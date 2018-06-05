/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard; 
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.builder.ClientBuilder;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.project.ImportProjectsWizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author Alex (ali@perforce.com)
 */
public class ConnectionWizard extends AbstractConnectionWizard implements IConnectionWizard,
        INewWizard {

    /**
     * Connection wizard
     */
    public ConnectionWizard() {
        setNeedsProgressMonitor(true);
    }

    /**
     * Connection wizard
     * 
     * @param initial
     */
    public ConnectionWizard(IP4Connection initial) {
    	super(initial);
    }

    public ConnectionWizard(String initialPort, String initialUser,
            String initialClient, String initialCharset) {
        super(initialPort, initialUser, initialClient, initialCharset);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        serverPage = new ServerWizardPage("serverPage"); //$NON-NLS-1$
        addPage(serverPage);
        clientPage = new ClientWizardPage("clientPage",true); //$NON-NLS-1$
        addPage(clientPage);
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPort()
     */
    public String getPort() {
        return serverPage.getPort();
    }

    /**
     * Get the user for the server
     * 
     * @return - perforce user
     */
    public String getUser() {
        return serverPage.getUser();
    }

    /**
     * Get the client for the server
     * 
     * @return - perforce client workspace name
     */
    public String getClient() {
        return clientPage.getClient();
    }

//    /**
//     * Get the depot folders to import as eclipse projectsx
//     * 
//     * @return - p4 folders array
//     */
//    public IP4Folder[] getImportedFolders() {
//        return importPage.getImportedFolders();
//    }
//
//    /**
//     * Import folder returned from {@link #getImportedFolders()} as projects
//     * associated with the specified connection
//     * 
//     * @param connection
//     */
//    protected void importProjects(IP4Connection connection) {
//
//        IP4Folder[] imports = getImportedFolders();
//
//        final List<IP4Container> retrievedFolders = new ArrayList<IP4Container>();
//        for (IP4Folder folder : imports) {
//            IP4Folder updated = connection.getFolder(folder.getRemotePath());
//            if (updated != null) {
//                retrievedFolders.add(updated);
//            }
//        }
//
//        if (retrievedFolders.size() > 0) {
//            PerforceUIPlugin.syncExec(new Runnable() {
//
//                public void run() {
//                    ImportProjectAction checkout = new ImportProjectAction();
//                    checkout.selectionChanged(null, new StructuredSelection(
//                            retrievedFolders));
//                    checkout.run(null);
//                }
//            });
//        }
//    }

    private ConnectionParameters createParameters() {
        ConnectionParameters params = new ConnectionParameters();
        params.setPort(getPort());
        params.setClient(getClient());
        params.setUser(getUser());
        params.setCharset(getCharset());
        params.setPassword(getPassword());
        params.setSavePassword(serverPage.savePassword());
        params.setAuthTicket(getAuthTicket());
        return params;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final ConnectionParameters params = createParameters();
        PerforceUIPlugin.storePasswordInUI(params);

        final boolean newClient = isNewClient();
        final boolean[] finished = new boolean[] { true };
        final String stream=clientPage.getStream();

        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    monitor.beginTask(MessageFormat.format(
                            Messages.ConnectionWizard_AddingConnection,
                            params.getPort()), 4);

                    // Create client if needed
                    if (newClient) {
                        finished[0] = createClient(params,stream,monitor);
                        if (!finished[0]) {
                            monitor.done();
                            return;
                        }
                    } else {
                        monitor.worked(1);
                    }
                    saveConnection(params, monitor);

//                    importProjects(params, monitor);
                    saveServerHistory(monitor);

                    monitor.done();
                }
            });
        } catch (Throwable t) {
            PerforceProviderPlugin.logError(t);
        }

        if(finished[0] && clientPage.isLaunchImportWizard()){
        	final Rectangle bound = ConnectionWizard.this.getContainer().getShell().getBounds();
        	UIJob job=new UIJob(Messages.ConnectionWizard_PrepareForImportingProjects) {
				
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
		            IP4Connection connection = P4ConnectionManager.getManager()
		                    .getConnection(params);

			        ImportProjectsWizard wizard = new ImportProjectsWizard(connection);
			        WizardDialog dialog = new WizardDialog(P4UIUtils.getShell(), wizard){
			        	@Override
			        	protected void configureShell(Shell newShell) {
			        		super.configureShell(newShell);
			        		newShell.setBounds(bound);
			        	}
			        };
			        dialog.setBlockOnOpen(false);
			        dialog.create();
			        PlatformUI.getWorkbench().getHelpSystem().setHelp(dialog.getShell(),
							IHelpContextIds.P4_IMPORT_PROJECT);
			        dialog.open();
					return Status.OK_STATUS;
				}
			};
			job.schedule();
        }
        
        return finished[0];
    }

    private boolean createClient(ConnectionParameters params,String stream,
            IProgressMonitor monitor) {
        // Create client if needed
        monitor.subTask(NLS.bind(Messages.ConnectionWizard_CreatingClient,
                params.getClient()));
        ClientBuilder builder = new ClientBuilder(params,
                clientPage.getLocation(), stream);
        IErrorHandler handler = new ErrorHandler() {

            @Override
            public boolean shouldRetry(IP4Connection connection,
                    final P4JavaException exception) {
                boolean retry = false;
                if (P4ConnectionManager.isLoginError(exception.getMessage())) {
                    retry = P4ConnectionManager.getManager().displayException(
                            connection, exception, true, false);
                } else {
                    P4ConnectionManager.getManager().displayException(
                            connection, exception, false, false);
                }
                return retry;
            }

        };
        if (!builder.build(handler)) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    getContainer().showPage(clientPage);
                    P4ConnectionManager
                            .getManager()
                            .openError(
                                    getShell(),
                                    Messages.ConnectionWizard_ClientExistsTitle,
                                    NLS.bind(
                                            Messages.ConnectionWizard_ClientExistsMessage,
                                            getClient()));
                }
            });
            return false;
        }
        monitor.worked(1);
        return true;
    }

    private void saveConnection(ConnectionParameters params,
            IProgressMonitor monitor) {
        monitor.subTask(Messages.ConnectionWizard_SavingConnection);
        // Add connection
        P4ConnectionManager.getManager().add(params);
        monitor.worked(1);
    }

//    private void importProjects(ConnectionParameters params,
//            IProgressMonitor monitor) {
//        // Import projects from new connection
//        if (importPage.isImportSelected()
//                && P4ConnectionManager.getManager().containsConnection(params)) {
//            monitor.subTask(Messages.ConnectionWizard_SettingUpProjectImport);
//            IP4Connection connection = P4ConnectionManager.getManager()
//                    .getConnection(params);
//            importProjects(connection);
//        }
//        monitor.worked(1);
//    }

    private void saveServerHistory(IProgressMonitor monitor) {
        monitor.subTask(Messages.ConnectionWizard_SavingServerHistory);
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                serverPage.saveServerHistory();
            }
        });
        monitor.worked(1);
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getCharset()
     */
    public String getCharset() {
        return serverPage.getCharset();
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPassword()
     */
    public String getPassword() {
        return serverPage.getPassword();
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getAuthTicket()
     */
    public String getAuthTicket() {
        return this.authTicket;
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#setAuthTicket(java.lang.String)
     */
    public void setAuthTicket(String authTicket) {
        this.authTicket = authTicket;
    }
    
    public String getStream(){
        if(isNewClient()){
            return clientPage.getStream();
        }else{
            IP4Connection conn = new P4Connection(createParameters());
            return conn.getClient().getStream();
        }
    }

    public boolean isNewClient() {
        boolean newClient = clientPage.isNewClientSelected();
        return newClient;
    }
}
