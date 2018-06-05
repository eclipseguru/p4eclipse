/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

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
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
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
import org.osgi.framework.Bundle;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ChangelistDetailsWidget;
import com.perforce.team.ui.changelists.ChangelistFileWidget;
import com.perforce.team.ui.changelists.ChangelistWidget;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.viewer.FilterViewer;
import com.perforce.team.ui.views.SessionManager;

/**
 * This table shows submitted changelists with filters for user, client, and
 * path
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedChangelistTable extends FilterViewer implements
        ISelectionChangedListener {

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.submittedcolumns"; //$NON-NLS-1$

    /**
     * FILE_FOLDER_HISTORY
     */
    public static final String FILE_FOLDER_HISTORY = "com.perforce.team.ui.submitted.FILE_FOLDER_HISTORY"; //$NON-NLS-1$

    /**
     * USER_HISTORY
     */
    public static final String USER_HISTORY = "com.perforce.team.ui.submitted.USER_HISTORY"; //$NON-NLS-1$

    /**
     * WORKSPACE_HISTORY
     */
    public static final String WORKSPACE_HISTORY = "com.perforce.team.ui.submitted.WORKSPACE_HISTORY"; //$NON-NLS-1$

    /**
     * MAC_COLUMN0_OFFSET
     */
    public static final int MAC_COLUMN0_OFFSET = 17;

    /**
     * MAC_COLUMN0_OFFSET_CARBON
     */
    public static final int MAC_COLUMN0_OFFSET_COCOA = 20;

    /**
     * MAC_COLUMN_OFFSET
     */
    public static final int MAC_COLUMN_OFFSET = 34;

    /**
     * MAC_COLUMN_OFFSET_COCOA
     */
    public static final int MAC_COLUMN_OFFSET_COCOA = 52;

    /**
     * WIN_COLUMN0_OFFSET
     */
    public static final int WIN_COLUMN0_OFFSET = 17;

    /**
     * WIN_COLUMN_OFFSET
     */
    public static final int WIN_COLUMN_OFFSET = 58;

    /**
     * LINUX_COLUMN0_OFFSET
     */
    public static final int LINUX_COLUMN0_OFFSET = 17;

    /**
     * LINUX_COLUMN0_OFFSET
     */
    public static final int LINUX_COLUMN0_OFFSET_32 = 0;

    /**
     * LINUX_COLUMN_OFFSET
     */
    public static final int LINUX_COLUMN_OFFSET = 53;

    /**
     * eclipse33OrGreater
     */
    public static boolean eclipse33OrGreater = false;

    static {
        // This tests for a 3.3+ interface since the lazy tree content provider
        // was changed between the two versions and so it requires a few
        // eclipse-specific method differences between eclipse 3.2 and eclipse
        // 3.3+
        try {
            Bundle bundle = Platform.getBundle("org.eclipse.jface"); //$NON-NLS-1$
            if (bundle != null) {
                eclipse33OrGreater = bundle
                        .loadClass("org.eclipse.jface.viewers.ILazyTreePathContentProvider") != null; //$NON-NLS-1$
            }
        } catch (Exception e) {
            eclipse33OrGreater = false;
        } catch (Error e) {
            eclipse33OrGreater = false;
        }
    }

    /**
     * Table config class with column 0 offset, non-0 column offset and whether
     * the tree/table should be re-drawn when a column is resized.
     * 
     */
    public static class TableConfig {

        /**
         * Column offset for first column
         */
        public int column0Offset;

        /**
         * Column offset for all but zero column
         */
        public int columnOffset;

        /**
         * Redraw on resize of columns
         */
        public boolean redrawOnResize = false;
    }

    /**
     * Get table config based on platform
     * 
     * @return - table config
     */
    public static TableConfig getConfig() {
        TableConfig config = new TableConfig();
        if (P4CoreUtils.isMac()) {
            boolean isCocoa = "cocoa".equals(Platform.getWS()); //$NON-NLS-1$
            if (!isCocoa) {
                config.column0Offset = MAC_COLUMN0_OFFSET;
                config.columnOffset = MAC_COLUMN_OFFSET;
            } else {
                config.redrawOnResize = true;
                config.column0Offset = MAC_COLUMN0_OFFSET_COCOA;
                config.columnOffset = MAC_COLUMN_OFFSET_COCOA;
            }
        } else if (P4CoreUtils.isWindows()) {
            config.column0Offset = WIN_COLUMN0_OFFSET;
            config.columnOffset = WIN_COLUMN_OFFSET;
        } else if (P4CoreUtils.isLinux()) {
            if (eclipse33OrGreater) {
                config.column0Offset = LINUX_COLUMN0_OFFSET;
            } else {
                config.column0Offset = LINUX_COLUMN0_OFFSET_32;
            }
            config.columnOffset = LINUX_COLUMN_OFFSET;
        }
        return config;
    }

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

    private IP4SubmittedChangelist selectedList = null;
    private IP4SubmittedChangelist[] changeLists = null;

    private ISubmittedChangelistListener callback = null;

    // Composite containing view controls
    private Composite viewComposite;

    private SashForm sash;

    private boolean displayDetails;
    private boolean enableEdit = false;
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
            if (e1 instanceof IP4SubmittedFile && e2 instanceof IP4Job) {
                return -1;
            } else if (e1 instanceof IP4Job && e2 instanceof IP4SubmittedFile) {
                return 1;
            } else if (e1 instanceof IP4SubmittedFile
                    && e2 instanceof IP4SubmittedFile) {
                String a1 = ((IP4SubmittedFile) e1).getActionPath();
                String a2 = ((IP4SubmittedFile) e2).getActionPath();
                if (a1 != null && a2 != null) {
                    return a1.compareTo(a2);
                }
            } else if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
                String id1 = ((IP4Job) e1).getId();
                String id2 = ((IP4Job) e2).getId();
                if (id1 != null && id2 != null) {
                    return id1.compareTo(id2);
                }
            }
            return 0;
        }

    };

    /**
     * Submitted tree table content provider
     */
    private class SubmittedLazyContentProvider implements
            ILazyTreeContentProvider {

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
            if (element instanceof IP4Changelist) {
                final IP4Changelist list = (IP4Changelist) element;
                if (list.needsRefresh()) {
                    viewer.setChildCount(element, 1);
                    P4Runner.schedule(new P4Runnable() {

                        @Override
                        public void run(IProgressMonitor monitor) {
                            list.refresh();
                            UIJob job = new UIJob(
                                    Messages.SubmittedChangelistTable_FetchingSubmittedChangelist) {

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
                    IP4Changelist list = changeLists[index];
                    viewer.replace(parent, index, list);
                    if (list.needsRefresh()) {
                        viewer.setChildCount(list, 1);
                    } else {
                        viewer.setChildCount(list, list.members().length);
                    }
                }
            } else if (parent instanceof IP4Changelist) {
                IP4Changelist list = (IP4Changelist) parent;
                if (list.needsRefresh()) {
                    viewer.replace(list, 0, loading);

                    // Only do this on < Eclipse 3.3
                    if (!eclipse33OrGreater) {
                        updateChildCount(list, 1);
                    }
                } else {
                    IP4Resource[] files = list.members();

                    // Only do this on < Eclipse 3.3
                    if (!eclipse33OrGreater) {
                        viewer.setChildCount(list, files.length);
                    }

                    Arrays.sort(files, sorter);
                    if (index >= 0 && index < files.length) {
                        if (files[index] instanceof IP4SubmittedFile) {
                            viewer.replace(parent, index, files[index]);
                        } else if (files[index] instanceof IP4Job) {
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
    private class SubmittedLabelProvider extends PerforceLabelProvider {

        /**
         * Creates the submitted label provider and initializes the images
         */
        public SubmittedLabelProvider() {
            super(false);
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element == loading) {
                    return loadingImage;
                }
            }
            return super.getColumnImage(element, columnIndex);
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IP4Changelist) {
                IP4Changelist list = (IP4Changelist) element;
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
                	return super.getColumnText(element, columnIndex);
                }
            } else if (columnIndex == 0 && element == loading) {
                return Messages.SubmittedChangelistTable_Loading;
            } else {
            	// DO NOT pollute the canvas by drawing anything here,
            	// because the draw does not wipe the background.
            	// MUST leave empty here!
            	return ""; //$NON-NLS-1$
            }
        }
    }

    /**
     * Creates the submitted changelist table
     * 
     */
    public SubmittedChangelistTable() {
        this(null, null, null, false);
    }

    /**
     * Creates the submitted changelist table
     * 
     * @param folders
     * @param users
     * @param clients
     * @param enableEdit
     */
    public SubmittedChangelistTable(String[] folders, String[] users,
            String[] clients, boolean enableEdit) {
        if (folders != null) {
            this.folders = folders;
        }
        if (users != null) {
            this.users = users;
        }
        if (clients != null) {
            this.clients = clients;
        }
        this.enableEdit = enableEdit;
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
        return P4UIUtils.okToUse(viewer);
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
        folderFileLabel.setText(Messages.SubmittedChangelistTable_FolderFile);
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
                .setToolTipText(Messages.SubmittedChangelistTable_ClearFolderFileFilter);
        folderFileClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                folderFileCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        userLabel = new Label(filterComposite, SWT.LEFT);
        userLabel.setText(Messages.SubmittedChangelistTable_UserLabel);
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
        userClearItem
                .setToolTipText(Messages.SubmittedChangelistTable_ClearUserFilter);
        userClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                userCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        workspaceLabel = new Label(filterComposite, SWT.LEFT);
        workspaceLabel
                .setText(Messages.SubmittedChangelistTable_WorkspaceLabel);
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
                .setToolTipText(Messages.SubmittedChangelistTable_ClearWorkspaceFilter);
        workspaceClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                workspaceCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });
    }

    /**
     * Initialize submitted changelist table
     * 
     * @param parent
     */
    public void createPartControl(Composite parent) {
        createPartControl(parent, SWT.NONE, null);
    }

    /**
     * Initialize submitted changelist table
     * 
     * @param parent
     * @param listener
     */
    public void createPartControl(Composite parent,
            ISubmittedChangelistListener listener) {
        createPartControl(parent, SWT.NONE, listener);
    }

    /**
     * Initialize submitted changelist table
     * 
     * @param parent
     * @param tableStyle
     */
    public void createPartControl(Composite parent, int tableStyle) {
        createPartControl(parent, tableStyle, null);
    }

    /**
     * Initialize submitted changelist table
     * 
     * @param parent
     * @param tableStyle
     * @param listener
     */
    public void createPartControl(Composite parent, int tableStyle,
            ISubmittedChangelistListener listener) {
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(parent, IHelpContextIds.SUBMITTED_VIEW);

        this.callback = listener;

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
        ChangelistWidget widget = new ChangelistWidget(enableEdit) {

            @Override
            protected ChangelistFileWidget createFileWidget() {
                return new SubmittedChangelistFileWidget(enableEdit);
            }

            @Override
            protected ChangelistDetailsWidget createDetailsWidget() {
                return new ChangelistDetailsWidget() {

                    @Override
                    protected String getDateLabelText() {
                        return Messages.SubmittedChangelistTable_DateSubmitted;
                    }

                    @Override
                    protected String getUserLabelText() {
                        return Messages.SubmittedChangelistTable_SubmittedBy;
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
                showMore.setText(Messages.SubmittedChangelistTable_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(MessageFormat.format(
                        Messages.SubmittedChangelistTable_ShowNumMore, max));
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

    /**
     * Create the table showing the submitted changelists
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
                | SWT.FULL_SELECTION | style |SWT.SINGLE);
        viewer.setUseHashlookup(true);
        final Tree tree = viewer.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(false);

        TableConfig config = getConfig();
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
                if (event.item.getData() instanceof IP4SubmittedFile) {
                    IP4File file = ((IP4SubmittedFile) event.item.getData())
                            .getFile();
                    IFileSpec spec = file.getP4JFile();
                    int rev = 0;
                    if (spec != null) {
                        rev = spec.getEndRevision();
                    }
                    draw(event, file.getRemotePath() + "#" + rev); //$NON-NLS-1$
                } else if (event.item.getData() instanceof IP4Job) {
                    draw(event, ((IP4Job) event.item.getData()).getId());
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
        addColumn(tree, 0, Messages.SubmittedChangelistTable_Changelist, 3,
                config.redrawOnResize);
        addColumn(tree, 1, Messages.SubmittedChangelistTable_Date, 5,
                config.redrawOnResize);
        addColumn(tree, 2, Messages.SubmittedChangelistTable_User, 5,
                config.redrawOnResize);
        addColumn(tree, 3, Messages.SubmittedChangelistTable_Workspace, 10,
                config.redrawOnResize);
        addColumn(tree, 4, Messages.SubmittedChangelistTable_Description, 20,
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

        viewer.setContentProvider(new SubmittedLazyContentProvider());
        viewer.setLabelProvider(new SubmittedLabelProvider());
        viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof IP4SubmittedFile && e2 instanceof IP4Job) {
                    return -1;
                } else if (e1 instanceof IP4Job
                        && e1 instanceof IP4SubmittedFile) {
                    return 1;
                } else if (e1 instanceof IP4SubmittedFile
                        && e2 instanceof IP4SubmittedFile) {
                    IP4SubmittedFile f1 = (IP4SubmittedFile) e1;
                    IP4SubmittedFile f2 = (IP4SubmittedFile) e2;
                    if (f1.getFile() != null && f2.getFile() != null) {
                        String a1 = f1.getFile().getActionPath();
                        if (a1 != null) {
                            return a1.compareTo(f2.getFile().getActionPath());
                        }
                    }
                } else if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
                    String id1 = ((IP4Job) e1).getId();
                    String id2 = ((IP4Job) e2).getId();
                    if (id1 != null) {
                        return id1.compareTo(id2);
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

    private void updateDetailsPanel(final IP4Changelist change) {
        this.detailPanel.setInput(change);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                if (change != selectedList) {
                    return;
                }
                if (change.needsRefresh()) {
                    change.refresh();
                }
                if (change != selectedList) {
                    return;
                }
                detailPanel.loadFiles(change);
                UIJob job = new UIJob(
                        Messages.SubmittedChangelistTable_UpdatingSubmittedChangelistJobsAndFiles) {

                    @Override
                    public IStatus runInUIThread(IProgressMonitor monitor) {
                        if (okToUse() && change == selectedList) {
                            detailPanel.setInput(change);
                            detailPanel.refreshFiles();
                        }
                        return Status.OK_STATUS;
                    }

                };
                job.schedule();
            }
        });
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
                IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES);
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
        IP4SubmittedChangelist change = null;
        if (selected instanceof IP4SubmittedChangelist) {
            change = (IP4SubmittedChangelist) selected;
        } else if (selected instanceof IP4SubmittedFile
                || selected instanceof IP4Job) {
            TreeItem[] items = viewer.getTree().getSelection();
            if (items.length == 1) {
                TreeItem parent = items[0].getParentItem();
                if (parent != null
                        && parent.getData() instanceof IP4SubmittedChangelist) {
                    change = (IP4SubmittedChangelist) parent.getData();
                }
            }
        }
        if (selectedList != change) {
            selectedList = change;
            if (change != null) {
                updateDetailsPanel(change);
            } else {
                clearDetailsPanel();
            }
        }else if(change==null){
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
                return Messages.SubmittedChangelistTable_LoadingSubmittedChangelists;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Resource resource = p4Resource;
                if (resource == null || currResource != resource) {
                    isLoading = false;
                    return;
                }
                final IP4SubmittedChangelist[] newChangelists = resource
                        .getConnection().getSubmittedChangelists(finalPaths,
                                retrieveCount, finalUser, finalWorkspace);
                UIJob job = new UIJob(
                        Messages.SubmittedChangelistTable_UpdatingSubmittedChangelistView) {

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
                            if (callback != null) {
                                callback.changelistsLoaded(changeLists);
                            }
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
     * Refresh the specified submitted changelist in the viewer
     * 
     * @param list
     */
    public void refresh(IP4SubmittedChangelist list) {
        if (list != null && changeLists != null) {
            for (IP4SubmittedChangelist current : changeLists) {
                if (list.getId() == current.getId()) {
                    current.markForRefresh();
                    this.viewer.refresh(current);
                    break;
                }
            }
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
    public IP4SubmittedChangelist[] getSelectedChangelists() {
        Set<IP4SubmittedChangelist> lists = new HashSet<IP4SubmittedChangelist>();
        ITreeSelection selection = (ITreeSelection) this.viewer.getSelection();
        for (Object selected : selection.toArray()) {
            if (selected instanceof IP4SubmittedChangelist) {
                lists.add((IP4SubmittedChangelist) selected);
            }
        }
        return lists.toArray(new IP4SubmittedChangelist[0]);
    }

    /**
     * Shows the next amount of submitted changelists
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
     * @return - array of submitted changelists
     */
    public IP4SubmittedChangelist[] getChangelists() {
        return this.changeLists;
    }

    /**
     * Get details widget
     * 
     * @return - changelist widget
     */
    public ChangelistWidget getChangelistWidget() {
        return this.detailPanel;
    }

}
