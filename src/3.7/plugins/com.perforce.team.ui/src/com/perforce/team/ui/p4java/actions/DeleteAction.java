/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.TeamException;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @author ali
 */
public class DeleteAction extends OpenAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (isValidFile(file)) {
                                enabled = true;
                                break;
                            }
                        } else {
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
     * Is the specified resource associated with a client using the rmdir
     * option?
     * 
     * @param resource
     * @return - true if resource is associated with an rmdir enabled client,
     *         false otherwise
     */
    private boolean isRmdirResource(IP4Resource resource) {
        boolean rmdir = false;
        IClient client = resource.getClient();
        if (client != null) {
            IClientOptions options = client.getOptions();
            rmdir = options != null && options.isRmdir();
        }
        return rmdir;
    }

    /**
     * Fix for job034539, look for resources in the collection that are
     * associated with clients that are using the rmdir option and refresh
     * parent containers for files and folders found in the collection.
     * 
     * @param collection
     */
    private void handleRmdirClientRefresh(P4Collection collection) {
        for (IP4Resource resource : collection.members()) {
            if (isRmdirResource(resource)) {
                IContainer[] refreshContainers = null;
                if (resource instanceof IP4File) {
                    IFile localFile = ((IP4File) resource)
                            .getLocalFileForLocation();
                    if (localFile != null) {
                        refreshContainers = new IContainer[] { localFile
                                .getParent() };
                    }
                } else if (resource instanceof IP4Folder) {
                    refreshContainers = ((IP4Folder) resource)
                            .getLocalContainers();
                }
                if (refreshContainers != null) {
                	/*
                	 * Fix for job045495, job034539. Enforce the rule in a separate refresh job.
                	 */
                    for (final IContainer refresh : refreshContainers) {
                        if (refresh != null){
                        	Job job = new Job("Refresh deleted empty folders..."){ //$NON-NLS-1$

								@Override
								protected IStatus run(
										IProgressMonitor monitor) {
	                            	try {
										refresh.refreshLocal(IResource.DEPTH_ONE, null);
									} catch (CoreException e) {
										PerforceProviderPlugin.logError(e);
									}
									return Status.OK_STATUS;
								}
                        		
                        	};
                        	IWorkspace workspace = ResourcesPlugin.getWorkspace();
                        	job.setRule(workspace.getRuleFactory().refreshRule(refresh));
                        	job.schedule();
                        }
                    }
                }
            }
        }
    }

    private void delete(final P4Collection collection, final int changelist,
            final String description, final boolean setActive) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return getJobTitle();
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 1);
                monitor.subTask(generateTitle(null, collection));
                collection.delete(changelist, description, setActive);
                monitor.worked(1);
                monitor.done();

                collection.refreshLocalResources(IResource.DEPTH_ONE);
                handleRmdirClientRefresh(collection);

                updateActionState();
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#runModifyAction(int,
     *      java.lang.String, com.perforce.team.core.p4java.P4Collection,
     *      boolean)
     */
    @Override
    protected void runModifyAction(int changelist, String description,
            final P4Collection collection, boolean setActive) {
        delete(collection, changelist, description, setActive);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getComboTitle()
     */
    @Override
    public String getComboTitle() {
        return Messages.DeleteAction_OpenInChangelist;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {
        return Messages.DeleteAction_MarkForDelete;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#isValidFile(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean isValidFile(IP4File file) {
        return file.getP4JFile() != null && !file.isOpened();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getJobTitle()
     */
    @Override
    protected String getJobTitle() {
        return Messages.DeleteAction_MarkingForDelete;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDefaultDescription()
     */
    @Override
    protected String getDefaultDescription() {
        return P4Collection.DELETE_DEFAULT_DESCRIPTION;
    }

}
