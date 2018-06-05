/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.diff.DiffSorter;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistFileWidget {

    /**
     * TYPE_PREFERENCE
     */
    public static final String TYPE_PREFERENCE = "com.perforce.team.ui.changelists.TYPE_PREFERENCE"; //$NON-NLS-1$

    private Type type = Type.COMPRESSED;
    private IP4Resource[] files = new IP4Resource[0];
    private Composite displayArea;

    /**
     * Base folder file label provider
     */
    protected FolderFileLabelProvider labelProvider;

    private TreeViewer viewer;

    // Inputs
    private Folder[] tree;
    private Folder[] compressed;

    /**
     *
     */
    public ChangelistFileWidget() {
        this.type = getTypePreference();
    }

    /**
     * Get displayed files
     * 
     * @return - files
     */
    public IP4Resource[] getFiles() {
        return this.files;
    }

    /**
     * Get number of files currently displayed
     * 
     * @return - file count
     */
    public int getFileCount() {
        return this.viewer.getTree().getItemCount();
    }

    /**
     * Set input of changelist file widget
     * 
     * @param files
     */
    public void setFiles(IP4Resource[] files) {
        if (files != null) {
            this.files = files;
            this.tree = null;
            this.compressed = null;
        }
    }

    /**
     * Generate file tree
     */
    public void generateFileTree() {
        if (tree == null) {
            IP4Resource[] modelFiles = this.files;
            if (modelFiles != null) {
                tree = Folder.buildTree(modelFiles, Type.TREE);
            }
        }
    }

    /**
     * Generate compressed file tree
     */
    public void generateCompressedFileTree() {
        if (compressed == null) {
            IP4Resource[] modelFiles = this.files;
            if (modelFiles != null) {
                compressed = Folder.buildTree(modelFiles, Type.COMPRESSED);
                Folder.compressFolders(compressed);
            }
        }
    }

    /**
     * Create control
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        createControl(parent, null);
    }

    /**
     * Create a content provider for the specified viewer
     * 
     * @param viewer
     * @return - non-null tree content provider
     */
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new PerforceContentProvider(viewer, true, this);
    }

    /**
     * Create a decorated label provider
     * 
     * @param viewer
     * @return decorated label provider
     */
    protected ILabelProvider createDecoratedLabelProvider(TreeViewer viewer) {
        ChangelistDecorator decorator = new ChangelistDecorator(true);
        this.labelProvider = createLabelProvider(viewer);
        StyledChangelistLabelProvider styledProvider = new StyledChangelistLabelProvider(
                this.labelProvider);
        DecoratedChangelistLabelProvider decorated = new DecoratedChangelistLabelProvider(
                this.labelProvider, styledProvider, decorator);
        decorated.setAddDecorations(false);
        return decorated;
    }

    /**
     * Create a label provider for the specified viewer
     * 
     * @param viewer
     * @return - non-null table label provider
     */
    protected FolderFileLabelProvider createLabelProvider(TreeViewer viewer) {
        return new FolderFileLabelProvider(this.type);
    }

    /**
     * Configure the tree viewer
     * 
     * @param viewer
     */
    protected void configureViewer(TreeViewer viewer) {
    	viewer.setUseHashlookup(true);
    }

    /**
     * Create the tree viewer for this widget
     * 
     * @param parent
     * @return - tree viewer
     */
    protected TreeViewer createViewer(Composite parent) {
        return new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL);
    }

    /**
     * Fill the toolbar with items
     * 
     * @param toolbar
     */
    protected void fillToolbar(ToolBar toolbar) {
        ToolItem flatMode = new ToolItem(toolbar, SWT.RADIO);
        Image flatImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_FLAT_LAYOUT).createImage();
        P4UIUtils.registerDisposal(flatMode, flatImage);
        flatMode.setImage(flatImage);
        flatMode.setToolTipText(Messages.ChangelistFileWidget_FlatMode);
        flatMode.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setType(Type.FLAT);
            }

        });
        if (type == Type.FLAT) {
            flatMode.setSelection(true);
        }

        ToolItem treeMode = new ToolItem(toolbar, SWT.RADIO);
        Image treeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_TREE_LAYOUT).createImage();
        P4UIUtils.registerDisposal(treeMode, treeImage);
        treeMode.setImage(treeImage);
        treeMode.setToolTipText(Messages.ChangelistFileWidget_TreeMode);
        treeMode.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setType(Type.TREE);
            }

        });
        if (type == Type.TREE) {
            treeMode.setSelection(true);
        }

        ToolItem compressedMode = new ToolItem(toolbar, SWT.RADIO);
        Image compressedImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_COMPRESSED_LAYOUT).createImage();
        P4UIUtils.registerDisposal(compressedMode, compressedImage);
        compressedMode.setImage(compressedImage);
        compressedMode
                .setToolTipText(Messages.ChangelistFileWidget_CompressedMode);
        compressedMode.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setType(Type.COMPRESSED);
            }

        });
        if (type == Type.COMPRESSED) {
            compressedMode.setSelection(true);
        }

        new ToolItem(toolbar, SWT.SEPARATOR);
        createExpandOptions(toolbar);
    }

    /**
     * Create expand/collapse toolbar items
     * 
     * @param toolbar
     */
    protected void createExpandOptions(ToolBar toolbar) {
        ToolItem expand = new ToolItem(toolbar, SWT.PUSH);
        Image expandImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_EXPAND_ALL).createImage();
        P4UIUtils.registerDisposal(expand, expandImage);
        expand.setImage(expandImage);
        expand.setToolTipText(Messages.ChangelistFileWidget_ExpandAll);
        expand.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.expandAll();
            }

        });

        ToolItem collapse = new ToolItem(toolbar, SWT.PUSH);
        collapse.setToolTipText(Messages.ChangelistFileWidget_CollapseAll);
        Image collapseImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_COLLAPSE).createImage();
        P4UIUtils.registerDisposal(collapse, collapseImage);
        collapse.setImage(collapseImage);
        collapse.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.collapseAll();
            }

        });
    }

    /**
     * Create a toolbar
     * 
     * @param parent
     */
    protected void createToolbar(Composite parent) {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.WRAP);
        fillToolbar(toolbar);
    }

    /**
     * Create control
     * 
     * @param parent
     * @param filter
     */
    public void createControl(Composite parent, ViewerFilter filter) {
        this.displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        this.displayArea.setLayout(daLayout);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        createToolbar(this.displayArea);

        this.viewer = createViewer(this.displayArea);
        if (filter != null) {
            this.viewer.addFilter(filter);
        }
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        this.viewer.getTree().setLayoutData(vData);
        this.viewer.setSorter(new DiffSorter());
        this.viewer.setContentProvider(createContentProvider(this.viewer));
        this.viewer.setLabelProvider(createDecoratedLabelProvider(viewer));
        configureViewer(this.viewer);
    }

    /**
     * Set display type
     * 
     * @param type
     */
    public void setType(Type type) {
        if (type != null && type != this.type) {
            this.type = type;
            if (this.labelProvider != null) {
                this.labelProvider.setType(this.type);
            }
            refreshInput();
            saveTypePreference();
        }
    }

    /**
     * Refresh the type model displayed in this widget
     */
    public void refreshInput() {
        Object[] input = null;
        IP4Resource[] modelFiles = this.files;
        if (modelFiles != null) {
            switch (this.type) {
            case FLAT:
                input = modelFiles;
                break;
            case TREE:
                input = tree;
                break;
            case COMPRESSED:
                input = compressed;
                break;
            default:
                break;
            }
        }
        if (input == null) {
            input = PerforceContentProvider.EMPTY;
        }
        this.viewer.setInput(input);
        updateExpansionState();
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#updateExpansionState()
     */
    protected void updateExpansionState() {
        for (TreeItem item : getViewer().getTree().getItems()) {
            expand(item);
        }
    }

    /**
     * Expand tree item
     * 
     * @param item
     */
    protected void expand(TreeItem item) {
        if (item.getData() instanceof Folder) {
            getViewer().expandToLevel(item.getData(), 1);
            for (TreeItem child : item.getItems()) {
                expand(child);
            }
        }
    }

    /**
     * Get display type
     * 
     * @return - type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Get main control
     * 
     * @return - composite
     */
    public Composite getControl() {
        return this.displayArea;
    }

    /**
     * Get the tree viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * Get tree
     * 
     * @return - tree
     */
    public Tree getTree() {
        return this.viewer.getTree();
    }

    /**
     * Refresh this widget
     */
    public void refresh() {
        this.viewer.refresh();
    }

    /**
     * Get default type to use
     * 
     * @return - display type
     */
    protected Type getTypePreference() {
        Type type = Type.FLAT;
        String value = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getString(getTypePreferenceKey());
        try {
            type = Type.valueOf(value);
        } catch (Exception e) {
            // Ignore exception
        }
        return type;
    }

    /**
     * Save current type preference.
     */
    protected void saveTypePreference() {
        Type type = getType();
        if (type != null) {
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(getTypePreferenceKey(), type.toString());
        }
    }

    /**
     * Get type preference key
     * 
     * @return - type preference
     */
    protected String getTypePreferenceKey() {
        return TYPE_PREFERENCE;
    }

    /**
     * Show a loading input element
     */
    public void showLoading() {
        IContentProvider provider = this.viewer.getContentProvider();
        if (provider instanceof PerforceContentProvider) {
            this.viewer.setInput(new PerforceContentProvider.Loading());
//                    .setInput(((PerforceContentProvider) provider).new Loading());
        }
    }
}
