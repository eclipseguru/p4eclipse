/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.ChangelistSelection;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.InfiniteProgressMonitor;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.RefactorDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class MoveManager extends RefactorManager implements
        IMoveManager {

    /**
     * PROGRESS
     */
    public static final int PROGRESS = 100;

    private boolean useSession = false;
    private boolean firePostEvent = false;

    /**
     * Create a move manager
     */
    public MoveManager() {
        this(false, false);
    }

    /**
     * Create a move manager
     * 
     * @param useSession
     * @param firePostEvent
     */
    public MoveManager(boolean useSession, boolean firePostEvent) {
        this.useSession = useSession;
        this.firePostEvent = firePostEvent;
    }

    /**
     * Do the two string differ only in case?
     * 
     * @param from
     * @param to
     * @return true if case only, false otherwise
     */
    protected boolean caseOnlyChange(String from, String to) {
        if (from != null && to != null) {
            return from.equalsIgnoreCase(to);
        } else {
            return false;
        }
    }

    /**
     * Do the two resources have the same parent resource?
     * 
     * @param resource1
     * @param resource2
     * @return true if same parent, false otherwise
     */
    protected boolean sameParent(IResource resource1, IResource resource2) {
        if (resource1 != null && resource2 != null) {
            resource1 = resource1.getParent();
            resource2 = resource2.getParent();
            if (resource1 != null) {
                return resource1.equals(resource2);
            } else {
                return resource2 == null;
            }
        } else {
            return false;
        }
    }

    /**
     * Validate move
     * 
     * @param from
     * @param to
     * @return true if move can proceed, false to abort
     */
    protected boolean validateMove(IResource from, IResource to) {
        boolean moveable = false;
        if (from != null && to != null) {
            IP4Connection fromConnection = P4ConnectionManager.getManager()
                    .getConnection(from.getProject());
            IP4Connection toConnection = P4ConnectionManager.getManager()
                    .getConnection(to.getProject());
            if (fromConnection != null && fromConnection.equals(toConnection)) {
                moveable = true;
                if (!fromConnection.isCaseSensitive()) {
                    if (from instanceof IContainer && to instanceof IContainer) {
                        if (caseOnlyChange(from.getFullPath().toOSString(), to.getFullPath().toOSString())) {
                            showCaseRenameError(fromConnection);
                        }
                    } else if (from instanceof IFile && to instanceof IFile) {
                        if (sameParent(from, to)
                                && caseOnlyChange(from.getName(), to.getName())) {
                            showCaseRenameError(fromConnection);
                        }
                    }

                }
            }
        }
        return moveable;
    }

    /**
     * Show case rename error
     * 
     * @param connection
     * @throws OperationCanceledException
     */
    protected void showCaseRenameError(final IP4Connection connection)
            throws OperationCanceledException {
        final String title = MessageFormat.format(
                Messages.MoveManager_CaseRenamesTitle, connection
                        .getParameters().getPort());
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openError(
                        P4UIUtils.getShell(), title,
                        Messages.MoveManager_CaseRenamesMessage);
            }
        });
        throw new OperationCanceledException(title);
    }

    private boolean canMoveFiles(IP4Resource fromResource,
            IP4Resource toResource) {
        return fromResource instanceof IP4File && toResource instanceof IP4File
                && fromResource.getConnection() == toResource.getConnection();
    }

    /**
     * Move a file
     * 
     * @param source
     * @param destination
     * @param files
     * @param changelistSelection
     * @param monitor
     * @param session
     */
    protected void internalMoveFile(IFile source, IFile destination,
            Collection<IP4File> files, ChangelistSelection changelistSelection,
            IProgressMonitor monitor, MoveSession session) {
        IP4Resource fromResource = P4ConnectionManager.getManager()
                .getResource(source);
        IP4Resource toResource = P4ConnectionManager.getManager().getResource(
                destination);
        if (canMoveFiles(fromResource, toResource)) {
            IP4File fromFile = (IP4File) fromResource;
            IP4File toFile = (IP4File) toResource;
            boolean moved = moveFile(fromFile, toFile, changelistSelection);
            if (session != null) {
                session.moved(source, destination, moved);
            }
            if (files != null) {
                IP4Resource finalSource = P4ConnectionManager.getManager()
                        .getResource(source);
                IP4Resource finalDest = P4ConnectionManager.getManager()
                        .getResource(destination);
                // Use the latest version from the connection manager as the
                // first ones obtained might be stale and we don't want a
                // useless refresh if the connection manager has a more
                // complete copy of the file data
                if (finalSource instanceof IP4File) {
                    files.add((IP4File) finalSource);
                }
                if (finalDest instanceof IP4File) {
                    files.add((IP4File) finalDest);
                }
            }
            monitor.worked(1);
        }
    }

    /**
     * Move a folder from the source to the destination
     * 
     * @param destination
     * @param source
     * @param files
     * @param changelist
     * @param monitor
     * @param session
     */
    protected abstract void moveFolder(IFolder source, IFolder destination,
            List<IP4File> files, ChangelistSelection changelist,
            IProgressMonitor monitor, MoveSession session);

    /**
     * Move a p4 file
     * 
     * @param fromFile
     * @param toFile
     * @param selection
     * @return - boolean
     */
    protected abstract boolean moveFile(IP4File fromFile, IP4File toFile,
            ChangelistSelection selection);

    /**
     * Pre move update tree
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     */
    protected void preUpdateTree(IResourceTree tree, IFolder source,
            IFolder destination, int flags, IProgressMonitor monitor) {
        // Does nothing, subclasses should override if need
    }

    /**
     * Post move updated tree
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     */
    protected void postUpdateTree(IResourceTree tree, IFolder source,
            IFolder destination, int flags, IProgressMonitor monitor) {
        // Does nothing, subclasses should override if need
    }

    /**
     * Pre move update tree
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     */
    protected void preUpdateTree(IResourceTree tree, IFile source,
            IFile destination, int flags, IProgressMonitor monitor) {
        // Does nothing, subclasses should override if need
    }

    /**
     * Post move updated tree
     * 
     * @param tree
     * @param source
     * @param destination
     * @param flags
     * @param monitor
     */
    protected void postUpdateTree(IResourceTree tree, IFile source,
            IFile destination, int flags, IProgressMonitor monitor) {
        // Does nothing, subclasses should override if need
    }

    /**
     * @see com.perforce.team.ui.refactor.IMoveManager#move(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFolder,
     *      org.eclipse.core.resources.IFolder, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean move(IResourceTree tree, IFolder source,
            IFolder destination, int flags, IProgressMonitor monitor) {
        if (isEnabled()) {
            if (!validateMove(source, destination)) {
                return true;
            }
            InfiniteProgressMonitor subMonitor = new InfiniteProgressMonitor(
                    monitor, 2);
            subMonitor.beginTask("", PROGRESS); //$NON-NLS-1$

            ChangelistSelection selection = getRefactorId(source, destination,
                    subMonitor);

            MoveSession session = useSession ? new MoveSession(tree, flags,
                    monitor) : null;
            List<IP4File> files = firePostEvent
                    ? new ArrayList<IP4File>()
                    : null;

            preUpdateTree(tree, source, destination, flags, monitor);
            moveFolder(source, destination, files, selection, subMonitor,
                    session);
            postUpdateTree(tree, source, destination, flags, monitor);

            if (session != null) {
                session.finish();
            }
            if (files != null) {
                firePostEvent(files);
                firePostAddEvent(destination);
            }
            subMonitor.done();
        } else {
            tree.standardMoveFolder(source, destination, flags, monitor);
        }
        return true;
    }

    /**
     * @see com.perforce.team.ui.refactor.IMoveManager#move(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IProject,
     *      org.eclipse.core.resources.IProjectDescription, int,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean move(IResourceTree tree, IProject source,
            IProjectDescription description, int updateFlags,
            IProgressMonitor monitor) {
        tree.standardMoveProject(source, description, updateFlags, monitor);
        return true;
    }

    /**
     * @see com.perforce.team.ui.refactor.IMoveManager#move(org.eclipse.core.resources.team.IResourceTree,
     *      org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile,
     *      int, org.eclipse.core.runtime.IProgressMonitor)
     */
    public boolean move(IResourceTree tree, IFile source, IFile destination,
            int flags, IProgressMonitor monitor) {
        if (isEnabled()) {
            if (!validateMove(source, destination)) {
                return true;
            }
            SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
            subMonitor.beginTask("", 2); //$NON-NLS-1$
            ChangelistSelection selection = getRefactorId(source, destination,
                    subMonitor);
            subMonitor.worked(1);

            MoveSession session = useSession ? new MoveSession(tree, flags,
                    monitor) : null;
            List<IP4File> files = firePostEvent
                    ? new ArrayList<IP4File>()
                    : null;

            preUpdateTree(tree, source, destination, flags, monitor);
            internalMoveFile(source, destination, files, selection, subMonitor,
                    session);
            postUpdateTree(tree, source, destination, flags, monitor);

            if (session != null) {
                session.finish();
            }
            if (files != null) {
                firePostEvent(files);
                firePostAddEvent(destination);
            }
            subMonitor.done();
        } else {
            tree.standardMoveFile(source, destination, flags, monitor);
        }
        return true;
    }

    /**
     * Should the refactor dialog be shown?
     * 
     * Dialog should be shown for following cases:
     * 
     * - source is a {@link IFile} and is either opened for add or not open for
     * delete and have revision is greater than zero
     * 
     * - source is anything but an {@link IFile}
     * 
     * @param source
     * @param destination
     * @return - true if dialog should be shown, false otherwise
     */
    private boolean showDialog(IResource source, IResource destination) {
        boolean show = true;
        if (source instanceof IFile) {
            IP4Resource resource = P4ConnectionManager.getManager()
                    .getResource(source);
            if (resource instanceof IP4File) {
                IP4File p4File = (IP4File) resource;
                show = p4File.openedForAdd()
                        || (!p4File.openedForDelete() && p4File
                                .getHaveRevision() > 0);
            } else {
                show = false;
            }
        }
        return show;
    }

    /**
     * Fire a refresh event after refactoring
     * 
     * @param files
     */
    protected void firePostEvent(List<IP4File> files) {
        if (!files.isEmpty()) {
            P4Event event = new P4Event(EventType.REFRESHED,
                    files.toArray(new IP4File[0]));
            P4Workspace.getWorkspace().notifyListeners(event);
        }
    }

    private void firePostAddEvent(IResource resource){
    	IP4Resource p4resource = P4ConnectionManager.getManager().getResource(resource);
        P4Event event = new P4Event(EventType.MOVE_ADDED,
                p4resource);
        P4Workspace.getWorkspace().notifyListeners(event);
	}


    /**
     * Get id of changelist for refactoring
     * 
     * @param source
     * @param destination
     * @param monitor
     * @return - int changelist id
     */
    protected ChangelistSelection getRefactorId(final IResource source,
            final IResource destination, IProgressMonitor monitor) {
        final int[] id = new int[] { getActiveId(source) };
        final String[] comment = new String[] { null };
        final boolean[] canceled = new boolean[] { false };
        if (id[0] < 0 && isEnabled(IPreferenceConstants.REFACTOR_DIALOG)) {
            final IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(source.getProject());
            if (connection != null && showDialog(source, destination)) {
                final boolean[] makeActive = new boolean[] { false };
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        RefactorDialog dialog = new RefactorDialog(P4UIUtils
                                .getDialogShell(), source, destination,
                                connection);
                        if (RefactorDialog.OK == dialog.open()) {
                            id[0] = dialog.getPendingId();
                            comment[0] = dialog.getPendingComment();
                            makeActive[0] = dialog.useSelected();
                        } else {
                            canceled[0] = true;
                        }
                    }
                });
                if (id[0] == IP4PendingChangelist.NEW && comment[0] != null) {
                    monitor.setTaskName("Create new pending changelist"); //$NON-NLS-1$

                    IP4PendingChangelist newList = connection.createChangelist(
                            comment[0], null);
                    if (newList != null) {
                        id[0] = newList.getId();
                    }
                }
                if (makeActive[0]) {
                    connection.setActivePendingChangelist(id[0]);
                }
            }
        }
        if (canceled[0]) {
            throw new OperationCanceledException(
                    "Changelist selection dialog was cancelled."); //$NON-NLS-1$
        }
        monitor.worked(1);
        boolean explicit = true;
        if (id[0] < 0) {
            id[0] = IChangelist.DEFAULT;
            explicit = false;
        }
        return new ChangelistSelection(id[0], explicit);
    }

}
