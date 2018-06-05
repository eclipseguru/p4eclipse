/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

/**
 * <ul>	
 * 	<li>p4 revert -k</li>
 * 	<li>p4 edit</li>
 * </ul>
 */
public class RevertEditAction extends OpenAction {

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

    private void edit(final P4Collection collection, final int changelist,
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
                collection.revertThenEdit(changelist, description, setActive);
                monitor.worked(1);
                monitor.done();

                collection.refreshLocalResources(IResource.DEPTH_INFINITE);
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
            P4Collection collection, boolean setActive) {
        edit(collection, changelist, description, setActive);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getComboTitle()
     */
    @Override
    public String getComboTitle() {
        return Messages.EditAction_OpenInChangelist;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {
        return Messages.EditAction_CheckOut;
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
        return Messages.EditAction_CheckingOut;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDefaultDescription()
     */
    @Override
    protected String getDefaultDescription() {
        return P4Collection.EDIT_DEFAULT_DESCRIPTION;
    }
}
