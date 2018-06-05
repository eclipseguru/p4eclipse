package com.perforce.team.core;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.Team;
import org.eclipse.team.core.TeamException;
import org.osgi.framework.BundleContext;

import com.perforce.p4java.Log;
import com.perforce.p4java.server.callback.ILogCallback;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * Core Perforce plugin
 */
public class PerforceProviderPlugin extends Plugin {

    // The singleton instance of the plugin
    private static PerforceProviderPlugin instance;

    // Event listeners
    private static ListenerList settingsListeners = new ListenerList();

    // The workspace root
    private static IWorkspaceRoot root;

    /**
     * The ID for this plugin
     */
    public static final String ID = "com.perforce.team.core"; //$NON-NLS-1$

    // Parameters for current project being managed
    private static ConnectionParameters currParams;

    private boolean stopped = false;

    // private static OutputStreamWriter debug;

    /**
     * Plugin constructor
     */
    public PerforceProviderPlugin() {
        super();
        instance = this;
    }

    /**
     * Log an exception
     * 
     * @param e
     *            the exception to log
     */
    public static void log(TeamException e) {
        instance.getLog().log(e.getStatus());
    }

    /**
     * Log an IStatus
     * 
     * @param status
     *            the status to log
     */
    public static void log(IStatus status) {
        instance.getLog().log(status);
    }

    /**
     * Logs an info level message and throwable
     * 
     * @param message
     * @param throwable
     */
    public static void logInfo(String message, Throwable throwable) {
        if (message != null || throwable != null) {
            if (message == null) {
                message = ""; //$NON-NLS-1$
            }
            IStatus status = new Status(IStatus.INFO, ID, IStatus.OK, message,
                    throwable);
            if(instance!=null)
            	instance.getLog().log(status);
        }
    }

    /**
     * Logs a warning level message and throwable
     * 
     * @param message
     * @param throwable
     */
    public static void logWarning(String message, Throwable throwable) {
        if (message != null || throwable != null) {
            if (message == null) {
                message = ""; //$NON-NLS-1$
            }
            IStatus status = new Status(IStatus.WARNING, ID, IStatus.OK,
                    message, throwable);
            instance.getLog().log(status);
        }
    }

    /**
     * Logs an error level message and throwable
     * 
     * @param message
     * @param throwable
     */
    public static void logError(String message, Throwable throwable) {
        if (message != null || throwable != null) {
            if (message == null) {
                message = ""; //$NON-NLS-1$
            }
            IStatus status = new Status(IStatus.ERROR, ID, IStatus.OK, message,
                    throwable);
            instance.getLog().log(status);
        }
    }

    /**
     * Logs an info level throwable
     * 
     * @param throwable
     */
    public static void logInfo(Throwable throwable) {
        if (throwable != null) {
            logInfo(throwable.getMessage(), throwable);
        }
    }

    /**
     * Logs a warning level throwable
     * 
     * @param throwable
     */
    public static void logWarning(Throwable throwable) {
        if (throwable != null) {
            logWarning(throwable.getMessage(), throwable);
        }
    }

    /**
     * Logs an error level throwable
     * 
     * @param throwable
     */
    public static void logError(Throwable throwable) {
        if (throwable != null) {
            logError(throwable.getMessage(), throwable);
        }
    }

    /**
     * Logs an info level message
     * 
     * @param message
     */
    public static void logInfo(String message) {
        if (message != null) {
            logInfo(message, null);
        }
    }

    /**
     * Logs a warning level message
     * 
     * @param message
     */
    public static void logWarning(String message) {
        if (message != null) {
            logWarning(message, null);
        }
    }

    /**
     * Logs an error level message
     * 
     * @param message
     */
    public static void logError(String message) {
        if (message != null) {
            logError(message, null);
        }
    }

    /**
     * Get paths to an array of resources
     * 
     * @param resources
     *            the resources to get the paths for
     * @return the paths for the resources
     */
    public static String[] getResourcePath(IResource[] resources) {
        String[] paths = new String[resources.length];
        for (int i = 0; i < resources.length; i++) {
            paths[i] = getResourcePath(resources[i]);
        }
        return paths;
    }

    /**
     * Get full path to a resource
     * 
     * @param resource
     *            the resource to get the path for
     * @return the full path to the resource
     */
    public static String getResourcePath(IResource resource) {
        String resourcePath = null;
        if (resource != null) {
            IPath path = resource.getLocation();
            if (path != null) {
                resourcePath = path.toOSString();
            }
        }
        return resourcePath;
    }

    /**
     * Get the instance for the plugin
     * 
     * @return the plugin instance
     */
    public static PerforceProviderPlugin getPlugin() {
        return instance;
    }

    /**
     * Add listener for project settings changes
     * 
     * @param listener
     *            the listener to add
     */
    public static void addProjectSettingsChangeListener(
            IProjectSettingsChangeListener listener) {
        settingsListeners.add(listener);
    }

    /**
     * Remove listener for project settings changes
     * 
     * @param listener
     *            the listener to remove
     */
    public static void removeProjectSettingsChangeListener(
            IProjectSettingsChangeListener listener) {
        settingsListeners.remove(listener);
    }

    /**
     * Broadcast changes to projects settings
     * 
     * @param project
     *            the project whose settings have changed
     * @param params
     *            the new connection parameters
     */
    public static void broadcastProjectSettingsChanges(IProject project,
            ConnectionParameters params) {
        for (Object listener : settingsListeners.getListeners()) {
            ((IProjectSettingsChangeListener) listener).projectSettingsChanged(
                    project, params);
        }
    }

    /**
     * Called when a project is managed by Perforce
     * 
     * @param project
     *            the project to be managed
     * @param params
     *            the Perforce connection parameters to use for this project
     * @return - true on success, false on failure
     */
    public static boolean manageProject(IProject project,
            ConnectionParameters params) {
        try {
            currParams = params;
            RepositoryProvider.map(project, PerforceTeamProvider.ID);
            return true;
        } catch (TeamException e) {
            log(e);
            return false;
        }
    }

    /**
     * Get current params
     * 
     * @return - current params
     */
    public static ConnectionParameters getCurrParams() {
        return currParams;
    }

    /**
     * Called when a project is no longer managed by Perforce
     * 
     * @param project
     *            the project to remove from Perforce management
     */
    public static void unmanageProject(IProject project) {
        try {
            RepositoryProvider.unmap(project);
        } catch (TeamException e) {
        }
    }

    /**
     * Get the workspace root
     * 
     * @return the workspace root
     */
    public static IWorkspaceRoot getWorkspaceRoot() {
        if (root == null) {
            root = ResourcesPlugin.getWorkspace().getRoot();
        }
        return root;
    }

    /**
     * Get container for a given path
     * 
     * @param path
     * @return - container
     */
    public static IContainer getFolderForPath(IPath path) {
        IWorkspaceRoot root = getWorkspaceRoot();

        IContainer[] folders = root.findContainersForLocationURI(path.toFile().toURI());
        if (folders.length == 1) {
            return folders[0];
        }
        // in nested projects, there might be multiple folder, just just refer
        // to container
        return root.getContainerForLocation(path);

    }

    /**
     * Get a workspace resource file(s) for a specific path. If a linked
     * resource is used in multiple places then a single path to the resource
     * will produce multiple resource files
     * 
     * @param filePath
     *            the path to get the file for
     * @return the workspace resource file or null if no such file exists
     */
    public static IFile[] getResourcesForPath(String filePath) {
        // Ignore depot paths
        if (filePath != null && !filePath.startsWith("//")) { //$NON-NLS-1$
            Path path = new Path(filePath);
            IWorkspaceRoot root = getWorkspaceRoot();
            IFile file = root.getFileForLocation(path);
            if (file != null) {
                return new IFile[] { file };
            }
            IFile[] files = root.findFilesForLocationURI(path.toFile().toURI());
            if (files.length > 0) {
                return files;
            }
        }
        return null;
    }

    /**
     * Eclipse 2.0.0 uses isIgnoredHint(IFile) whilst 2.1 (and greater) uses
     * isIgnoredHint(IResource);
     * 
     * @param resource
     * @return - true if ignored
     */
    public static boolean isIgnoredHint(IResource resource) {
        // Ignore any derived resources or resources under derived resources;
        // also
        // uses Team.isIgnoredHint() to do the .ignore processing as well.

        if (Team.isIgnoredHint(resource)) {
            return true;
        } else {
            // Go back up the chain of resources looking for derivedness...

            IResource iResource = resource.getParent();
            while (iResource != null) {
                if (iResource.isDerived()) {
                    return true;
                }
                iResource = iResource.getParent();
            }
        }

        return false;
    }

    /**
     * Get a workspace resource file(s) for a specific path Handles the case
     * where the filePath differs in case from actual file
     * 
     * @param filePath
     *            the path to get the file for
     * @return the workspace resource file or null if no such file exists
     */
    public static IFile[] getWorkspaceFiles(String filePath) {
        IFile[] files = null;
        if (filePath != null) {
            files = getResourcesForPath(filePath);
            // Maybe case does not match (on Windows);
            if (files == null) {
                try {
                    // Try again using correct case
                    String newPath = new File(filePath).getCanonicalPath();
                    if (!newPath.equals(filePath)) {
                        files = getResourcesForPath(newPath);
                    }
                } catch (IOException e) {
                }
            }
        }
        return files;
    }

    /**
     * Get a workspace resource file for a specific path Handles the case where
     * the filePath differs in case from actual file
     * 
     * @param filePath
     *            the path to get the file for
     * @return the workspace resource file or null if no such file exists
     */
    public static IFile getWorkspaceFile(String filePath) {
        IFile[] files = getWorkspaceFiles(filePath);
        return files == null ? null : files[0];
    }

    /**
     * Get a workspace resource files for specific paths
     * 
     * @param paths
     *            the paths for which to get files
     * @return the workspace resource files
     */
    public static IFile[] getWorkspaceFiles(String[] paths) {
        List<IFile> files = new ArrayList<IFile>();
        for (int i = 0; i < paths.length; i++) {
            IFile file = getWorkspaceFile(paths[i]);
            if (file != null) {
                files.add(file);
            }
        }
        return files.toArray(new IFile[files.size()]);
    }

    /**
     * Get the perforce provider for a specific resource
     * 
     * @param resource
     *            the resource to get the provider for
     * @return returns the perforce provider or null if there is no perforce
     *         provider for this resource
     */
    public static PerforceTeamProvider getPerforceProviderFor(IResource resource) {
        IProject project = resource.getProject();
        // Sometimes a resource may not have a project - Not sure why
        if (project == null) {
            return null;
        }
        return PerforceTeamProvider.getPerforceProvider(resource);
    }

    /**
     * Get a resource from an object
     * 
     * @param object
     *            the object to get the resource from
     * @return return the resource for the object or null if no resource
     */
    public static IResource getResource(Object object) {
        if (object instanceof IResource) {
            return (IResource) object;
        }
        if (object instanceof IAdaptable) {
            return (IResource) ((IAdaptable) object)
                    .getAdapter(IResource.class);
        }
        if (object instanceof IP4Resource) {
            IFile[] files = getResourcesForPath(((IP4Resource) object)
                    .getLocalPath());
            if (files != null && files.length > 0) {
                return files[0];
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);

        Log.setLogCallback(new ILogCallback() {

            public void internalWarn(String warnString) {
                if (!stopped) {
                    logWarning(warnString);
                }
            }

            public void internalInfo(String infoString) {
                if (!stopped) {
                    logInfo(infoString);
                }
            }

            public void internalError(String errorString) {
                if (!stopped) {
                    logError(errorString);
                }
            }

            public void internalException(Throwable thr) {
                internalError(thr.getLocalizedMessage());
            }

            public void internalStats(String statsString) {
                internalInfo(statsString);
            }

            public LogTraceLevel getTraceLevel() {
                return P4Workspace.getWorkspace().getTraceLevel();
            }

            public void internalTrace(LogTraceLevel level, String traceString) {
                internalInfo(traceString);
            }

        });
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {

        /*
         * Need to go and save all project params as they may have changed
         * passwords.
         */
        IWorkspaceRoot root = getWorkspaceRoot();
        IProject[] projects = root.getProjects();
        for (int i = 0; i < projects.length; i++) {
            PerforceTeamProvider provider = PerforceTeamProvider
                    .getPerforceProvider(projects[i]);
            if (provider != null) {
                provider.setProjectProperties(provider
                        .getProjectProperties(false));
            }
        }

        P4Workspace.getWorkspace().saveConnections();
        stopped = true;
        super.stop(context);
    }

    /**
     * Gets the local files mapped to a specified local path
     * 
     * @param localPath
     * @return - local files
     */
    public static IFile[] getLocalFiles(String localPath) {
        IFile[] files = null;
        if (localPath != null) {
            IPath path = new Path(localPath);
            files = getWorkspaceRoot().findFilesForLocationURI(path.toFile().toURI());
            if (files == null || files.length == 0) {
                File file = new File(localPath);
                try {
                    String checkedPath = file.getCanonicalPath();
                    if (!localPath.equals(checkedPath)) {
                        path = new Path(checkedPath);
                        files = getWorkspaceRoot().findFilesForLocationURI(path.toFile().toURI());
                    }
                } catch (IOException e) {
                }
            }
        }
        if (files == null) {
            files = new IFile[0];
        }
        return files;
    }

    /**
     * Gets the local file for the specified local path
     * 
     * @param localPath
     * @return - ifile
     */
    public static IFile getLocalFile(String localPath) {
        return getWorkspaceFile(localPath);
    }

    /**
     * Gets the local containers mapped to a specified local path
     * 
     * @param localPath
     * @return - local containers
     */
    public static IContainer[] getLocalContainers(String localPath) {
        IContainer[] containers = null;
        if (localPath != null) {
            IPath path = new Path(localPath);
            containers = getWorkspaceRoot().findContainersForLocationURI(path.toFile().toURI());
            if (containers == null || containers.length == 0) {
                File file = new File(localPath);
                try {
                    String checkedPath = file.getCanonicalPath();
                    if (!localPath.equals(checkedPath)) {
                        path = new Path(checkedPath);
                        containers = getWorkspaceRoot()
                                .findContainersForLocationURI(path.toFile().toURI());
                    }
                } catch (IOException e) {
                }
            }
        }
        if (containers == null) {
            containers = new IContainer[0];
        }
        return containers;
    }

}
