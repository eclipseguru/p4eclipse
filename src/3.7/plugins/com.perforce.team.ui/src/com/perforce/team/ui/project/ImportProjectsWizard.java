package com.perforce.team.ui.project;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.IConnectionWizard;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;

/**
 * Wizard for import multiple projects from given connection into Eclipse workspace.
 * <p/>
 * This is a one page wizard, which is different from ExistingProjectWizard which
 * is used in ImportWizard.
 * 
 * @author ali
 *
 */
public class ImportProjectsWizard extends Wizard implements IConnectionWizard{

    protected ImportProjectsWizardPage importPage;
	private IP4Connection connection;

    public ImportProjectsWizard(IP4Connection connection) {
        setNeedsProgressMonitor(true);
        this.connection=connection;
    }

    @Override
    public void addPages() {
        importPage = new ImportProjectsWizardPage("importPage",false); //$NON-NLS-1$
        addPage(importPage);
    }

    @Override
	public boolean performFinish() {
        final boolean[] finished = new boolean[] { true };
        try {
            getContainer().run(true, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    if (monitor == null) {
                        monitor = new NullProgressMonitor();
                    }
                    importProjects(monitor);
                }
            });
        } catch (InvocationTargetException e) {
            PerforceProviderPlugin.logError(e);
        } catch (InterruptedException e) {
            PerforceProviderPlugin.logError(e);
        }
        return finished[0];
	}

    /**
     * Get the depot folders to import as eclipse projects
     * 
     * @return - p4 folders array
     */
    public IP4Folder[] getImportedFolders() {
        return importPage.getImportedFolders();
    }

    /**
     * Import folder returned from {@link #getImportedFolders()} as projects
     * associated with the specified connection
     * 
     * @param connection
     */
    private void importProjects(final IProgressMonitor monitor) {

        IP4Folder[] imports = getImportedFolders();

        monitor.beginTask(Messages.ImportProjectsWizard_ImportingProjects, imports.length+1);

        final List<IP4Container> retrievedFolders = new ArrayList<IP4Container>();
        for (IP4Folder folder : imports) {
        	monitor.subTask(MessageFormat.format(Messages.ImportProjectsWizard_CheckoutFolder,folder.getRemotePath()));
            IP4Folder updated = connection.getFolder(folder.getRemotePath());
            if (updated != null) {
                retrievedFolders.add(updated);
            }
            monitor.worked(1);
        }

        if (retrievedFolders.size() > 0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    ImportProjectAction checkout = new ImportProjectAction();
                    checkout.selectionChanged(null, new StructuredSelection(
                            retrievedFolders));
                    checkout.runAction(monitor);
                }
            });
        }
        
        monitor.done();
    }

	public String getPort() {
		return connection.getParameters().getPort();
	}

	public String getUser() {
		return connection.getUser();
	}

	public String getClient() {
		return connection.getClientName();
	}

	public String getCharset() {
		return connection.getParameters().getCharset();
	}

	public String getPassword() {
		return connection.getParameters().getPassword();
	}

	public String getAuthTicket() {
		return connection.getParameters().getAuthTicket();
	}

	public void setAuthTicket(String authTicket) {
		connection.getParameters().setAuthTicket(authTicket);
	}

    public String getStream(){
    	return connection.getClient().getStream();
    }
}
