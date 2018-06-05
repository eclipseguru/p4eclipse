/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.NodeOutlinePage;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;

import java.util.Iterator;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.editor.CSourceViewer;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CTimeLapseEditor extends NodeModelTimeLapseEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.c.CTimeLapseEditor"; //$NON-NLS-1$

    private CSourceViewer viewer;
    private CFoldingProvider provider;

    /**
     * C time lapse editor
     */
    public CTimeLapseEditor() {
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeDecorator()
     */
    @Override
    protected NodeTickDecorator createNodeDecorator() {
        return new CTickDecorator(getModel());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#enableProjection()
     */
    @Override
    protected void enableProjection() {
        super.enableProjection();
        IVerticalRulerColumn column = null;
        CompositeRuler ruler = getRuler();
        if (ruler != null) {
            Iterator<?> columnIterator = ruler.getDecoratorIterator();
            while (columnIterator.hasNext()) {
                column = (IVerticalRulerColumn) columnIterator.next();
            }
            if (column != null && column instanceof AnnotationRulerColumn) {
                column.getControl().setBackground(
                        column.getControl().getDisplay()
                                .getSystemColor(SWT.COLOR_WHITE));
                column.getControl().redraw();
            }
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        super.fillToolbar(toolbar);
        configureLinking(toolbar,
                Messages.CTimeLapseEditor_LinkWithOutlineViewSelection);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#disableFolding()
     */
    @Override
    protected void disableFolding() {
        if (this.provider != null) {
            this.provider.clearModel();
        }
        super.disableFolding();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createOutlinePage()
     */
    @Override
    protected NodeOutlinePage createOutlinePage() {
        return new COutlinePage((ICElement) getRoot(), getModel());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#showNode(java.lang.Object)
     */
    @Override
    public void showNode(Object node) {
        super.showNode(node);
        if (node != null && !(node instanceof ICElement)) {
            return;
        }
        if (node instanceof IFunctionDeclaration) {
            String id = getModel().getHandle(node);
            getFormatter().setFilter(id);
            getDecorator().setNodeId(id);
        } else {
            getFormatter().setFilter(null);
            getDecorator().setNodeId(null);
        }
        if (node instanceof ISourceReference) {
            try {
                ISourceRange sourceRange = ((ISourceReference) node)
                        .getSourceRange();
                if (sourceRange != null) {
                    showRange(new Range(sourceRange.getStartPos(),
                            sourceRange.getLength()));
                    int selectOffset = sourceRange.getIdStartPos();
                    int selectLength = sourceRange.getIdLength();
                    if (selectOffset > -1 && selectLength > 0) {
                        selectRange(new Range(selectOffset, selectLength));
                    }
                }
            } catch (CModelException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#updateVisibleRange()
     */
    @Override
    protected void updateVisibleRange() {
        if (isLinking() && !isFiltering() && this.selected != null) {
            CNodeModel cModel = (CNodeModel) getModel();
            ICElement element = cModel.getCElement(getRevision(),
                    cModel.getHandle(this.selected));
            if (element != null) {
                if (this.outlinePage != null) {
                    if (!this.outlinePage.isDisposed()) {
                        this.outlinePage.setSelection(new StructuredSelection(
                                element));
                    } else {
                        this.outlinePage = null;
                    }
                }
                showNode(element);
                return;
            }
        }
        super.updateVisibleRange();
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#dispose(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void dispose(IEditorInput input) {
        super.dispose(input);
        CUtils.getProvider().disconnect(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#getViewer()
     */
    @Override
    protected ITextViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getFilterLabel()
     */
    @Override
    protected String getFilterLabel() {
        return Messages.CTimeLapseEditor_Functions;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getNodeLabel()
     */
    @Override
    protected String getNodeLabel() {
        return "function"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getRange(java.lang.String)
     */
    @Override
    protected Range getRange(String filterKey) {
        Range range = null;
        ICElement function = ((CNodeModel) getModel()).getCElement(
                getRevision(), filterKey);
        if (function instanceof ISourceReference) {
            try {
                ISourceRange sourceRange = ((ISourceReference) function)
                        .getSourceRange();
                if (sourceRange != null) {
                    range = new Range(sourceRange.getStartPos(),
                            sourceRange.getLength());
                }
            } catch (CModelException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return range;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#buildDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void buildDocument(IEditorInput input) {
        this.provider.install((ICElement) this.root);
        super.buildDocument(input);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createViewer(Composite parent) {
        CompositeRuler ruler = configureRulers(true);

        this.viewer = new CSourceViewer(parent, ruler, null, false,
                SWT.LEFT_TO_RIGHT | SWT.H_SCROLL | SWT.V_SCROLL, CUIPlugin
                        .getDefault().getCombinedPreferenceStore()) {

            @Override
            public void setTopIndex(int index) {
                if (!updatingDoc) {
                    super.setTopIndex(index);
                }
            }

            @Override
            public void setSelectedRange(int selectionOffset,
                    int selectionLength) {
                if (!updatingDoc) {
                    super.setSelectedRange(selectionOffset, selectionLength);
                }
            }
        };

        // Add projection support
        ProjectionSupport support = new ProjectionSupport(this.viewer,
                new DefaultMarkerAnnotationAccess(), getSharedTextColors());
        support.install();
        provider = new CFoldingProvider(viewer, support);

        CTextTools tools = CUIPlugin.getDefault().getTextTools();
        if (tools != null) {
            IPreferenceStore store = CUIPlugin.getDefault()
                    .getCombinedPreferenceStore();
            this.viewer.configure(new CSourceViewerConfiguration(tools
                    .getColorManager(), store, null,
                    ICPartitions.C_PARTITIONING));
        }
        configureViewer(PreferenceConstants.EDITOR_TEXT_FONT);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeModel()
     */
    @Override
    protected IFilterNodeModel createNodeModel() {
        return new CNodeModel(getRevisions(), this);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected IDocument getDocument(IEditorInput input) {
        return CUIPlugin.getDefault().getDocumentProvider().getDocument(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#installPartitioner(org.eclipse.jface.text.IDocument)
     */
    @Override
    protected void installPartitioner(IDocument document) {
        IDocumentPartitioner partitioner = CUtils.createPartitioner();
        if (document instanceof IDocumentExtension3) {
            IDocumentExtension3 extension3 = (IDocumentExtension3) document;
            extension3.setDocumentPartitioner(ICPartitions.C_PARTITIONING,
                    partitioner);
        } else {
            document.setDocumentPartitioner(partitioner);
        }
        partitioner.connect(document);
    }

}
