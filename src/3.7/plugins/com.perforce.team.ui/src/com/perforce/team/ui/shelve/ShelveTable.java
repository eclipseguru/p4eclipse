/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ChangelistDetailsWidget;
import com.perforce.team.ui.changelists.ChangelistFileWidget;
import com.perforce.team.ui.changelists.ChangelistWidget;
import com.perforce.team.ui.decorator.IconCache;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.submitted.SubmittedChangelistFileWidget;
import com.perforce.team.ui.submitted.SubmittedChangelistTable;
import com.perforce.team.ui.submitted.SubmittedChangelistTable.TableConfig;
import com.perforce.team.ui.viewer.FilterViewer;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelveTable extends FilterViewer implements
        ISelectionChangedListener {

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.shelvedcolumns"; //$NON-NLS-1$

    /**
     * FILE_FOLDER_HISTORY
     */
    public static final String FILE_FOLDER_HISTORY = "com.perforce.team.ui.shelve.FILE_FOLDER_HISTORY"; //$NON-NLS-1$

    /**
     * USER_HISTORY
     */
    public static final String USER_HISTORY = "com.perforce.team.ui.shelve.USER_HISTORY"; //$NON-NLS-1$

    /**
     * WORKSPACE_HISTORY
     */
    public static final String WORKSPACE_HISTORY = "com.perforce.team.ui.shelve.WORKSPACE_HISTORY"; //$NON-NLS-1$

    private int column0Offset;
    private int columnOffset;
    private Image loadingImage;
    private Image clearImage;

    private Label folderFileLabel;
    private Combo folderFileCombo;
    private ToolBar folderFileBar;
    private ToolItem folderFileClearItem;
    private Label userLabel;
    private Combo userCombo;
    private ToolBar userBar;
    private ToolItem userClearItem;
    private Label workspaceLabel;
    private Combo workspaceCombo;
    private ToolBar workspaceBar;
    private ToolItem workspaceClearItem;

    // Viewer for submitted changelists
    private TreeViewer viewer;

    private IP4ShelvedChangelist selectedList = null;
    private IP4ShelvedChangelist[] changeLists = null;

    // Composite containing view controls
    private Composite viewComposite;

    private SashForm sash;

    private boolean displayDetails;

    private boolean isLoading = false;

    private IP4Resource p4Resource = null;
    private Object loading = new Object();
    private int retrieveCount = 0;
    private String[] folders = new String[0];
    private String[] users = new String[0];
    private String[] clients = new String[0];

    private ChangelistWidget detailPanel;
    private Link showMore;

    private Comparator<IP4Resource> sorter = new Comparator<IP4Resource>() {

        public int compare(IP4Resource e1, IP4Resource e2) {
            if (e1 instanceof IP4ShelveFile && e2 instanceof IP4ShelveFile) {
                String a1 = ((IP4ShelveFile) e1).getActionPath();
                String a2 = ((IP4ShelveFile) e2).getActionPath();
                if (a1 != null && a2 != null) {
                    return a1.compareTo(a2);
                }
            }
            return 0;
        }

    };

    /**
     * Shelve tree table content provider
     */
    private class ShelveLazyContentProvider implements ILazyTreeContentProvider {

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        public void dispose() {
        }

        /**
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#getParent(java.lang.Object)
         */
        public Object getParent(Object element) {
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java.lang.Object,
         *      int)
         */
        public void updateChildCount(Object element, int currentChildCount) {
            if (element instanceof IP4ShelvedChangelist) {
                final IP4ShelvedChangelist list = (IP4ShelvedChangelist) element;
                if (list.needsRefresh()) {
                    viewer.setChildCount(element, 1);
                    P4Runner.schedule(new P4Runnable() {

                        @Override
                        public void run(IProgressMonitor monitor) {
                            list.refresh();
                            UIJob job = new UIJob(
                                    Messages.ShelveTable_FetchingShelvedChangelist) {

                                @Override
                                public IStatus runInUIThread(
                                        IProgressMonitor monitor) {
                                    if (okToUse()) {
                                        try {
                                            viewer.remove(list,
                                                    new Object[] { loading });
                                            viewer.setChildCount(list,
                                                    list.members().length);
                                            viewer.expandToLevel(list, 1);
                                        } catch (SWTException e) {
                                            // Fix for job037292, suppress
                                            // widget disposed exceptions since
                                            // the tree viewer was already
                                            // checked to be non-disposed.
                                            //
                                            // Exceptions currently appear to be
                                            // thrown on Windows using Eclipse
                                            // 3.2 when multiple objects are
                                            // loaded at once
                                            if (e.code != SWT.ERROR_WIDGET_DISPOSED) {
                                                throw e;
                                            }
                                        }
                                    }
                                    return Status.OK_STATUS;
                                }

                            };
                            job.setSystem(true);
                            job.schedule();
                        }

                    });
                } else {
                    int size = list.members().length;
                    if (size != currentChildCount) {
                        viewer.setChildCount(element, size);
                    }
                }
            }
        }

        /**
         * @see org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java.lang.Object,
         *      int)
         */
        public void updateElement(Object parent, int index) {
            if (parent == changeLists && changeLists != null) {
                int changeLength = changeLists.length;
                if ((index >= 0) && (index < changeLength)) {
                    IP4ShelvedChangelist list = changeLists[index];
                    viewer.replace(parent, index, list);
                    if (list.needsRefresh()) {
                        viewer.setChildCount(list, 1);
                    } else {
                        viewer.setChildCount(list, list.members().length);
                    }
                }
            } else if (parent instanceof IP4ShelvedChangelist) {
                IP4ShelvedChangelist list = (IP4ShelvedChangelist) parent;
                if (list.needsRefresh()) {
                    viewer.replace(list, 0, loading);

                    // Only do this on < Eclipse 3.3
                    if (!SubmittedChangelistTable.eclipse33OrGreater) {
                        updateChildCount(list, 1);
                    }
                } else {
                    IP4Resource[] files = list.members();

                    // Only do this on < Eclipse 3.3
                    if (!SubmittedChangelistTable.eclipse33OrGreater) {
                        viewer.setChildCount(list, files.length);
                    }

                    Arrays.sort(files, sorter);
                    if (index >= 0 && index < files.length) {
                        if (files[index] instanceof IP4ShelveFile) {
                            viewer.replace(parent, index, files[index]);
                        }
                    }
                }
            } else if (parent == loading && index == 0) {
                viewer.replace(parent, 0, loading);
            }
        }
    }

    /**
     * Submitted label provider class
     */
    private class ShelvedLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        // The shelved changelist image
        private Image shelvedImage = null;
        private IconCache icons = new IconCache();

        /**
         * Creates the submitted label provider and initializes the images
         */
        public ShelvedLabelProvider() {
            icons = new IconCache();
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element == loading) {
                    return loadingImage;
                }
                if (element instanceof IP4ShelvedChangelist) {
                    if (shelvedImage == null) {
                        shelvedImage = getShelvedImage();
                    }
                    return shelvedImage;
                } else if (element instanceof IP4ShelveFile) {
                    return icons.getImage((IP4ShelveFile) element);
                }
            }
            return null;
        }

        /**
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            if (shelvedImage != null) {
                shelvedImage.dispose();
                shelvedImage = null;
            }
            icons.dispose();
        }

        private Image getShelvedImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_SHELVE)
                    .createImage();
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IP4ShelvedChangelist) {
                IP4ShelvedChangelist list = (IP4ShelvedChangelist) element;
                switch (columnIndex) {
                case 0:
                    return Integer.toString(list.getId());
                case 1:
                    return P4UIUtils.formatLabelDate(list.getDate());
                case 2:
                    return list.getUserName();
                case 3:
                    return list.getClientName();
                case 4:
                    return P4CoreUtils.removeWhitespace(list.getDescription());
                default:
                    return ""; //$NON-NLS-1$
                }
            } else if (columnIndex == 0 && element == loading) {
                return Messages.ShelveTable_Loading;
            } else {
                return ""; //$NON-NLS-1$
            }
        }
    }

    /**
     * Creates the shelved changelist table
     * 
     * @param folders
     * @param users
     * @param clients
     */
    public ShelveTable(String[] folders, String[] users, String clients[]) {
        if (folders != null) {
            this.folders = folders;
        }
        if (users != null) {
            this.users = users;
        }
        if (clients != null) {
            this.clients = clients;
        }
    }

    /**
     * Shutdown this table
     */
    public void dispose() {

        if (this.loadingImage != null && !this.loadingImage.isDisposed()) {
            this.loadingImage.dispose();
        }

        if (clearImage != null && !clearImage.isDisposed()) {
            clearImage.dispose();
            clearImage = null;
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

    /**
     * Set the resource only, no UI updates are done
     * 
     * @param resource
     */
    public void setResource(IP4Resource resource) {
        this.p4Resource = resource;
    }

    private void createFilterArea(Composite parent) {
        filterComposite = new Composite(parent, SWT.NONE);
        GridLayout fcLayout = new GridLayout(6, false);
        filterComposite.setLayout(fcLayout);
        filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        folderFileLabel = new Label(filterComposite, SWT.LEFT);
        folderFileLabel.setText(Messages.ShelveTable_FolderFile);
        folderFileCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        for (String folder : this.folders) {
            folderFileCombo.add(folder);
        }
        final SelectionListener comboAdapter = P4UIUtils
                .createComboSelectionListener(new Runnable() {

                    public void run() {
                        updateChangelists();
                    }
                });
        folderFileCombo.addSelectionListener(comboAdapter);
        GridData ffcData = new GridData(SWT.FILL, SWT.FILL, true, false);
        ffcData.horizontalSpan = 4;
        folderFileCombo.setLayoutData(ffcData);

        clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();

        folderFileBar = new ToolBar(filterComposite, SWT.FLAT);
        folderFileClearItem = new ToolItem(folderFileBar, SWT.PUSH);
        folderFileClearItem.setImage(clearImage);
        folderFileClearItem
                .setToolTipText(Messages.ShelveTable_ClearFolderFileFilter);
        folderFileClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                folderFileCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        userLabel = new Label(filterComposite, SWT.LEFT);
        userLabel.setText(Messages.ShelveTable_UserLabel);
        userLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        userCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        for (String user : this.users) {
            userCombo.add(user);
        }
        userCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        userCombo.addSelectionListener(comboAdapter);
        userBar = new ToolBar(filterComposite, SWT.FLAT);
        userClearItem = new ToolItem(userBar, SWT.PUSH);
        userClearItem.setImage(clearImage);
        userClearItem.setToolTipText(Messages.ShelveTable_ClearUserFilter);
        userClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                userCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        workspaceLabel = new Label(filterComposite, SWT.LEFT);
        workspaceLabel.setText(Messages.ShelveTable_WorkspaceLabel);
        workspaceCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        for (String workspace : this.clients) {
            workspaceCombo.add(workspace);
        }
        workspaceCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        workspaceCombo.addSelectionListener(comboAdapter);
        workspaceBar = new ToolBar(filterComposite, SWT.FLAT);
        workspaceClearItem = new ToolItem(workspaceBar, SWT.PUSH);
        workspaceClearItem.setImage(clearImage);
        workspaceClearItem
                .setToolTipText(Messages.ShelveTable_ClearWorkspaceFilter);
        workspaceClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                workspaceCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });
    }

    /**
     * Refresh a shelved changelist
     * 
     * @param list
     */
    public void refreshShelvedChangelist(IP4ShelvedChangelist list) {
        if (list != null && changeLists != null) {
            for (IP4ShelvedChangelist current : changeLists) {
                if (list.getId() == current.getId()) {
                    current.markForRefresh();
                    this.viewer.refresh(current);
                    break;
                }
            }
        }
    }

    /**
     * Remove one or more shelved changelists from the view
     * 
     * @param lists
     */
    public void removeShelvedChangelist(IP4ShelvedChangelist[] lists) {
        if (lists != null && lists.length > 0 && changeLists != null) {
            List<IP4ShelvedChangelist> remaining = new ArrayList<IP4ShelvedChangelist>(
                    Arrays.asList(this.changeLists));
            for (IP4ShelvedChangelist list : lists) {
                remaining.remove(list);
            }
            this.changeLists = remaining
                    .toArray(new IP4ShelvedChangelist[remaining.size()]);
            this.viewer.remove(lists);
        }
    }

    /**
     * Add a shelved changelist and select it to the viewer
     * 
     * @param list
     */
    public void addShelvedChangelist(IP4ShelvedChangelist list) {
        if (list != null) {
            if (changeLists != null) {
                IP4ShelvedChangelist[] newJobs = new IP4ShelvedChangelist[changeLists.length + 1];
                newJobs[0] = list;
                System.arraycopy(changeLists, 0, newJobs, 1, changeLists.length);
                changeLists = newJobs;
            } else {
                changeLists = new IP4ShelvedChangelist[] { list };
            }
            this.viewer.setInput(changeLists);
            this.viewer.getTree().setItemCount(changeLists.length);
            this.viewer.getTree().update();
            this.viewer.setSelection(new StructuredSelection(list), true);
        }
    }

    /**
     * Initialize shelved changelist table
     * 
     * @param parent
     */
    public void createPartControl(Composite parent) {
        createPartControl(parent, SWT.NONE);
    }

    /**
     * Initialize shelved changelist table
     * 
     * @param parent
     * @param tableStyle
     */
    public void createPartControl(Composite parent, int tableStyle) {
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.SUBMITTED_VIEW);

        sash = DialogUtils.createSash(parent);

        viewComposite = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout(1, true);
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        viewComposite.setLayout(gl);
        viewComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createFilterArea(viewComposite);

        Tree table = createTable(viewComposite, tableStyle);
        detailPanel = createDetailPanel(sash, table.getBackground());

        createImages();

    }

    private void createImages() {
        loadingImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_LOADING)
                .createImage();
    }

    private ChangelistWidget createDetailPanel(Composite parent,
            Color background) {
        ChangelistWidget widget = new ChangelistWidget(true) {

            @Override
            protected ChangelistFileWidget createFileWidget() {
                return new SubmittedChangelistFileWidget(true);
            }

            @Override
            protected void createJobsTab() {
                // Do not show jobs tab for shelved changes
            }

            @Override
            protected ChangelistDetailsWidget createDetailsWidget() {
                return new ChangelistDetailsWidget() {

                    @Override
                    protected String getUserLabelText() {
                        return Messages.ShelveTable_ShelvedBy;
                    }

                };
            }

        };
        widget.createControl(parent);
        return widget;
    }

    /**
     * Get the tree viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return viewer;
    }

    /**
     * Handle set focus event for this view
     */
    public void setFocus() {
        Tree control = getTree();
        if (control != null) {
            control.setFocus();
        }
    }

    /**
     * Get the table control
     * 
     * @return -tree
     */
    public Tree getTree() {
        if (viewer != null) {
            Tree control = viewer.getTree();
            if (control != null && !control.isDisposed()) {
                return control;
            }
        }
        return null;
    }

    private void updateSash() {
        if (displayDetails) {
            sash.setMaximizedControl(null);
        } else {
            sash.setMaximizedControl(viewComposite);
        }
    }

    /**
     * Show or hide the display details for the selected changelist
     * 
     * @param show
     *            - true to show, false to hide
     */
    public void showDisplayDetails(boolean show) {
        displayDetails = show;
        updateSash();
    }

    private void updateMoreButton(boolean layout) {
        if (showMore != null && !showMore.isDisposed()) {
            int max = getMaxChangelists();
            if (max == -1) {
                showMore.setText(Messages.ShelveTable_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(MessageFormat.format(
                        Messages.ShelveTable_ShowNumMore, max));
                showMore.setEnabled(true);
            }
            if (layout) {
                showMore.getParent().layout(new Control[] { showMore });
            }
        }
    }

    private void updateMoreButton() {
        updateMoreButton(true);
    }

    private String getFilePath(IP4ShelveFile file) {
        IFileSpec spec = file.getFile().getP4JFile();
        if (spec instanceof IExtendedFileSpec) {
            int rev = ((IExtendedFileSpec) spec).getHaveRev();
            if (rev >= 0) {
                return file.getRemotePath() + "#" + rev; //$NON-NLS-1$
            }
        }
        return file.getRemotePath();
    }

    /**
     * Create the table showing the shelved changelists
     */
    private Tree createTable(Composite parent, int style) {
        showMore = new Link(parent, SWT.PUSH);
        showMore.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showMore();
            }

        });
        showMore.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        retrieveCount = getMaxChangelists();
        updateMoreButton(false);

        viewer = new TreeViewer(parent, SWT.VIRTUAL | SWT.BORDER
                | SWT.FULL_SELECTION | style | SWT.MULTI);
        viewer.setUseHashlookup(true);
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);

        TableConfig config = SubmittedChangelistTable.getConfig();
        column0Offset = config.column0Offset;
        columnOffset = config.columnOffset;

        Listener paintListener = new Listener() {

            private void draw(Event event, String resource) {
                int offset = 0;
                if (event.index > 0) {
                    offset += tree.getColumn(0).getWidth();
                    for (int i = 1; i < event.index; i++) {
                        offset += tree.getColumn(i).getWidth();
                    }
                    offset = event.x - offset + columnOffset;
                } else {
                    offset = event.x + column0Offset;
                }
                event.gc.drawString(resource, offset, event.y, true);
            }

            public void handleEvent(Event event) {
                if (event.item.getData() instanceof IP4ShelveFile) {
                    IP4ShelveFile file = (IP4ShelveFile) event.item.getData();
                    draw(event, getFilePath(file));
                }
            }
        };
        tree.addListener(SWT.PaintItem, paintListener);
        tree.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                saveColumnSizes();
            }

        });

        TableLayout layout = new TableLayout();
        tree.setLayout(layout);

        // Add all columns to table
        addColumn(tree, 0, Messages.ShelveTable_Changelist, 3,
                config.redrawOnResize);
        addColumn(tree, 1, Messages.ShelveTable_Date, 5, config.redrawOnResize);
        addColumn(tree, 2, Messages.ShelveTable_User, 5, config.redrawOnResize);
        addColumn(tree, 3, Messages.ShelveTable_Workspace, 10,
                config.redrawOnResize);
        addColumn(tree, 4, Messages.ShelveTable_Description, 20,
                config.redrawOnResize);

        Map<String, Integer> columnSizes = loadColumnSizes();

        for (TreeColumn column : tree.getColumns()) {
            int width = 100;
            if (columnSizes.containsKey(column.getText())) {
                int size = columnSizes.get(column.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            layout.addColumnData(new ColumnPixelData(width, true));
        }

        viewer.setContentProvider(new ShelveLazyContentProvider());
        viewer.setLabelProvider(new ShelvedLabelProvider());
        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof IP4ShelveFile && e2 instanceof IP4ShelveFile) {
                    IP4ShelveFile f1 = (IP4ShelveFile) e1;
                    IP4ShelveFile f2 = (IP4ShelveFile) e2;
                    String a1 = f1.getActionPath();
                    if (a1 != null) {
                        return a1.compareTo(f2.getActionPath());
                    }
                }
                return super.compare(viewer, e1, e2);
            }

        });

        viewer.addSelectionChangedListener(this);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        tree.setLayoutData(gd);

        return tree;
    }

    /**
     * Add another column to the table
     */
    private TreeColumn addColumn(final Tree tree, int colno, String title,
            int weight, boolean redrawOnResize) {
        TreeColumn col = new TreeColumn(tree, SWT.NONE);
        if (redrawOnResize) {
            col.addListener(SWT.Resize, new Listener() {

                public void handleEvent(Event event) {
                    tree.redraw();
                }
            });
        }
        col.setResizable(true);
        col.setText(title);
        return col;
    }

    private Map<String, Integer> loadColumnSizes() {
        return SessionManager.loadColumnSizes(COLUMN_SIZES);
    }

    /**
     * Save the current column sizes
     */
    private void saveColumnSizes() {
        SessionManager.saveColumnPreferences(viewer.getTree(), COLUMN_SIZES);
    }

    private void updateDetailsPanel(final IP4ShelvedChangelist change) {
        this.detailPanel.setInput(change);
        if (change.needsRefresh()) {
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    change.refresh();
                    UIJob job = new UIJob(
                            Messages.ShelveTable_UpdatingShelvedChangelistFiles) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (okToUse() && change == selectedList
                                    && !change.needsRefresh()) {
                                updateDetailsPanel(change);
                            }
                            return Status.OK_STATUS;
                        }

                    };
                    job.schedule();
                }
            });
        }
    }

    /**
     * Clear the details panel
     */
    public void clearDetailsPanel() {
        this.detailPanel.setInput(null);
    }

    /**
     * Clear the filters
     */
    public void clearFilters() {
        folderFileCombo.setText(""); //$NON-NLS-1$
        userCombo.setText(""); //$NON-NLS-1$
        workspaceCombo.setText(""); //$NON-NLS-1$
    }

    /**
     * Gets the text in the change detail field
     * 
     * @return - change details text field value
     */
    public String getChangeDetails() {
        return this.detailPanel.getDetailsWidget().getChangelistText();
    }

    /**
     * Gets the text in the date detail field
     * 
     * @return - date details text field value
     */
    public String getDateDetails() {
        return this.detailPanel.getDetailsWidget().getDateText();
    }

    /**
     * Gets the text in the cleint detail field
     * 
     * @return - client details text field value
     */
    public String getClientDetails() {
        return this.detailPanel.getDetailsWidget().getWorkspaceText();
    }

    /**
     * Gets the text in the user detail field
     * 
     * @return - user details text field value
     */
    public String getUserDetail() {
        return this.detailPanel.getDetailsWidget().getUserText();
    }

    /**
     * Gets the text in the files description field
     * 
     * @return - description details text field value
     */
    public String getDescriptionDetail() {
        return this.detailPanel.getDetailsWidget().getDescriptionText();
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    private int getMaxChangelists() {
        return getPreferenceStore().getInt(
                IPreferenceConstants.NUM_SHELVES_RETRIEVE);
    }

    /**
     * Refresh the retrieve count with the latest value from the pref store
     */
    public void refreshRetrieveCount() {
        retrieveCount = getMaxChangelists();
    }

    /**
     * Update show more link
     * 
     */
    public void updateMoreLink() {
        refreshRetrieveCount();
        updateMoreButton();
        if (selectedList != null) {
            updateChangelists();
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        Object selected = ((StructuredSelection) event.getSelection())
                .getFirstElement();
        IP4ShelvedChangelist change = null;
        if (selected instanceof IP4ShelvedChangelist) {
            change = (IP4ShelvedChangelist) selected;
        } else if (selected instanceof IP4ShelveFile) {
            TreeItem[] items = viewer.getTree().getSelection();
            if (items.length == 1) {
                TreeItem parent = items[0].getParentItem();
                if (parent != null
                        && parent.getData() instanceof IP4ShelvedChangelist) {
                    change = (IP4ShelvedChangelist) parent.getData();
                }
            }
        }
        selectedList = change;
        if (change != null) {
            updateDetailsPanel(change);
        } else {
            clearDetailsPanel();
        }

    }

    /**
     * Shows the changelists for a p4 resource
     * 
     * @param resource
     */
    public void showChangelists(IP4Resource resource) {
        if (resource != null && resource.getConnection() != null
                && !resource.getConnection().isOffline()) {
            retrieveCount = getMaxChangelists();
            p4Resource = resource;
            if (p4Resource != null) {
                String path = p4Resource.getActionPath();
                if (path != null) {
                    folderFileCombo.setText(path);
                } else {
                    folderFileCombo.setText(""); //$NON-NLS-1$
                }
            } else {
                folderFileCombo.setText(""); //$NON-NLS-1$
            }
            userCombo.setText(""); //$NON-NLS-1$
            workspaceCombo.setText(""); //$NON-NLS-1$
            updateChangelists();
        } else {
            p4Resource = null;
            folderFileCombo.setText(""); //$NON-NLS-1$
            userCombo.setText(""); //$NON-NLS-1$
            workspaceCombo.setText(""); //$NON-NLS-1$
        }
    }

    private void enableFilters(boolean enabled) {
        folderFileCombo.setEnabled(enabled);
        userCombo.setEnabled(enabled);
        workspaceCombo.setEnabled(enabled);
    }

    private boolean checkFilter(String[] paths, String user, String workspace) {
        if (user == null) {
            user = ""; //$NON-NLS-1$
        }
        if (workspace == null) {
            workspace = ""; //$NON-NLS-1$
        }
        if (paths == null) {
            paths = new String[] { "" }; //$NON-NLS-1$
        }
        if (paths.length == 1 && paths[0] != null) {
            return paths[0].equals(folderFileCombo.getText().trim())
                    && user.equals(userCombo.getText().trim())
                    && workspace.equals(workspaceCombo.getText().trim());
        }
        return false;
    }

    private void updateHistory(String path, String user, String workspace) {
        if (path != null && path.length() > 0) {
            List<String> paths = new ArrayList<String>();
            paths.add(path);
            for (String item : folderFileCombo.getItems()) {
                if (!paths.contains(item)) {
                    paths.add(item);
                }
                if (paths.size() == 10) {
                    break;
                }
            }
            folderFileCombo.removeAll();
            for (String item : paths) {
                folderFileCombo.add(item, folderFileCombo.getItemCount());
            }
            folderFileCombo.select(0);
            SessionManager.saveHistory(paths, FILE_FOLDER_HISTORY);
        }

        if (user != null && user.length() > 0) {
            List<String> users = new ArrayList<String>();
            users.add(user);
            for (String item : userCombo.getItems()) {
                if (!users.contains(item)) {
                    users.add(item);
                }
                if (users.size() == 10) {
                    break;
                }
            }
            userCombo.removeAll();
            for (String item : users) {
                userCombo.add(item, userCombo.getItemCount());
            }
            userCombo.select(0);
            SessionManager.saveHistory(users, USER_HISTORY);
        }

        if (workspace != null && workspace.length() > 0) {
            List<String> workspaces = new ArrayList<String>();
            workspaces.add(workspace);
            for (String item : workspaceCombo.getItems()) {
                if (!workspaces.contains(item)) {
                    workspaces.add(item);
                }
                if (workspaces.size() == 10) {
                    break;
                }
            }
            workspaceCombo.removeAll();
            for (String item : workspaces) {
                workspaceCombo.add(item, workspaceCombo.getItemCount());
            }
            workspaceCombo.select(0);
            SessionManager.saveHistory(workspaces, WORKSPACE_HISTORY);
        }

    }

    /**
     * Update the changelists
     */
    public void updateChangelists() {
        final IP4Resource currResource = this.p4Resource;
        isLoading = true;
        updateSash();
        viewer.setInput(this.loading);
        String[] paths = null;
        String path = folderFileCombo.getText().trim();
        if (path.length() == 0) {
            paths = null;
        } else {
            paths = new String[] { path };
        }
        String user = userCombo.getText().trim();
        if (user.length() == 0) {
            user = null;
        }
        String workspace = workspaceCombo.getText().trim();
        if (workspace.length() == 0) {
            workspace = null;
        }
        final String[] finalPaths = paths;
        final String finalUser = user;
        final String finalWorkspace = workspace;
        enableFilters(false);
        updateHistory(path, user, workspace);
        viewer.getTree().setItemCount(1);

        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.ShelveTable_LoadingShelvedChangelists;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Resource resource = p4Resource;
                if (resource == null || currResource != resource) {
                    isLoading = false;
                    return;
                }
                final IP4ShelvedChangelist[] newChangelists = resource
                        .getConnection().getShelvedChangelists(finalPaths,
                                retrieveCount, finalUser, finalWorkspace);
                UIJob job = new UIJob(
                        Messages.ShelveTable_UpdatingShelvedChangelistView) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (currResource != p4Resource) {
                            return Status.CANCEL_STATUS;
                        }
                        if (okToUse()
                                && checkFilter(finalPaths, finalUser,
                                        finalWorkspace)) {
                            changeLists = newChangelists;
                            viewer.setInput(changeLists);
                            viewer.getTree().setItemCount(changeLists.length);
                            updateSash();

                            // Calling update here loads the virtual tree nodes
                            // so the selection can be set successfully
                            viewer.getTree().update();
                            if (changeLists.length > 0) {
                                viewer.setSelection(new StructuredSelection(
                                        changeLists[0]));
                                // Update details panel if setting selection
                                // doesn't
                                if (viewer.getSelection().isEmpty()) {
                                    updateDetailsPanel(changeLists[0]);
                                }
                            }
                            enableFilters(true);
                            isLoading = false;
                        }
                        return Status.OK_STATUS;
                    }

                };
                job.schedule();
            }
        });
    }

    /**
     * Refresh the table
     */
    public void refresh() {
        refreshRetrieveCount();
        if (p4Resource != null) {
            updateChangelists();
        } else {
            viewer.refresh();
        }
    }

    /**
     * Get the main control wrapped around the table
     * 
     * @return - main control
     */
    public Composite getControl() {
        return this.sash;
    }

    /**
     * Get the selected changelists
     * 
     * @return - non-null array of selected changelists
     */
    public IP4ShelvedChangelist[] getSelectedChangelists() {
        Set<IP4ShelvedChangelist> lists = new HashSet<IP4ShelvedChangelist>();
        ITreeSelection selection = (ITreeSelection) this.viewer.getSelection();
        for (Object selected : selection.toArray()) {
            if (selected instanceof IP4ShelvedChangelist) {
                lists.add((IP4ShelvedChangelist) selected);
            }
        }
        return lists.toArray(new IP4ShelvedChangelist[lists.size()]);
    }

    /**
     * Shows the next amount of shelved changelists
     */
    public void showMore() {
        if (retrieveCount != -1) {
            retrieveCount += getMaxChangelists();
        }
        updateChangelists();
    }

    /**
     * True if the table is currently loading a changelist
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        return this.isLoading;
    }

    /**
     * Get the changelists currently displayed
     * 
     * @return - array of shelved changelists
     */
    public IP4ShelvedChangelist[] getChangelists() {
        return this.changeLists;
    }
}