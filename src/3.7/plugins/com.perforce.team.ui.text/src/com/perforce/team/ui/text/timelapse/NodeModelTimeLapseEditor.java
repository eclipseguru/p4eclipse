/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import java.text.MessageFormat;
import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.timelapse.TimeLapseSlider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class NodeModelTimeLapseEditor extends NodeTimeLapseEditor {

    /**
     * BUILD
     */
    public static final String BUILD = "com.perforce.team.ui.text.timelapse.BUILD"; //$NON-NLS-1$

    /**
     * FILTER
     */
    public static final String FILTER = "com.perforce.team.ui.text.timelapse.FILTER"; //$NON-NLS-1$

    /**
     * TICK_CHANGE_COLOR
     */
    public static final String TICK_CHANGE_COLOR = "com.perforce.team.ui.text.timelapse.TICK_CHANGE_COLOR"; //$NON-NLS-1$

    private NodeTickFormatter formatter;
    private Color formatterBg = null;
    private NodeTickDecorator decorator;
    private IFilterNodeModel nodeModel;
    
    private boolean build = false;
    private boolean revFilter = false;

    protected ToolItem buildItem;
    protected ToolItem filterItem;
    
    /**
     * Node outline page
     */
    protected NodeOutlinePage outlinePage;

    /**
     * Selected node
     */
    protected Object selected = null;

    /**
     * Model root
     */
    protected Object root = null;

    /**
     * Create a node outline page for this editor. Sub-classes should override.
     * 
     * @return - node outline page
     */
    protected NodeOutlinePage createOutlinePage() {
        return null;
    }

    /**
     * Show a node in the editor, sub-classes should override to link outline
     * selections to showing in editor. The default implementation stores the
     * node in the {@link #selected} variable.
     * 
     * @param node
     */
    protected void showNode(Object node) {
        this.selected = node;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#updateVisibleRange()
     */
    @Override
    protected void updateVisibleRange() {
        // Clear out current node if updating and not in linking mode and there
        // exists a currently selected node
        if (!isLinking() && this.outlinePage != null && this.selected != null) {
            if (!this.outlinePage.isDisposed()) {
                this.outlinePage.setSelection(StructuredSelection.EMPTY);
            } else {
                this.outlinePage = null;
            }
            showNode(null);
        }
        super.updateVisibleRange();
    }

    /**
     * Create the node model to use for this editor
     * 
     * @return - non-null node model
     */
    protected abstract IFilterNodeModel createNodeModel();

    /**
     * Get the node label
     * 
     * @return - non-null label for nodes
     */
    protected abstract String getNodeLabel();

    /**
     * Create the node tick decorator for this editor. Sub-classes should
     * override
     * 
     * @return - node tick decorator
     */
    protected NodeTickDecorator createNodeDecorator() {
        return null;
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IContentOutlinePage.class) {
            if (this.outlinePage == null || this.outlinePage.isDisposed()) {
                this.outlinePage = createOutlinePage();
                if (this.outlinePage != null) {
                    this.outlinePage.setRevision(getRevision());
                    this.outlinePage
                            .addSelectionChangedListener(new ISelectionChangedListener() {

                                public void selectionChanged(
                                        SelectionChangedEvent event) {
                                    if (isLoading()) {
                                        return;
                                    }
                                    if (event.getSelection() instanceof IStructuredSelection) {
                                        Object first = ((IStructuredSelection) event
                                                .getSelection())
                                                .getFirstElement();
                                        if (first != null) {
                                            showNode(first);
                                            updateSliderFilter();
                                        } else if (!isFiltering()
                                                && !isSettingInput()
                                                && !outlinePage.isLoading()) {
                                            showNode(null);
                                            updateSliderFilter();
                                        }
                                    }
                                }
                            });
                }
            }
            return this.outlinePage;
        }
        return super.getAdapter(adapter);
    }

    /**
     * Install any needed partitioner to the document. Does nothing by default,
     * sub-classes should override
     * 
     * @param document
     */
    protected void installPartitioner(IDocument document) {

    }

    /**
     * Get a document for an input
     * 
     * @param input
     * 
     * @return - document of content from input
     */
    protected abstract IDocument getDocument(IEditorInput input);

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#buildDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void buildDocument(IEditorInput input) {
        IDocument document = getDocument(input);
        ITextViewer viewer = getViewer();
        disableProjection();
        setRedraw(false);
        IDocument currDoc = viewer.getDocument();
        if (currDoc != null) {
            currDoc.set(document.get());
        } else {
            currDoc = new Document(document.get());
            installPartitioner(currDoc);
            if (viewer instanceof ISourceViewer) {
                ((ISourceViewer) viewer).setDocument(currDoc,
                        new AnnotationModel());
            } else {
                viewer.setDocument(currDoc);
            }
        }
        setRedraw(true);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        setBuild(store.getBoolean(BUILD));
        this.revFilter = store.getBoolean(FILTER);
        this.formatterBg = new Color(P4UIUtils.getDisplay(),
                PreferenceConverter.getColor(store, TICK_CHANGE_COLOR));
        P4UIUtils.registerDisposal(getControl(), this.formatterBg);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#dispose()
     */
    @Override
    public void dispose() {
        if (this.nodeModel != null) {
            this.nodeModel.clear();
        }
        super.dispose();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        super.fillToolbar(toolbar);

        addBuildItem(toolbar);
        addFilterItem(toolbar);
    }

    protected void addBuildItem(ToolBar toolbar) {
        buildItem = new ToolItem(toolbar, SWT.CHECK);

        Image buildImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_BUILD).createImage();
        P4UIUtils.registerDisposal(buildItem, buildImage);
        buildItem.setImage(buildImage);
        buildItem.setToolTipText(MessageFormat.format(Messages.TextTimeLapseEditor_GenerateHistoryByAnalyzing, captilize(getNodeLabel())));
        buildItem.setSelection(isBuild());
        buildItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setBuild(buildItem.getSelection());
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(BUILD, isBuild());
                filterItem.setEnabled(isBuild());
                filterItem.setSelection(revFilter && isBuild());
                preserveSelectionLoad();
            }

        });

	}

    private String captilize(String text) {
    	if(text.isEmpty())
    		return text;
    	text=text.trim();
    	
    	StringBuilder sb=new StringBuilder();
		for(int i=0;i<text.length();i++){
			if(i==0)
				sb.append(Character.toUpperCase(text.charAt(0)));
			else
				sb.append(text.charAt(i));
		}

		return sb.toString();
	}

	protected void addFilterItem(ToolBar toolbar) {
        filterItem = new ToolItem(toolbar, SWT.CHECK);
        Image filterImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_FILTER).createImage();
        P4UIUtils.registerDisposal(filterItem, filterImage);
        filterItem.setImage(filterImage);
        filterItem
                .setToolTipText(Messages.NodeModelTimeLapseEditor_FilterSliderTickMarksBySelection);
        filterItem.setSelection(this.revFilter && isBuild());
        filterItem.setEnabled(isBuild());
        filterItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                revFilter = filterItem.getSelection();
                formatter.setFilter(revFilter);
                formatter.filter();
                updateSliderFilter();
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(FILTER, revFilter);
            }

        });

    }
    
	/**
     * Update the slider filter
     */
    protected void updateSliderFilter() {
        TimeLapseSlider slider = getSlider();
        if (slider != null) {
            if (!slider.filter(false)) {
                slider.redraw();
                slider.update();
                slider.updateActions();
            }
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#buildTree(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void buildTree(IEditorInput input) {
        INodeModel model = getModel();
        IP4Revision revision = getRevision();
        if (model != null) {
            if (!isBuild()) {
                model.clear();
            }
            this.root = model.parseInput(input, revision);
            // Don't re-build if we have already built all revisions
            if (this.root != null && !isBuild()) {
                model.findNodes(this.root, revision);
            }
            if (decorator != null) {
                decorator.setModel(model);
            }
        }
        if (this.outlinePage != null && !this.outlinePage.isDisposed()) {
            this.outlinePage.setRevision(revision);
            this.outlinePage.setModel(model);
            this.outlinePage.setRoot(this.root);
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#updateFilterEntries()
     */
    @Override
    protected void updateFilterEntries() {
        if (!isBuild()) {
            super.updateFilterEntries();
        }
    }

    /**
     * Get root node of model
     * 
     * @return - root
     */
    protected Object getRoot() {
        return this.root;
    }

    /**
     * Get the node model
     * 
     * @return - node model
     */
    protected IFilterNodeModel getModel() {
        return this.nodeModel;
    }

    /**
     * Get the node tick formatter
     * 
     * @return - node tick formatter
     */
    protected NodeTickFormatter getFormatter() {
        return this.formatter;
    }

    /**
     * Get the node tick decorator
     * 
     * @return - node tick decorator
     */
    protected NodeTickDecorator getDecorator() {
        return this.decorator;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getFilters()
     */
    @Override
    protected Collection<String> getFilters() {
        return this.nodeModel.getFilterLabels();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getKey(java.lang.String)
     */
    @Override
    protected String getKey(String filterText) {
        return this.nodeModel.getFilterKey(filterText);
    }

    /**
     * Build the node model
     */
    protected void build() {
        nodeModel.buildAll();
        TimeLapseSlider slider = getSlider();
        if (slider != null) {
            slider.setDecorator(decorator);
            slider.setPositioner(formatter);
            slider.setFormatter(formatter);
        }
    }

    /**
     * Is revision filtering enabled?
     * 
     * @return - true if filtering revisions, false otherwise
     */
    protected boolean isRevisionFiltering() {
        return this.revFilter;
    }

    /**
     * Is the node model in build all mode?
     * 
     * @return - true if in build all mode, false otherwise
     */
    protected boolean isBuild() {
        return this.build;
    }
    
    protected void setBuild(boolean build){
    	this.build=build;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#configureSlider(com.perforce.team.ui.timelapse.TimeLapseSlider)
     */
    @Override
    protected void configureSlider(TimeLapseSlider slider) {
        if (isBuild()) {
            slider.setDecorator(decorator);
            slider.setPositioner(formatter);
            slider.setFormatter(formatter);
        } else {
            slider.setPositioner(null);
            slider.setFormatter(null);
            super.configureSlider(slider);
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#preUpdateEditor()
     */
    @Override
    protected void preUpdateEditor() {
        super.preUpdateEditor();
        // This was moved from loadEditor since decoration may involve the
        // UI-thread and so the decorator is created and configured here since
        // this method is always invoked on the ui-thread post-editor load.
        String decorateId = decorator != null ? decorator.getNodeId() : null;
        if (decorator != null) {
            decorator.dispose();
        }
        decorator = createNodeDecorator();
        if (decorator != null) {
            decorator.setNodeId(decorateId);
        }
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#loadEditor(com.perforce.team.core.p4java.IP4File,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected void loadEditor(IP4File file, IProgressMonitor monitor) {
        super.loadEditor(file, monitor);
        if (nodeModel != null) {
            nodeModel.clear();
        }
        nodeModel = createNodeModel();
        String formatId = formatter != null ? formatter.getId() : null;
        formatter = new NodeTickFormatter(nodeModel);
        formatter.setColor(this.formatterBg);
        formatter.setFilter(this.revFilter);
        if (isBuild()) {
            monitor.setTaskName("Parsing each revision for: "
                    + file.getActionPath());
            build();
            formatter.setFilter(formatId);
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#filter(boolean)
     */
    @Override
    protected void filter(boolean newFilter) {
        if (this.outlinePage != null && newFilter && getFilter() != null) {
            this.selected = null;
            this.outlinePage.setSelection(StructuredSelection.EMPTY);
        }
        super.filter(newFilter);
        String filter = getFilter();
        boolean redraw = true;
        if (newFilter) {
            formatter.setFilter(filter);
            if (decorator != null) {
                decorator.setNodeId(filter);
            }
            redraw = !getSlider().filter(false);
        }
        if (redraw) {
            if (newFilter || filter != null) {
                getSlider().redraw();
                getSlider().update();
            }
            getSlider().updateActions();
        }
    }

}
