/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ISubmitMessageProvider;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmitAction extends P4Action {

    private P4Collection collectionSelection = new P4Collection();
    private String description = null;
    private boolean reopen = false;
    private String status = null;
    private ISubmitMessageProvider messageProvider = null;

    /**
     * @return the messageProvider
     */
    public ISubmitMessageProvider getMessageProvider() {
        return this.messageProvider;
    }

    /**
     * @param messageProvider
     *            the message provider to set
     */
    public void setMessageProvider(ISubmitMessageProvider messageProvider) {
        this.messageProvider = messageProvider;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = getResourceSelection();
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4PendingChangelist) {
                        IP4PendingChangelist list = (IP4PendingChangelist) resource;
                        if (list.isOnClient() && list.getFiles().length > 0) {
                            enabled = true;
                            break;
                        }
                    } else if (resource instanceof IP4Container) {
                        enabled = true;
                        break;
                    } else if (resource instanceof IP4File) {
                        IP4File file = (IP4File) resource;
                        if (file.isOpened()) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * Gets the selected elements that were reverted
     *
     * @return - collection of reverted resources
     */
    public P4Collection getSelected() {
        return this.collectionSelection;
    }

    private void resetStateValidation() {
        if (this.collectionSelection != null
                && !this.collectionSelection.isEmpty()) {
            this.collectionSelection.resetStateValidation();
        }
    }

    /**
     * Submits the specified files and jobs as part of the specified list
     *
     * @param list
     * @param uncheckedJobs jobs previous associated with list but removed
     * @param checkedJobs jobs associated with list
     * @param files
     * @param description
     * @param reopen
     * @param status
     */
    public void submit(final IP4PendingChangelist list, final IP4Job[] uncheckedJobs, final IP4Job[] checkedJobs,
            final IP4File[] files, final String description,
            final boolean reopen, final String status) {
        if (files != null) {
            for (IP4File file : files) {
                collectionSelection.add(file);
            }
            WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

                @Override
                protected void execute(final IProgressMonitor monitor)
                        throws CoreException, InvocationTargetException,
                        InterruptedException {
                    monitor.beginTask("", 1000); //$NON-NLS-1$
                    monitor.setTaskName(Messages.SubmitAction_SubmittingChangelist);
                    try {
                        if (uncheckedJobs != null && uncheckedJobs.length > 0) {
                            createCollection(uncheckedJobs).unfix(list);
                        }
                        monitor.worked(100);

                        final int[] ids=new int[1];
                        try {
							Tracing.printExecTime2(() -> {
								ids[0] = list.submit(reopen, description, files, checkedJobs, status,
										new SubProgressMonitor(monitor, 700));
							}, "SUBMIT", "Submitting pendinglist...");
						} catch (CoreException | InvocationTargetException | InterruptedException e) {
							throw e;
						} catch(Exception e) {
							PerforceProviderPlugin.logError(e);
						}
                        int id=ids[0];

                        // Fix for job037435, refresh local resources to handle
                        // ktext types
                        monitor.setTaskName(Messages.SubmitAction_RefreshingSubmittedFiles);
                        monitor.worked(100);
                        createCollection(files).refreshLocalResources(
                                IResource.DEPTH_ONE);
                        monitor.worked(100);

                        if (id > 0) {
                            monitor.setTaskName(MessageFormat.format(
                                    Messages.SubmitAction_ChangelistSubmitted,
                                    id));
                        }
                    } finally {
                        monitor.done();
                    }
                    updateActionState();
                }

            };
            SubmitAction.this.run(operation, Messages.SubmitAction_SubmittingChangelist,
                    Messages.SubmitAction_SubmitFailed, PROGRESS_JOB);
        }
    }

    private Map<IP4Connection, List<IP4Resource>> getConnectionMapping(
            P4Collection collection) {
        Map<IP4Connection, List<IP4Resource>> connectionResources = new HashMap<IP4Connection, List<IP4Resource>>();
        for (IP4Resource resource : collection.members()) {
            IP4Connection connection = resource.getConnection();
            if (connection != null) {
                // Check to try to fetch any unfetched job specs for
                // connections
                connection.getJobSpec();
                List<IP4Resource> connResources = connectionResources
                        .get(connection);
                if (connResources == null) {
                    connResources = new ArrayList<IP4Resource>();
                    connectionResources.put(connection, connResources);
                }
                connResources.add(resource);
            }
        }
        return connectionResources;
    }

    private void submitListsFromCollection(final P4Collection collection,
            final boolean showDialog) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                // Generate map of connection to resources to handle submit
                // selected from multiple connections
                Map<IP4Connection, List<IP4Resource>> connectionResources = getConnectionMapping(collection);
                Map<IP4PendingChangelist, List<IP4File>> initialCheckedMap = new HashMap<IP4PendingChangelist, List<IP4File>>();

                // Iterate over all connections and find any files or folders
                // that belong to a changelist
                for (Map.Entry<IP4Connection, List<IP4Resource>> entry: connectionResources.entrySet()) {
                	IP4Connection connection = entry.getKey();
                	List<IP4Resource> resources = entry.getValue();
                    IP4PendingChangelist[] allLists = connection
                            .getCachedPendingChangelists();
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4PendingChangelist) {
                            // Handle pending changelists in the selection
                            IP4PendingChangelist list = (IP4PendingChangelist) resource;
                            if (!initialCheckedMap.containsKey(list)) {
                                if(list.needsRefresh())
                                	list.refresh();
                            }
                            initialCheckedMap.put(list,
                                    Arrays.asList(list.getPendingFiles()));
                        } else if (resource instanceof IP4File) {
                            // Handle files in the selection
                            IP4File file = (IP4File) resource;
                            IP4PendingChangelist changelist = file.getChangelist(true);
                            if (changelist != null) {
                                if (!initialCheckedMap.containsKey(changelist)) {
                                    if(changelist.needsRefresh())
                                    	changelist.refresh();
                                }
                                List<IP4File> files = initialCheckedMap
                                        .get(changelist);
                                if (files == null) {
                                    files = new ArrayList<IP4File>();
                                    initialCheckedMap.put(changelist, files);
                                }
                                if (!files.contains(file)) {
                                    files.add(file);
                                }
                            }
                        } else if (resource instanceof IP4Container) {
                            // Handle containers in the selection
                            for (IP4PendingChangelist list : allLists) {
                            	List<IP4File> existing = initialCheckedMap
                            			.get(list);
                            	if(list.needsRefresh())
                            		list.refresh();

                            	List<IP4File> files = findCheckedFiles(
                            			(IP4Container) resource, list);
                            	if (existing == null){
                            		if(files.size()>0)
                            			initialCheckedMap.put(list, files);
                            	}else{
                            		existing.addAll(files);
                                }
                            }
                        }
                    }
                }
                showSubmitDialogs(initialCheckedMap, showDialog);
            }

            @Override
            public String getTitle() {
                return Messages.SubmitAction_SubmittingChangelistTitle;
            }
        };
        runRunnable(runnable);
    }

    private List<IP4File> findCheckedFiles(IP4Container container,
            IP4PendingChangelist list) {
        List<IP4File> files = new ArrayList<IP4File>();

        // Try matching local path
        String cPath = container.getLocalPath();
        if (cPath != null) {
            cPath = cPath.toUpperCase();
            for (IP4Resource file : list.members()) {
                if (file instanceof IP4File) {
                    String localPath = file.getLocalPath();
                    if (localPath != null) {
                        localPath = localPath.toUpperCase();
                        if (localPath.startsWith(cPath)) {
                            files.add((IP4File) file);
                        }
                    }
                }
            }
        } else {
            // Try matching remote path
            cPath = container.getRemotePath();
            if (cPath != null) {
                for (IP4Resource file : list.members()) {
                    if (file instanceof IP4File) {
                        String remotePath = file.getRemotePath();
                        if (remotePath != null) {
                            remotePath = remotePath.toUpperCase();
                            if (remotePath.startsWith(cPath)) {
                                files.add((IP4File) file);
                            }
                        }
                    }
                }
            }
        }
        return files;
    }

    private boolean validateList(IP4PendingChangelist list) {
        boolean valid = false;
        if (!list.isDefault() && list.isShelved()) {
            if (P4ConnectionManager
                    .getManager()
                    .openQuestion(
                            P4UIUtils.getDialogShell(),
                            Messages.SubmitAction_ChangelistHasShelvedFilesTitle,
                            MessageFormat
                                    .format(Messages.SubmitAction_ChangelistHasShelvedFilesMessage,
                                            list.getId()))) {
                list.deleteShelved();
                valid = !list.isShelved();
            }
        } else {
            valid = true;
        }
        return valid;
    }

    private void showSubmitDialogs(
            final Map<IP4PendingChangelist, List<IP4File>> lists,
            final boolean showDialog) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (!lists.isEmpty()) {
                    for (final IP4PendingChangelist list : lists.keySet()) {
                        final IP4File[] files = lists.get(list).toArray(
                                new IP4File[0]);
						Tracing.printExecTime(() -> PerforceUIPlugin.saveDirtyResources(getTargetPage(), files),
								"SUBMIT", "SubmitAction.saveDirtyResources()");

                        if (showDialog) {
                            boolean listValid = validateList(list);
                            if (listValid) {
                                String description = null;
                                if (messageProvider != null) {
                                    description = messageProvider
                                            .getDescription(list);
                                }
                                Tracing.printTrace(Policy.DEBUG, "SUBMIT", "create ChangeSpecDialog"); //$NON-NLS-1$ //$NON-NLS-2$
                                ChangeSpecDialog dialog = new ChangeSpecDialog(
                                        list, files, getShell(), true,
                                        description);
                                Tracing.printTrace(Policy.DEBUG, "SUBMIT", "poping up ChangeSpecDialog"); //$NON-NLS-1$ //$NON-NLS-2$
                                if (ChangeSpecDialog.OK == dialog.open()) {
                                    IP4Job[] checkedJobs = dialog.getCheckedJobs();
                                    IP4Job[] uncheckedJobs = dialog
                                            .getUncheckedJobs();

                                    submit(list, uncheckedJobs, checkedJobs,
                                            dialog.getCheckedFiles(),
                                            dialog.getDescription(),
                                            dialog.reopenFiles(),
                                            dialog.getStatus());
                                }
                            }
                        } else if (description != null) {
							Tracing.printExecTime(() -> submit(list, null, null, files, description, reopen, status),
									"SUBMIT", "SubmitAction.submit()");
                        }
                    }
                	Tracing.printExecTime(() -> resetStateValidation(), "SUBMIT", "SubmitAction.resetStateValidation()");
                } else {
                    P4ConnectionManager.getManager().openInformation(
                            getShell(),
                            Messages.SubmitAction_NoFilesToSubmitTitle,
                            Messages.SubmitAction_NoFilesToSubmitMessage);
                }
            }
        });
    }

    /**
     * Runs the submit and optionally shows it as a dialog or just submits with
     * the current collection and settings
     *
     * @param showDialog
     */
    public void runAction(boolean showDialog) {
        P4Collection collection = getResourceSelection();
        submitListsFromCollection(collection, showDialog);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        runAction(true);
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the reopen
     */
    public boolean isReopen() {
        return this.reopen;
    }

    /**
     * @param reopen
     *            the reopen to set
     */
    public void setReopen(boolean reopen) {
        this.reopen = reopen;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
