/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class NodeOutlinePage extends Page implements
        IContentOutlinePage {

    /**
     * EMPTY array
     */
    protected static final Object[] EMPTY = new Object[0];

    /**
     * REFRESH_INTERVAL - load job scheduling interval
     */
    protected static final int REFRESH_INTERVAL = 200;

    private ListenerList listeners;

    /**
     * Root of outline
     */
    protected Object root;

    /**
     * Node model
     */
    protected INodeModel model;

    /**
     * Current revision
     */
    protected IP4Revision revision;

    private Composite displayArea;
    private TreeViewer viewer;
    private ITreeContentProvider contentProvider = null;
    private boolean isLoading = false;
    private ISelection nextSelection;

    private Job loadingJob = new Job(
            Messages.NodeOutlinePage_LoadingTimelapseOutline) {

        @Override
        protected IStatus run(IProgressMonitor monitor) {
            final Object loadRoot = root;
            if (loadRoot != null) {
                final List<Object> expand = new ArrayList<Object>();
                Object[] elements = contentProvider.getElements(loadRoot);
                for (Object element : elements) {
                    if (expand(element)) {
                        expand.add(element);
                    }
                    Object[] children = contentProvider.getChildren(element);
                    for (Object child : children) {
                        if (expand(child)) {
                            expand.add(child);
                        }
                    }
                }
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (loadRoot == root && okToUse()) {
                            viewer.setInput(root);
                            viewer.setExpandedElements(expand
                                    .toArray(new Object[expand.size()]));
                            if (nextSelection != null) {
                                viewer.setSelection(nextSelection);
                            }
                            isLoading = false;
                        }
                    }
                });
            }
            return Status.OK_STATUS;
        }

    };

    /**
     * Creates a java outline page
     */
    public NodeOutlinePage() {
        this.listeners = new ListenerList();
    }

    /**
     * Creates a java outline page
     * 
     * @param root
     */
    public NodeOutlinePage(Object root) {
        this();
        setRoot(root);
    }

    /**
     * Creates a java outline page
     * 
     * @param root
     * @param model
     */
    public NodeOutlinePage(Object root, INodeModel model) {
        this();
        setRoot(root);
        setModel(model);
    }

    /**
     * Is this outline page disposed?
     * 
     * @return - true if disposed or never created, false otherwise
     */
    public boolean isDisposed() {
        return !okToUse();
    }

    private boolean okToUse() {
        return viewer != null && viewer.getTree() != null
                && !viewer.getTree().isDisposed();
    }

    /**
     * Set root of this outline page
     * 
     * @param root
     */
    public void setRoot(Object root) {
        this.root = root;
        setInput();
    }

    /**
     * Set the node model for this outline page
     * 
     * @param model
     */
    public void setModel(INodeModel model) {
        this.model = model;
    }

    /**
     * Set the current revision for this outline page
     * 
     * @param revision
     */
    public void setRevision(IP4Revision revision) {
        this.revision = revision;
    }

    /**
     * Is the outline page loading
     * 
     * @return - true if loading, false otherwise
     */
    public boolean isLoading() {
        return this.isLoading;
    }

    private void setInput() {
        if (okToUse()) {
            if (this.root != null) {
                isLoading = true;
                loadingJob.schedule(REFRESH_INTERVAL);
            } else {
                loadingJob.cancel();
                this.viewer.setInput(EMPTY);
            }
        }
    }

    /**
     * Should the specified element be expand initially
     * 
     * @param element
     * @return - true to expand false otherwise
     */
    protected abstract boolean expand(Object element);

    /**
     * Get non-null content provider
     * 
     * @return - content provider
     */
    protected abstract ITreeContentProvider getContentProvider();

    /**
     * Get non-null label provider
     * 
     * @return - label provider
     */
    protected abstract IBaseLabelProvider getLabelProvider();

    /**
     * Configure a viewer
     * 
     * @param viewer
     */
    protected abstract void configureViewer(TreeViewer viewer);

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        this.displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginWidth = 0;
        daLayout.marginHeight = 0;
        this.displayArea.setLayout(daLayout);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        this.viewer = new TreeViewer(this.displayArea, SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.SINGLE);
        this.viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        for (Object listener : listeners.getListeners()) {
            this.viewer
                    .addSelectionChangedListener((ISelectionChangedListener) listener);
        }
        listeners.clear();

        configureViewer(viewer);

        this.contentProvider = getContentProvider();
        viewer.setContentProvider(this.contentProvider);
        viewer.setLabelProvider(getLabelProvider());

        setInput();

        Action clearAction = new Action() {

            @Override
            public void run() {
                nextSelection = null;
                viewer.setSelection(StructuredSelection.EMPTY);
            }
        };
        clearAction.setToolTipText(Messages.NodeOutlinePage_ClearSelection);
        clearAction.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_CLEAR));

        getSite().getActionBars().getToolBarManager().add(clearAction);
    }

    /**
     * Get node outline page viewer
     * 
     * @return - tree viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.displayArea;
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.displayArea.setFocus();
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        if (this.viewer != null) {
            this.viewer.addSelectionChangedListener(listener);
        } else {
            this.listeners.add(listener);
        }
    }

    /**
     * Decorate node with specified id with current and max revision.
     * 
     * @param label
     * @param element
     * @return - decorated string
     */
    protected String decorateRevisions(String label, Object element) {
        if (element != null && model != null && revision != null
                && model.isComplete()) {
            String id = model.getHandle(element);
            if (id != null) {
                int have = model.getRelativeChangeCount(revision, id);
                int head = model.getTotalChangeCount(revision, id);
                if (have > 0 && head > 0) {
                    StringBuilder builder = new StringBuilder(label);
                    builder.append(" #"); //$NON-NLS-1$
                    builder.append(have);
                    builder.append('/');
                    builder.append(head);
                    label = builder.toString();
                }
            }
        }
        return label;
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
     */
    public ISelection getSelection() {
        return this.viewer.getSelection();
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (viewer != null) {
            this.viewer.removeSelectionChangedListener(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        if (okToUse()) {
            if (!isLoading) {
                this.nextSelection = null;
                this.viewer.setSelection(selection);
            } else {
                this.nextSelection = selection;
            }
        }
    }
}
