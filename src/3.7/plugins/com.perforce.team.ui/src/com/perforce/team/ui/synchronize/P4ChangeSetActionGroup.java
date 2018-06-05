/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.core.p4java.synchronize.P4SubmittedChangeSet;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.internal.core.subscribers.ChangeSet;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;

/**
 * Action group for p4 change sets
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4ChangeSetActionGroup extends SynchronizePageActionGroup {

    /**
     * CHANGELIST_ACTION_GROUP
     */
    public static final String CHANGELIST_ACTION_GROUP = "perforce_changelist_group"; //$NON-NLS-1$

    private Action edit;
    private Action toggleActivation;
    private Action view;

    private P4PendingChangeSet getPendingChangeSet(
            IStructuredSelection selection) {
        ChangeSet changeSet = getChangeSet(selection);
        return changeSet instanceof P4PendingChangeSet
                ? (P4PendingChangeSet) changeSet
                : null;
    }

    private ChangeSet getChangeSet(IStructuredSelection selection) {
        ChangeSet changeSet = null;
        if (selection != null && selection.size() == 1) {
            changeSet = P4CoreUtils.convert(selection.getFirstElement(),
                    ChangeSet.class);
        }
        return changeSet;
    }

    private boolean containsSubmittedChangeSet(IStructuredSelection selection) {
        if (selection != null && selection.size() > 0) {
            ChangeSet set = null;
            for (Object element : selection.toArray()) {
                set = P4CoreUtils.convert(element, ChangeSet.class);
                if (set == null) {
                    return false;
                } else if (!(set instanceof P4SubmittedChangeSet)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private P4SubmittedChangeSet[] getSubmittedChangeSets(
            IStructuredSelection selection) {
        List<P4SubmittedChangeSet> submitted = new ArrayList<P4SubmittedChangeSet>();
        if (selection != null) {
            ChangeSet set = null;
            for (Object element : selection.toArray()) {
                set = P4CoreUtils.convert(element, ChangeSet.class);
                if (set instanceof P4SubmittedChangeSet) {
                    submitted.add((P4SubmittedChangeSet) set);
                }
            }
        }
        return submitted.toArray(new P4SubmittedChangeSet[submitted.size()]);
    }

    private void createMenuActions(
            final ISynchronizePageConfiguration configuration) {

        edit = new SynchronizeModelAction(
                Messages.P4ChangeSetActionGroup_EditChangelist, configuration) {

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                super.updateSelection(selection);
                return getPendingChangeSet(selection) != null;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new EditChangelistModelOperation(configuration,
                        getPendingChangeSet(getStructuredSelection()));
            }

        };

        toggleActivation = new SynchronizeModelAction(
                Messages.P4ChangeSetActionGroup_MakeActivePendingChangelist,
                configuration) {

            private boolean active = false;

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                super.updateSelection(selection);
                P4PendingChangeSet set = getPendingChangeSet(selection);
                if (set != null) {
                    IP4PendingChangelist list = set.getChangelist();
                    if (list != null) {
                        active = list.isActive();
                    } else {
                        active = false;
                    }
                } else {
                    active = false;
                }
                if (active) {
                    setText(Messages.P4ChangeSetActionGroup_ClearAsActivePendingChangelist);
                } else {
                    setText(Messages.P4ChangeSetActionGroup_MakeActivePendingChangelist);
                }
                return set != null;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                P4PendingChangeSet set = getPendingChangeSet(getStructuredSelection());
                SynchronizeModelOperation operation = null;
                if (active) {
                    operation = new DeactivateChangelistModelOperation(
                            configuration, set);
                } else {
                    operation = new ActivateChangelistModelOperation(
                            configuration, set);
                }
                return operation;
            }

        };

        view = new SynchronizeModelAction(
                Messages.P4ChangeSetActionGroup_ViewChangelist, configuration) {

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                super.updateSelection(selection);
                return containsSubmittedChangeSet(selection);
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new ViewChangelistModelOperation(configuration,
                        getSubmittedChangeSets(getStructuredSelection()));
            }

        };
        view.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SUBMITTED_EDITOR));
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public void initialize(ISynchronizePageConfiguration configuration) {
        super.initialize(configuration);
        createMenuActions(configuration);
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        menu.add(new Separator(CHANGELIST_ACTION_GROUP));
        if (edit.isEnabled()) {
            menu.add(edit);
        }
        if (toggleActivation.isEnabled()) {
            menu.add(toggleActivation);
        }
        if (view.isEnabled()) {
            menu.add(view);
        }
    }

}
