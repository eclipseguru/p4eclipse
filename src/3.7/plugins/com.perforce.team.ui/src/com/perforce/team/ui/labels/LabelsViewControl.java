package com.perforce.team.ui.labels;

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

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.PerforceFilterViewControl;


public class LabelsViewControl extends PerforceFilterViewControl {

    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.labels.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.labels.HIDE_FILTERS"; //$NON-NLS-1$


    // Action to refresh view
    private Action refreshAction;

    // Action to switch showing details panel on/off
    private Action showDetailsAction;

    private Action openPrefs;

    private Action syncLabelAction;

    // True if showing label details
    private boolean displayDetails;

    private LabelsViewer labelsViewer = null;

    public LabelsViewControl(IPerforceView view) {
        super(view);

        this.labelsViewer = new LabelsViewer();
        setFilterViewer(this.labelsViewer);

    }
    
    /**
     * Gets the table control used by the labels view
     * 
     * @return - table control
     */
    public Table getTableControl() {
        return labelsViewer.getTableControl();
    }

    /**
     * Gets the table viewer
     * 
     * @return - table viewer
     */
    public TableViewer getTableViewer() {
        return labelsViewer.getViewer();
    }


    public void setFocus() {
    	Table control = labelsViewer.getTableControl();
    	if (control != null && !control.isDisposed()) {
    		control.setFocus();
    	}
    }

    public void dispose() {
        removeProjectListeners();
    }

    public void refresh() {
    	this.labelsViewer.refresh();
    }

    @Override
    protected String getFilterPreference() {
        return HIDE_FILTERS;
    }

    @Override
    protected String getSelectedName() {
        return Messages.LabelsView_Labels;
    }

    @Override
    protected void createControl(Composite parent) {
        createMenus();
        showNoConnection();
        addProjectListeners();
    }
    

    /**
     * Sets the folder/file path
     * 
     * @param path
     */
    public void setPath(String path) {
    	labelsViewer.setPath(path);
    }
    

    private void hookContextMenu() {
        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(labelsViewer.getTableControl());
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(syncLabelAction);
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
            }
        });
        manager.setRemoveAllWhenShown(true);
        if(labelsViewer.getTableControl()!=null)
        	labelsViewer.getTableControl().setMenu(menu);
        registerContextMenu(manager, labelsViewer.getViewer());
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
            refreshAction.setEnabled(true);
            labelsViewer.createControl(this.displayArea, con, false,
                    displayDetails);
            showDisplayArea();
            hookContextMenu();
            labelsViewer.getViewer().addDoubleClickListener(
                    new IDoubleClickListener() {

                        public void doubleClick(DoubleClickEvent event) {
                            if (event.getSelection() instanceof IStructuredSelection) {
                                IStructuredSelection selection = (IStructuredSelection) event
                                        .getSelection();
                                if (selection.getFirstElement() instanceof IP4Label) {
                                    // TODO open edit label dialog
                                }
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
    

    /**
     * Create toolbar and context menus
     */
    private void createMenus() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        refreshAction = new Action(
                Messages.LabelsView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                labelsViewer.refresh();
            }
        };
        refreshAction.setToolTipText(Messages.LabelsView_RefreshLabels);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));

        openPrefs = new Action(Messages.LabelsView_OpenLabelPrefs,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(LabelsPreferencePage.ID);
            }
        };

        showDetailsAction = new Action(Messages.LabelsView_ShowLabelDetails) {

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

        // Create the local tool bar
        IActionBars bars = getActionBars();
        if(bars!=null){
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(refreshAction);
            tbm.add(openPrefs);
            tbm.update(false);

            syncLabelAction = new Action(Messages.LabelsView_GetRevision,
                    IAction.AS_PUSH_BUTTON) {
    
                @Override
                public void run() {
                    SyncRevisionAction sync = new SyncRevisionAction();
                    sync.selectionChanged(null, new StructuredSelection(
                            p4Connection));
                    IP4Label label = getLabelSelection();
                    if (label != null) {
                        sync.runWithDialog(label.getName(), true);
                    }
                }
            };
    
            // Create the pulldown menu
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
        }
    }

    public IP4Label getLabelSelection() {
        IP4Label selected = null;
        if(getTableViewer()!=null){
            IStructuredSelection selection = (IStructuredSelection) getTableViewer().getSelection();
            if (selection.size() == 1 && selection.getFirstElement() instanceof IP4Label) {
                selected = (IP4Label) selection.getFirstElement();
            }
        }
        return selected;
    }

    public boolean isLoading() {
        return this.labelsViewer != null && this.labelsViewer.isLoading();
    }

    public void refreshRetrieveCount() {
        if (this.labelsViewer != null) {
            this.labelsViewer.refreshRetrieveCount();
        }
    }

    public void showMore() {
        if (this.labelsViewer != null) {
            this.labelsViewer.showMore();
        }
    }

    public void showDetails(boolean show) {
        if (this.labelsViewer != null && showingConnection()) {
            this.labelsViewer.updateSash(show);
        }
    }

    public void showDisplayDetails(boolean show) {
        if (this.labelsViewer != null)
            this.labelsViewer.updateSash(show);
    }

    public LabelWidget getLabelDetails() {
        if (this.labelsViewer != null) {
            return this.labelsViewer.getDetails();
        }
        return null;
    }


//    /**
//     * Gets the underlying labels viewer
//     * 
//     * @return - labels viewer
//     */
//    public LabelsViewer getLabelsViewer() {
//        return this.labelsViewer;
//    }


}
