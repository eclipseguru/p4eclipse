package com.perforce.team.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.JobColumnsDialog;
import com.perforce.team.ui.dialogs.JobsDialog;
import com.perforce.team.ui.dialogs.JobsPreferencesDialog;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.NewJobAction;


public class JobViewControl extends PerforceFilterViewControl implements IPropertyChangeListener {

    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.jobs.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.jobs.HIDE_FILTERS"; //$NON-NLS-1$

    
    // Action to refresh view
    private Action refreshAction;

    // Action to set view columns
    private Action columnsAction;

    private Action openPrefs;

    // Action to switch showing details panel on/off
    private Action showDetailsAction;

    private Action addJobAction;

    // True if showing changelist details
    private boolean displayDetails;

    // Job dialog panel
    private JobsDialog jobsList = null;

    private IP4Listener workspaceListener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
            if (EventType.REFRESHED == event.getType()
                    || EventType.CREATE_JOB == event.getType()) {
                final IP4Job[] jobs = event.getJobs();
                if (jobs.length > 0) {
                    UIJob refreshJob = new UIJob(
                            Messages.JobView_RefreshingJobs) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            TableViewer viewer = getTableViewer();
                            if (p4Connection != null && viewer != null
                                    && !viewer.getTable().isDisposed()) {
                                switch (event.getType()) {
                                case REFRESHED:
                                    handleRefresh(jobs, viewer);
                                    break;
                                case CREATE_JOB:
                                    handleCreate(jobs);
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
			return JobViewControl.this.getClass().getSimpleName();
		}
    };

    public JobViewControl(IPerforceView view) {
        super(view);
        this.jobsList = new JobsDialog();
        setFilterViewer(jobsList);
    }

    public void setFocus() {
        if (jobsList != null) {
            Table control = jobsList.getTableControl();
            if (control != null && !control.isDisposed()) {
                control.setFocus();
            }
        }
    }

    public void dispose() {

        P4ConnectionManager.getManager().removeListener(workspaceListener);

        removeProjectListeners();

        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        store.removePropertyChangeListener(this);
    }

    public void refresh() {
        if (this.jobsList != null) {
            this.jobsList.refresh();
        }
    }

    @Override
    protected String getFilterPreference() {
        return HIDE_FILTERS;
    }

    @Override
    protected String getSelectedName() {
        return Messages.JobView_Jobs;
    }

    @Override
    protected void createControl(Composite parent) {
        P4ConnectionManager.getManager().addListener(workspaceListener);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.JOBS_VIEW);

        jobsList.createControl(parent, false, displayDetails);
        createMenus();
        Table tbl = jobsList.getTableControl();
        if(tbl!=null){
	        MenuManager manager = new MenuManager();
	        Menu menu = manager.createContextMenu(tbl);
	        tbl.setMenu(menu);
	        registerContextMenu(manager, jobsList.getViewer());
        }
        jobsList.getViewer().addDropSupport(
                DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE
                        | DND.DROP_DEFAULT,
                new Transfer[] { ResourceTransfer.getInstance(),
                        FileTransfer.getInstance(),
                        LocalSelectionTransfer.getTransfer(), },
                new JobsDropAdapter(this));
        jobsList.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (event.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection selection = (IStructuredSelection) event
                            .getSelection();
                    if (selection.getFirstElement() instanceof IP4Job) {
                        EditJobAction edit = new EditJobAction();
                        edit.selectionChanged(null, new StructuredSelection(
                                selection.getFirstElement()));
                        edit.doubleClick(null);
                    }
                }
            }
        });

        showNoConnection();

        addProjectListeners();

        // Listen for job view preference changes
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        store.addPropertyChangeListener(this);
    }

    /**
     * Capture changes to job preferences
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (property == IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS) {
            jobsList.refresh();
        }
    }

    private void handleRefresh(IP4Job[] jobs, TableViewer viewer) {
        if (jobsList != null) {
            for (IP4Job job : jobs) {
                if (p4Connection.equals(job.getConnection())) {
                    viewer.update(job, null);
                    jobsList.refreshDetailsPanel(job);
                }
            }
        }
    }

    private void handleCreate(IP4Job[] jobs) {
        if (jobsList != null) {
            for (IP4Job job : jobs) {
                if (p4Connection.equals(job.getConnection())) {
                    jobsList.addJob(job);
                }
            }
        }
    }

    /**
     * Set the input to the viewer
     * 
     * @param con
     */
    @Override
    protected void setViewerInput(IP4Connection con) {
        if (connectionOK(con)) {
            // This is a check to test that the connection is still online
            // con.refreshParams();
            addJobAction.setEnabled(true);
            refreshAction.setEnabled(true);
            columnsAction.setEnabled(true);
            jobsList.clearFilters();
            jobsList.createJobTable(con);
            showDisplayArea();
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
        addJobAction.setEnabled(false);
        refreshAction.setEnabled(false);
        columnsAction.setEnabled(false);
    }

    /**
     * Create toolbar and context menus
     */
    private void createMenus() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        addJobAction = new Action(Messages.JobView_CreateNewJob,
                Action.AS_PUSH_BUTTON) {

            @Override
            public void run() {
                NewJobAction newJob = new NewJobAction();
                newJob.selectionChanged(null, new StructuredSelection(
                        p4Connection));
                newJob.run(null);
            };
        };
        addJobAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_ADD));

        refreshAction = new Action(
                Messages.JobView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                jobsList.refresh();
            }
        };
        refreshAction.setToolTipText(Messages.JobView_RefreshJobs);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(refreshAction, IHelpContextIds.JOBS_REFRESH);

        columnsAction = new Action(
                Messages.JobView_Columns,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_JOB_COLUMNS_ENABLED)) {

            @Override
            public void run() {
                setupColumns();
            }
        };
        columnsAction.setToolTipText(Messages.JobView_SetJobViewColumns);
        columnsAction
                .setDisabledImageDescriptor(plugin
                        .getImageDescriptor(IPerforceUIConstants.IMG_JOB_COLUMNS_DISABLED));
        columnsAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_JOB_COLUMNS));
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(columnsAction, IHelpContextIds.JOBS_COLUMNS);

        openPrefs = new Action(Messages.JobView_OpenJobsPreferences,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(JobsPreferencesDialog.ID);
            }
        };

        showDetailsAction = new Action(Messages.JobView_ShowJobDetails) {

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
                            jobsList.updateSash(displayDetails);
                        }
                    }
                });

        // MenuManager manager = new MenuManager();
        // Menu menu = manager.createContextMenu(jobsList.getTableControl());
        // manager.addMenuListener(new IMenuListener() {
        // public void menuAboutToShow(IMenuManager manager) {
        // manager.add(new Separator(
        // IWorkbenchActionConstants.MB_ADDITIONS));
        // }
        // });
        // manager.setRemoveAllWhenShown(true);
        // getTableControl().setMenu(menu);
        // getSite().registerContextMenu(manager, jobsList.getViewer());

        // Create the local tool bar
        IActionBars bars = getActionBars();
        if(bars!=null){
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(addJobAction);
            tbm.add(refreshAction);
            tbm.add(columnsAction);
            tbm.add(openPrefs);
            tbm.update(false);
    
            // Create the pulldown menu
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
        }
    }

    public Table getTableControl() {
        return jobsList != null ? jobsList.getTableControl() : null;
    }

    public TableViewer getTableViewer() {
        return jobsList != null ? jobsList.getViewer() : null;
    }

    /**
     * Allow user to configure which job columns get displayed
     */
    private void setupColumns() {
        JobColumnsDialog dlg = new JobColumnsDialog(getView().getShell(),
                jobsList);
        if (dlg.open() == Window.OK) {
            setViewerInput(p4Connection);
        }
    }

    /**
     * Sets the folder/file path
     * 
     * @param path
     */
    public void setPath(String path) {
        if (jobsList != null) {
            jobsList.setPath(path);
        }
    }

    public JobsDialog getJobsDialog() {
        return this.jobsList;
    }

    public boolean isLoading() {
        return this.jobsList != null && this.jobsList.isLoading();
    }

    public void refreshRetrieveCount() {
        if (this.jobsList != null) {
            this.jobsList.refreshRetrieveCount();
        }
    }

    public void showMore() {
        if (this.jobsList != null) {
            this.jobsList.showMore();
        }
    }



}
