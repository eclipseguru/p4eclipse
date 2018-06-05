/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.FastSyncInfoFilter.SyncInfoDirectionFilter;
import org.eclipse.team.internal.ui.synchronize.ChangeSetDiffNode;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.SynchronizeModelAction;
import org.eclipse.team.ui.synchronize.SynchronizeModelOperation;
import org.eclipse.team.ui.synchronize.SynchronizePageActionGroup;
import org.eclipse.ui.IActionBars;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSyncActionGroup extends SynchronizePageActionGroup {

    /**
     * FILE_ACTION_GROUP
     */
    public static final String FILE_ACTION_GROUP = "perforce_file_group"; //$NON-NLS-1$

    /**
     * OTHER_ACTION_GROUP
     */
    public static final String OTHER_ACTION_GROUP = "perforce_other_group"; //$NON-NLS-1$

    private Action history;
    private Action expandAll;
    private Action revert;
    private Action revertUnchanged;
    private Action commit;
    private Action update;
    private Action commitAll;
    private Action updateAll;
    private Action resolve;
    private Action reopen;
    private Action shelve;
    private Action timelapse;
    private Action consistency;

    private void createToolbarActions(
            final ISynchronizePageConfiguration configuration) {
        expandAll = new Action() {

            @Override
            public void run() {
                Viewer viewer = configuration.getPage().getViewer();
                if (viewer instanceof AbstractTreeViewer) {
                    ((AbstractTreeViewer) viewer).expandAll();
                }
            }

        };
        expandAll.setText(Messages.PerforceSyncActionGroup_ExpandAll);
        expandAll.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_EXPAND_ALL));

        updateAll = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_UpdateAll, configuration,
                getVisibleRootsSelectionProvider()) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.INCOMING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new UpdateModelOperation(configuration, elements);
            }
        };
        updateAll.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_UPDATE_ALL));

        commitAll = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_SubmitAll, configuration,
                getVisibleRootsSelectionProvider()) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new CommitModelOperation(configuration, elements,
                        getStructuredSelection());
            }

        };
        commitAll.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_COMMIT_ALL));
    }

    private void createMenuActions(
            final ISynchronizePageConfiguration configuration) {
        commit = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_Submit, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new CommitModelOperation(configuration, elements,
                        getStructuredSelection());
            }

        };
        commit.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SUBMIT));

        revert = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_Revert, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(new int[] {
                        SyncInfo.OUTGOING, SyncInfo.CONFLICTING, });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new RevertModelOperation(configuration, elements);
            }
        };
        revert.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_REVERT));

        revertUnchanged = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_RevertUnchanged, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new RevertUnchangedModelOperation(configuration,
                        elements);
            }
        };

        update = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_Update, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.INCOMING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new UpdateModelOperation(configuration, elements);
            }
        };
        update.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SYNC));

        resolve = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_Resolve, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.CONFLICTING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new ResolveModelOperation(configuration, elements);
            }
        };
        resolve.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_RESOLVE));

        history = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_RevisionHistory, configuration) {

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
            	if(selection!=null)
            		super.updateSelection(selection);
                if (selection != null && selection.size() == 1) {
                    Object element = selection.getFirstElement();
                    if (element instanceof ISynchronizeModelElement) {
                        return ((ISynchronizeModelElement) element).getKind() != 0;
                    }
                }
                return false;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new HistoryModelOperation(configuration, elements);
            }

        };
        history.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_HISTORY));

        reopen = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_MoveToChangelist,
                configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(new int[] {
                        SyncInfo.OUTGOING, SyncInfo.CONFLICTING, });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new ReopenModelOperation(configuration, elements);
            }
        };

        shelve = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_Shelve, configuration) {

            @Override
            protected FastSyncInfoFilter getSyncInfoFilter() {
                return new SyncInfoDirectionFilter(
                        new int[] { SyncInfo.OUTGOING });
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new ShelveModelOperation(configuration, elements);
            }
        };
        shelve.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SHELVE_ACTION));

        timelapse = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_TimelapseView, configuration) {

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
            	if(selection!=null)
            		super.updateSelection(selection);
                if (selection != null && selection.size() == 1) {
                    Object element = selection.getFirstElement();
                    if (element instanceof ISynchronizeModelElement) {
                        return ((ISynchronizeModelElement) element).getKind() != 0;
                    }
                }
                return false;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new TimeLapseModelOperation(configuration, elements);
            }
        };
        timelapse.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_TIME_LAPSE));

        consistency = new SynchronizeModelAction(
                Messages.PerforceSyncActionGroup_CheckConsistency,
                configuration) {

            @Override
            protected boolean updateSelection(IStructuredSelection selection) {
                boolean enabled = super.updateSelection(selection);
                if (enabled) {
                    for (Object element : selection.toArray()) {
                        if (element instanceof ChangeSetDiffNode) {
                            enabled = false;
                            break;
                        }
                    }
                }
                return enabled;
            }

            @Override
            protected SynchronizeModelOperation getSubscriberOperation(
                    ISynchronizePageConfiguration configuration,
                    IDiffElement[] elements) {
                return new CheckConsistencyModelOperation(configuration,
                        elements);
            }
        };
        consistency
                .setImageDescriptor(PerforceUIPlugin.getPlugin()
                        .getImageDescriptor(
                                IPerforceUIConstants.IMG_CHECK_CONSISTENCY));
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#initialize(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public void initialize(final ISynchronizePageConfiguration configuration) {
        super.initialize(configuration);
        createToolbarActions(configuration);
        createMenuActions(configuration);
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillActionBars(org.eclipse.ui.IActionBars)
     */
    @Override
    public void fillActionBars(IActionBars actionBars) {
        super.fillActionBars(actionBars);
        appendToGroup(actionBars.getToolBarManager(),
                ISynchronizePageConfiguration.NAVIGATE_GROUP, expandAll);
        appendToGroup(actionBars.getToolBarManager(),
                ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, commitAll);
        appendToGroup(actionBars.getToolBarManager(),
                ISynchronizePageConfiguration.SYNCHRONIZE_GROUP, updateAll);
    }

    /**
     * @see org.eclipse.team.ui.synchronize.SynchronizePageActionGroup#fillContextMenu(org.eclipse.jface.action.IMenuManager)
     */
    @Override
    public void fillContextMenu(IMenuManager menu) {
        super.fillContextMenu(menu);
        menu.add(new Separator(FILE_ACTION_GROUP));
        menu.add(commit);
        menu.add(resolve);
        menu.add(revert);
        menu.add(revertUnchanged);
        menu.add(update);
        menu.add(reopen);
        menu.add(new Separator(OTHER_ACTION_GROUP));
        menu.add(consistency);
        menu.add(history);
        menu.add(shelve);
        menu.add(timelapse);
    }
}
