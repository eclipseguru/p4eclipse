/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.connection.AbstractConnectionWizard;
import com.perforce.team.ui.connection.ClientWizardPage;
import com.perforce.team.ui.connection.IConnectionWizard;
import com.perforce.team.ui.connection.ServerWizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ExistingProjectWizard extends AbstractConnectionWizard implements
        IConnectionWizard, INewWizard, IImportWizard {

    private ConnectionSelectionPage connectionPage;

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        connectionPage = new ConnectionSelectionPage("connectionPage"); //$NON-NLS-1$
        addPage(connectionPage);
        serverPage = new ServerWizardPage("serverPage"); //$NON-NLS-1$
        addPage(serverPage);
        clientPage = new ClientWizardPage("clientPage"); //$NON-NLS-1$
        addPage(clientPage);
        importPage = new ImportProjectsWizardPage("importPage", false); //$NON-NLS-1$
        addPage(importPage);
    }

    /**
     * @see com.perforce.team.ui.connection.ConnectionWizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        connectionPage.getConnectionViewer().addDoubleClickListener(
                new IDoubleClickListener() {

                    public void doubleClick(DoubleClickEvent event) {
                        getContainer().showPage(getNextPage(connectionPage));
                    }
                });
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        if (connectionPage.isExistingConnection()) {
            try {
                getContainer().run(true, false, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException, InterruptedException {
                        IP4Connection connection = connectionPage.getConnection();
                        importProjects(connection,monitor);
                    }
                });
            } catch (InvocationTargetException e) {
                PerforceProviderPlugin.logError(e);
            } catch (InterruptedException e) {
                PerforceProviderPlugin.logError(e);
            }
	
        } else {
            return super.performFinish();
        }
        return true;
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getClient()
     */
    @Override
    public String getClient() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.getConnection().getParameters().getClient();
        } else {
            return clientPage.getClient();
        }
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPort()
     */
    @Override
    public String getPort() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.getConnection()
                    .getParameters().getPort();
        } else {
            return serverPage.getPort();
        }
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getUser()
     */
    @Override
    public String getUser() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.getConnection().getParameters().getUser();
        } else {
            return serverPage.getUser();
        }
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
     */
    @Override
    public IWizardPage getNextPage(IWizardPage page) {
        if (page == connectionPage) {
            if (connectionPage.isExistingConnection()) {
                return importPage;
            } else {
                return serverPage;
            }
        }
        return super.getNextPage(page);
    }

    /**
     * 
     * @see org.eclipse.jface.wizard.Wizard#canFinish()
     */
    @Override
    public boolean canFinish() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.isPageComplete()
                    && importPage.isPageComplete();
        } else {
            return importPage.isPageComplete() && clientPage.isPageComplete()
                    && serverPage.isPageComplete();
        }
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getCharset()
     */
    @Override
    public String getCharset() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.getConnection().getParameters().getCharset();
        } else {
            return serverPage.getCharset();
        }
    }

    /**
     * @see com.perforce.team.ui.connection.IConnectionWizard#getPassword()
     */
    @Override
    public String getPassword() {
        if (connectionPage.isExistingConnection()) {
            return connectionPage.getConnection().getParameters().getPassword();
        } else {
            return serverPage.getPassword();
        }
    }

}
