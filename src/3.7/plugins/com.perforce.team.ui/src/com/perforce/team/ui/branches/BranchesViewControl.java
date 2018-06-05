package com.perforce.team.ui.branches;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PerforceFilterViewControl;


public class BranchesViewControl extends PerforceFilterViewControl {
    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.branches.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.branches.HIDE_FILTERS"; //$NON-NLS-1$
    

    // Action to refresh view
    private Action refreshAction;

    // Action to switch showing details panel on/off
    private Action showDetailsAction;

    private Action openPrefs;

    // True if showing branch details
    private boolean displayDetails;

    private BranchesViewer branchesViewer = null;

    private IP4Listener workspaceListener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
            if (EventType.REFRESHED == event.getType()
                    || EventType.CREATE_BRANCH == event.getType()) {
                final IP4Branch[] branches = event.getBranches();
                if (branches.length > 0) {
                    UIJob refreshJob = new UIJob(
                            Messages.BranchesView_RefreshingBranches) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            TableViewer viewer = getTableViewer();
                            if (p4Connection != null && viewer != null
                                    && !viewer.getTable().isDisposed()) {
                                switch (event.getType()) {
                                case REFRESHED:
                                    handleRefresh(branches);
                                    break;
                                case CREATE_BRANCH:
                                    handleCreate(branches);
                                    break;
                                default:
                                    break;
                                }

                            }
                            return Status.OK_STATUS;
                        }
                    };
                    refreshJob.schedule();
                }
            }
        }
		public String getName() {
			return BranchesViewControl.this.getClass().getSimpleName();
		}
    };

    public BranchesViewControl(IPerforceView view) {
        super(view);
        this.branchesViewer = new BranchesViewer();
        setFilterViewer(this.branchesViewer);
    }

    private void handleRefresh(IP4Branch[] branches) {
        for (IP4Branch branch : branches) {
            if (p4Connection.equals(branch.getConnection())) {
            	TableViewer tblViewer = getTableViewer();
                if(tblViewer!=null)
                	tblViewer.update(branch, null);
                branchesViewer.refreshDetails(branch);
            }
        }
    }

    private void handleCreate(IP4Branch[] branches) {
        if (branchesViewer != null) {
            for (IP4Branch branch : branches) {
                if (p4Connection.equals(branch.getConnection())) {
                    branchesViewer.addBranch(branch);
                }
            }
        }
    }

    public void setFocus() {
        if (branchesViewer != null) {
            Table control = branchesViewer.getTableControl();
            if (control != null && !control.isDisposed()) {
                control.setFocus();
            }
        }
    }

    public void dispose() {
        removeProjectListeners();
        P4ConnectionManager.getManager().removeListener(workspaceListener);
    }

    public void refresh() {
        if (this.branchesViewer != null) {
            this.branchesViewer.refresh();
        }
    }

    @Override
    protected String getFilterPreference() {
        return HIDE_FILTERS;
    }

    @Override
    protected String getSelectedName() {
        return Messages.BranchesView_Branches;
    }

    @Override
    protected void createControl(Composite parent) {
        createMenus();

        showNoConnection();

        addProjectListeners();

        P4ConnectionManager.getManager().addListener(workspaceListener);
    }

    public Table getTableControl() {
        return branchesViewer != null ? branchesViewer.getTableControl() : null;
    }

    public TableViewer getTableViewer() {
        return branchesViewer != null ? branchesViewer.getViewer() : null;
    }


    private void hookContextMenu() {
        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(branchesViewer.getTableControl());
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        manager.setRemoveAllWhenShown(true);
        if(null!=branchesViewer.getTableControl())
        	branchesViewer.getTableControl().setMenu(menu);
        registerContextMenu(manager, branchesViewer.getViewer());
    }

    /**
     * Set the input to the viewer
     * 
     * @param con
     */
    @Override
    protected void setViewerInput(IP4Connection con) {
        if (connectionOK(con)) {
            refreshAction.setEnabled(true);
            branchesViewer.createControl(displayArea, con, false,
                    displayDetails);
            showDisplayArea();
            hookContextMenu();
            branchesViewer.getViewer().addDoubleClickListener(
                    new IDoubleClickListener() {

                        public void doubleClick(DoubleClickEvent event) {
                            IP4Branch branch = getBranchSelection();
                            if (branch != null) {
                                edit(branch);
                            }
                        }
                    });
        } else {
            showNoConnection();
        }
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#showNoConnection()
     */
    @Override
    protected void showNoConnection() {
        super.showNoConnection();
        refreshAction.setEnabled(false);
    }

    private void edit(IP4Branch branch) {
        EditBranchAction edit = new EditBranchAction();
        edit.selectionChanged(null, new StructuredSelection(branch));
        edit.run(null);
    }

    /**
     * Create toolbar and context menus
     */
    private void createMenus() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        refreshAction = new Action(
                Messages.BranchesView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                branchesViewer.refresh();
            }
        };
        refreshAction.setToolTipText(Messages.BranchesView_RefreshBranches);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));

        openPrefs = new Action(Messages.BranchesView_OpenBranchesPreferences,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(BranchesPreferencePage.ID);
            }
        };

        showDetailsAction = new Action(Messages.BranchesView_ShowBranchDetails) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };
        displayDetails = plugin.getPreferenceStore()
                .getBoolean(DISPLAY_DETAILS);
        showDetailsAction.setChecked(displayDetails);
        showDetailsAction
                .addPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        displayDetails = showDetailsAction.isChecked();
                        PerforceUIPlugin.getPlugin().getPreferenceStore()
                                .setValue(DISPLAY_DETAILS, displayDetails);
                        if (showingConnection()) {
                            showDetails(displayDetails);
                        }
                    }
                });

        Action newBranchAction = new Action(Messages.BranchesView_NewBranch,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_ADD)) {

            @Override
            public void run() {
                IP4Connection connection = p4Connection;
                if (connection != null) {
                    NewBranchAction newBranch = new NewBranchAction();
                    newBranch.selectionChanged(null, new StructuredSelection(
                            connection));
                    newBranch.run(null);
                }
            }
        };

        // Create the local tool bar
        IActionBars bars = getActionBars();
        if(bars!=null){
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(newBranchAction);
            tbm.add(refreshAction);
            tbm.add(openPrefs);
            tbm.update(false);
    
            // Create the pulldown menu
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
        }
    }

    private IP4Branch getBranchSelection() {
        IP4Branch selected = null;
        IStructuredSelection selection = (IStructuredSelection) this.branchesViewer
                .getViewer().getSelection();
        if (selection.size() == 1) {
            selected = (IP4Branch) selection.getFirstElement();
        }
        return selected;
    }

    /**
     * Gets the underlying branches viewer
     * 
     * @return - branches viewer
     */
    public BranchesViewer getBranchesViewer() {
        return this.branchesViewer;
    }

    public boolean isLoading() {
        return this.branchesViewer != null && this.branchesViewer.isLoading();
    }

    public void refreshRetrieveCount() {
        if (this.branchesViewer != null) {
            this.branchesViewer.refreshRetrieveCount();
        }
    }

    public void showMore() {
        if (this.branchesViewer != null) {
            this.branchesViewer.showMore();
        }
    }

    public void showDetails(boolean show) {
        if (this.branchesViewer != null && showingConnection()) {
            this.branchesViewer.updateSash(show);
        }
    }

    public BranchWidget getBranchDetails() {
        if (this.branchesViewer != null) {
            return this.branchesViewer.getDetails();
        }
        return null;
    }

}
