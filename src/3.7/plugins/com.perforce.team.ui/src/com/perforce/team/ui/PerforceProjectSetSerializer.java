package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.ProjectSetSerializationContext;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.dialogs.SetConnectionDialog;
import com.perforce.team.ui.project.ProjectNameDialog;
import com.perforce.team.ui.project.ReuseConnectionDialog;

/**
 * Perforce project set serializer
 */
public class PerforceProjectSetSerializer extends ProjectSetCapability {

    private static final String SERVER_PORT = "PORT"; //$NON-NLS-1$
    private static final String PROJECT_NAME = "NAME"; //$NON-NLS-1$
    private static final String DEPOT_PATH = "PATH"; //$NON-NLS-1$

    /**
     * @see org.eclipse.team.core.ProjectSetCapability#asReference(org.eclipse.core.resources.IProject[],
     *      org.eclipse.team.core.ProjectSetSerializationContext,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public String[] asReference(IProject[] providerProjects,
            ProjectSetSerializationContext context, IProgressMonitor monitor)
            throws TeamException {
        String[] refs = new String[providerProjects.length];
        for (int i = 0; i < refs.length; i++) {
            refs[i] = toProjectString(providerProjects[i]);
        }
        return refs;
    }

    /**
     * Adds the specified reference strings as projects
     * 
     * @param referenceStrings
     * @param context
     * @param monitor
     * @param parameters
     * @return - projects created
     * @throws TeamException
     */
    public IProject[] addToWorkspace(String[] referenceStrings,
            ProjectSetSerializationContext context, IProgressMonitor monitor,
            ConnectionParameters parameters) throws TeamException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        IProject[] projects = new IProject[referenceStrings.length];
        monitor.beginTask("", referenceStrings.length); //$NON-NLS-1$
        final Map<String, ConnectionParameters> reused = new HashMap<String, ConnectionParameters>();
        for (int i = 0; i < projects.length; i++) {
            ConnectionParameters initialParams = parameters;
            Map<String, String> properties = parseProjectString(referenceStrings[i]);
            String projectName = properties.get(PROJECT_NAME);
            String depotPath = properties.get(DEPOT_PATH);
            final String serverPort = properties.get(SERVER_PORT);

            if (depotPath != null && serverPort != null) {
                monitor.setTaskName(MessageFormat.format(
                        Messages.PerforceProjectSetSerializer_ImportingFrom,
                        depotPath, serverPort));
            }

            final ConnectionParameters[] params = new ConnectionParameters[] { null };
            if (serverPort != null && initialParams == null) {
                initialParams = reused.get(serverPort);
            }
            if (initialParams == null) {
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        ReuseConnectionDialog dialog = new ReuseConnectionDialog(
                                PerforceUIPlugin.getDisplay().getActiveShell());
                        ConnectionParameters dialogParams = new ConnectionParameters();
                        dialogParams.setPort(serverPort);
                        dialog.setConnectionParams(dialogParams);
                        if (dialog.open() == SetConnectionDialog.OK) {
                            params[0] = dialog.getConnectionParams();
                            if (serverPort != null && dialog.reuse()) {
                                // Add mapping for original port to params
                                reused.put(serverPort, params[0]);
                                // Add mapping for entered port to params as
                                // this may be different as the original port
                                // Note both the original port and entered port
                                // are added to the re-use cache
                                String enteredPort = params[0].getPort();
                                if (enteredPort != null) {
                                    reused.put(enteredPort, params[0]);
                                }
                            }
                        }
                    }
                });
            } else {
                params[0] = initialParams;
            }
            if (params[0] != null) {
                IP4Connection connection = P4ConnectionManager.getManager()
                        .getConnection(params[0]);
                if (!connection.isConnected()) {
                    connection.connect();
                }
                IP4Folder folder = connection.getFolder(depotPath);
                if(folder!=null){
	                // Ensure the latest client view path
	                folder.updateLocation();
	                projects[i] = createProject(folder, null, monitor, projectName);
                }
            }
            monitor.worked(1);
        }
        monitor.done();
        return projects;
    }

    /**
     * @see org.eclipse.team.core.ProjectSetCapability#addToWorkspace(java.lang.String[],
     *      org.eclipse.team.core.ProjectSetSerializationContext,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public IProject[] addToWorkspace(String[] referenceStrings,
            ProjectSetSerializationContext context, IProgressMonitor monitor)
            throws TeamException {
        return addToWorkspace(referenceStrings, context, monitor, null);
    }

    private static String findProjectName(IP4Folder parent) {
        String name = null;
        String localPath = parent.getLocalPath() + "/.project"; //$NON-NLS-1$
        InputStream stream = null;
        try {
            stream = new FileInputStream(localPath);
            IProjectDescription desc = ResourcesPlugin.getWorkspace()
                    .loadProjectDescription(stream);
            if (desc != null) {
                name = desc.getName();
            }
        } catch (CoreException e) {
            name = null;
        } catch (FileNotFoundException e) {
            name = null;
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // Ignore close issues
                }
            }
        }
        return name;
    }

    private static void loadLocalLocation(IP4Folder folder,
            IProgressMonitor monitor) throws OperationCanceledException {
        monitor.setTaskName(MessageFormat.format(
                Messages.PerforceProjectSetSerializer_FetchingLocalLocation,
                folder.getRemotePath()));
        // Do a refresh via p4 where for this folder to
        // ensure it has the latest client view path
        folder.updateLocation();

        monitor.worked(10);

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private static void syncLocation(IP4Folder folder, IProgressMonitor monitor)
            throws OperationCanceledException {
        monitor.setTaskName(MessageFormat.format(
                Messages.PerforceProjectSetSerializer_Syncing,
                folder.getLocalPath()));

        // Go and sync all the files for this project
        // We need put some intelligent here to check if the file already in have table.
        P4ConnectionManager.getManager().createP4Collection(folder).sync(new SubProgressMonitor(monitor, 10));

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private static IProjectDescription loadDescription(IP4Folder folder,
            IProgressMonitor monitor, String defaultName) {
        String localPath = folder.getLocalPath();

        monitor.setTaskName(MessageFormat.format(
                Messages.PerforceProjectSetSerializer_LoadingProjectName,
                localPath));
        String name = defaultName;
        if (name == null) {
            name = findProjectName(folder);
        }
        if (name == null) {
            name = folder.getName();
        }

        IProject existingProject = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        if (existingProject != null && existingProject.exists()) {
            // Display dialog to use a different name
            final String[] otherName = new String[] { name };
            final boolean[] cancelled = new boolean[] { false };
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    ProjectNameDialog dialog = new ProjectNameDialog(P4UIUtils
                            .getShell(), otherName[0]);
                    if (dialog.open() == ProjectNameDialog.OK) {
                        otherName[0] = dialog.getEnteredName();
                    } else {
                        cancelled[0] = true;
                    }
                }
            });
            name = otherName[0];
            if (cancelled[0]) {
                return null;
            }
        }

        IProjectDescription description = ResourcesPlugin.getWorkspace()
                .newProjectDescription(name);
        IPath rootLoc = Platform.getLocation();
        if (localPath != null && null!=rootLoc) {
            String defaultPath = rootLoc.append(name).toOSString();

            // Check location is not in the default
            // workspace location
            if (!localPath.equalsIgnoreCase(defaultPath)) {
                description.setLocation(new Path(localPath));
            }
        }

        monitor.worked(10);

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
        return description;
    }

    private static IProject createProject(final IProjectDescription description,
            IProgressMonitor monitor) throws CoreException {
        final String name = description.getName();

        monitor.setTaskName(MessageFormat.format(
                Messages.PerforceProjectSetSerializer_CreatingProject, name));

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IProject newProjectHandle = workspace.getRoot().getProject(name);
        try {
            // create project folder if necessary
        	URI uri = description.getLocationURI();
        	if(uri!=null){
	            File path = new File(uri);
	            if(!path.exists()){
	                if(!path.mkdirs()){
	                	String msg = MessageFormat.format(
								Messages.PerforceProjectSetSerializer_CreateFolderError,
								path.getAbsolutePath());
	                	PerforceProviderPlugin.logError(msg);
	                }
	            }
        	}

            // create project
            newProjectHandle.create(description, null);
            newProjectHandle.open(null);
        } catch (CoreException e) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager
                            .getManager()
                            .openError(
                                    P4UIUtils.getShell(),
                                    Messages.PerforceProjectSetSerializer_ErrorCreatingProjectTitle,
                                    MessageFormat
                                            .format(Messages.PerforceProjectSetSerializer_ErrorCreatingProjectMessage,
                                                    name, description.getLocationURI()));
                }
            });
            throw e;
        }

        monitor.worked(10);

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }

        return newProjectHandle;
    }

    private static void manageProject(IP4Folder folder, IProject project,
            IProgressMonitor monitor) {
        String name = project.getName();
        monitor.setTaskName(MessageFormat.format(
                Messages.PerforceProjectSetSerializer_ManagingProject, name));

        PerforceProviderPlugin.manageProject(project, folder.getConnection()
                .getParameters());

        monitor.worked(10);

        if (monitor.isCanceled()) {
            throw new OperationCanceledException();
        }
    }

    /**
     * Create a collection of projects
     * 
     * @param collection
     * @param shell
     * @param showErrors
     * @return - scheduled job
     */
    public static Job createProjects(final P4Collection collection,
            final Shell shell, final boolean showErrors) {
        return createProjects(collection, shell, showErrors, true);
    }

    private static void createProject(IProgressMonitor monitor,
            String defaultName, IP4Folder container, boolean showErrors,
            StringBuilder errors) throws CoreException {
        SubProgressMonitor projectMonitor = new SubProgressMonitor(monitor, 10);
        projectMonitor.beginTask("", 50);//$NON-NLS-1$
        try {
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            // Do a fresh p4 where to get the latest location
            loadLocalLocation(container, projectMonitor);

            String localPath = container.getLocalPath();
            if (localPath != null) {
            	
            	if(!isFilenameValid(localPath)){
            		errors.append(container.getRemotePath()).append("\n");
            	}else{

	                // Sync the project, note, at this time project is not created yet
	                syncLocation(container, projectMonitor);
	
	                // Generate a project description
	                IProjectDescription description = loadDescription(container,
	                        projectMonitor, defaultName);
	
	                // Null-descriptions should be skipped
	                if (description != null) {
	                    // Create the project
	                    IProject project = createProject(description,
	                            projectMonitor);
	
	                    // Associate the project with the folder's
	                    // connection
	                    manageProject(container, project, projectMonitor);
	                } else {
	                    projectMonitor.worked(20);
	                }
            	}
            	
                P4Collection collection = new P4Collection();
                collection.add(container);
                collection.refresh();
                
            } else if (showErrors) {
                errors.append(container.getRemotePath()).append("\n"); //$NON-NLS-1$
            }
        } finally {
            projectMonitor.done();
            P4ConnectionManager.getManager().notifyListeners(
                    new P4Event(EventType.REFRESHED, container));
        }
    }

    public static boolean isFilenameValid(String file) {
    	if(file==null)
    		return false;

    	try {
			String path = new File(file).getCanonicalPath();
			if(path==null)
				return false;
			
			if(path!=null){
				String f = file.replace("/", "").replace("\\", "");
				String p = path.replace("/", "").replace("\\", "");
				if(p.equalsIgnoreCase(f))
					return true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	return false;
	}

    /**
     * Create a single project with the specified name
     * 
     * @param container
     * @param defaultName
     * @param shell
     * @param showErrors
     * @param async
     * @return - job that project is/was created in
     */
    public static Job createProject(final IP4Folder container,
            final String defaultName, final Shell shell,
            final boolean showErrors, boolean async) {
        final StringBuilder errors = new StringBuilder();
        final String taskName = Messages.PerforceProjectSetSerializer_ImportingOneProject;
        final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                monitor.beginTask(taskName, 10);
                try {
                    createProject(monitor, defaultName, container, showErrors,
                            errors);
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                    throw e;
                } finally {
                    monitor.done();
                }

                if (showErrors && errors.length() > 0) {
                    UIJob errorJob = new UIJob(
                            Messages.PerforceProjectSetSerializer_DisplayingPerforceErrors) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            P4ConnectionManager
                                    .getManager()
                                    .openError(
                                            shell,
                                            Messages.PerforceProjectSetSerializer_ImportFailedTitle,
                                            Messages.PerforceProjectSetSerializer_ImportFailedMessage
                                                    + errors.toString());
                            return Status.OK_STATUS;
                        }
                    };
                    errorJob.schedule();
                }
            }
        };

        Job importJob = null;
        if (async) {
            importJob = new Job(taskName) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        op.run(monitor);
                    } catch (InvocationTargetException e) {
                        return Status.CANCEL_STATUS;
                    } catch (InterruptedException e) {
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };
            PlatformUI.getWorkbench().getProgressService()
                    .showInDialog(shell, importJob);
            importJob.schedule();
        } else {
            try {
                op.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
            } catch (InterruptedException e) {
            }
        }
        return importJob;
    }

    /**
     * Create a collection of projects
     * 
     * @param collection
     * @param shell
     * @param showErrors
     * @param async
     * @return - scheduled job
     */
    public static Job createProjects(final P4Collection collection,
            final Shell shell, final boolean showErrors, boolean async) {
        final List<IP4Folder> containers = new ArrayList<IP4Folder>();
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4Folder) {
                containers.add((IP4Folder) resource);
            }
        }
        final StringBuilder errors = new StringBuilder();
        final String taskName = MessageFormat.format(
                Messages.PerforceProjectSetSerializer_ImportingProjects,
                containers.size());
        final WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                monitor.beginTask(taskName, containers.size());
                try {
                    for (IP4Folder container : containers) {
                        try {
                            createProject(monitor, null, container, showErrors,
                                    errors);
                        } catch (CoreException e) {
                            PerforceProviderPlugin.logError(e);
                            throw e;
                        }
                    }
                } finally {
                    monitor.done();
                }

                if (showErrors && errors.length() > 0) {
                    UIJob errorJob = new UIJob(
                            Messages.PerforceProjectSetSerializer_DisplayingPerforceErrors) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            P4ConnectionManager
                                    .getManager()
                                    .openError(
                                            shell,
                                            Messages.PerforceProjectSetSerializer_ImportFailedTitle,
                                            Messages.PerforceProjectSetSerializer_ImportFailedMessage
                                                    + errors.toString());
                            return Status.OK_STATUS;
                        }
                    };
                    errorJob.schedule();
                }
            }
        };

        Job importJob = null;
        if (async) {
            importJob = new Job(taskName) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    try {
                        op.run(monitor);
                    } catch (InvocationTargetException e) {
                        return Status.CANCEL_STATUS;
                    } catch (InterruptedException e) {
                        return Status.CANCEL_STATUS;
                    }
                    return Status.OK_STATUS;
                }
            };
            PlatformUI.getWorkbench().getProgressService()
                    .showInDialog(shell, importJob);
            importJob.schedule();
        } else {
            try {
                op.run(new NullProgressMonitor());
            } catch (InvocationTargetException e) {
            } catch (InterruptedException e) {
            }
        }
        return importJob;
    }

    /**
     * Creates a project from a p4 container. Does a sync and refresh on the
     * container.
     * 
     * @param container
     * @param shell
     * @param monitor
     * @param name
     * @return - created project
     */
    public static IProject createProject(final IP4Container container,
            Shell shell, IProgressMonitor monitor, String name) {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        String location = container.getLocalPath();
        if (name == null) {
            name = container.getName();
        }
        final IProjectDescription description = workspace
                .newProjectDescription(name);
        if (location != null) {
        	IPath eclipseLoc = Platform.getLocation();
        	
        	if(eclipseLoc!=null){
	            String defaultPath = eclipseLoc.append(name)
	                    .toOSString();
	
	            // Check location is not in the default workspace location
	            if (location.compareToIgnoreCase(defaultPath) != 0) {
	                description.setLocation(new Path(location));
	            }
        	}
        }

        final IProject newProjectHandle = workspace.getRoot().getProject(name);

        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

            @Override
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    monitor.beginTask("", 3000);//$NON-NLS-1$

                    // Go and sync all the files for this project
                    SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 500);
                    P4UIProgressListener callback = new P4UIProgressListener(CmdSpec.SYNC, container.getConnection(),subMonitor);
                    P4ConnectionManager
                            .getManager()
                            .createP4Collection(new IP4Resource[] { container })
                            .sync(subMonitor, callback);

                    newProjectHandle.create(description,
                            new SubProgressMonitor(monitor, 500));

                    if (monitor.isCanceled()) {
                        throw new OperationCanceledException();
                    }

                    newProjectHandle
                            .open(new SubProgressMonitor(monitor, 1000));
                    PerforceProviderPlugin.manageProject(newProjectHandle,
                            container.getConnection().getParameters());

                    P4ConnectionManager.getManager().notifyListeners(
                            new P4Event(EventType.REFRESHED, container));
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            if (monitor != null) {
                op.run(monitor);
            } else {
                new ProgressMonitorDialog(shell).run(true, true, op);
            }
        } catch (InvocationTargetException e) {
            PerforceProviderPlugin.logError(e);
        } catch (InterruptedException e) {
        }

        return newProjectHandle;
    }

    private String toProjectString(final IProject project) throws TeamException {
        String depotPath = null;
        final IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project);
        if (connection != null) {
            final IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(project);
            if (resource instanceof IP4Folder) {
                IP4Folder projectFolder = (IP4Folder) resource;
                depotPath = projectFolder.getFirstWhereRemotePath();
            }
        }
        if (depotPath == null) {
            throw new TeamException(
                    Messages.PerforceProjectSetSerializer_UnableToConnect);
        }
        return SERVER_PORT + "=" + connection.getParameters().getPort() + ";" //$NON-NLS-1$ //$NON-NLS-2$
                + PROJECT_NAME + "=" + project.getName() + ";" + DEPOT_PATH //$NON-NLS-1$ //$NON-NLS-2$
                + "=" + depotPath; //$NON-NLS-1$
    }

    private Map<String, String> parseProjectString(String s) {
        Map<String, String> properties = new HashMap<String, String>();
        StringTokenizer tokenizer = new StringTokenizer(s, ";"); //$NON-NLS-1$
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int idx = token.indexOf('=');
            properties.put(token.substring(0, idx), token.substring(idx + 1));
        }
        return properties;
    }

}
