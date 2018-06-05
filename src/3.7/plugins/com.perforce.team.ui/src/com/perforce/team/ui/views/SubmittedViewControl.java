package com.perforce.team.ui.views;

import java.util.ArrayList;
import java.util.List;

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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.OpenChangelistDialog;
import com.perforce.team.ui.dialogs.ChangesPreferencesDialog;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.jobs.JobsWidget;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.submitted.ISubmittedChangelistListener;
import com.perforce.team.ui.submitted.OpenEditorAction;
import com.perforce.team.ui.submitted.SubmittedChangelistTable;


public class SubmittedViewControl extends PerforceFilterViewControl implements
    IPropertyChangeListener, IDoubleClickListener {

    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.submitted.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.submitted.HIDE_FILTERS"; //$NON-NLS-1$

    private SubmittedChangelistTable submittedTable = null;

    // Action to refresh view
    private Action refreshAction;

    // Action to switch showing details panel on/off
    private Action showDetailsAction;

    private Action linkWithDepotView;
    private Action openPrefs;

    // Job actions
    private Action openJobAction;

    // Changelist actions
    private Action openChangelistAction;

    // Open changelist by prompting to enter id
    private Action openByIdAction;

    private boolean link = false;

    private IP4Resource p4Resource = null;

    private ISubmittedChangelistListener listener = new ISubmittedChangelistListener() {

        public void changelistsLoaded(IP4SubmittedChangelist[] lists) {
            if (refreshAction != null) {
                refreshAction.setEnabled(true);
            }
            if (openByIdAction != null) {
                openByIdAction.setEnabled(true);
            }
        }
    };

    private IP4Listener resourceListener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
            if (event.getType() == EventType.FIXED
                    || event.getType() == EventType.UNFIXED) {
                final IP4SubmittedChangelist[] submitted = getAffectedLists(event
                        .getJobs());
                if (submitted.length > 0) {
                    UIJob job = new UIJob(
                            Messages.SubmittedView_UpdateSubmittedChangelistsView) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            Control control = submittedTable.getControl();
                            if (control != null && !control.isDisposed()) {
                                handleUpdate(submitted);
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule();
                }
            }
        }
		public String getName() {
			return SubmittedViewControl.this.getClass().getSimpleName();
		}
    };

    private IP4SubmittedChangelist[] getAffectedLists(IP4Job[] jobs) {
        List<IP4SubmittedChangelist> connectionChanges = new ArrayList<IP4SubmittedChangelist>();
        if (p4Resource != null) {
            IP4Connection connection = p4Resource.getConnection();
            for (IP4Job job : jobs) {
                if (connection.equals(job.getConnection())) {
                    IP4Resource parent = job.getParent();
                    if (parent instanceof IP4SubmittedChangelist) {
                        if (!connectionChanges.contains(parent)) {
                            connectionChanges
                                    .add((IP4SubmittedChangelist) parent);
                        }
                    }
                }
            }
        }
        return connectionChanges
                .toArray(new IP4SubmittedChangelist[connectionChanges.size()]);
    }

    private void handleUpdate(IP4SubmittedChangelist[] lists) {
        for (IP4SubmittedChangelist list : lists) {
            submittedTable.refresh(list);
        }
    }
    
    /**
     * Is this view's main control not disposed
     * 
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return submittedTable != null && submittedTable.okToUse();
    }
    
    public SubmittedViewControl(IPerforceView view) {
        super(view);
    }

    public void setFocus() {
        Tree control = this.submittedTable.getTree();
        if (control != null) {
            control.setFocus();
        }
    }

    public void dispose() {
        P4Workspace.getWorkspace().removeListener(resourceListener);

        if (submittedTable != null) {
            submittedTable.dispose();
        }

        removeProjectListeners();

        IPreferenceStore store = getPreferenceStore();
        store.removePropertyChangeListener(this);
    }

    public void refresh() {
        // TODO Auto-generated method stub

    }


    @Override
    protected String getFilterPreference() {
        return HIDE_FILTERS;
    }

    @Override
    protected String getSelectedName() {
        return Messages.SubmittedView_SubmittedChangelists;
    }

    @Override
    protected void createControl(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem()
        .setHelp(parent, IHelpContextIds.SUBMITTED_VIEW);

        this.submittedTable = new SubmittedChangelistTable(
                PerforceProjectView.getItems(SubmittedChangelistTable.FILE_FOLDER_HISTORY),
                PerforceProjectView.getItems(SubmittedChangelistTable.USER_HISTORY),
                PerforceProjectView.getItems(SubmittedChangelistTable.WORKSPACE_HISTORY), true);
        setFilterViewer(this.submittedTable);
        this.submittedTable.createPartControl(parent, listener);
        this.submittedTable.getViewer().addDoubleClickListener(this);
        this.submittedTable.getViewer().addDropSupport(
                DND.DROP_COPY | DND.DROP_LINK | DND.DROP_MOVE
                        | DND.DROP_DEFAULT,
                new Transfer[] { ResourceTransfer.getInstance(),
                        FileTransfer.getInstance(),
                        LocalSelectionTransfer.getTransfer(), },
                new SubmittedDropAdapter(this));
        
        createMenus();
        
        showNoConnection();
        
        addProjectListeners();
        
        // Listen for submitted view preference changes
        IPreferenceStore store = getPreferenceStore();
        store.addPropertyChangeListener(this);
        P4Workspace.getWorkspace().addListener(resourceListener);

    }

    private Object getSingleObjectSelection() {
        Object selected = null;
        IStructuredSelection selection = (IStructuredSelection) this.submittedTable
                .getViewer().getSelection();
        if (selection.size() == 1) {
            selected = selection.getFirstElement();
        }
        return selected;
    }

    private void createJobActions() {
        openJobAction = new Action(Messages.SubmittedView_EditJob) {

            @Override
            public void run() {
                Object selected = getSingleObjectSelection();
                if (selected instanceof IP4Job) {
                    EditJobAction action = new EditJobAction();
                    action.selectionChanged(null, new StructuredSelection(
                            selected));
                    action.doubleClick(null);
                }
            }
        };
    }

    private void createSubmittedActions() {
        openChangelistAction = new Action(Messages.SubmittedView_ViewChangelist) {

            @Override
            public void run() {
                Object selected = getSingleObjectSelection();
                if (selected instanceof IP4SubmittedChangelist) {
                    ViewChangelistAction action = new ViewChangelistAction();
                    action.selectionChanged(null, new StructuredSelection(
                            selected));
                    action.run(null);
                }
            }
        };
        openByIdAction = new Action(
                Messages.SubmittedView_OpenChangelist,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_SUBMITTED_EDITOR)) {

            @Override
            public void run() {
                if (p4Connection == null) {
                    return;
                }
                OpenChangelistDialog dialog = new OpenChangelistDialog(
                        displayArea.getShell(),
                        Messages.SubmittedView_OpenSubmittedChangelist);
                if (OpenChangelistDialog.OK == dialog.open()) {
                    int id = dialog.getId();
                    if (id > IChangelist.DEFAULT) {
                        ViewChangelistAction action = new ViewChangelistAction();
                        action.view(id, p4Connection);
                    }
                }
            }
        };
    }

    /**
     * Create the different menus
     */
    private void createMenus() {
        createJobActions();
        createSubmittedActions();

        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
        refreshAction = new Action(
                Messages.SubmittedView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                if (submittedTable != null) {
                    submittedTable.refresh();
                }
            }
        };

        refreshAction
                .setToolTipText(Messages.SubmittedView_RefreshSubmittedChangelists);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(refreshAction, IHelpContextIds.SUBMITTED_REFRESH);

        link = getPreferenceStore().getBoolean(
                IPreferenceConstants.LINK_SUBMITTED);
        linkWithDepotView = new Action(
                Messages.SubmittedView_LinkWithConnectionsView,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                link = linkWithDepotView.isChecked();
                getPreferenceStore().setValue(
                        IPreferenceConstants.LINK_SUBMITTED, link);
            }

        };
        linkWithDepotView.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_LINK));
        linkWithDepotView.setChecked(link);

        // collapseAction = new Action("Collapse All") {
        // public void run() {
        // viewer.collapseAll();
        // }
        // };
        // collapseAction.setToolTipText("Collapse All");
        // collapseAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
        // .getImageDescriptor(IPerforceUIConstants.IMG_COLLAPSE));

        openPrefs = new Action(
                Messages.SubmittedView_OpenChangelistPreferences,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(ChangesPreferencesDialog.ID);
            }
        };

        showDetailsAction = new Action(
                Messages.SubmittedView_ShowChangelistDetails) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };

        showDetailsAction.setChecked(plugin.getPreferenceStore().getBoolean(
                DISPLAY_DETAILS));
        showDetailsAction
                .addPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        getPreferenceStore().setValue(DISPLAY_DETAILS,
                                showDetailsAction.isChecked());
                        submittedTable.showDisplayDetails(showDetailsAction
                                .isChecked());
                    }
                });
        this.submittedTable.showDisplayDetails(showDetailsAction.isChecked());

        TreeViewer viewer = this.submittedTable.getViewer();
        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(viewer.getControl());
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(refreshAction);
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        manager.setRemoveAllWhenShown(true);
        viewer.getControl().setMenu(menu);
        registerContextMenu(manager, viewer);

        JobsWidget jobsWidget = this.submittedTable.getChangelistWidget()
                .getJobsWidget();
        if (jobsWidget != null) {
            TreeViewer jobViewer = jobsWidget.getViewer();
            MenuManager jobManager = new MenuManager();
            Menu jobMenu = jobManager.createContextMenu(jobViewer.getControl());
            jobViewer.getControl().setMenu(jobMenu);
            registerContextMenu(jobManager, jobViewer);
        }

        TreeViewer fileViewer = this.submittedTable.getChangelistWidget()
                .getFilesWidget().getViewer();
        MenuManager fileManager = new MenuManager();
        Menu fileMenu = fileManager.createContextMenu(fileViewer.getControl());
        fileViewer.getControl().setMenu(fileMenu);
        registerContextMenu(fileManager, fileViewer);

        // Create the local tool bar
        IActionBars bars = getActionBars();
        if(bars!=null){
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(refreshAction);
            tbm.add(openByIdAction);
            tbm.add(linkWithDepotView);
            // tbm.add(collapseAction);
            tbm.add(openPrefs);
            tbm.update(false);
    
            // Create the pulldown menu
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#setViewerInput(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    protected void setViewerInput(final IP4Connection connection) {
        if (connectionOK(connection)) {
            p4Resource = connection;
            // Set parent connection so changeConnection when called directly
            // will know the current connection
            p4Connection = connection;
            updateConnectionLabel();
            updateChangelists();
        } else {
            showNoConnection();
        }
    }

    private void updateChangelists() {
        this.submittedTable.setResource(this.p4Resource);
        this.submittedTable.clearFilters();
        showDisplayArea();
        this.submittedTable.updateChangelists();
    }

    /**
     * Sets the UI to reflect an invalid or no perforce collection selected
     */
    @Override
    protected void showNoConnection() {
        super.showNoConnection();
        refreshAction.setEnabled(false);
        openByIdAction.setEnabled(false);
        this.submittedTable.getViewer().setInput(null);
        this.p4Resource = null;
        this.submittedTable.setResource(this.p4Resource);
        this.submittedTable.clearDetailsPanel();
    }

    /**
     * Capture changes to submitted view preferences
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (property == IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES) {
            if (this.submittedTable != null) {
                this.submittedTable.updateMoreLink();
            }
        }
    }

    public TreeViewer getViewer() {
        if (this.submittedTable != null) {
            return this.submittedTable.getViewer();
        }
        return null;
    }

    public String getChangeDetails() {
        return this.submittedTable.getChangeDetails();
    }

    public String getDateDetails() {
        return this.submittedTable.getDateDetails();
    }

    public String getClientDetails() {
        return this.submittedTable.getClientDetails();
    }

    public String getUserDetail() {
        return this.submittedTable.getUserDetail();
    }

    public String getDescriptionDetail() {
        return this.submittedTable.getDescriptionDetail();
    }

    public void showChangelists(IP4Resource resource) {
        if (!okToUse()) {
            return;
        }
        if (resource != null && resource.getConnection() != null
                && !resource.getConnection().isOffline()) {
            p4Resource = resource;
            p4Connection = resource.getConnection();
            this.submittedTable.setResource(p4Resource);
            updateConnectionLabel();
            showDisplayArea();
            this.submittedTable.showChangelists(p4Resource);
        } else {
            p4Resource = null;
            this.submittedTable.setResource(p4Resource);
            this.submittedTable.showChangelists(null);
            showNoConnection();
        }
    }

    public void scheduleShowChangelists(final IP4Resource resource) {
        UIJob job = new UIJob(
                Messages.SubmittedView_UpdatingSubmittedChangelists) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                showChangelists(resource);
                return Status.OK_STATUS;
            }
        };
        job.setRule(RULE);
        job.schedule();
        
    }

    public IP4SubmittedChangelist[] getChangelists() {
        if (this.submittedTable != null) {
            return this.submittedTable.getChangelists();
        }
        return null;
    }

    @Override
    public void selectionChanged(IWorkbenchPart part, final ISelection selection) {
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                if (selection instanceof IStructuredSelection) {
                    Object original = ((IStructuredSelection) selection)
                            .getFirstElement();
                    Object element = original;
                    if (!(element instanceof IP4Resource)) {
                        element = getP4Resource(element);
                    }
                    if (p4Resource != element && element instanceof IP4Resource) {
                        if (link && original instanceof IP4Resource) {
                            final IP4Resource resourceElement = (IP4Resource) element;
                            PerforceUIPlugin.syncExec(new Runnable() {

                                public void run() {
                                    showChangelists(resourceElement);
                                }
                            });
                        } else {
                            final IP4Connection newConnection = ((IP4Resource) element)
                                    .getConnection();
                            if (p4Resource != null) {
                                IP4Connection current = p4Resource
                                        .getConnection();
                                if (newConnection != null
                                        && !newConnection.equals(current)) {
                                    PerforceUIPlugin.syncExec(new Runnable() {

                                        public void run() {
                                            setViewerInput(newConnection);
                                        }
                                    });
                                }
                            } else {
                                PerforceUIPlugin.syncExec(new Runnable() {

                                    public void run() {
                                        setViewerInput(newConnection);
                                    }
                                });
                            }
                        }
                    }
                }
            }

        }, RULE);
    }

    public void showDisplayDetails(boolean show) {
        this.submittedTable.showDisplayDetails(show);
    }

    public boolean isLoading() {
        return this.submittedTable != null && this.submittedTable.isLoading();
    }

    public void showMore() {
        if (submittedTable != null) {
            submittedTable.showMore();
        }
    }

    public SubmittedChangelistTable getChangelistTable() {
        return this.submittedTable;
    }
    
    public void doubleClick(DoubleClickEvent event) {
        Object selected = getSingleObjectSelection();
        if (selected instanceof IP4Job) {
            openJobAction.run();
        } else if (selected instanceof IP4SubmittedFile) {
            OpenEditorAction open = new OpenEditorAction();
            open.selectionChanged(null,
                    new StructuredSelection(selected));
            open.run(null);
        } else if (selected instanceof IP4SubmittedChangelist) {
            openChangelistAction.run();
        }
    }

    public void enableDoubleClick(boolean enable) {
        if(enable){
            this.submittedTable.getViewer().addDoubleClickListener(this);            
        }else{
            this.submittedTable.getViewer().removeDoubleClickListener(this);
        }
    }

}
