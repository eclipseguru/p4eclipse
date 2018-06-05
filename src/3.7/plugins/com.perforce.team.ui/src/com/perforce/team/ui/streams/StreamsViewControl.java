package com.perforce.team.ui.streams;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.views.IPerforceView;
import com.perforce.team.ui.views.Messages;
import com.perforce.team.ui.views.PerforceFilterViewControl;


public class StreamsViewControl extends PerforceFilterViewControl implements IPartListener2, IPropertyChangeListener{
    //////////// Preference store keys. /////
    
    /**
     * DISPLAY_DETAILS
     */
    public static final String DISPLAY_DETAILS = "com.perforce.team.ui.streams.display_details"; //$NON-NLS-1$

    /**
     * HIDE_FILTERS
     */
    public static final String HIDE_FILTERS = "com.perforce.team.ui.streams.HIDE_FILTERS"; //$NON-NLS-1$

    /**
     * TREE_EXPAND state
     */
    public static final String TREE_EXPAND = "com.perforce.team.ui.streams.expand_tree"; //$NON-NLS-1$ 

    private FilteredStreamsViewer streamsViewer;

    private boolean visible=false;

    protected void fillContextMenu(IMenuManager mgr) {
        
    }
    
    public boolean isShownUnloadedOnly(){
    	return streamsViewer.getFilterModel().isShowUnloadedOnly();
    }
    
    public void setVisible(boolean show){ // Used only for non-view based implementation. This will show streams always.
        this.visible=show;
    }

    public StreamsViewControl(IPerforceView view) {
        super(view);
    }

    public void setFocus() {
        if(streamsViewer!=null)
            streamsViewer.setFocus();
    }

    public void dispose() {
        removeProjectListeners();
        IWorkbenchPartSite site = getView().getSite();
        if(site!=null)
            site.getPage().removePartListener(this);

    }

    public void refresh() {
    	if(streamsViewer!=null)
    		streamsViewer.refreshInput(false);
    }

    @Override
    protected String getSelectedName() {
        return Messages.StreamsView_Streams;
    }

    public void showDisplayDetails(boolean checked) {
        streamsViewer.showDisplayDetails(checked);
    }
    
    public TreeViewer getTreeViewer(){
        return streamsViewer.getTreeViewer();
    }
    
    public IP4Stream getSelectedStream(){
        return streamsViewer.getSelectedStream();
    }

    public void enableAutoUpdate(boolean enable){
        streamsViewer.enableAutoUpdate(enable);
    }

    public void enableDoubleClick(boolean enable){
        
        if(enable)
            streamsViewer.getTreeViewer().addDoubleClickListener(streamsViewer);
        else
            streamsViewer.getTreeViewer().removeDoubleClickListener(streamsViewer);
    }
    
    @Override
    protected void createControl(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem()
        .setHelp(parent, IHelpContextIds.STREAMS_VIEW);
        streamsViewer = new FilteredStreamsViewer(getView().getSite());
        streamsViewer.createPartControl(parent);
        
        setFilterViewer(this.streamsViewer);
        
        addProjectListeners();
        
        createMenus();
        
        IWorkbenchPartSite site = getView().getSite();
        if(site != null)
            site.getPage().addPartListener(this);
        
        // Listen for submitted view preference changes
        IPreferenceStore store = getPreferenceStore();
        store.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (property == IPerforceUIConstants.PREF_RETRIEVE_NUM_STREAMS) {
            if (streamsViewer != null) {
                getView().getShell().getDisplay().asyncExec(new Runnable() {
                    public void run() {
                        streamsViewer.updateMoreLink();
                    }
                });
            }
        }
    }
    
    private void createMenus() {
        final Action showDetailsAction = new Action(
                Messages.StreamsView_ShowStreamDetails) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };

        showDetailsAction.setChecked(getPreferenceStore().getBoolean(
                DISPLAY_DETAILS));
        showDetailsAction
                .addPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        getPreferenceStore().setValue(DISPLAY_DETAILS,
                                showDetailsAction.isChecked());
                        streamsViewer.showDisplayDetails(showDetailsAction
                                .isChecked());
                    }
                });
        this.streamsViewer.showDisplayDetails(showDetailsAction.isChecked());

        IActionBars bars = getActionBars();
        if(bars!=null){
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(new Separator());
            pulldown.add(showDetailsAction);
            createFilterAction(pulldown);
            pulldown.update(false);
            pulldown.add(new Separator());
        }
        
    }

    @Override
    protected void setViewerInput(final IP4Connection connection) {
        p4Connection = connection;

        if(visible){
            if (!updateStreams()) {
                showNoConnection();
            }
        }
    }

    private boolean updateStreams() {
        if (connectionOK(p4Connection)) {
            IP4Connection viewerInput = streamsViewer.getConnection();
            if(viewerInput!=p4Connection )
                streamsViewer.clearFilter();
            
            if(viewerInput!=p4Connection){
                refresh(true,true);
            }
            return true;
        }
        return false;
    }

    @Override
    protected String getFilterPreference() {
        return HIDE_FILTERS;
    }
    
    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    public void refresh(boolean clearCache, boolean retrieve) {
        if(!connectionOK(p4Connection))
            return;
        
        if(clearCache)
            streamsViewer.clearCache(p4Connection);
        updateConnectionLabel();
        showDisplayArea();
        streamsViewer.setConnection(p4Connection);
        streamsViewer.refreshInput(retrieve);
    }

    public void collapseAll() {
        getPreferenceStore().setValue(TREE_EXPAND, false);
        streamsViewer.expand();
    }

    public void expandAll() {
        getPreferenceStore().setValue(TREE_EXPAND, true);
        streamsViewer.expand();
    }
    
    public void partActivated(IWorkbenchPartReference partRef) {
        if(partRef.getId().equals(StreamsView.VIEW_ID)){
            if(!visible)
                updateStreams();
            visible=true;
        }
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    public void partOpened(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
        if(partRef.getId().equals(StreamsView.VIEW_ID))
            visible=false;
    }

    public void partVisible(IWorkbenchPartReference partRef) {
        if(partRef.getId().equals(StreamsView.VIEW_ID)){
            if(!visible)
                updateStreams();
            visible=true;
        }
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

}
