/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.jobs;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.p4java.actions.EditJobAction;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JobsWidget {

    private Composite displayArea;
    private TreeViewer viewer;

    private boolean enableEdit;

    /**
     * @param enableEdit
     * 
     */
    public JobsWidget(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    /**
     * Get number of jobs currently displayed
     * 
     * @return - job count
     */
    public int getJobCount() {
        return this.viewer.getTree().getItemCount();
    }

    /**
     * Set input of changelist file widget
     * 
     * @param jobs
     */
    public void setInput(IP4Resource[] jobs) {
        if (jobs != null) {
            this.viewer.setInput(jobs);
        } else {
            this.viewer.setInput(PerforceContentProvider.EMPTY);
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
     * Create content provider
     * 
     * @param viewer
     * @return - non-null tree content provider
     */
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new PerforceContentProvider(viewer, true);
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

        this.viewer = new TreeViewer(this.displayArea, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL);
        if (filter != null) {
            this.viewer.addFilter(filter);
        }
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        this.viewer.getTree().setLayoutData(vData);
        this.viewer.setSorter(new ViewerSorter() {

            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
                    String id1 = ((IP4Job) e1).getId();
                    String id2 = ((IP4Job) e2).getId();
                    if (id1 != null) {
                        return id1.compareTo(id2);
                    }
                }
                return super.compare(viewer, e1, e2);
            }

        });
        this.viewer.setContentProvider(createContentProvider(this.viewer));
        this.viewer.setLabelProvider(new PerforceLabelProvider(false) {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IP4Job) {
                    IP4Job job = (IP4Job) element;
                    String desc = job.getShortDescription();
                    if (desc != null && desc.length() > 0) {
                        return job.getId() + " : " //$NON-NLS-1$
                                + P4CoreUtils.removeWhitespace(desc);
                    }
                }
                return super.getColumnText(element, columnIndex);
            }

        });

        if (this.enableEdit) {
            this.viewer.addDoubleClickListener(new IDoubleClickListener() {

                public void doubleClick(DoubleClickEvent event) {
                    IStructuredSelection select = (IStructuredSelection) viewer
                            .getSelection();
                    if (select.size() == 1) {
                        handleDoubleClick(select.getFirstElement());
                    }
                }
            });
        }
    }

    /**
     * Handle double click job opening
     * 
     * @param selected
     */
    protected void handleDoubleClick(Object selected) {
        if (selected instanceof IP4Job) {
            EditJobAction edit = new EditJobAction();
            edit.selectionChanged(null, new StructuredSelection(selected));
            edit.doubleClick(null);
        }
    }

    /**
     * Get tree viewer
     * 
     * @return - viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
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
     * Show a loading input element
     */
    public void showLoading() {
        IContentProvider provider = this.viewer.getContentProvider();
        if (provider instanceof PerforceContentProvider) {
            this.viewer.setInput(new PerforceContentProvider.Loading());
//                    .setInput(((PerforceContentProvider) provider).new Loading());
        }
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
     * Fill the toolbar with items
     * 
     * @param toolbar
     */
    protected void fillToolbar(ToolBar toolbar) {

    }

    /**
     * Is this widget's viewer usable?
     * 
     * @return - true is usable, false if null or disposed
     */
    public boolean okToUse() {
        return P4UIUtils.okToUse(this.viewer);
    }

}
