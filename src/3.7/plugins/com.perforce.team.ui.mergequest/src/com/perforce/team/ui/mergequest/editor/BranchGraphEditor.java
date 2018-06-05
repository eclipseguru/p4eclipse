/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.BranchGraphContainer;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.core.mergequest.processor.InterchangesProcessor;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ConnectionListener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.PreferenceBranchGraphBuilder;
import com.perforce.team.ui.mergequest.ReorderGraphDialog;
import com.perforce.team.ui.mergequest.editor.actions.GraphActionRegistry;
import com.perforce.team.ui.mergequest.editor.outline.BranchGraphOutlinePage;
import com.perforce.team.ui.mergequest.editor.palette.BranchGraphEditorPalettePage;
import com.perforce.team.ui.mergequest.preferences.IPreferenceConstants;
import com.perforce.team.ui.mergequest.wizards.io.ExportBranchGraphWizard;
import com.perforce.team.ui.mergequest.wizards.io.ImportBranchGraphWizard;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphEditor extends SharedHeaderFormEditor implements
        IBranchGraphEditor, IP4ConnectionListener {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.editors.mergequest"; //$NON-NLS-1$

    private static final String[] ZOOM_LEVELS = new String[] { "50%", "66%", //$NON-NLS-1$ //$NON-NLS-2$
            "75%", "100%", "125%", "150%", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    private IP4Connection connection;
    private IBranchGraphContainer graphContainer;
    private IBranchGraphBuilder builder;
    private Action addAction;
    private Action deleteAction;
    private Action renameAction;
    private Action reorderAction;
    private Action importAction;
    private Action exportAction;
    private Action gridAction;
    private Action refreshAction;
    private Action zoomAction;
    private IPropertyChangeListener prefListener;

    private GraphActionRegistry registry;
    private BranchGraphEditorPalettePage palettePage;
    private BranchGraphOutlinePage outlinePage;

    /**
     * Create a new branch graph editor
     */
    public BranchGraphEditor() {
    }

    /**
     * @see org.eclipse.ui.forms.editor.SharedHeaderFormEditor#dispose()
     */
    @Override
    public void dispose() {
        if (this.prefListener != null) {
            P4BranchGraphPlugin.getDefault().getPreferenceStore()
                    .removePropertyChangeListener(this.prefListener);
        }
        P4Workspace.getWorkspace().removeConnectionListener(this);
        super.dispose();
    }

    private void importGraphs() {
        ImportBranchGraphWizard wizard = new ImportBranchGraphWizard(connection);
        WizardDialog dialog = new WizardDialog(getContainer().getShell(),
                wizard);
        if (WizardDialog.OK == dialog.open()) {
            IBranchGraph[] graphs = wizard.getSelected();
            int lastIndex = -1;
            boolean imported = false;
            for (IBranchGraph graph : graphs) {
                graph = graphContainer.importGraph(graph);
                if (graph != null) {
                    graph.setConnection(connection);
                    imported = true;
                    lastIndex = createGraphPage(graph);
                }
            }
            if (imported) {
                persist();
            }
            if (lastIndex >= 0) {
                setActivePage(lastIndex);
            }
        }
    }

    private void exportGraphs() {
        ExportBranchGraphWizard wizard = new ExportBranchGraphWizard(
                graphContainer);
        WizardDialog dialog = new WizardDialog(getContainer().getShell(),
                wizard);
        dialog.open();
    }

    private void addNewPage() {
        InputDialog dialog = new InputDialog(getContainer().getShell(),
                Messages.BranchGraphEditor_EnterGraphName,
                Messages.BranchGraphEditor_GraphName, null,
                new IInputValidator() {

                    public String isValid(String newText) {
                        String message = null;
                        newText = newText.trim();
                        if (newText.length() == 0) {
                            message = ""; //$NON-NLS-1$
                        } else if (graphContainer.getGraphByName(newText) != null) {
                            message = Messages.BranchGraphEditor_GraphWithNameExists;
                        }
                        return message;
                    }
                });
        if (InputDialog.OK == dialog.open()) {
            String graphName = dialog.getValue();
            IBranchGraph graph = createGraph(graphName);
            if (graphContainer.add(graph)) {
                persist();
            }
            int index = createGraphPage(graph);
            if (index >= 0) {
                setActivePage(index);
            }
            updateTitle();
        }
    }

    private IBranchGraph createGraph(String name) {
        IBranchGraph graph = this.graphContainer.createGraph(null);
        graph.setName(name);
        graph.setConnection(this.connection);
        return graph;
    }

    private int createGraphPage(IBranchGraph graph) {
        int index = -1;
        IBranchGraphPage page = new BranchGraphPage(this, graph, this.builder);
        try {
            index = addPage(page);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
        return index;
    }

    private void deleteCurrentPage() {
        IBranchGraphPage page = getActiveGraphPage();
        if (page != null) {
            IBranchGraph graph = page.getGraph();
            if (graph != null
                    && P4ConnectionManager.getManager().openConfirm(
                            Messages.BranchGraphEditor_RemoveGraphTitle,
                            MessageFormat.format(
                                    Messages.BranchGraphEditor_RemoveGraph,
                                    graph.getName()))) {
                removePage(getActivePage());
                graphContainer.remove(graph);
                persist();
                updateTitle();
            }
        }
    }

    /**
     * Get active branch graph page
     * 
     * @return - branch graph page
     */
    public IBranchGraphPage getActiveGraphPage() {
        IFormPage page = getActivePageInstance();
        if (page instanceof IBranchGraphPage) {
            return (IBranchGraphPage) page;
        } else {
            return null;
        }
    }

    /**
     * Get active graph
     * 
     * @return - branch graph
     */
    public IBranchGraph getActiveGraph() {
        IBranchGraphPage page = getActiveGraphPage();
        return page != null ? page.getGraph() : null;
    }

    private void updateActionPreferences() {
        gridAction.setChecked(P4BranchGraphPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_GRID));
    }

    private void createActions() {
        this.prefListener = new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (IPreferenceConstants.SHOW_GRID.equals(event.getProperty())) {
                    updateActionPreferences();
                }
            }
        };

        addAction = new Action(Messages.BranchGraphEditor_AddGraph,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.GRAPH_ADD)) {

            @Override
            public void run() {
                addNewPage();
            }

        };

        deleteAction = new Action(
                Messages.BranchGraphEditor_DeleteGraph,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.GRAPH_DELETE)) {

            @Override
            public void run() {
                deleteCurrentPage();
            }

        };

        renameAction = new Action(Messages.BranchGraphEditor_RenameGraph) {

            @Override
            public void run() {
                renameCurrentPage();
            }

        };

        reorderAction = new Action(Messages.BranchGraphEditor_ReorderPages) {

            @Override
            public void run() {
                reorderPages();
            }

        };

        importAction = new Action(Messages.BranchGraphEditor_ImportGraphs,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.IMPORT)) {

            @Override
            public void run() {
                importGraphs();
            }

        };

        exportAction = new Action(Messages.BranchGraphEditor_ExportGraphs,
                P4BranchGraphPlugin
                        .getImageDescriptor(IP4BranchGraphConstants.EXPORT)) {

            @Override
            public void run() {
                exportGraphs();
            }

        };

        gridAction = new Action(Messages.BranchGraphEditor_ShowGridLines,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                P4BranchGraphPlugin.getDefault().getPreferenceStore()
                        .setValue(IPreferenceConstants.SHOW_GRID, isChecked());
            }
        };
        gridAction.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.GRID));
        gridAction.setChecked(P4BranchGraphPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_GRID));

        refreshAction = new Action(Messages.BranchGraphEditor_RefreshGraph,
                PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_REFRESH)) {

            @Override
            public void run() {
                IBranchGraphPage page = getActiveGraphPage();
                if (page != null) {
                    page.refresh();
                }
            }
        };

        zoomAction = new Action(Messages.BranchGraphEditor_SetZoomLevel,
                IAction.AS_DROP_DOWN_MENU) {

            @Override
            public void runWithEvent(Event event) {
                if (event.widget instanceof ToolItem) {
                    showZoomMenu((ToolItem) event.widget);
                }
            }

        };
        zoomAction.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.ZOOM));

        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this.prefListener);
    }

    private void showZoomMenu(ToolItem item) {
        Menu menu = new Menu(item.getParent());

        String currentValue = P4BranchGraphPlugin.getDefault()
                .getPreferenceStore()
                .getString(IPreferenceConstants.ZOOM_LEVEL);
        for (final String level : ZOOM_LEVELS) {
            MenuItem menuItem = new MenuItem(menu, SWT.CHECK);
            menuItem.setText(level);
            menuItem.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    P4BranchGraphPlugin.getDefault().getPreferenceStore()
                            .setValue(IPreferenceConstants.ZOOM_LEVEL, level);
                }

            });
            if (currentValue.equals(level)) {
                menuItem.setSelection(true);
            }
        }

        Rectangle bounds = item.getBounds();
        Point location = item.getParent().toDisplay(bounds.x,
                bounds.y + bounds.height);
        menu.setLocation(location);
        menu.setVisible(true);
    }

    private void reorderPages() {
        IBranchGraph current = getActiveGraph();
        List<IBranchGraph> currentOrder = new ArrayList<IBranchGraph>();
        for (Object page : pages) {
            if (page instanceof IBranchGraphPage) {
                currentOrder.add(((IBranchGraphPage) page).getGraph());
            }
        }
        BranchGraph[] ordered = currentOrder
                .toArray(new BranchGraph[currentOrder.size()]);
        ReorderGraphDialog reorder = new ReorderGraphDialog(getContainer()
                .getShell(), graphContainer.getGraphs());
        if (ReorderGraphDialog.OK == reorder.open()) {
            BranchGraph[] newOrder = reorder.getOrder();
            if (ordered.length == newOrder.length && !Arrays.equals(ordered,newOrder)) {
                graphContainer.setGraphs(newOrder);
                persist();
                while (getPageCount() > 0) {
                    removePage(getPageCount() - 1);
                }
                int select = 0;
                for (BranchGraph graph : newOrder) {
                    int index = createGraphPage(graph);
                    if (graph.equals(current)) {
                        select = index;
                    }
                }
                setActivePage(select);
            }
        }
    }

    private void renameCurrentPage() {
        IBranchGraphPage page = getActiveGraphPage();
        if (page == null) {
            return;
        }
        IBranchGraph graph = page.getGraph();
        final String currentName = graph.getName();
        InputDialog dialog = new InputDialog(getContainer().getShell(),
                Messages.BranchGraphEditor_RenameBranchGraph,
                Messages.BranchGraphEditor_GraphName, currentName,
                new IInputValidator() {

                    public String isValid(String newText) {
                        String message = null;
                        newText = newText.trim();
                        if (message == null && newText.length() == 0) {
                            message = Messages.BranchGraphEditor_EnterName;
                        }
                        if (message == null
                                && !currentName.equals(newText)
                                && graphContainer.getGraphByName(newText) != null) {
                            message = MessageFormat
                                    .format(Messages.BranchGraphEditor_GraphWithNameExists,
                                            newText);
                        }
                        return message;
                    }
                });
        if (InputDialog.OK == dialog.open()) {
            String newName = dialog.getValue().trim();
            if (graph.setName(newName)) {
                setPageText(getCurrentPage(), newName);
                persist();
            }
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.SharedHeaderFormEditor#createHeaderContents(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createHeaderContents(IManagedForm headerForm) {
        createActions();
        String prefix = Messages.BranchGraphEditor_BranchGraph;
        Image image = P4BranchGraphPlugin.getImageDescriptor(
                IP4BranchGraphConstants.BRANCH_GRAPH).createImage();
        Form form = headerForm.getForm().getForm();
        P4UIUtils.registerDisposal(form, image);
        form.setImage(image);

        IToolBarManager manager = form.getToolBarManager();
        manager.add(zoomAction);
        manager.add(refreshAction);
        manager.add(gridAction);
        manager.add(new Separator());
        manager.add(addAction);
        manager.add(deleteAction);
        manager.add(importAction);
        manager.add(exportAction);
        manager.update(true);

        form.setText(MessageFormat.format(Messages.BranchGraphEditor_Title,
                prefix, this.connection.getParameters().getPort()));
        getToolkit().decorateFormHeading(form);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        this.connection = P4CoreUtils.convert(input, IP4Connection.class);
        if (connection == null) {
            throw new PartInitException(
                    Messages.BranchGraphEditor_InputNotAdapted);
        }
        super.init(site, input);
        setPartName(input.getName());
        builder = new PreferenceBranchGraphBuilder(P4BranchGraphPlugin
                .getDefault().getPreferenceStore(), this.connection);
        try {
            graphContainer = builder.load();
        } catch (IOException e) {
            graphContainer = new BranchGraphContainer();
            PerforceProviderPlugin.logError(e);
        }
        this.registry = new GraphActionRegistry(this);
        this.registry.loadActions();
        P4Workspace.getWorkspace().addConnectionListener(this);
    }

    /**
     * Get registry
     * 
     * @return - registry
     */
    public GraphActionRegistry getRegistry() {
        return this.registry;
    }

    private BranchGraphOutlinePage createOutlinePage() {
        BranchGraphOutlinePage newOutlinePage = new BranchGraphOutlinePage(
                this.connection, this);
        return newOutlinePage;
    }

    private BranchGraphEditorPalettePage createPalettePage() {
        BranchGraphEditorPalettePage newPalletePage = new BranchGraphEditorPalettePage(
                this);
        return newPalletePage;
    }

    /**
     * @see org.eclipse.ui.part.MultiPageEditorPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IP4Connection.class || adapter == IP4Resource.class) {
            return this.connection;
        }
        if (ActionRegistry.class == adapter) {
            return this.registry.getAdapter(adapter);
        }
        if (adapter == IContentOutlinePage.class) {
            if (this.outlinePage == null || this.outlinePage.isDisposed()) {
                this.outlinePage = createOutlinePage();
            }
            return this.outlinePage;
        }
        if (adapter == PalettePage.class) {
            if (this.palettePage == null || this.palettePage.isDisposed()) {
                this.palettePage = createPalettePage();
            }
            return this.palettePage;
        }
        Object adapted = super.getAdapter(adapter);
        if (adapted == null) {
            IBranchGraphPage page = getActiveGraphPage();
            if (page != null) {
                adapted = page.getAdapter(adapter);
            }
        }
        return adapted;
    }

    private void updateTitle() {
        if (getPageCount() > 1) {
            String title = MessageFormat.format(
                    Messages.BranchGraphEditor_Title, getEditorInput()
                            .getName(), getPageCount());
            setPartName(title);
        } else {
            setPartName(getEditorInput().getName());
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#removePage(int)
     */
    @Override
    public void removePage(int pageIndex) {
        super.removePage(pageIndex);
        hookMenu();
    }

    private void hookMenu() {
        Menu menu = getContainer().getMenu();
        if (!P4UIUtils.okToUse(menu)) {
            MenuManager manager = new MenuManager();
            menu = manager.createContextMenu(getContainer());
            manager.setRemoveAllWhenShown(true);
            getContainer().setMenu(menu);
            manager.addMenuListener(new IMenuListener() {

                public void menuAboutToShow(IMenuManager manager) {
                    manager.add(addAction);
                    manager.add(deleteAction);
                    manager.add(renameAction);
                    manager.add(reorderAction);
                    manager.add(importAction);
                    manager.add(exportAction);
                    manager.add(gridAction);
                }
            });
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
     */
    @Override
    protected void addPages() {
        IBranchGraph[] graphs = graphContainer.getGraphs();
        if (graphs.length > 0) {
            for (IBranchGraph graph : graphs) {
                graph.setConnection(this.connection);
                createGraphPage(graph);
            }
        } else {
            IBranchGraph graph = createGraph("Page 1"); //$NON-NLS-1$
            graphContainer.add(graph);
            createGraphPage(graph);
        }
        updateTitle();
        hookMenu();
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @return - interchanges processor
     */
    public InterchangesProcessor getProcessor() {
    	IBranchGraphPage page = getActiveGraphPage();
    	if(page!=null)
    		return page.getProcessor();
    	else 
    		return null;
    }

    /**
     * Persist the editor
     */
    public void persist() {
        try {
            this.builder.persist(this.graphContainer);
        } catch (final IOException e) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    String message = MessageFormat.format(
                            Messages.BranchGraphEditor_SaveErrorMessage, e
                                    .getClass().getCanonicalName(), e
                                    .getLocalizedMessage());
                    P4ConnectionManager.getManager().openError(
                            P4UIUtils.getDialogShell(),
                            Messages.BranchGraphEditor_SaveErrorTitle, message);
                }
            });
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionAdded(com.perforce.team.core.p4java.IP4Connection)
     */
    public void connectionAdded(IP4Connection connection) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionChanged(com.perforce.team.core.p4java.IP4Connection,
     *      com.perforce.team.core.ConnectionParameters)
     */
    public void connectionChanged(IP4Connection connection,
            ConnectionParameters previousParams) {
        if (previousParams.equals(this.connection.getParameters())) {
            this.connection = connection;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionRemovalRequested(com.perforce.team.core.ConnectionParameters)
     */
    public void connectionRemovalRequested(ConnectionParameters params) {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionListener#connectionRemoved(com.perforce.team.core.p4java.IP4Connection)
     */
    public void connectionRemoved(IP4Connection connection) {
        if (this.connection.equals(connection)) {
            PerforceUIPlugin.asyncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(getContainer())) {
                        close(false);
                    }
                }
            });
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ConnectionProvider#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormEditor#createToolkit(org.eclipse.swt.widgets.Display)
     */
    @Override
    protected FormToolkit createToolkit(Display display) {
        return new BranchGraphToolkit(display);
    }

}