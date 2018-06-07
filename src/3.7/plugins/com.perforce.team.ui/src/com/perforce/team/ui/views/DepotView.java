package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
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
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4DecoratedLabelProvider;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.NewServerAction;
import com.perforce.team.ui.actions.OpenAction;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.p4java.actions.RemoveServerAction;
import com.perforce.team.ui.p4java.actions.ServerInfoAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * View showing all files/folders in the depot
 */
public class DepotView extends ViewPart {

    private Composite displayArea;

    // No server defined UI
    private Composite noServersArea;
    private Label noServersDefined;

    // The tree viewer
    private TreeViewer viewer;
    private PerforceContentProvider contentProvider;

    private IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(final P4Event event) {
			UIJob job = new UIJob(Messages.DepotView_RefreshingDepotViewTree) {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					Tracing.printExecTime(() -> {

						if (okToUse()) {
							switch (event.getType()) {

							case ADDED:
								handleAdded(event);
								break;
							case AVAILABLE:
								handleAvailable(event);
								break;
							case CHANGED:
								handleChanged(event);
								break;
							case REFRESHED:
								handleRefresh(event);
								break;
							case REMOVED:
								handleRemoved(event);
								break;
							default:
								break;
							}
						}
					}, DepotView.this.getClass().getSimpleName() + ":resourceChanged()", "{0}", event);
					return Status.OK_STATUS;
				}
			};
            job.setSystem(true);
            job.schedule();
        }

		public String getName() {
			return DepotView.this.getClass().getSimpleName();
		}
    };

    // Some actions
    private Action refreshAction;
    private Action addAction;
    private Action deleteAction;
    private Action propertiesAction;
    private Action deletedAction;
    private Action openAction;
    private Action collapseAction;

    // True if showing show clients changelists
    private boolean showDeletedFiles;

    // Memento used to store state information
    private IMemento memento;

    private Action filterClientAction;

    private boolean filterClientFiles;

    /**
     * The ID for this view
     */
    public static final String VIEW_ID = "com.perforce.team.ui.DepotView"; //$NON-NLS-1$

    // Persistance tags
    private static final String TAG_SERVER = "server"; //$NON-NLS-1$
    private static final String TAG_SELECTION = "selection"; //$NON-NLS-1$
    private static final String TAG_EXPANDED = "expanded"; //$NON-NLS-1$
    private static final String TAG_ELEMENT = "element"; //$NON-NLS-1$
    private static final String TAG_PATH = "path"; //$NON-NLS-1$
    private static final String TAG_VERTICAL_POSITION = "verticalPosition"; //$NON-NLS-1$
    private static final String TAG_HORIZONTAL_POSITION = "horizontalPosition"; //$NON-NLS-1$

    /**
     * Is this view's main control not disposed
     *
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return viewer != null && viewer.getTree() != null
                && !viewer.getTree().isDisposed();
    }

    /**
     * Initialise this view
     *
     * @param site
     * @param memento
     * @throws PartInitException
     */
    @Override
    public void init(IViewSite site, IMemento memento) throws PartInitException {
        super.init(site, memento);
        this.memento = memento;
    }

    /**
     * Get the Depot view
     *
     * @return - depot view
     */
    public static DepotView getView() {
        return (DepotView) PerforceUIPlugin.getActivePage().findView(VIEW_ID);
    }

    /**
     * Shows the depot view
     *
     * @return - shown view
     */
    public static DepotView showView() {
        try {
            return (DepotView) PerforceUIPlugin.getActivePage().showView(
                    VIEW_ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return null;
    }

    private void handleChanged(P4Event changedEvent) {
        for (IP4Connection connection : changedEvent.getConnections()) {
            if (connection.isOffline()) {
                viewer.refresh(connection);
            } else {
                viewer.update(connection, null);
            }
        }
    }

    private void handleRemoved(P4Event removedEvent) {
        viewer.remove(removedEvent.getConnections());
        updateTopControl();
    }

    private void handleAdded(P4Event addedEvent) {
    	List<IP4Connection> newConnections=new ArrayList<IP4Connection>();
        for (IP4Resource resource : addedEvent.getResources()) {
            if (resource instanceof IP4Connection) {
                IP4Connection connection = (IP4Connection) resource;
                if (!connection.isDisposed()) {
                    connection.setShowClientOnly(filterClientFiles);
                    connection
                            .setShowFoldersWIthOnlyDeletedFiles(showDeletedFiles);
                    newConnections.add(connection);
//                    viewer.add(viewer.getInput(), resource);
//                    if (connection.isConnected() && !connection.isOffline()) {
//                        viewer.setSelection(new StructuredSelection(resource));
//                    }
//                    updateTopControl();
//                    viewer.reveal(resource);
                }
            }
        }
        if(newConnections.size()>0){
        	viewer.setInput(P4ConnectionManager.getManager().getConnections());
        	for(IP4Connection connection:newConnections){
	          if (connection.isConnected() && !connection.isOffline()) {
	              viewer.setSelection(new StructuredSelection(connection));
	          }
	          updateTopControl();
	          viewer.reveal(connection);
	          break;
        	}
        }
    }

    private void handleAvailable(P4Event availableEvent) {
        viewer.refresh(availableEvent.getConnections());
    }

    /**
     * Fix for job034259, remove any file(s) from this view if it is not remote
     * and not opened. Files opened for add should appear in this view but when
     * reverted should be removed.
     *
     * @param file
     */
    private void handleRemove(IP4File[] files) {
        if (files.length > 0) {
            List<IP4File> remove = new ArrayList<IP4File>();
            for (IP4File file : files) {
                if (!file.isOpened() && !file.isRemote()) {
                    remove.add(file);
                }
            }
            // Do a bulk remove in case the event is large
            if (!remove.isEmpty()) {
                viewer.remove(remove.toArray(new IP4File[remove.size()]));
            }
        }
    }

    private void handleRefresh(P4Event refreshEvent) {
        // Handle removable before refreshing
        handleRemove(refreshEvent.getFiles());

        for (IP4Resource resource : refreshEvent.getResources()) {
        	try {
        		viewer.refresh(resource);
			} catch (Throwable e) {
				e.printStackTrace();
			}
        }
    }

    /**
     * Save the state of this view
     *
     * @param memento
     */
    @Override
    public void saveState(IMemento memento) {
        if (PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SAVE_EXPANDED_DEPOTS)) {

            // save visible expanded elements
            Object[] expandedElements = viewer.getVisibleExpandedElements();
            if (expandedElements.length > 0) {
                IMemento expandedMem = memento.createChild(TAG_EXPANDED);
                for (int i = 0; i < expandedElements.length; i++) {
                    IMemento elementMem = expandedMem.createChild(TAG_ELEMENT);
                    IP4Resource element = (IP4Resource) expandedElements[i];
                    if (element instanceof IP4Connection
                            || element instanceof IP4Container) {
                        elementMem.putString(TAG_SERVER,
                                getServerString(element));
                        elementMem.putString(TAG_PATH, element.getActionPath());
                    }
                }
            }

            // save selection
            Object[] elements = ((IStructuredSelection) viewer.getSelection())
                    .toArray();
            if (elements.length > 0) {
                IMemento selectionMem = memento.createChild(TAG_SELECTION);
                for (int i = 0; i < elements.length; i++) {
                    IMemento elementMem = selectionMem.createChild(TAG_ELEMENT);
                    elementMem.putString(TAG_SERVER,
                            getServerString(elements[i]));
                    elementMem.putString(TAG_PATH,
                            ((IP4Resource) elements[i]).getActionPath());
                }
            }

            Tree tree = viewer.getTree();

            // save vertical position
            ScrollBar bar = tree.getVerticalBar();
            int position = bar != null ? bar.getSelection() : 0;
            memento.putString(TAG_VERTICAL_POSITION, String.valueOf(position));

            // save horizontal position
            bar = tree.getHorizontalBar();
            position = bar != null ? bar.getSelection() : 0;
            memento.putString(TAG_HORIZONTAL_POSITION, String.valueOf(position));
        }
    }

    private void restoreState(IMemento memento) {
        IMemento childMem = memento.getChild(TAG_EXPANDED);
        if (childMem != null) {
            IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
            for (int i = 0; i < elementMem.length; i++) {
                String paramString = elementMem[i].getString(TAG_SERVER);
                if (paramString != null) {
                    String path = elementMem[i].getString(TAG_PATH);
                    expandElement(paramString, path);
                }
            }
        }

        // Restore selection
        childMem = memento.getChild(TAG_SELECTION);
        if (childMem != null) {
            List<Object> list = new ArrayList<Object>();
            IMemento[] elementMem = childMem.getChildren(TAG_ELEMENT);
            for (int i = 0; i < elementMem.length; i++) {
                String paramString = elementMem[i].getString(TAG_SERVER);
                Object element = expandElement(paramString,
                        elementMem[i].getString(TAG_PATH));
                list.add(element);
            }
            viewer.setSelection(new StructuredSelection(list));
        }

        Tree tree = viewer.getTree();

        // Restore scrollbar positions
        ScrollBar bar = tree.getVerticalBar();
        if (bar != null) {
            try {
                String posStr = memento.getString(TAG_VERTICAL_POSITION);
                int position;
                position = new Integer(posStr).intValue();
                bar.setSelection(position);
            } catch (NumberFormatException e) {
            }
        }
        bar = tree.getHorizontalBar();
        if (bar != null) {
            try {
                String posStr = memento.getString(TAG_HORIZONTAL_POSITION);
                int position;
                position = new Integer(posStr).intValue();
                bar.setSelection(position);
            } catch (NumberFormatException e) {
            }
        }
    }

    private Object expandElement(String paramString, String path) {
        // Fix for job037057, handle absence of ' ' since that would signify old
        // format of just an integer, not a connection parameters string
        if (paramString == null || paramString.length() == 0
                || paramString.indexOf(' ') == -1) {
            return null;
        }
        if (path == null) {
            return null;
        }
        ConnectionParameters params = new ConnectionParameters(paramString);
        IP4Container curr = null;
        if (P4ConnectionManager.getManager().containsConnection(params)) {
            curr = P4ConnectionManager.getManager().getConnection(params);
        }
        if (curr == null || curr.getConnection().isOffline()) {
            return null;
        }
        StringTokenizer tokens = new StringTokenizer(path, "/"); //$NON-NLS-1$

        while (tokens.hasMoreTokens()) {
            String name = tokens.nextToken();
            viewer.setExpandedState(curr, true);
            Object[] children = curr.members();
            boolean found = false;
            for (int i = 0; i < children.length; i++) {
                IP4Resource child = (IP4Resource) children[i];
                if (name.equals(child.getName())
                        && child instanceof IP4Container) {
                    curr = (IP4Container) child;
                    found = true;
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        viewer.setExpandedState(curr, true);
        return curr;
    }

    private String getServerString(Object element) {
        ConnectionParameters params = null;
        if (element instanceof IP4Resource) {
            params = ((IP4Resource) element).getConnection().getParameters();
        }
        if (params != null) {
            return params.toString();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Create everything for this view
     *
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.DEPOT_VIEW);

        displayArea = new Composite(parent, SWT.NONE);
        StackLayout daLayout = new StackLayout();
        displayArea.setLayout(daLayout);

        noServersArea = new Composite(displayArea, SWT.NONE);
        noServersArea.setLayout(new GridLayout(1, false));
        noServersArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        noServersDefined = new Label(noServersArea, SWT.WRAP);
        noServersDefined.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));
        noServersDefined
                .setText(Messages.DepotView_NoConnectionsCurrentlyDefined);
        viewer = new TreeViewer(displayArea);
        viewer.setUseHashlookup(true);
        noServersDefined.setBackground(viewer.getTree().getBackground());
        noServersArea.setBackground(viewer.getTree().getBackground());
        contentProvider = new PerforceContentProvider(viewer) {

        	@Override
        	public Object[] getElements(Object inputElement) {
        		return P4ConnectionManager.getManager().getConnections();
        	}

        };
        viewer.setContentProvider(contentProvider);
        viewer.setLabelProvider(P4DecoratedLabelProvider.create());
        addContextMenu();
        viewer.setComparator(ViewUtil.getViewComparator());

        viewer.addDragSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT,
                new Transfer[] { LocalSelectionTransfer.getTransfer(),
                        FileTransfer.getInstance(), }, new DepotDragAdapter(
                        viewer));
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                handleDoubleClick(event);
            }
        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                boolean empty = viewer.getSelection().isEmpty();
                propertiesAction.setEnabled(!empty);
                deleteAction.setEnabled(!empty);
            }

        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                if (selection.getFirstElement() instanceof IP4Resource) {
                    P4ConnectionManager.getManager().setSelection(
                            ((IP4Resource) selection.getFirstElement())
                                    .getConnection());
                }
            }
        });
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
            	boolean selected=true;
                if (!showDeletedFiles && element instanceof IP4File) {
                    IP4File file = (IP4File) element;
                    selected= !file.isHeadActionDelete();
                }

                if(selected && filterClientFiles) {
                    if (element instanceof IP4Connection) {
                        selected=true;
                    } else if (element instanceof P4Depot) {
                        selected= ((P4Depot) element).isLocal();
                    } else if (element instanceof IP4File) {
                        IP4File resource = (IP4File) element;
                        selected= resource.getLocalPath() != null;
                    }
                }
                return selected;
            }

        });
        getSite().setSelectionProvider(viewer);
        IP4Connection[] connections = ViewUtil.getConnections(filterClientFiles, showDeletedFiles);
        viewer.setInput(connections);

        P4ConnectionManager.getManager().addListener(p4Listener);

        if (memento != null
                && PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .getBoolean(IPreferenceConstants.SAVE_EXPANDED_DEPOTS)) {
            restoreState(memento);
        }
        contentProvider.setLoadAsync(true);

        if (connections.length == 0) {
            daLayout.topControl = noServersArea;
        } else {
            daLayout.topControl = viewer.getControl();
        }
        displayArea.layout();
    }

    /**
     * @param event
     */
    public void handleDoubleClick(DoubleClickEvent event) {
        IStructuredSelection selection = (IStructuredSelection) event
                .getSelection();
        Object element = selection.getFirstElement();

        // If it's a file then open an editor for it
        if (element instanceof IP4File) {
            OpenAction.openFile((IP4File) element);
        } else if (viewer.isExpandable(element)) {
            viewer.setExpandedState(element, !viewer.getExpandedState(element));
        }
    }

    /**
     * @param obj
     */
    public void refresh(Object obj) {
        viewer.refresh(obj, true);
    }

    /**
     * Handle what happens when this view gets the focus
     */
    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }

    /**
     * Refresh this view
     */
    public void refresh() {
        if (okToUse()) {
            viewer.collapseAll();
            viewer.refresh();
            updateTopControl();
        }
    }

    private void updateTopControl() {
        if (okToUse()) {
            StackLayout layout = (StackLayout) displayArea.getLayout();
            Control previous = layout.topControl;
            if (viewer.getTree().getItemCount() > 0) {
                layout.topControl = viewer.getControl();
            } else {
                layout.topControl = noServersArea;
            }
            if (previous != layout.topControl) {
                displayArea.layout();
            }
        }
    }

    /**
     * Show deleted depot files in view?
     *
     * @return returns true if deleted files are shown
     */
    public boolean getShowDeletedFiles() {
        return showDeletedFiles;
    }

    /**
     * filter directories under client spec
     *
     * @return - true if show filter client
     */
    public boolean getShowFilterClient() {
        return this.filterClientFiles;
    }

    /**
     * Create viewer context menu
     */
    private void addContextMenu() {
        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();

        final NewServerAction newAction = new NewServerAction();
        final RemoveServerAction removeAction = new RemoveServerAction();
        final ServerInfoAction serverInfoAction = new ServerInfoAction();

        refreshAction = new Action(
                Messages.DepotView_Refresh,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_ENABLED)) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                for (Object refresh : selection.toArray()) {
                    if (refresh instanceof IP4Container) {
                        ((IP4Container) refresh).markForRefresh();
                        if(refresh instanceof IP4Connection){
                        	((IP4Connection) refresh).refresh();
                        }
                    } else if (refresh instanceof IP4File) {
                        P4Collection collection = P4ConnectionManager
                                .getManager()
                                .createP4Collection(
                                        new IP4Resource[] { (IP4File) refresh });
                        collection.refresh();
                    }
                    viewer.refresh(refresh, true);
                }
            }
        };
        refreshAction.setToolTipText(Messages.DepotView_Refresh);
        refreshAction.setDisabledImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH_DISABLED));
        refreshAction.setHoverImageDescriptor(plugin
                .getImageDescriptor(IPerforceUIConstants.IMG_REFRESH));
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(refreshAction, IHelpContextIds.DEPOT_REFRESH);

        addAction = new Action(Messages.DepotView_NewConnection,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_ADD)) {

            @Override
            public void run() {
                newAction.selectionChanged(null, viewer.getSelection());
                newAction.run(this);
            }

        };

        deleteAction = new Action(Messages.DepotView_RemoveConnection,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_DELETE)) {

            @Override
            public void run() {
                if (!viewer.getSelection().isEmpty()) {
                    removeAction.selectionChanged(this, viewer.getSelection());
                    removeAction.run(this);
                }
            }

        };
        deleteAction.setEnabled(false);

        propertiesAction = new Action(Messages.DepotView_ServerInformation,
                plugin.getImageDescriptor(IPerforceUIConstants.IMG_PROPERTIES)) {

            @Override
            public void run() {
                if (!viewer.getSelection().isEmpty()) {
                    serverInfoAction.selectionChanged(this,
                            viewer.getSelection());
                    serverInfoAction.run(this);
                }
            }

        };
        propertiesAction.setEnabled(false);

        openAction = new Action(Messages.DepotView_Open) {

            @Override
            public void run() {
                Object[] selection = ((IStructuredSelection) viewer
                        .getSelection()).toArray();
                List<String> paths = new ArrayList<String>();
                for (int i = 0; i < selection.length; i++) {
                    if (selection[i] instanceof IP4File) {
                        String localPath = ((IP4File) selection[i])
                                .getLocalPath();
                        if (localPath != null) {
                            paths.add(localPath);
                        }
                    }
                }
                OpenAction.openFiles(paths.toArray(new String[paths.size()]));
            }
        };

        filterClientAction = new Action(
                Messages.DepotView_FilterByClientWorkspace) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };
        filterClientFiles = plugin.getPreferenceStore().getBoolean(
        		IPreferenceConstants.FILTER_CLIENT_FILES);
        filterClientAction.setChecked(filterClientFiles);
        filterClientAction
                .addPropertyChangeListener(new IPropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent event) {
                        PerforceUIPlugin
                                .getPlugin()
                                .getPreferenceStore()
                                .setValue(IPreferenceConstants.FILTER_CLIENT_FILES,
                                        filterClientAction.isChecked());
                        filterClientFiles = filterClientAction.isChecked();
                        for (IP4Connection connection : P4ConnectionManager
                                .getManager().getConnections()) {
                            connection.setShowClientOnly(filterClientFiles);
                        }
                        refresh();
                    }
                });

        deletedAction = new Action(Messages.DepotView_ShowDeletedDepotFiles) {

            @Override
            public int getStyle() {
                return IAction.AS_CHECK_BOX;
            }
        };
        showDeletedFiles = plugin.getPreferenceStore().getBoolean(
        		IPreferenceConstants.SHOW_DELETED_FILES);
        deletedAction.setChecked(showDeletedFiles);

        deletedAction.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(IPreferenceConstants.SHOW_DELETED_FILES, deletedAction.isChecked());
                showDeletedFiles = deletedAction.isChecked();
                for (IP4Connection connection : P4ConnectionManager
                        .getManager().getConnections()) {
                    connection
                            .setShowFoldersWIthOnlyDeletedFiles(showDeletedFiles);
                }
                refresh();
            }
        });

        collapseAction = new Action(Messages.DepotView_CollapseAll) {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAction.setToolTipText(Messages.DepotView_CollapseAll);
        collapseAction.setImageDescriptor(PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_COLLAPSE));

        MenuManager manager = new MenuManager();
        Tree tree = viewer.getTree();
        Menu menu = manager.createContextMenu(tree);
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator("perforce.opengroup")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group1")); //$NON-NLS-1$
                manager.add(refreshAction);
                manager.add(new Separator("perforce.group2")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group3")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group4")); //$NON-NLS-1$
                manager.add(new Separator("perforce.group5")); //$NON-NLS-1$
                manager.add(addAction);
                manager.add(deleteAction);
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));

                addOpenMenu(manager);
                addOpenWithMenu(manager);
            }
        });
        manager.setRemoveAllWhenShown(true);
        tree.setMenu(menu);
        noServersDefined.setMenu(menu);
        getSite().registerContextMenu(manager, viewer);

        manager.addMenuListener(MenuFilter.createTeamMainFilter());

        // Create the local tool bar
        IActionBars bars = getViewSite().getActionBars();
        IToolBarManager tbm = bars.getToolBarManager();
        tbm.add(addAction);
        tbm.add(deleteAction);
        tbm.add(propertiesAction);
        tbm.add(refreshAction);
        tbm.add(collapseAction);
        tbm.update(false);

        // Create the pulldown menu
        IMenuManager pulldown = bars.getMenuManager();
        pulldown.add(deletedAction);
        pulldown.add(filterClientAction);
        pulldown.update(false);
    }

    /**
     * Add "Open" context menu entry
     *
     * @param menu
     */
    private void addOpenMenu(IMenuManager menu) {
        IStructuredSelection selection = (IStructuredSelection) viewer
                .getSelection();
        if (selection.size() != 1) {
            return;
        }
        Object o = selection.getFirstElement();
        if (o instanceof IP4File) {
            IP4File depotFile = (IP4File) selection.getFirstElement();
            IFile workspaceFile = depotFile.getLocalFileForLocation();
            if (workspaceFile != null) {
                menu.appendToGroup("perforce.opengroup", openAction); //$NON-NLS-1$
            }
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
        if (o instanceof IP4File) {
            IP4File depotFile = (IP4File) selection.getFirstElement();
            IFile workspaceFile = depotFile.getLocalFileForLocation();
            if (workspaceFile != null) {
                // Create a menu flyout.
                IMenuManager submenu = new MenuManager(
                        Messages.DepotView_OpenWith);
                submenu.add(new OpenWithMenu(getViewSite().getPage(),
                        workspaceFile));

                // Add the submenu.
                menu.appendToGroup("perforce.opengroup", submenu); //$NON-NLS-1$
            }
        }
    }

    /**
     * Gets the underlying treeviewer in this view
     *
     * @return - depot tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        P4ConnectionManager.getManager().removeListener(p4Listener);
    }

}

