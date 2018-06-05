/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.processor.InterchangesProcessor;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.editor.palette.BranchGraphPalettePage;
import com.perforce.team.ui.mergequest.editor.palette.BranchGraphPaletteRoot;
import com.perforce.team.ui.mergequest.editor.palette.BranchGraphPaletteViewerProvider;
import com.perforce.team.ui.mergequest.editor.palette.IFlyoutPaletteProvider;
import com.perforce.team.ui.mergequest.figures.theme.FigureThemeHelper;
import com.perforce.team.ui.mergequest.parts.BranchGraphEditPartFactory;
import com.perforce.team.ui.mergequest.parts.BranchGraphViewer;
import com.perforce.team.ui.mergequest.parts.SharedResources;
import com.perforce.team.ui.mergequest.preferences.IPreferenceConstants;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.DefaultEditDomain;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.dnd.TemplateTransferDropTargetListener;
import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.palette.FlyoutPaletteComposite;
import org.eclipse.gef.ui.palette.PaletteViewerProvider;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.gef.ui.views.palette.PalettePage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphPage extends FormPage implements IBranchGraphPage,
        IPropertyChangeListener, IFlyoutPaletteProvider {

    private IBranchGraphBuilder builder;
    private IBranchGraph graph;
    private InterchangesProcessor processor;
    private Composite displayArea;
    private BranchGraphViewer graphViewer;
    private BranchGraphEditor editor;

    private ScalableFreeformRootEditPart rootEditPart;
    private EditDomain domain;
    private PaletteRoot root;
    private PaletteViewerProvider provider;
    private FlyoutPaletteComposite splitter;
    private BranchGraphPalettePage page;

    private SharedResources sharedResources = new SharedResources();

    private PropertyChangeListener builderListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (Branch.SOURCE_MAPPINGS.equals(property)) {
                boolean newMapping = false;
                Mapping mapping = (Mapping) evt.getNewValue();
                if (mapping == null) {
                    mapping = (Mapping) evt.getOldValue();
                } else {
                    newMapping = true;
                }
                if (newMapping) {
                    refreshStatus(mapping);
                }
            } else if (Mapping.DIRECTION.equals(property)) {
                Mapping mapping = (Mapping) evt.getSource();
                refreshStatus(mapping);
            } else if (Mapping.NAME.equals(property)) {
                if (evt.getSource() instanceof BranchSpecMapping) {
                    refreshStatus((Mapping) evt.getSource());
                }
            }
            editor.persist();
        }
    };

    /**
     * @param editor
     * @param graph
     * @param builder
     */
    public BranchGraphPage(BranchGraphEditor editor, IBranchGraph graph,
            IBranchGraphBuilder builder) {
        super(editor, "branchGraphPage" + graph.getName(), graph.getName()); //$NON-NLS-1$
        this.editor = editor;
        this.graph = graph;
        this.builder = builder;
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

    private void refreshStatus() {
        refreshStatus(graph.getMappings());
    }

    private void refreshStatus(Mapping mapping) {
        if (mapping != null) {
            refreshStatus(new Mapping[] { mapping });
        }
    }

    private void refreshStatus(Mapping[] mappings) {
        processor.refresh(mappings);
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) {
        super.init(site, input);
        this.processor = new InterchangesProcessor(this.editor, this.graph);
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
     * Set selection
     * 
     * @param selection
     */
    public void setSelection(ISelection selection) {
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        Composite parent = managedForm.getForm().getBody();
        GridLayout pLayout = new GridLayout(1, true);
        pLayout.marginHeight = 0;
        pLayout.marginWidth = 0;
        parent.setLayout(pLayout);
        this.displayArea = new Composite(parent, SWT.NONE);
        this.displayArea.setBackground(this.displayArea.getDisplay()
                .getSystemColor(SWT.COLOR_WHITE));
        this.displayArea.setBackgroundMode(SWT.INHERIT_DEFAULT);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        this.displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.displayArea.setLayoutData(daData);
        this.displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                sharedResources.dispose();
            }
        });

        createGraphicalViewer(this.displayArea);

        this.graph.addPropertyListener(builderListener);
        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .addPropertyChangeListener(this);
        refreshStatus();
    }

    private EditDomain getEditDomain() {
        if (domain == null) {
            domain = new DefaultEditDomain(this);
        }
        return domain;
    }

    private PaletteViewerProvider getPaletteViewerProvider() {
        if (provider == null) {
            provider = new BranchGraphPaletteViewerProvider(getEditDomain());
        }
        return provider;
    }

    private PaletteRoot getPaletteRoot() {
        if (root == null) {
            root = new BranchGraphPaletteRoot();
        }
        return root;
    }

    private void createGraphicalViewer(Composite parent) {
        splitter = new FlyoutPaletteComposite(parent, SWT.NONE, getSite()
                .getPage(), getPaletteViewerProvider(),
                BranchGraphPaletteViewerProvider.getPalettePreferences());
        splitter.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.graphViewer = new BranchGraphViewer();
        this.graphViewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING,
                new Dimension(10, 10));
        this.graphViewer.createControl(splitter);
        EditDomain domain = getEditDomain();
        domain.setPaletteRoot(getPaletteRoot());
        domain.getCommandStack().addCommandStackListener(
                this.editor.getRegistry());
        this.graphViewer.addSelectionChangedListener(this.editor.getRegistry());
        domain.addViewer(this.graphViewer);
        this.graphViewer.getControl().setBackground(parent.getBackground());

        this.graphViewer.setEditPartFactory(new BranchGraphEditPartFactory(
                this.graph, this.sharedResources));

        rootEditPart = new ScalableFreeformRootEditPart();
        this.graphViewer.setRootEditPart(rootEditPart);

        FigureThemeHelper bgHelper = new FigureThemeHelper();
        IFigure rootFigure = rootEditPart.getFigure();
        rootFigure.setOpaque(true);
        bgHelper.setFigure(rootFigure);
        bgHelper.setBackgroundKey(FigureThemeHelper.BG_PREFIX + "graph"); //$NON-NLS-1$
        bgHelper.activate();

        FigureThemeHelper gridHelper = new FigureThemeHelper();
        gridHelper.setFigure(rootEditPart.getLayer(LayerConstants.GRID_LAYER));
        gridHelper.setForegroundKey(FigureThemeHelper.FG_PREFIX + "grid"); //$NON-NLS-1$
        gridHelper.activate();

        this.graphViewer.setKeyHandler(new GraphicalViewerKeyHandler(
                this.graphViewer));
        ActionRegistry registry = P4CoreUtils.convert(this.editor,
                ActionRegistry.class);
        ContextMenuProvider cmProvider = new GraphContextMenuProvider(
                this.graph, this.sharedResources, this.graphViewer, registry);
        this.graphViewer.setContextMenu(cmProvider);
        getSite().registerContextMenu(cmProvider, this.graphViewer);
        this.graphViewer.setContents(this.graph);

        this.graphViewer
                .addDropTargetListener(new TemplateTransferDropTargetListener(
                        this.graphViewer));

        splitter.setGraphicalControl(this.graphViewer.getControl());
        if (page != null) {
            splitter.setExternalViewer(page.getPaletteViewer());
            page = null;
        }
        updateGrid();
        updateZoom();
        getSite().setSelectionProvider(this.graphViewer);
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        if (this.graphViewer != null) {
            this.graphViewer.dispose();
        }
        this.graph.removePropertyListener(this.builderListener);
        this.processor.dispose();
        P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .removePropertyChangeListener(this);
        CommandStack stack = getEditDomain().getCommandStack();
        if (stack != null) {
            stack.removeCommandStackListener(this.editor.getRegistry());
        }
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.displayArea.setFocus();
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#getProcessor()
     */
    public InterchangesProcessor getProcessor() {
        return this.processor;
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#getSelectionProvider()
     */
    public ISelectionProvider getSelectionProvider() {
        return this.graphViewer;
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#getGraph()
     */
    public IBranchGraph getGraph() {
        return this.graph;
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#getBuilder()
     */
    public IBranchGraphBuilder getBuilder() {
        return this.builder;
    }

    private void updateGrid() {
        boolean selected = P4BranchGraphPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_GRID);
        graphViewer.setProperty(SnapToGrid.PROPERTY_GRID_ENABLED, selected);
        graphViewer.setProperty(SnapToGrid.PROPERTY_GRID_VISIBLE, selected);
    }

    private void updateZoom() {
        String level = P4BranchGraphPlugin.getDefault().getPreferenceStore()
                .getString(IPreferenceConstants.ZOOM_LEVEL);
        // Fall back to 100% zoom if level is empty
        if (level.length() == 0) {
            level = "100%"; //$NON-NLS-1$
        }
        this.rootEditPart.getZoomManager().setZoomAsText(level);
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(org.eclipse.jface.util.PropertyChangeEvent event) {
        if (IPreferenceConstants.SHOW_GRID.equals(event.getProperty())) {
            updateGrid();
        } else if (IPreferenceConstants.ZOOM_LEVEL.equals(event.getProperty())) {
            updateZoom();
        }
    }

    private BranchGraphPalettePage createPalettePage() {
        return new BranchGraphPalettePage(getPaletteViewerProvider(), this);
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == CommandStack.class) {
            return getEditDomain().getCommandStack();
        } else if (adapter == PalettePage.class) {
            if (splitter == null) {
                page = createPalettePage();
                return page;
            }
            return createPalettePage();
        } else if (adapter == EditPartViewer.class
                || adapter == GraphicalViewer.class) {
            return this.graphViewer;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#getGraphSelectionProvider()
     */
    public ISelectionProvider getGraphSelectionProvider() {
        return this.graphViewer;
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.palette.IFlyoutPaletteProvider#getFlyout()
     */
    public FlyoutPaletteComposite getFlyout() {
        return this.splitter;
    }

    /**
     * @see com.perforce.team.ui.mergequest.editor.IBranchGraphPage#refresh()
     */
    public void refresh() {
        refreshStatus();
    }

}
