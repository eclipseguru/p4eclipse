/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.NodeOutlinePage;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;

import java.util.Iterator;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
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
public class JavaTimeLapseEditor extends NodeModelTimeLapseEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.java.JavaTimeLapseEditor"; //$NON-NLS-1$

    private JavaSourceViewer viewer;
    private JavaFoldingProvider provider;

    /**
     * Create new java time lapse editor
     */
    public JavaTimeLapseEditor() {
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeDecorator()
     */
    @Override
    protected NodeTickDecorator createNodeDecorator() {
        return new JavaTickDecorator(getModel());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeModel()
     */
    @Override
    protected IFilterNodeModel createNodeModel() {
        return new JavaNodeModel(getRevisions(), this);
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
                Messages.JavaTimeLapseEditor_LinkWithOutlineViewSelection);
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
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#showNode(java.lang.Object)
     */
    @Override
    public void showNode(Object node) {
        super.showNode(node);
        if (node != null && !(node instanceof IJavaElement)) {
            return;
        }
        if (node instanceof IMethod) {
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
                    showRange(new Range(sourceRange.getOffset(),
                            sourceRange.getLength()));
                }
                int offset = -1;
                int length = 0;
                if (node instanceof IMember) {
                    sourceRange = ((IMember) node).getNameRange();
                    if (sourceRange != null) {
                        offset = sourceRange.getOffset();
                        length = sourceRange.getLength();
                    }
                } else if (node instanceof ITypeParameter) {
                    sourceRange = ((ITypeParameter) node).getNameRange();
                    if (sourceRange != null) {
                        offset = sourceRange.getOffset();
                        length = sourceRange.getLength();
                    }
                } else if (node instanceof ILocalVariable) {
                    sourceRange = ((ILocalVariable) node).getNameRange();
                    if (sourceRange != null) {
                        offset = sourceRange.getOffset();
                        length = sourceRange.getLength();
                    }
                } else if (node instanceof IAnnotation) {
                    sourceRange = ((IAnnotation) node).getNameRange();
                    if (sourceRange != null) {
                        offset = sourceRange.getOffset();
                        length = sourceRange.getLength();
                    }
                }
                if (offset > -1 && length > 0) {
                    selectRange(new Range(offset, length));
                }
            } catch (JavaModelException e) {
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
            JavaNodeModel javaModel = (JavaNodeModel) getModel();
            IJavaElement element = javaModel.getJavaElement(getRevision(),
                    javaModel.getHandle(this.selected));
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
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createViewer(Composite parent) {
        CompositeRuler ruler = configureRulers(true);

        IPreferenceStore store = JavaUtils.getPreferences();

        this.viewer = new JavaSourceViewer(parent, ruler, null, false,
                SWT.LEFT_TO_RIGHT | SWT.H_SCROLL | SWT.V_SCROLL, store) {

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
        provider = new JavaFoldingProvider(viewer, support);

        JavaTextTools tools = JavaUtils.getTools();
        if (tools != null) {
            this.viewer.configure(new JavaSourceViewerConfiguration(tools
                    .getColorManager(), store, null,
                    IJavaPartitions.JAVA_PARTITIONING));
        }
        configureViewer(PreferenceConstants.EDITOR_TEXT_FONT);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#dispose(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void dispose(IEditorInput input) {
        super.dispose(input);
        JavaUtils.getProvider().disconnect(input);
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
        return Messages.JavaTimeLapseEditor_Methods;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getNodeLabel()
     */
    @Override
    protected String getNodeLabel() {
        return "method"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getRange(java.lang.String)
     */
    @Override
    protected Range getRange(String filterKey) {
        Range range = null;
        IJavaElement method = ((JavaNodeModel) getModel()).getJavaElement(
                getRevision(), filterKey);
        if (method instanceof ISourceReference) {
            try {
                ISourceRange sourceRange = ((ISourceReference) method)
                        .getSourceRange();
                if (sourceRange != null) {
                    range = new Range(sourceRange.getOffset(),
                            sourceRange.getLength());
                }
            } catch (JavaModelException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return range;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#buildDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void buildDocument(IEditorInput input) {
        this.provider.install((IJavaElement) this.root);
        super.buildDocument(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createOutlinePage()
     */
    @Override
    protected NodeOutlinePage createOutlinePage() {
        return new JavaOutlinePage((IJavaElement) getRoot(), getModel());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected IDocument getDocument(IEditorInput input) {
        return JavaUtils.getProvider().getDocument(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#installPartitioner(org.eclipse.jface.text.IDocument)
     */
    @Override
    protected void installPartitioner(IDocument document) {
        JavaTextTools tools = JavaUtils.getTools();
        if (tools != null) {
            tools.setupJavaDocumentPartitioner(document,
                    IJavaPartitions.JAVA_PARTITIONING);
        }
    }

}
