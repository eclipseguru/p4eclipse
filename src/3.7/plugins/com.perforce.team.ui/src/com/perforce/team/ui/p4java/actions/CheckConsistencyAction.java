/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.Messages;
import com.perforce.team.ui.p4java.dialogs.CheckConsistencyDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CheckConsistencyAction extends P4Action {

    /**
     * Consistency object encapsulating differing, new, and missing files
     */
    public static class Consistency {

        /**
         * Files that differ
         */
        public IFile[] diffFiles;

        /**
         * Missing files
         */
        public IFile[] missingFiles;

        /**
         * New files
         */
        public IFile[] newFiles;

        /**
         * Associated p4 connection
         */
        public IP4Connection connection;

    }

    private P4Collection getCollection(IFile[] files) {
        P4Collection collection = createCollection();
        for (IFile file : files) {
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(file);
            if (resource instanceof IP4File) {
                collection.add(resource);
            }
        }
        return collection;
    }

    private void openFiles(IP4Connection con, CheckConsistencyDialog dlg) {
        int pendingId = dlg.getSelectedChange();
        final String pendingDescription = dlg.getSelectedDescription();

        if (pendingId == IP4PendingChangelist.NEW) {
            IP4PendingChangelist created = con.createChangelist(
                    pendingDescription, null);
            if (created != null) {
                pendingId = created.getId();
            } else {
                pendingId = IChangelist.DEFAULT;
            }
        }

        final IFile[] diffFiles = dlg.getDiffFiles();

        if (diffFiles != null && diffFiles.length > 0) {
            P4Collection diffCollection = getCollection(diffFiles);
            if (!diffCollection.isEmpty()) {
                diffCollection.edit(pendingId);
            }
        }

        final IFile[] missingFiles = dlg.getMissingFiles();
        if (missingFiles != null && missingFiles.length > 0) {
            P4Collection deleteCollection = getCollection(missingFiles);
            if (!deleteCollection.isEmpty()) {
                deleteCollection.delete(pendingId);
            }
        }

        final IFile[] newFiles = dlg.getNewFiles();
        if (newFiles != null && newFiles.length > 0) {
            P4Collection addCollection = getCollection(newFiles);
            if (!addCollection.isEmpty()) {
                addCollection.add(pendingId);
            }
        }
    }

    private void doneWork(IProgressMonitor monitor) throws InterruptedException {
        monitor.worked(1);
        if (monitor.isCanceled()) {
            throw new InterruptedException();
        }
    }

    /**
     * Get unopened files that are different
     * 
     * @param con
     *            the connection
     * @param paths
     *            the paths to check
     * @param list
     *            unopened files are added to this list
     */
    private void getDiffResources(final IP4Connection con,
            final String[] paths, List<IFile> list) {
        IFileSpec[] specs = con.getDifferingFiles(paths);
        List<String> clientFiles = new ArrayList<String>();
        for (IFileSpec spec : specs) {
            String path = P4Resource.normalizeLocalPath(spec);
            if (path != null) {
                clientFiles.add(path);
            }
        }
        addFilesToList(clientFiles.toArray(new String[0]), list);
    }

    /**
     * Get unopened files that are missing
     * 
     * @param con
     *            the connection
     * @param paths
     *            the paths to check
     * @param list
     *            unopened files are added to this list
     */
    private void getMissingResources(IP4Connection con, final String[] paths,
            List<IFile> list) {
        IFileSpec[] specs = con.getMissingFiles(paths);
        List<String> clientFiles = new ArrayList<String>();
        for (IFileSpec spec : specs) {
            String path = P4Resource.normalizeLocalPath(spec);
            if (path != null) {
                IP4File file = con.getFile(spec);
                if (file == null || !file.openedForDelete()) {
                    clientFiles.add(path);
                }
            }
        }
        addFilesToList(clientFiles.toArray(new String[0]), list);
    }

    /**
     * Get new files that are not under Perforce control
     * 
     * @param con
     *            the connection
     * @param folder
     *            the folder to check for new files
     * @param list
     *            new files are added to this list
     */
    private void getNewResources(IP4Connection con, IContainer folder,
            List<IFile> list) {

        IP4Resource resource = con.getResource(folder);
        if (resource instanceof IP4Container) {
            IP4Container p4Container = (IP4Container) resource;
            p4Container.refresh();
        }
        try {
            IResource[] members = folder.members();
            for (int i = 0; i < members.length; i++) {
                if (members[i] instanceof IFile) {
                    IP4Resource foundResource = con.getResource(members[i]);
                    if (foundResource instanceof IP4File
                            && isNewFile((IFile) members[i],
                                    (IP4File) foundResource)) {
                        list.add((IFile) members[i]);
                    }
                }
            }
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

    /**
     * Get new files that are not under Perforce control
     * 
     * @param con
     *            the connection
     * @param files
     *            the files to check
     * @param list
     *            new files are added to this list
     */
    private void getNewResources(IP4Connection con, IFile[] files,
            List<IFile> list) {
        for (int i = 0; i < files.length; i++) {
            IP4Resource p4Resource = con.getResource(files[i]);
            if (p4Resource instanceof IP4File) {
                IP4File p4File = (IP4File) p4Resource;
                if (isNewFile(files[i], p4File)) {
                    list.add(files[i]);
                }
            }
        }
    }

    /**
     * Determine if file resource is new
     * 
     * @param lookup
     *            table of current perforce files
     * @param file
     *            the file resource
     * @return true if file is new
     */
    private boolean isNewFile(IFile localFile, IP4File p4File) {
        boolean isNew = false;
        if (!isResourceIgnored(localFile)) {
            if (p4File == null) {
                isNew = true;
            } else if (p4File.getP4JFile() == null) {
                isNew = true;
            } else {
                // Check for case where file is currently deleted (and not open
                // for add)

                if (p4File.getAction() == null && p4File.isHeadActionDelete()) {
                    isNew = true;
                }
            }
        }
        return isNew;
    }

    private void getFoldersFiles(final IResource[] resources,
            final List<IContainer> folders, final List<IResource> files) {
        for (int i = 0; i < resources.length; i++) {
            if (resources[i] instanceof IContainer) {
                try {
                    resources[i].accept(new IResourceVisitor() {

                        public boolean visit(IResource resource)
                                throws CoreException {
                            if (resource instanceof IContainer) {
                                folders.add((IContainer) resource);
                            }
                            return true;
                        }
                    });
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                }
            } else if (resources[i] instanceof IFile) {
                files.add(resources[i]);
            }
        }
    }

    private String getDiffFolderPath(IResource folder) {
        return PerforceProviderPlugin.getResourcePath(folder)
                + File.separatorChar + "*"; //$NON-NLS-1$
    }

    private String getMissingFolderPath(IResource folder) {
        return PerforceProviderPlugin.getResourcePath(folder)
                + File.separatorChar + "..."; //$NON-NLS-1$
    }

    private void addFilesToList(String[] paths, List<IFile> list) {
        IFile[] files = PerforceProviderPlugin.getWorkspaceFiles(paths);
        for (int i = 0; i < files.length; i++) {
            if (!list.contains(files[i])) {
                list.add(files[i]);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final Map<IP4Connection, List<IResource>> providerMap = getProviderMap();
        final IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                final List<Consistency> consistencies = new ArrayList<Consistency>();
                for (IP4Connection connection : providerMap.keySet()) {
                    List<IResource> resourceList = providerMap.get(connection);
                    IResource[] resources = resourceList
                            .toArray(new IResource[resourceList.size()]);
                    if (resources.length > 0) {
                        Consistency consistency = calculateConsistency(
                                connection, resources, monitor);
                        if (consistency != null) {
                            consistencies.add(consistency);
                        }
                    }
                }
                if (!consistencies.isEmpty()) {
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            displayDialog(consistencies
                                    .toArray(new Consistency[consistencies
                                            .size()]));
                        }
                    });
                }
            }

            @Override
            public String getTitle() {
                return Messages.CheckConsistencyAction_CheckingConsistency;
            }
        };
        if (isAsync()) {
            Job ccJob = new Job(runnable.getTitle()) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    runnable.run(monitor);
                    return Status.OK_STATUS;
                }
            };
            PlatformUI.getWorkbench().getProgressService()
                    .showInDialog(getShell(), ccJob);
            ccJob.schedule();
        } else {
            runnable.run(new NullProgressMonitor());
        }
    }

    /**
     * Calculate the consistency of the specified resources
     * 
     * @param connection
     * @param resources
     * @param monitor
     * @return - consistency object
     */
    public Consistency calculateConsistency(IP4Connection connection,
            IResource[] resources, IProgressMonitor monitor) {
        Consistency consistency = new Consistency();
        consistency.connection = connection;

        // Refresh all resources, fix for job035073
        monitor.setTaskName(Messages.CheckConsistencyAction_RefreshingResources);
        for (IResource resource : resources) {
            try {
                resource.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }

        final List<IContainer> folders = new ArrayList<IContainer>();
        final List<IResource> files = new ArrayList<IResource>();
        getFoldersFiles(resources, folders, files);

        final List<IFile> diffList = new ArrayList<IFile>();
        final List<IFile> missingList = new ArrayList<IFile>();
        final List<IFile> newList = new ArrayList<IFile>();

        try {
            int numTasks = folders.size();
            if (files.size() > 0) {
                numTasks++;
            }
            numTasks *= 3;
            monitor.beginTask(Messages.CheckConsistencyAction_TASKTITLE,
                    numTasks);

            for (IContainer folder : folders) {
                String diffPath = getDiffFolderPath(folder);
                String missingPath = getMissingFolderPath(folder);
                monitor.subTask(NLS.bind(
                        Messages.CheckConsistencyAction_FOLDERTASKTITLE,
                        folder.getFullPath()));
                getNewResources(connection, folder, newList);
                doneWork(monitor);
                getDiffResources(connection, new String[] { diffPath }, diffList);
                doneWork(monitor);
                getMissingResources(connection, new String[] { missingPath }, missingList);
                doneWork(monitor);
            }

            if (files.size() > 0) {
                IFile[] fileResources = files.toArray(new IFile[files.size()]);
                String[] paths = PerforceProviderPlugin
                        .getResourcePath(fileResources);
                monitor.subTask(Messages.CheckConsistencyAction_CHECKINGFILE);
                getDiffResources(connection, paths, diffList);
                doneWork(monitor);
                getMissingResources(connection, paths, missingList);
                doneWork(monitor);
                getNewResources(connection, fileResources, newList);
                doneWork(monitor);
            }
        } catch (InterruptedException e) {
            consistency.diffFiles = null;
            consistency.missingFiles = null;
            consistency.newFiles = null;
            return consistency;
        }
        consistency.diffFiles = diffList.toArray(new IFile[diffList.size()]);
        consistency.missingFiles = missingList.toArray(new IFile[missingList
                .size()]);
        consistency.newFiles = newList.toArray(new IFile[newList.size()]);
        return consistency;
    }

    private void displayDialog(Consistency[] consistencies) {
        for (Consistency consistency : consistencies) {
            if (consistency.diffFiles != null && consistency.newFiles != null
                    && consistency.missingFiles != null
                    && consistency.connection != null) {
                if (consistency.diffFiles.length > 0
                        || consistency.missingFiles.length > 0
                        || consistency.newFiles.length > 0) {
                    CheckConsistencyDialog dlg = new CheckConsistencyDialog(
                            P4UIUtils.getDialogShell(), consistency.connection,
                            consistency.diffFiles, consistency.missingFiles,
                            consistency.newFiles);
                    if (dlg.open() == Window.OK) {
                        openFiles(consistency.connection, dlg);
                    }
                } else {
                    P4ConnectionManager.getManager().openInformation(getShell(),
                            Messages.CheckConsistencyAction_DIALOGTITLE,
                            Messages.CheckConsistencyAction_DIALOGMESSAGE);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return containsOnlineConnection();
    }

}
