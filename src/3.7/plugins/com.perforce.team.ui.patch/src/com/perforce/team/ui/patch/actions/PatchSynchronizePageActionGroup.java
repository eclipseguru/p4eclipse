/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.actions;

import com.perforce.team.ui.synchronize.PerforceSyncActionGroup;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PatchSynchronizePageActionGroup extends SynchronizePageActionGroup {

    private IAction createPatchAction;

    /**
     * Create patch action group
     */
    public PatchSynchronizePageActionGroup() {

    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public void initialize(ISynchronizePageConfiguration configuration) {
        super.initialize(configuration);

        createPatchAction = new SynchronizeModelAction(
                Messages.PatchSynchronizePageActionGroup_CreatePatchTitle,
                configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new CreatePatchModelOperation(configuration, elements);
            }
        };
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);

        menu.appendToGroup(PerforceSyncActionGroup.OTHER_ACTION_GROUP,
                createPatchAction);
    }

}
