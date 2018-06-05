package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.ui.IConfigurationWizard;
import org.eclipse.team.ui.IConfigurationWizardExtension;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.ide.undo.MoveProjectOperation;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.connection.SelectConnectionWizardPage;
import com.perforce.team.ui.decorator.PerforceDecorator;

/**
 * Config wizard to managing a p4 project
 * 
 * @author Alex Li (ali@perforce.com)
 */
public class ConfigWizard extends Wizard implements IConfigurationWizard,
        IConfigurationWizardExtension {

    /**
     * Projects
     */
    protected IProject[] projects;

    public IProject[] getProjects() {
        return Arrays.copyOf(projects, projects.length);
    }

    /**
     * Connection page
     */
    protected SelectConnectionWizardPage connectionPage;

    /**
     * Creates the config wizard
     */
    public ConfigWizard() {
        setDialogSettings(PerforceUIPlugin.getPlugin().getDialogSettings());
        connectionPage = new SelectConnectionWizardPage("connectionPage"); //$NON-NLS-1$
        connectionPage.setWizard(this);
        addPage(connectionPage);
        setWindowTitle(Messages.ConfigWizard_ShareProject);
        setNeedsProgressMonitor(true);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        try {
            final Map<IProject, File> projectsToMove = connectionPage
                    .getProjects(true);
            final Map<IProject, File> projectsNoMove = connectionPage.getProjects(false);
            
            final ConnectionParameters params = connectionPage.getConnection()
                    .getParameters();

            Map<IProject, String> errorProjects = connectionPage.getInvalidProject(
                    projectsToMove, connectionPage.getConnection());
            if (!errorProjects.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<IProject, String> entry : errorProjects.entrySet()) {
                	IProject prj=entry.getKey();
                    sb.append('[' + prj.getName() + ']' + "  " //$NON-NLS-1$
                            + entry.getValue());
                    sb.append("\n"); //$NON-NLS-1$
                }
                sb.deleteCharAt(sb.length() - 1);
                MessageDialog.openError(null, Messages.SharingWizard_Error,
                        Messages.SharingWizard_InvalidProjects + sb.toString());
                return false;
            }

            getContainer().run(false, false, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
					
                	monitor.beginTask(Messages.ConfigWizard_ShareProjects, 40);
					monitor.worked(10);
					
					SubProgressMonitor sub1 = new SubProgressMonitor(monitor, 10);
					sub1.beginTask(Messages.ConfigWizard_MovingProjects, projectsToMove.size()*10);
                    // move project
                    for (Map.Entry<IProject, File> entry : projectsToMove
                            .entrySet()) {

                        IProject project = entry.getKey();
                        IPath targetLocation = new Path(entry.getValue()
                                .getPath());
                        IPath currentLocation = project.getLocation();
                        if (!targetLocation.equals(currentLocation)) {
                            MoveProjectOperation op = new MoveProjectOperation(
                                    project,
                                    entry.getValue().toURI(),
                                    Messages.SharingWizard_MoveProjectActionLabel);
                            try {
                                IStatus result = op.execute(sub1, null);
                                sub1.worked(10);
                                if (!result.isOK())
                                    throw new RuntimeException();
                            } catch (ExecutionException e) {
                                if (e.getCause() != null)
                                    throw new InvocationTargetException(e
                                            .getCause());
                                throw new InvocationTargetException(e);
                            } finally {
                            }
                        }

                    }
                    sub1.done();

                    // add all projects to managed set.
                    Map<IProject, File> managedProjects = new HashMap<IProject, File>();
                    managedProjects.putAll(projectsToMove);
                    managedProjects.putAll(projectsNoMove);

                    SubProgressMonitor sub2 = new SubProgressMonitor(monitor, 10);
					sub2.beginTask(Messages.ConfigWizard_SharingProjects, managedProjects.size()*10);
					
                    List<IProject> managed = new ArrayList<IProject>();
                    try { // manage project
                          // Suspend decoration as on Eclipse 3.3 there is an
                          // issue with ViewerCell throwing an NPE when setting the text,
                          // appears to be fixed in 3.4+
                          // PerforceDecorator.suspendDecoration();

                        for (Map.Entry<IProject, File> entry : managedProjects
                                .entrySet()) {
                            IProject project = entry.getKey();
                            project.refreshLocal(IResource.DEPTH_INFINITE,
                                    sub2);
                            sub2.worked(10);
                            if (PerforceProviderPlugin.manageProject(project,
                                    params)) {
                                P4ConnectionManager.getManager().getConnection(
                                        project);
                                managed.add(project);
                            }
                        }
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    } finally {
                    }
                    sub2.done();

                    // refresh resources
                    if (managed.size() > 0) {
                        PerforceDecorator decorator = PerforceDecorator
                                .getActivePerforceDecorator();
                        if (decorator != null) {
                            decorator.resourceStateChanged(managed
                                    .toArray(new IProject[managed.size()]));
                        }
                    }
                    monitor.worked(10);
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            PerforceUIPlugin.handleError(Messages.SharingWizard_failed,
                    e.getCause(), true);
            return false;
        } catch (InterruptedException e) {
            // ignore for the moment
        }
        return true;
    }

    /**
     * @see org.eclipse.team.ui.IConfigurationWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.core.resources.IProject)
     */
    public void init(IWorkbench workbench, IProject project) {
        init(workbench, new IProject[] { project });
    }

    /**
     * Gets the connection page
     * 
     * @return - connection dialog
     */
    public SelectConnectionWizardPage getConnectionPage() {
        return this.connectionPage;
    }

    /**
     * @see org.eclipse.team.ui.IConfigurationWizardExtension#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.core.resources.IProject[])
     */
    public void init(IWorkbench workbench, IProject[] projects) {
        this.projects = projects;
    }
}
