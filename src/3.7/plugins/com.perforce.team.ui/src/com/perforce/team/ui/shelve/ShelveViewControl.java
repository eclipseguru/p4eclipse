package com.perforce.team.ui.shelve;

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
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.OpenChangelistDialog;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PendingDragAdapter;
import com.perforce.team.ui.views.PerforceFilterViewControl;
import com.perforce.team.ui.views.PerforceProjectView;


public class ShelveViewControl extends PerforceFilterViewControl implements IPropertyChangeListener{

    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.shelve.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.shelve.HIDE_FILTERS"; //$NON-NLS-1$

    private ShelveTable table;

    private IP4Listener resourceListener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
            if (event.getType() == EventType.DELETE_SHELVE
                    || event.getType() == EventType.SUBMIT_SHELVEDCHANGELIST
                    || event.getType() == EventType.CREATE_SHELVE
                    || event.getType() == EventType.UPDATE_SHELVE) {
            	
                List<IP4ShelvedChangelist> list = new ArrayList<IP4ShelvedChangelist>();

                if(event.getType()==EventType.SUBMIT_SHELVEDCHANGELIST){
	            	for(IP4Resource r:event.getResources()){
	            		if(r instanceof IP4ShelvedChangelist){
	            			list.add((IP4ShelvedChangelist) r);
	            		}
	            	}
                }else{
                	list=getAffectedLists(event.getPending());
                }
                
                final IP4ShelvedChangelist[] shelved=list.toArray(new IP4ShelvedChangelist[list.size()]);

                if (shelved.length > 0) {
                    UIJob job = new UIJob(
                            Messages.ShelveView_UpdateShelvedChangesView) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            Control control = table.getControl();
                            if (control != null && !control.isDisposed()) {

                                switch (event.getType()) {
                                case DELETE_SHELVE:
                                case SUBMIT_SHELVEDCHANGELIST:
                                    handleDelete(shelved);
                                    break;
                                case CREATE_SHELVE:
                                    handleCreate(shelved);
                                    break;
                                case UPDATE_SHELVE:
                                    handleUpdate(shelved);
                                    break;
                                default:
                                    break;
                                }
                            }
                            return Status.OK_STATUS;
                        }
                    };
                    job.schedule();
                }
            }
        }
		public String getName() {
			return ShelveViewControl.this.getClass().getSimpleName();
		}
    };

    private Action showDetailsAction;
    private Action refreshAction;
    private Action openAction;
    private Action prefsAction;
    private Action openByIdAction;

    public ShelveViewControl(IPerforceView view) {
        super(view);
    }

    private List<IP4ShelvedChangelist> getAffectedLists(
            IP4PendingChangelist[] pendings) {
        List<IP4ShelvedChangelist> shelved = new ArrayList<IP4ShelvedChangelist>();
        for (IP4PendingChangelist list : pendings) {
            IChangelist cList = list.getChangelist();
            IP4Connection connection = list.getConnection();
            if (cList != null && connection != null) {
                shelved.add(new P4ShelvedChangelist(connection, cList, list
                        .isReadOnly()));
            }
        }
        return shelved;
    }

    private void handleCreate(IP4ShelvedChangelist[] lists) {
        for (IP4ShelvedChangelist list : lists) {
            table.addShelvedChangelist(list);
        }
    }

    private void handleDelete(IP4ShelvedChangelist[] lists) {
        table.removeShelvedChangelist(lists);
    }

    private void handleUpdate(IP4ShelvedChangelist[] lists) {
        for (IP4ShelvedChangelist list : lists) {
            table.refreshShelvedChangelist(list);
        }
    }

    private void createActions() {
        final PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        showDetailsAction = new Action(
                Messages.ShelveView_ShowChangelistDetails) {

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
                        plugin.getPreferenceStore().setValue(DISPLAY_DETAILS,
                                showDetailsAction.isChecked());
                        table.showDisplayDetails(showDetailsAction.isChecked());
                    }
                });
        this.table.showDisplayDetails(showDetailsAction.isChecked());

        refreshAction = new Action(Messages.ShelveView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH)) {

            @Override
            public void run() {
                if (table != null) {
                    table.refresh();
                }
            }
        };
        openAction = new Action(Messages.ShelveView_OpenInEditor) {

            @Override
            public void run() {
                IP4ShelveFile file = getSelectedShelveFile();
                if (file != null) {
                    OpenEditorAction open = new OpenEditorAction();
                    open.selectionChanged(null, new StructuredSelection(file));
                    open.run(null);
                }
            }
        };
        prefsAction = new Action(Messages.ShelveView_OpenPreferences,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(ShelvePreferencePage.ID);
            }
        };
        prefsAction.setToolTipText(Messages.ShelveView_OpenPreferencePage);

        openByIdAction = new Action(Messages.ShelveView_OpenChangelist,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_SHELVED_EDITOR)) {

            @Override
            public void run() {
                if (p4Connection == null) {
                    return;
                }
                OpenChangelistDialog dialog = new OpenChangelistDialog(
                        displayArea.getShell(),
                        Messages.ShelveView_OpenShelvedChangelist);
                if (OpenChangelistDialog.OK == dialog.open()) {
                    int id = dialog.getId();
                    if (id > IChangelist.DEFAULT) {
                        ViewChangelistAction action = new ViewChangelistAction();
                        action.view(id, p4Connection, Type.SHELVED);
                    }
                }
            }
        };

        TreeViewer viewer = this.table.getViewer();
        final MenuManager manager = new MenuManager();
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

        IActionBars bars = getActionBars();
        if(bars!=null){
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(refreshAction);
            tbm.add(openByIdAction);
            tbm.add(prefsAction);
            tbm.update(false);
    
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
        }
    }

    public void setFocus() {
        table.getControl().setFocus();
    }

    public void dispose() {
        removeProjectListeners();
        P4Workspace.getWorkspace().removeListener(resourceListener);
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .removePropertyChangeListener(this);
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
        return Messages.ShelveView_ShelvedChangelists;
    }

    @Override
    protected void createControl(Composite parent) {
        table = new ShelveTable(PerforceProjectView.getItems(ShelveTable.FILE_FOLDER_HISTORY),
                PerforceProjectView.getItems(ShelveTable.USER_HISTORY),
                PerforceProjectView.getItems(ShelveTable.WORKSPACE_HISTORY));
        setFilterViewer(table);
        table.createPartControl(parent);
        table.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                Object resource = getSingleObjectSelection();
                if (resource instanceof IP4ShelveFile) {
                    openAction.run();
                } else if (resource instanceof IP4ShelvedChangelist) {
                    ViewChangelistAction view = new ViewChangelistAction();
                    view.selectionChanged(null, new StructuredSelection(
                            resource));
                    view.run(null);
                }
            }
        });
        table.getViewer().addDragSupport(
                DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new PendingDragAdapter(table.getViewer()));

        createActions();

        addProjectListeners();
        P4Workspace.getWorkspace().addListener(resourceListener);

        // Listen for shelved view preference changes
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .addPropertyChangeListener(this);

    }

    private Object getSingleObjectSelection() {
        Object selected = null;
        IStructuredSelection selection = (IStructuredSelection) this.table
                .getViewer().getSelection();
        if (selection.size() == 1) {
            selected = selection.getFirstElement();
        }
        return selected;
    }

    private IP4ShelveFile getSelectedShelveFile() {
        Object file = getSingleObjectSelection();
        return file instanceof IP4ShelveFile ? (IP4ShelveFile) file : null;
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#setViewerInput(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    protected void setViewerInput(IP4Connection connection) {
        if (connectionOK(connection)) {
            table.showChangelists(connection);
            showDisplayArea();
        } else {
            showNoConnection();
        }
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (property == IPreferenceConstants.NUM_SHELVES_RETRIEVE) {
            if (this.table != null) {
                this.table.updateMoreLink();
            }
        }
    }

    public boolean isLoading() {
        return this.table != null && this.table.isLoading();
    }

    public ShelveTable getTabel() {
        return this.table;
    }


}
