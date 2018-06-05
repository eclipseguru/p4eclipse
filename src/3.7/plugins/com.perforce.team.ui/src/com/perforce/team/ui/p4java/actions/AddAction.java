/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.IgnoredFiles;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AddAction extends OpenAction {

    private boolean makeWritable = false;

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
                if (resources.length > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource.getRemotePath() == null) {
                            enabled = true;
                            break;
                        } else if (resource instanceof IP4File) {
                            if (((IP4File) resource).isHeadActionDelete()) {
                                enabled = true;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return enabled;
    }

    private void add(final P4Collection collection, final int changelist,
            final String description, final boolean setActive) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                P4Collection filterIgnored = createCollection();
                filterIgnored.setType(collection.getType());
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4File) {
                        IFile file = ((IP4File) resource)
                                .getLocalFileForLocation();
                        if (file != null) {
                            IFile[] filtered = IgnoredFiles
                                    .filterAddFiles(new IFile[] { file });
                            if (filtered != null && filtered.length == 1
                                    && file.equals(filtered[0])) {
                                filterIgnored.add(resource);
                            }
                        }
                    } else {
                        filterIgnored.add(resource);
                    }
                }
                monitor.beginTask(getTitle(), 1);
                monitor.subTask(generateTitle(null, filterIgnored));
                P4Collection added = filterIgnored.addToChangelist(changelist, description,
                        setActive);
                monitor.worked(1);
                monitor.done();
                if (added != null && makeWritable) {
                    added.setReadOnly(false);
                }
                updateActionState();
            }

            /**
             * @see com.perforce.team.core.p4java.P4Runnable#getTitle()
             */
            @Override
            public String getTitle() {
                return getJobTitle();
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
    protected void runModifyAction(final int changelist,
            final String description, final P4Collection collection,
            boolean setActive) {
        add(collection, changelist, description, setActive);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getComboTitle()
     */
    @Override
    public String getComboTitle() {
        return Messages.AddAction_AddToChangelist;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {
        return Messages.AddAction_AddToSourceControl;
    }

    /**
     * @return the makeWritable
     */
    public boolean isMakeWritable() {
        return this.makeWritable;
    }

    /**
     * @param makeWritable
     *            the makeWritable to set
     */
    public void setMakeWritable(boolean makeWritable) {
        this.makeWritable = makeWritable;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#isValidFile(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean isValidFile(IP4File file) {
        return file.getRemotePath() == null || file.isHeadActionDelete();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getJobTitle()
     */
    @Override
    protected String getJobTitle() {
        return Messages.AddAction_AddingToSourceControl;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDefaultDescription()
     */
    @Override
    protected String getDefaultDescription() {
        return P4Collection.ADD_DEFAULT_DESCRIPTION;
    }
}
