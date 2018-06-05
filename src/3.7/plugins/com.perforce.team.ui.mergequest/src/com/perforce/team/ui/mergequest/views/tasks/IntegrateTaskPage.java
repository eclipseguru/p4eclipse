/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;
import com.perforce.team.ui.submitted.OpenEditorAction;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateTaskPage extends Page implements
        ISelectionChangedListener, PropertyChangeListener, ISelectionListener {

    private Composite displayArea;
    private IntegrateTaskViewer viewer;
    private Mapping selectedMapping = null;
    private IBranchGraphEditor editor = null;
    private WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
    private HashSet<IBranchGraphPage> pages = new HashSet<IBranchGraphPage>();
    private boolean loaded = false;

    private Listener showListener = new Listener() {

        public void handleEvent(Event event) {
            if (!loaded) {
                loadCurrentSelection();
            }
        }
    };

    private IPartListener2 partListener = new IPartListener2() {

        public void partActivated(IWorkbenchPartReference partRef) {
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
        }

        public void partVisible(IWorkbenchPartReference partRef) {
            if (!loaded && partRef != null
                    && partRef.getPart(false) instanceof IntegrateTaskView) {
                loadCurrentSelection();
            }
        }

        public void partInputChanged(IWorkbenchPartReference partRef) {
        }

    };

    private IPageChangedListener pageListener = new IPageChangedListener() {

        public void pageChanged(PageChangedEvent event) {
            pageChange();
            updateSelection();
        }
    };

    /**
     * Create a new integrate task page
     * 
     * @param editor
     */
    public IntegrateTaskPage(IBranchGraphEditor editor) {
        this.editor = editor;
    }

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
        displayArea.setBackground(displayArea.getDisplay().getSystemColor(
                SWT.COLOR_WHITE));

        displayArea.addListener(SWT.Show, this.showListener);

        getSite().getPage().addPartListener(partListener);

        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer = new IntegrateTaskViewer(this.editor, editor.getProcessor());
        viewer.createControl(displayArea);

        registerMenu(viewer.getSourceViewer(), true);
        registerMenu(viewer.getTargetViewer(), false);

        loadCurrentSelection();
    }

    private void loadCurrentSelection() {
        new UIJob(Messages.IntegrateTaskPage_UpdatingTasks) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (P4UIUtils.okToUse(displayArea) && displayArea.isVisible()
                        && !loaded) {
                    loaded = true;
                    IBranchGraphPage page = editor.getActiveGraphPage();
                    if (page != null) {
                        pageChange();
                        updateSelection();
                    }
                    editor.addPageChangedListener(pageListener);
                    getSite().getPage().addSelectionListener(
                            IntegrateTaskPage.this);
                }
                return Status.OK_STATUS;
            }
        }.schedule();
    }

    private void pageChange() {
        IBranchGraphPage page = editor.getActiveGraphPage();
        if (page != null && !pages.contains(page)) {
            page.getSelectionProvider().addSelectionChangedListener(this);
            page.getGraph().addPropertyListener(this);
            pages.add(page);
        }
    }

    private void updateSelection() {
        IBranchGraphPage page = editor.getActiveGraphPage();
        if (page != null) {
            ISelectionProvider provider = page.getSelectionProvider();
            selectionChanged(new SelectionChangedEvent(provider,
                    provider.getSelection()));
        }
    }

    private void registerMenu(final TreeViewer viewer, final boolean reverse) {
        final Action customIntegrate = new Action(
                Messages.IntegrateTaskPage_IntegrateChangelist,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_INTEGRATE)) {

            @Override
            public void run() {
                Object element = ((IStructuredSelection) viewer.getSelection())
                        .getFirstElement();
                if (element instanceof IP4SubmittedChangelist) {
                    IP4SubmittedChangelist changelist = (IP4SubmittedChangelist) element;
                    IP4Connection connection = changelist.getConnection();
                    if (selectedMapping instanceof BranchSpecMapping) {
                        BranchSpecMapping mapping = (BranchSpecMapping) selectedMapping;
                        IP4Branch branch = mapping.generateBranch(connection);
                        IntegrateAction action = new IntegrateAction();
                        action.integrateBranch(branch, reverse, changelist);
                    } else if (selectedMapping instanceof DepotPathMapping) {
                        DepotPathMapping mapping = (DepotPathMapping) selectedMapping;
                        IntegrateAction action = new IntegrateAction();
                        String source = reverse
                                ? mapping.getTargetPath()
                                : mapping.getSourcePath();
                        String target = reverse
                                ? mapping.getSourcePath()
                                : mapping.getTargetPath();
                        action.integratePaths(connection, source, target,
                                changelist);
                    }
                }
            }
        };
        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(viewer.getControl());
        manager.setRemoveAllWhenShown(true);
        viewer.getControl().setMenu(menu);
        getSite().registerContextMenu("changelistPage", manager, viewer); //$NON-NLS-1$
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                manager.add(new Separator(
                        IWorkbenchActionConstants.MB_ADDITIONS));
                IContributionItem integrate = manager
                        .find("com.perforce.team.ui.integratechangelist"); //$NON-NLS-1$
                if (integrate != null) {
                    manager.remove(integrate);
                    manager.prependToGroup("perforce.group1", customIntegrate); //$NON-NLS-1$
                }
            }
        });

        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                for (Object select : ((IStructuredSelection) viewer
                        .getSelection()).toArray()) {
                    if (P4CoreUtils.convert(select,
                            IP4SubmittedChangelist.class) != null) {
                        ViewChangelistAction view = new ViewChangelistAction();
                        view.selectionChanged(null, new StructuredSelection(
                                select));
                        view.run(null);
                    } else if (P4CoreUtils.convert(select,
                            IP4SubmittedFile.class) != null) {
                        OpenEditorAction open = new OpenEditorAction();
                        open.selectionChanged(null, new StructuredSelection(
                                select));
                        open.run(null);
                    } else if (P4CoreUtils.convert(select, IP4Job.class) != null) {
                        EditJobAction edit = new EditJobAction();
                        edit.selectionChanged(null, new StructuredSelection(
                                select));
                        edit.doubleClick(null);
                    } else {
                        viewer.setExpandedState(select,
                                !viewer.getExpandedState(select));
                    }
                }
            }
        });
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return displayArea;
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        if (P4UIUtils.okToUse(displayArea)) {
            displayArea.setFocus();
            if (!loaded) {
                loadCurrentSelection();
            }
        }
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        for (IBranchGraphPage page : this.pages) {
            page.getSelectionProvider().removeSelectionChangedListener(this);
            page.getGraph().removePropertyListener(this);
        }
        if (P4UIUtils.okToUse(displayArea)) {
            displayArea.removeListener(SWT.Show, this.showListener);
        }
        this.editor.removePageChangedListener(pageListener);
        viewer.dispose();
        getSite().getPage().removeSelectionListener(this);
        getSite().getPage().removePartListener(this.partListener);
        super.dispose();
        provider.dispose();
    }

    private void refreshViewer() {
        if (this.selectedMapping != null) {
            viewer.setInput(this.selectedMapping);
        } else {
            viewer.setInput(null);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        selectionChanged(null, event.getSelection());
    }

    private void handleRemove(PropertyChangeEvent evt) {
        Mapping mapping = this.selectedMapping;
        if (mapping == null) {
            return;
        }
        Object old = evt.getOldValue();
        if (old == null) {
            return;
        }
        if (mapping.equals(old)) {
            this.selectedMapping = null;
            asyncRefresh();
        } else if (old instanceof Branch) {
            String id = ((Branch) old).getId();
            if (id.equals(mapping.getSourceId())
                    || id.equals(mapping.getTargetId())) {
                this.selectedMapping = null;
                asyncRefresh();
            }
        }
    }

    private void asyncRefresh() {
        PerforceUIPlugin.asyncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(displayArea)) {
                    refreshViewer();
                }
            }
        });
    }

    /**
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String property = evt.getPropertyName();
        if (IBranchGraph.ELEMENT_REMOVED.equals(property)) {
            handleRemove(evt);
        }
    }

    /**
     * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (!P4UIUtils.okToUse(displayArea)) {
            return;
        }
        if (part != null && !part.equals(this.editor)) {
            return;
        }
        if (selection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection) selection)
                    .getFirstElement();
            Mapping mapping = P4CoreUtils.convert(selected, Mapping.class);
            if (mapping != null) {
                this.selectedMapping = mapping;
                refreshViewer();
            }
        }
    }
}
