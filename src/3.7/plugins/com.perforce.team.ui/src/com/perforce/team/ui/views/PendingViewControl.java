package com.perforce.team.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
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
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.OpenAction;
import com.perforce.team.ui.changelists.DecoratedChangelistLabelProvider;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.p4java.actions.DiffDepotAction;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.NewChangelistAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.shelve.DiffShelveHeadAction;
import com.perforce.team.ui.shelve.OpenEditorAction;


public class PendingViewControl extends AbstractPerforceViewControl implements IP4Listener{
    public PendingViewControl(IPerforceView view) {
        super(view);
    }

    // The key for storing show other changeslists state
    private static final String SHOW_OTHER_CHANGELISTS = "show_other_changeslists"; //$NON-NLS-1$

    private boolean isLoading = false;

    // The tree viewer
    private TreeViewer viewer;
    private PerforceContentProvider provider;

    // Some actions
    private Action refreshAction;
    private Action refreshListAction;
    private Action activateAction;
    private Action disactivateAction;
    private Action otherAction;
    private Action newChangelistAction;
    private Action openAction;
    private Action revertAction;
    private Action diffAction;
    private Action collapseAction;

    // True if showing show clients changelists
    private boolean showOtherChanges;

    // Memento used to store state information
    private IMemento memento;

    public void setMemento(IMemento memento){
        this.memento = memento;
    }
    public IMemento getMemento(){
        return this.memento;
    }

    /**
     * Save the state of this view
     * 
     * @param memento
     */
    public void saveState(IMemento memento) {
        memento.putInteger(SHOW_OTHER_CHANGELISTS, showOtherChanges ? 1 : 0);
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.PENDING_VIEW);

        viewer = new TreeViewer(parent);
        viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setComparator(ViewUtil.getViewComparator()); 
        viewer.setUseHashlookup(true);
        provider = new PerforceContentProvider(viewer, true) {

            @Override
            protected IP4Resource[] getMembers(IP4Container container) {
                if (container instanceof IP4PendingChangelist) {
                    return ((IP4PendingChangelist) container).getAllMembers();
                }
                return super.getMembers(container);
            }

        };
        viewer.setContentProvider(provider);

        viewer.setLabelProvider(DecoratedChangelistLabelProvider.create());
        addContextMenu();
        initDragDrop(viewer);
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick(event);
            }
        });

        addProjectListeners();
        P4ConnectionManager.getManager().addListener(this);
    }

    /**
     * Shutdown this view
     */
    public void dispose() {
        removeProjectListeners();
        P4ConnectionManager.getManager().removeListener(this);
    }

    /**
     * Refresh this view
     */
    public void refresh() {
        setViewerInput(p4Connection);
    }

    /**
     * Handle what happens when this view gets the focus
     */
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * Add drag and drop support to viewer
     */
    private void initDragDrop(TreeViewer viewer) {
        viewer.addDropSupport(
                DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE,
                new Transfer[] { ResourceTransfer.getInstance(),
                        FileTransfer.getInstance(),
                        LocalSelectionTransfer.getTransfer(), },
                new PendingDropAdapter(viewer));
        viewer.addDragSupport(DND.DROP_COPY | DND.DROP_DEFAULT | DND.DROP_MOVE,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new PendingDragAdapter(viewer));
    }

    /**
     * Handle double click events
     */
    private void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        Object element = selection.getFirstElement();

        // If it's a file then open an editor for it
        if (element instanceof IP4File) {
            OpenAction.openFile((IP4File) element);
        } else if (element instanceof IP4Job) {
            IP4Job job = (IP4Job) element;
            EditJobAction edit = new EditJobAction();
            edit.selectionChanged(null, new StructuredSelection(job));
            edit.doubleClick(null);
        } else if (element instanceof IP4ShelveFile) {
            OpenEditorAction action = new OpenEditorAction();
            action.selectionChanged(null, new StructuredSelection(element));
            action.run(null);
        } else if (viewer.isExpandable(element)) {
            // If it's a folder then open or close it
            viewer.setExpandedState(element, !viewer.getExpandedState(element));
        }
    }

    private IP4PendingChangelist[] getSelectedLists() {
        List<IP4PendingChangelist> lists = new ArrayList<IP4PendingChangelist>();
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        for (Object select : selection.toArray()) {
            if (select instanceof IP4PendingChangelist) {
                lists.add((IP4PendingChangelist) select);
            }
        }
        return lists.toArray(new IP4PendingChangelist[lists.size()]);
    }

    /**
     * Create viewer context menu
     */
    private void addContextMenu() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
        refreshAction = new Action(
                Messages.PendingView_RefreshAllChangelists,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                refresh();
            }
        };

        refreshListAction = new Action(
                Messages.PendingView_RefreshSelectedChangelists) {

            @Override
            public void run() {
                IP4PendingChangelist[] lists = getSelectedLists();
                if (lists.length > 0) {
                    for (IP4PendingChangelist list : lists) {
                        list.markForRefresh();
                        viewer.refresh(list);
                    }
                }
            }

        };

        activateAction = new Action(
                Messages.PendingView_MakeActivePendingChangelist) {

            @Override
            public void run() {
                IP4PendingChangelist[] lists = getSelectedLists();
                if (lists.length == 1) {
                    lists[0].makeActive();
                }
            }

        };

        disactivateAction = new Action(
                Messages.PendingView_ClearAsActivePendingChangelist) {

            @Override
            public void run() {
                IP4PendingChangelist[] lists = getSelectedLists();
                if (lists.length == 1 && lists[0].isActive()) {
                    lists[0].getConnection().setActivePendingChangelist(-1);
                }
            }

        };

        openAction = new Action(Messages.PendingView_Open) {

            @Override
            public void run() {
                Object[] selection = ((IStructuredSelection) viewer
                        .getSelection()).toArray();
                String[] paths = new String[selection.length];
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i] instanceof IP4File) {
                        paths[i] = ((IP4File) selection[i]).getLocalPath();
                    }
                }
                OpenAction.openFiles(paths);
            }
        };

        otherAction = new Action(
                Messages.PendingView_ShowOtherClientsChangelists) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };
        showOtherChanges = false;
        if (memento != null) {
            Integer showOther = memento.getInteger(SHOW_OTHER_CHANGELISTS);
            if (showOther != null && showOther.intValue() == 1) {
                showOtherChanges = true;
            }
        }
        otherAction.setChecked(showOtherChanges);
        otherAction.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (IAction.CHECKED.equals(event.getProperty())) {
                    updateAndRefreshChanges(otherAction.isChecked());
                }
            }
        });

        refreshAction
                .setToolTipText(Messages.PendingView_RefreshPendingChangelists);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(refreshAction, IHelpContextIds.PENDING_REFRESH);

        newChangelistAction = new Action(
                Messages.PendingView_NewPendingChangelist,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_NEW_CHANGELIST_ENABLED)) {

            @Override
            public void run() {
                if (p4Connection != null) {
                    NewChangelistAction action = new NewChangelistAction();
                    action.setCollection(new P4Collection(
                            new IP4Resource[] { p4Connection }));
                    action.runAction();
                }
            }
        };
        newChangelistAction
                .setToolTipText(Messages.PendingView_CreateNewPendingChangelist);
        newChangelistAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_NEW_CHANGELIST));
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(newChangelistAction,
                        IHelpContextIds.PENDING_NEW_CHANGELIST);

        final RevertAction wrappedRevert = new RevertAction();

        revertAction = new Action(Messages.PendingView_RevertFiles,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REVERT)) {

            @Override
            public void run() {
                wrappedRevert.selectionChanged(null, viewer.getSelection());
                wrappedRevert.runAction();
            }
        };

        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                wrappedRevert.selectionChanged(revertAction,
                        event.getSelection());
            }
        });

        diffAction = new Action(Messages.PendingView_DiffFileAgainstDepot,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_DIFF)) {

            @Override
            public void run() {
                // Handle IP4File objects in selection
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                DiffDepotAction action = new DiffDepotAction();
                action.selectionChanged(null, selection);
                action.runAction();

                if (!selection.isEmpty()) {
                    // Handle IP4ShelveFile objects in selection
                    for (Object select : selection.toArray()) {
                        if (select instanceof IP4ShelveFile) {
                            DiffShelveHeadAction diffShelve = new DiffShelveHeadAction();
                            diffShelve.selectionChanged(null,
                                    new StructuredSelection(select));
                            diffShelve.run(null);
                        }
                    }
                }
            }

        };

        collapseAction = new Action(Messages.PendingView_CollapseAll) {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAction.setToolTipText(Messages.PendingView_CollapseAll);
        collapseAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_COLLAPSE));

        MenuManager manager = new MenuManager();
        Tree tree = viewer.getTree();
        Menu menu = manager.createContextMenu(tree);
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator("perforce.opengroup")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group1")); //$NON-NLS-1$
                IP4PendingChangelist[] lists = getSelectedLists();
                if (lists.length > 0) {
                    manager.add(refreshListAction);
                    if (lists.length == 1 && lists[0].isOnClient()) {
                        if (lists[0].isActive()) {
                            manager.add(disactivateAction);
                        } else {
                            manager.add(activateAction);
                        }
                    }
                }
                manager.add(new Separator("perforce.group2")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group3")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group4")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group5")); //$NON-NLS-1$
                manager.add(refreshAction);

                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));

                addPropertiesMenu(manager);
                addOpenMenu(manager);
                addOpenWithMenu(manager);
            }
        });
        manager.setRemoveAllWhenShown(true);
        tree.setMenu(menu);
        registerContextMenu(manager, viewer);
        manager.addMenuListener(MenuFilter.createTeamMainFilter());

        IWorkbenchPartSite site = getView().getSite();
        // Create the local tool bar
        if(site instanceof IViewSite){
            IActionBars bars = ((IViewSite)site).getActionBars();
            IToolBarManager tbm = bars.getToolBarManager();
            tbm.add(refreshAction);
            tbm.add(newChangelistAction);
            tbm.add(revertAction);
            tbm.add(diffAction);
            tbm.add(collapseAction);
            tbm.update(false);
    
            // Create the pulldown menu
            IMenuManager pulldown = bars.getMenuManager();
            pulldown.add(otherAction);
            pulldown.update(false);
        }
    }

    /**
     * Add "Properties" context menu entry
     * 
     * @param menu
     */
    private void addPropertiesMenu(IMenuManager menu) {
        Object[] selection = ((IStructuredSelection) viewer.getSelection())
                .toArray();
        if (selection.length != 1) {
            return;
        }
        if (!(selection[0] instanceof IP4File)) {
            return;
        }
        IP4File file = (IP4File) selection[0];
        IResource resource = PerforceProviderPlugin.getWorkspaceFile(file
                .getLocalPath());
        if (resource != null) {
            menu.add(new Separator());
            menu.add(new PropertyDialogAction(new IShellProvider() {

                public Shell getShell() {
                    return getView().getShell();
                }
            }, viewer));

        }
    }

    /**
     * Add "Open" context menu entry
     * 
     * @param menu
     */
    private void addOpenMenu(IMenuManager menu) {
        Object[] selection = ((IStructuredSelection) viewer.getSelection())
                .toArray();
        // Check selected objects are all files that exist as resources
        if (selection.length > 0) {
            for (int i = 0; i < selection.length; i++) {
                if (!(selection[i] instanceof IP4File)) {
                    return;
                }
                IP4File file = (IP4File) selection[0];
                IResource resource = PerforceProviderPlugin
                        .getWorkspaceFile(file.getLocalPath());
                if (resource == null) {
                    return;
                }
            }
            menu.appendToGroup("perforce.opengroup", openAction); //$NON-NLS-1$
        }
    }

    /**
     * Add "Open With" context menu entry
     */
    private void addOpenWithMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.size() != 1) {
            return;
        }
        Object o = selection.getFirstElement();
        if (!(o instanceof IP4File)) {
            return;
        }
        IP4File file = (IP4File) o;
        IResource resource = PerforceProviderPlugin.getWorkspaceFile(file
                .getLocalPath());
        if (resource == null) {
            return;
        }

        // Create a menu flyout.
        IMenuManager submenu = new MenuManager(Messages.PendingView_OpenWith);
        IWorkbenchPartSite site = getView().getSite();
        if(site != null){
            submenu.add(new OpenWithMenu(site.getPage(), resource));
        }

        // Add the submenu.
        menu.appendToGroup("perforce.opengroup", submenu); //$NON-NLS-1$
    }

    /**
     * Gets the underlying tree viewer showing the pending changelists
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * Show other changes
     * 
     * @param show
     *            - true to show other changes, false to not
     */
    public void showOtherChanges(boolean show) {
        // This will cause a propert change event that will cause a call to
        // updateAndRefreshChanges
        otherAction.setChecked(show);
    }

    private void updateAndRefreshChanges(boolean show) {
        showOtherChanges = show;
        setViewerInput(this.p4Connection);
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#showNoConnection()
     */
    @Override
    protected void showNoConnection() {
        super.showNoConnection();
        viewer.setInput(null);
        setActionEnabled(false);
    }

    /**
     * Sets this view's actions as enable or disabled
     * 
     * @param enabled
     */
    public void setActionEnabled(boolean enabled) {
        newChangelistAction.setEnabled(enabled);
        refreshAction.setEnabled(enabled);
        diffAction.setEnabled(enabled);
        revertAction.setEnabled(enabled);
        otherAction.setEnabled(enabled);
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#setViewerInput(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    protected void setViewerInput(final IP4Connection connection) {
        if (connectionOK(connection)) {
            showDisplayArea();
            isLoading = true;
            setActionEnabled(false);
            viewer.setInput(new PerforceContentProvider.Loading());
            P4Runner.schedule(new P4Runnable() {

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.PendingView_LoadPendingChangelistsFor,
                            connection.getName());
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    if (connection != p4Connection) {
                        return;
                    }
                    final IP4PendingChangelist[] lists =connection.getPendingChangelists(showOtherChanges); 
                    PlatformUI.getWorkbench().getDisplay()
                            .syncExec(new Runnable() {

                                public void run() {
                                    if (PendingViewControl.this.p4Connection == connection) {
                                        if (lists != null && lists.length > 0) {
                                            viewer.setInput(lists);
                                        } else {
                                            showNoConnection();
                                        }
                                        isLoading = false;
                                        setActionEnabled(true);
                                    }
                                }

                            });
                }
            });
        } else {
            showNoConnection();
        }
    }

    /**
     * Is this view's main control not disposed
     * 
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return viewer != null && viewer.getTree() != null
                && !viewer.getTree().isDisposed();
    }

    private void addLists(IP4Resource[] resources) {
    	List<IP4PendingChangelist> newList=new ArrayList<IP4PendingChangelist>();

        IP4Connection connection = p4Connection;
        if (connection != null) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4PendingChangelist
                        && ((IP4PendingChangelist) resource).getId() > 0
                        && connection.equals(resource.getConnection())) {
                	newList.add((IP4PendingChangelist) resource);
                }
            }

            if(newList.size()>0){
            	viewer.setInput(connection.getPendingChangelists(showOtherChanges));
            	for(IP4PendingChangelist list:newList){
            		viewer.setSelection(new StructuredSelection(list));
            		viewer.reveal(list);
            		break;
            	}
            }
        }
        
    }

    private void handleUpdateEvent(Object[] elements) {
        viewer.update(elements, null);
    }

    private void handleJobEvent(IP4Resource[] resources, boolean reveal) {
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        if (resources.length > 0) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4Job) {
                    IP4Container parent = resource.getParent();
                    if (parent instanceof IP4PendingChangelist
                            && !processed.contains(parent)) {
                        viewer.refresh(parent);
                        processed.add((IP4PendingChangelist) parent);
                        if (reveal) {
                            viewer.reveal(resource);
                        }
                    }
                }
            }
        }
    }

    private void handleRemoveEvent(Object[] elements) {
    	List<IP4PendingChangelist> removedList=new ArrayList<IP4PendingChangelist>();
    	List<Object> removedObjs=new ArrayList<Object>();
    	for(Object obj : elements){
          if (obj instanceof IP4PendingChangelist) {
        	  removedList.add((IP4PendingChangelist) obj);
          }else{
        	  removedObjs.add(obj);
          }
      	}
    	if(removedList.size()>0){ // root level elements for virtual tree viewer
        	viewer.setInput(getConnection().getPendingChangelists(showOtherChanges));
    	}
    	
    	if(removedObjs.size()>0)
    		viewer.remove(removedObjs.toArray());
    }

    private void handleSubmitChangelistEvent(IP4PendingChangelist[] lists) {
        if (lists.length > 0) {
        	
//            viewer.remove(lists);
        	handleRemoveEvent(Arrays.asList(lists).toArray(new Object[0]));
        	
            List<IP4Changelist> processed = new ArrayList<IP4Changelist>();
            for (IP4PendingChangelist list : lists) {
                IP4Connection connection = list.getConnection();
                if (connection != null) {
                    IP4Changelist defaultChangelist = connection
                            .getPendingChangelist(0);
                    if (defaultChangelist != null
                            && !processed.contains(defaultChangelist)) {
                        defaultChangelist.markForRefresh();
                        viewer.refresh(defaultChangelist);
                        processed.add(defaultChangelist);
                    }
                }
            }
        }
    }

    private void handleOpened(IP4File[] files) {
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        for (IP4File file : files) {
            IP4PendingChangelist list = file.getChangelist();
            if (list != null && !processed.contains(list)) {
                viewer.refresh(list);
                viewer.reveal(file);
                processed.add((IP4PendingChangelist) list);
            }
        }
    }

    private void handleRefresh(IP4Resource[] resources) {
        if (resources.length == 0) {
            return;
        }
        List<IP4PendingChangelist> processed = new ArrayList<IP4PendingChangelist>();
        List<IP4File> remove = new ArrayList<IP4File>();
        for (IP4Resource resource : resources) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                if (!file.isOpened()) {
                    remove.add(file);
                } else {
                    IP4PendingChangelist list = file.getChangelist();
                    if (list != null && !processed.contains(list)) {
                        viewer.refresh(list);
                        if (viewer.getExpandedState(list)) {
                            viewer.expandToLevel(resource, 2);
                        }
                        processed.add((IP4PendingChangelist) list);
                    }
                }
            } else if (resource instanceof IP4PendingChangelist) {
                if (!processed.contains(resource)) {
                	if(!viewer.getTree().isDisposed()){
	                    viewer.refresh(resource);
	                    if (viewer.getExpandedState(resource)) {
	                        viewer.expandToLevel(resource, 2);
	                    }
                	}
                    processed.add((IP4PendingChangelist) resource);
                }
            }
        }
        if (!remove.isEmpty()) {
            handleRemoveEvent(remove.toArray());
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(final P4Event event) {
        EventType type = event.getType();
        if (type != EventType.CREATE_CHANGELIST
                && type != EventType.DELETE_CHANGELIST
                && type != EventType.CHANGED && type != EventType.FIXED
                && type != EventType.UNFIXED && type != EventType.REFRESHED
                && type != EventType.OPENED && type != EventType.REVERTED
                && type != EventType.MOVE_ADDED
                && type != EventType.SUBMITTED
                && type != EventType.SUBMIT_FAILED
                && type != EventType.SUBMIT_CHANGELIST
                && type != EventType.DELETE_SHELVE
                && type != EventType.UPDATE_SHELVE
                && type != EventType.SUBMIT_SHELVEDCHANGELIST
                && type != EventType.CREATE_SHELVE) {
            return;
        }
        final IP4Changelist[] lists = event.getChangelists();
        UIJob job = new UIJob(
                Messages.PendingView_UpdatingPendingChangelistView) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
            	Tracing.printExecTime(Policy.DEBUG, PendingViewControl.this.getClass().getSimpleName()+":resourceChanged()", event.toString(), new Runnable() {
            		public void run() {
                if (okToUse() && viewer.getInput() != null && !isLoading()) {
                    EventType type = event.getType();
                    switch (type) {
                    case REFRESHED:
                    case DELETE_SHELVE:
                    case UPDATE_SHELVE:
                    case CREATE_SHELVE:
                        handleRefresh(event.getResources());
                        break;
                    case CHANGED:
                        handleUpdateEvent(event.getPending());
                        break;
                    case FIXED:
                        handleJobEvent(event.getResources(), true);
                        break;
                    case UNFIXED:
                        handleJobEvent(event.getResources(), false);
                        break;
                    case CREATE_CHANGELIST:
                        addLists(event.getResources());
                        break;
                    case DELETE_CHANGELIST:
                        handleRemoveEvent(event.getPending());
                        break;
                    case SUBMIT_CHANGELIST:
                        handleSubmitChangelistEvent(event.getPending());
                        break;
                    case SUBMIT_FAILED:
                        refresh();
                        break;
                    case SUBMITTED:
                        handleRemoveEvent(event.getUnopenedFiles());
                        break;
                    case REVERTED:
                        handleRefresh(lists);
                        handleRemoveEvent(event.getFiles());
                        break;
                    case OPENED:
                        handleOpened(event.getFiles());
                        break;
                    case MOVE_ADDED:
                    	refresh();
                        break;
                    case SUBMIT_SHELVEDCHANGELIST:
                    	refresh();
                        break;
                    default:
                        break;
                    }
                }
            	}});
                return Status.OK_STATUS;
            }

        };
        job.setSystem(true);
        job.schedule();
    }

    /**
     * Is this view currently loading pending changelists?
     * 
     * @return true is loading the pending changelists, false otherwise
     */
    public boolean isLoading() {
        return this.isLoading;
    }

    /**
     * @see com.perforce.team.ui.views.PerforceProjectView#getSelectedName()
     */
    @Override
    protected String getSelectedName() {
        return Messages.PendingView_PendingChangelists;
    }
    
	public String getName() {
		return getClass().getSimpleName();
	}

}

class PendingViewContentProvider extends PerforceContentProvider implements ILazyTreeContentProvider{

	public PendingViewContentProvider(StructuredViewer viewer) {
		super(viewer,true);
	}

    @Override
    protected IP4Resource[] getMembers(IP4Container container) {
    	IP4Resource[] members;
        if (container instanceof IP4PendingChangelist) {
            members=((IP4PendingChangelist) container).getAllMembers();
        } else
        	members=super.getMembers(container);
        return members;
    }

	public void updateChildCount(Object element, int currentChildCount) {
		if(viewer instanceof TreeViewer){
    		Tracing.printTrace(getClass().getSimpleName()+":updateChildCount", MessageFormat.format("Element={0}",element));//$NON-NLS-1$,$NON-NLS-2$
		    if (element instanceof IP4Changelist){
		    	if(!((IP4Changelist) element).needsRefresh()) {// must use getMembers() instead of calling Conainter.memebers().
		    		IP4Resource[] members = getMembers((IP4Changelist) element);
		    		Tracing.printTrace(getClass().getSimpleName()+":updateChildCount", MessageFormat.format("Element=IP4Container[{0}], NoRefresh, children={1}",element,members.length));//$NON-NLS-1$,$NON-NLS-2$
		    		if(currentChildCount!=members.length)
		    			((TreeViewer) viewer).setChildCount(element, members.length);
		    	}else{// always assume there is a PENDING child for the container
		    		Tracing.printTrace(getClass().getSimpleName()+":updateChildCount", MessageFormat.format("Element=IP4Container[{0}], Refresh, children={1}",element,1));//$NON-NLS-1$,$NON-NLS-2$
		    		((TreeViewer) viewer).setChildCount(element, 1);
		    	}
		    } else if (element instanceof Loading) {
		    	Tracing.printTrace(getClass().getSimpleName()+":updateChildCount", "Element=Loading");//$NON-NLS-1$,$NON-NLS-2$
		    	((TreeViewer) viewer).setChildCount(element, 0);
		    } else if (element == roots){
		    	Tracing.printTrace(getClass().getSimpleName()+":updateChildCount", MessageFormat.format("Element=roots{0}, NoRefresh",roots.length>1000?Arrays.toString(new int[]{roots.length}):Arrays.toString(roots)));//$NON-NLS-1$,$NON-NLS-2$
		    	if(currentChildCount!=roots.length)
		    		((TreeViewer) viewer).setChildCount(element, roots.length);
		    } else {
		    	((TreeViewer) viewer).setChildCount(element, 0);
		    }
		}
	}
	
	protected void updateContainerNode(TreeViewer viewer, IP4Container container,
			int index) {
		IP4Resource[] members = getMembers(container);
		viewer.setChildCount(container, members.length);
		viewer.refresh(container);
		
	}

}
