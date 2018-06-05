/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.DecoratedChangelistLabelProvider;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ChangelistFixWidget implements IFixEditorPart,
        IP4Listener {

    private Composite displayArea;

    /**
     * Changelist viewer
     */
    protected TreeViewer viewer = null;

    /**
     * Job id
     */
    protected String id = null;

    /**
     * Changelists
     */
    protected Set<IP4Changelist> lists = null;

    private boolean enableEdit = false;
    private Runnable callback = null;

    /**
     * Changelist fix widget
     * 
     * @param enableEdit
     * @param id
     */
    public ChangelistFixWidget(boolean enableEdit, String id) {
        this.enableEdit = enableEdit;
        this.id = id;
        this.lists = new HashSet<IP4Changelist>(0);
    }

    /**
     * @param callback
     *            the callback to set
     */
    public void setCallback(Runnable callback) {
        this.callback = callback;
    }

    /**
     * Create a toolbar for this widget
     * 
     * @param parent
     */
    protected void createToolbar(Composite parent) {
        ToolBar toolbar = new ToolBar(parent, SWT.FLAT);
        fillToolbar(toolbar);
    }

    /**
     * Fill the toolbar with items
     * 
     * @param toolbar
     */
    protected void fillToolbar(ToolBar toolbar) {
        ToolItem expand = new ToolItem(toolbar, SWT.PUSH);
        Image expandImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_EXPAND_ALL).createImage();
        P4UIUtils.registerDisposal(expand, expandImage);
        expand.setImage(expandImage);
        expand.setToolTipText(Messages.ChangelistFixWidget_ExpandAll);
        expand.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.expandAll();
            }

        });

        ToolItem collapse = new ToolItem(toolbar, SWT.PUSH);
        collapse.setToolTipText(Messages.ChangelistFixWidget_CollapseAll);
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

    private void createFixArea(Composite parent) {
        viewer = new TreeViewer(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL);
        viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setLabelProvider(DecoratedChangelistLabelProvider.create());
        viewer.setContentProvider(createContentProvider(viewer));

        if (enableEdit) {
            viewer.addDoubleClickListener(new IDoubleClickListener() {

                public void doubleClick(DoubleClickEvent event) {
                    ISelection selection = viewer.getSelection();

                    ViewChangelistAction view = new ViewChangelistAction();
                    view.selectionChanged(null, selection);
                    view.run(null);

                    EditJobAction edit = new EditJobAction();
                    edit.selectionChanged(null, selection);
                    edit.doubleClick(null);

                    openEditor(selection);
                }
            });
        }

        configureViewer(viewer);
    }

    /**
     * Open files in the selection in an editor
     * 
     * @param selection
     */
    protected abstract void openEditor(ISelection selection);

    /**
     * Create content provider to use
     * 
     * @param viewer
     * @return - content provider
     */
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new PerforceContentProvider(viewer, true);
    }

    /**
     * Configure the changelist viewer
     * 
     * @param viewer
     */
    protected void configureViewer(TreeViewer viewer) {
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (id != null && element instanceof IP4Job) {
                    IP4Job job = (IP4Job) element;
                    return !id.equals(job.getId());
                }
                return true;
            }
        });
    }

    /**
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        createToolbar(displayArea);
        createFixArea(displayArea);
        P4Workspace.getWorkspace().addListener(this);
        displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                P4Workspace.getWorkspace().removeListener(
                        ChangelistFixWidget.this);
            }
        });
    }

    /**
     * Get the viewer for this widget
     * 
     * @return - viewer
     */
    public TreeViewer getViewer() {
        return this.viewer;
    }

    /**
     * Show a loading node in the tree viewer
     */
    public void showLoading() {
        if (P4UIUtils.okToUse(this.viewer)) {
            IContentProvider provider = this.viewer.getContentProvider();
            if (provider instanceof PerforceContentProvider) {
                this.viewer.setInput(new PerforceContentProvider.Loading());
//                        .setInput(((PerforceContentProvider) provider).new Loading());
            }
        }
    }

    private boolean isMatchingJob(IP4Job job) {
        return this.id.equals(job.getId());
    }

    /**
     * Is the resource an instanceof {@link IP4Changelist} or a extending
     * interface of {@link IP4Changelist} valid for this widget?
     * 
     * @param resource
     * @return - true is valid, false otherwise
     */
    protected abstract boolean isValid(IP4Resource resource);

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(P4Event event) {
        final EventType type = event.getType();
        if (type == EventType.FIXED || type == EventType.UNFIXED) {
            for (final IP4Job job : event.getJobs()) {
                if (isMatchingJob(job)) {
                    UIJob refresh = new UIJob(
                            Messages.ChangelistFixWidget_UpdatingChangelists) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (!P4UIUtils.okToUse(viewer)) {
                                return Status.CANCEL_STATUS;
                            }
                            IP4Container parent = job.getParent();
                            if (isValid(parent)) {
                                if (type == EventType.FIXED) {
                                    lists.add((IP4Changelist) parent);
                                } else if (type == EventType.UNFIXED) {
                                    lists.remove(parent);
                                }
                                viewer.refresh();
                                if (callback != null) {
                                    callback.run();
                                }
                            }
                            return Status.OK_STATUS;
                        }

                    };
                    refresh.schedule();
                    break;
                }
            }
        }
    }

    /**
     * @see com.perforce.team.ui.mylyn.editor.IFixEditorPart#setFixes(com.perforce.team.core.p4java.IP4Changelist[])
     */
    public int setFixes(IP4Changelist[] fixes) {
        Set<IP4Changelist> changelists = new HashSet<IP4Changelist>();
        for (IP4Changelist fix : fixes) {
            if (isValid(fix)) {
                changelists.add(fix);
            }
        }
        lists = changelists;
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(viewer)) {
                    viewer.setInput(lists);
                }
            }
        });
        return lists.size();
    }

    /**
     * Get the number of changelists displayed
     * 
     * @return - number of changelists
     */
    public int getSize() {
        return this.lists.size();
    }

    /**
     * Does this changelist widget contain the specified changelist?
     * 
     * This method can be called from any thread since it only checks the
     * backing model and not the content of any UI elements.
     * 
     * @param list
     * @return - true if contains specified list, false otherwise
     */
    public boolean contains(IP4Changelist list) {
        return list != null ? this.lists.contains(list) : null;
    }
    
    public String getName() {
    	return ChangelistFixWidget.class.getSimpleName();
    }
}
