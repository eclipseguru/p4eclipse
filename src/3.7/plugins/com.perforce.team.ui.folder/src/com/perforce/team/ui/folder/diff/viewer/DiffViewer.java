/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.changelists.Folder;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.diff.editor.DecoratingDiffLabelProvider;
import com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider;
import com.perforce.team.ui.folder.diff.editor.FileDiffDecorator;
import com.perforce.team.ui.folder.diff.editor.FileDiffLabelProvider;
import com.perforce.team.ui.folder.diff.editor.FolderDiffSorter;
import com.perforce.team.ui.folder.diff.editor.StatusFilter;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffViewer {

    private TreeViewer viewer = null;
    private FileDiffContentProvider contentProvider = null;
    private FileDiffLabelProvider labelProvider = null;

    private FileDiffContainer container = null;
    private GroupedDiffContainer groupContainer = null;
    private boolean showUnique = true;
    private boolean showIdentical = true;
    private boolean showContent = true;
    private Type type = Type.FLAT;

    private ScrolledComposite sc;

    /**
     * Create a diff viewer
     */
    public DiffViewer() {

    }

    /**
     * Create the tree that will be used in this viewer
     * 
     * @param parent
     * @param toolkit
     * @return tree
     */
    protected Tree createTree(Composite parent, FormToolkit toolkit) {
        Tree tree = new Tree(parent, SWT.NO_SCROLL | SWT.SINGLE
                | SWT.FULL_SELECTION);
        toolkit.adapt(tree, false, false);
        tree.setData(FormToolkit.KEY_DRAW_BORDER, Boolean.FALSE);
        tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        return tree;
    }

    /**
     * Create content provider
     * 
     * @param viewer
     * @return file diff content provider
     */
    protected FileDiffContentProvider createContentProvider(TreeViewer viewer) {
        return new FileDiffContentProvider(viewer, true);
    }

    /**
     * Create the controls for this diff viewer
     * 
     * @param parent
     * @param toolkit
     */
    public void createControl(Composite parent, FormToolkit toolkit) {
        sc = new ScrolledComposite(parent, SWT.NONE);
        sc.setExpandHorizontal(true);
        sc.setExpandVertical(true);
        sc.setLayout(GridLayoutFactory.fillDefaults().create());
        sc.setLayoutData(GridDataFactory.fillDefaults().grab(true, true)
                .create());

        this.viewer = new TreeViewer(createTree(sc, toolkit));
        sc.setContent(this.viewer.getTree());
        this.viewer.setUseHashlookup(true);
        this.contentProvider = createContentProvider(this.viewer);
        this.viewer.setContentProvider(this.contentProvider);
        this.labelProvider = new FileDiffLabelProvider();
        DecoratingDiffLabelProvider styledProvider = new DecoratingDiffLabelProvider(
                this.labelProvider, new FileDiffDecorator(true), null);
        this.viewer.setLabelProvider(styledProvider);
        this.viewer.setSorter(new FolderDiffSorter());
        updateFilters();
    }

    /**
     * Update expansion state
     */
    protected void updateExpansionState() {
        for (TreeItem item : getViewer().getTree().getItems()) {
            expand(item);
        }
        sc.setMinSize(this.viewer.getTree().computeSize(SWT.DEFAULT,
                SWT.DEFAULT));
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
     * Get underyling tree viewer
     * 
     * @return tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * Set type mode to display in
     * 
     * @param type
     */
    public void setType(Type type) {
        if (type != null && this.type != type) {
            this.type = type;
            this.labelProvider.setType(this.type);
            updateType();
        }
    }

    /**
     * Set container input to viewer
     * 
     * @param container
     * @param groupContainer
     */
    public void setInput(FileDiffContainer container,
            GroupedDiffContainer groupContainer) {
        this.container = container;
        this.groupContainer = groupContainer;
        if (this.container != null) {
            this.contentProvider.setContainer(this.container);
            updateType();
        } else {
            this.viewer.setInput(PerforceContentProvider.EMPTY);
        }
    }

    /**
     * Show loading
     */
    public void showLoading() {
        this.viewer.setInput(new PerforceContentProvider.Loading());//this.contentProvider.new Loading());
    }

    private void updateFilters() {
        if (this.container != null) {
            List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
            for (ViewerFilter existing : this.viewer.getFilters()) {
                if (!(existing instanceof StatusFilter)) {
                    filters.add(existing);
                }
            }
            if (!showUnique) {
                filters.add(new StatusFilter(Status.LEFT_ONLY));
                filters.add(new StatusFilter(Status.RIGHT_ONLY));
            }
            if (!showContent) {
                filters.add(new StatusFilter(Status.CONTENT));
            }
            if (!showIdentical) {
                filters.add(new StatusFilter(Status.IDENTICAL));
            }
            ViewerFilter[] newFilters = filters
                    .toArray(new ViewerFilter[filters.size()]);
            if (!Arrays.equals(newFilters,this.viewer.getFilters())) {
                this.viewer.setFilters(newFilters);
                sc.setMinSize(this.viewer.getTree().computeSize(SWT.DEFAULT,
                        SWT.DEFAULT));
            }
        }
    }

    private void updateType() {
        updateFilters();
        if (groupContainer != null) {
            groupContainer.setType(this.type);
            this.viewer.setInput(groupContainer.getElements());
            updateExpansionState();
        }
    }

    /**
     * Set whether or not to show unique pairs
     * 
     * @param show
     */
    public void showUniquePairs(boolean show) {
        if (show != this.showUnique) {
            this.showUnique = show;
            updateFilters();
            updateExpansionState();
        }
    }

    /**
     * Set whether or not to show content pairs
     * 
     * @param show
     */
    public void showContentPairs(boolean show) {
        if (show != this.showContent) {
            this.showContent = show;
            updateFilters();
            updateExpansionState();
        }
    }

    /**
     * Set whether or not to show identical pairs
     * 
     * @param show
     */
    public void showIdenticalPairs(boolean show) {
        if (show != this.showIdentical) {
            this.showIdentical = show;
            updateFilters();
            updateExpansionState();
        }
    }

}
