/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.outline;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.branches.EditBranchAction;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphOutlinePage extends Page implements
        IContentOutlinePage, IPageChangedListener {

    private TreeViewer viewer;
    private SelectionSynchronizer synchronizer;

    private ListenerList listeners;
    private IP4Connection connection;
    private IPageChangeProvider pageProvider;
    private IBranchGraph graph;
    private PropertyChangeListener builderListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (Branch.SOURCE_MAPPINGS.equals(property)) {
                Mapping mapping = (Mapping) evt.getNewValue();
                if (mapping == null) {
                    mapping = (Mapping) evt.getOldValue();
                }
                if (mapping != null) {
                	refreshBranch(mapping.getSource());
                    revealProxies(mapping);
                }
            } else if (Branch.TARGET_MAPPINGS.equals(property)) {
                Mapping mapping = (Mapping) evt.getNewValue();
                if (mapping == null) {
                    mapping = (Mapping) evt.getOldValue();
                }
                refreshBranch(mapping.getTarget());
            } else if (Branch.NAME.equals(property)
                    || Branch.TYPE.equals(property)) {
                viewer.refresh(evt.getSource());
            } else if (IBranchGraph.ELEMENT_ADDED.equals(property)
                    || IBranchGraph.ELEMENT_REMOVED.equals(property)) {
                viewer.refresh();
            }
        }
    };

    private void refreshBranch(Branch branch) {
        if (branch != null) {
            viewer.refresh(branch);
            viewer.expandToLevel(branch, 1);
        }
    }

    private void revealProxies(Mapping mapping) {
        MappingProxy[] proxies = MappingProxy.generateProxies(mapping);
        if (proxies.length > 0) {
            viewer.reveal(proxies[0]);
        }
    }

    /**
     * Create branch graph outline page
     * 
     * @param connection
     * @param pageProvider
     */
    public BranchGraphOutlinePage(IP4Connection connection,
            IPageChangeProvider pageProvider) {
        this.connection = connection;
        this.listeners = new ListenerList();
        this.pageProvider = pageProvider;
    }

    /**
     * Set input to outline page
     * 
     * @param page
     */
    public void setInput(IBranchGraphPage page) {
        unhookListeners();
        this.graph = page.getGraph();
        this.synchronizer.register(page);
        hookListener();
        if (!isDisposed()) {
            viewer.setInput(this.graph);
            viewer.expandToLevel(2);
        }

    }

    private void hookListener() {
        if (this.graph != null) {
            this.graph.addPropertyListener(this.builderListener);
        }
    }

    private void unhookListeners() {
        if (this.graph != null) {
            this.graph.removePropertyListener(this.builderListener);
        }
    }

    /**
     * @see org.eclipse.ui.part.Page#dispose()
     */
    @Override
    public void dispose() {
        unhookListeners();
        if (this.synchronizer != null) {
            this.synchronizer.dispose();
        }
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        this.viewer = new TreeViewer(parent, SWT.MULTI | SWT.V_SCROLL
                | SWT.H_SCROLL);
        this.synchronizer = new SelectionSynchronizer(this.viewer);
        this.viewer.setUseHashlookup(true);
        this.viewer.setAutoExpandLevel(2);
        new BranchMappingLabelProvider(this.viewer);
        this.viewer.setContentProvider(new BranchMappingContentProvider(
                connection, this.viewer) {

            @Override
            public Object[] getElements(Object element) {
                if (graph != null) {
                    return graph.getBranches();
                } else {
                    return EMPTY;
                }
            }

        });
        this.viewer.setSorter(new BranchGraphOutlineSorter());
        this.viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        this.viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                Object first = ((IStructuredSelection) event.getSelection())
                        .getFirstElement();
                Mapping mapping = P4CoreUtils.convert(first, Mapping.class);
                if (mapping instanceof BranchSpecMapping) {
                    BranchSpecMapping branchMapping = (BranchSpecMapping) mapping;
                    EditBranchAction edit = new EditBranchAction();
                    edit.selectionChanged(null, new StructuredSelection(
                            branchMapping.generateBranch(connection)));
                    edit.run(null);
                }
            }
        });

        for (Object listener : listeners.getListeners()) {
            this.viewer
                    .addSelectionChangedListener((ISelectionChangedListener) listener);
        }
        listeners.clear();

        MenuManager manager = new MenuManager();
        Menu menu = manager.createContextMenu(this.viewer.getTree());
        this.viewer.getTree().setMenu(menu);
        MappingProxySelectionProvider proxyProvider = new MappingProxySelectionProvider(
                this.viewer);
        getSite().setSelectionProvider(proxyProvider);
        getSite().registerContextMenu("branchGraphOutlinePage", manager, //$NON-NLS-1$
                proxyProvider);

        this.pageProvider.addPageChangedListener(this);
        parent.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                pageProvider
                        .removePageChangedListener(BranchGraphOutlinePage.this);
            }
        });
        Object selectedPage = this.pageProvider.getSelectedPage();
        if (selectedPage != null) {
            pageChanged(new PageChangedEvent(this.pageProvider, selectedPage));
        }

        addToolbarActions();
    }

    private void addToolbarActions() {
        Action collapseAction = new Action() {

            @Override
            public void run() {
                viewer.collapseAll();
            }
        };
        collapseAction
                .setToolTipText(Messages.BranchGraphOutlinePage_CollapseAll);
        collapseAction.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_COLLAPSE));

        getSite().getActionBars().getToolBarManager().add(collapseAction);
    }

    /**
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return this.viewer.getControl();
    }

    /**
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        this.viewer.getTree().setFocus();
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
        } else {
            this.listeners.remove(listener);
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection) {
        if (P4UIUtils.okToUse(viewer)) {
            viewer.setSelection(selection);
        }
    }

    /**
     * Is this page disposed?
     * 
     * @return true if disposed, false is okay to use
     */
    public boolean isDisposed() {
        return !P4UIUtils.okToUse(this.viewer);
    }

    /**
     * @see org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.eclipse.jface.dialogs.PageChangedEvent)
     */
    public void pageChanged(PageChangedEvent event) {
        if (event.getSelectedPage() instanceof IBranchGraphPage) {
            setInput((IBranchGraphPage) event.getSelectedPage());
        }
    }
}
