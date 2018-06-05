/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.text.TextUtils;
import com.perforce.team.ui.text.timelapse.IFilterNodeModel;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.NodeOutlinePage;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;
import com.perforce.team.ui.timelapse.TimeLapseInput;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormTimeLapseEditor extends NodeModelTimeLapseEditor {

    /**
     * HIDE_COMMENTS
     */
    public static final String HIDE_COMMENTS = "com.perforce.team.ui.text.timelapse.form.HIDE_COMMENTS"; //$NON-NLS-1$

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.text.timelapse.form.FormTimeLapseEditor"; //$NON-NLS-1$

    private TextFileDocumentProvider provider;
    private ProjectionViewer viewer;

    private boolean hideComments = true;

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        super.fillToolbar(toolbar);
        configureLinking(toolbar,
                Messages.FormTimeLapseEditor_LinkWithOutlineViewSelection);

        final ToolItem showComments = new ToolItem(toolbar, SWT.CHECK);
        showComments
                .setToolTipText(Messages.FormTimeLapseEditor_DisplayComments);
        Image commentImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_COMMENT).createImage();
        P4UIUtils.registerDisposal(showComments, commentImage);
        showComments.setImage(commentImage);
        showComments.setSelection(!hideComments);
        showComments.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                hideComments = !showComments.getSelection();
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(HIDE_COMMENTS, hideComments);
                filter(false);
            }

        });
    }

    @Override
    protected void addBuildItem(ToolBar toolbar){
    }
    
    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#createTextAnnotateModel(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected ITextAnnotateModel createTextAnnotateModel(IP4File file) {
        return new FormTextAnnotateModel(file, null,
                ((TimeLapseInput) getEditorInput()).useChangelistKeys());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        this.hideComments = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore().getBoolean(HIDE_COMMENTS);
        super.init(site, input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#showEntireDocument()
     */
    @Override
    protected void showEntireDocument() {
        if (hideComments) {
            FormNodeModel formModel = (FormNodeModel) getModel();
            FormField element = formModel.getFirstFormField(getRevision());
            if (element != null) {
                int length = getViewer().getDocument().getLength();
                getViewer().setVisibleRegion(element.getOffset(),
                        length - element.getOffset());
            }
        } else {
            super.showEntireDocument();
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#showNode(java.lang.Object)
     */
    @Override
    public void showNode(Object node) {
        super.showNode(node);
        if (node instanceof FormField) {
            FormField field = (FormField) node;
            String id = getModel().getHandle(node);
            getFormatter().setFilter(id);
            getDecorator().setNodeId(id);
            showRange(new Range(field.getOffset(), field.getLength()));
            selectRange(new Range(field.getOffset(), field.getName().length()));
        } else {
            getFormatter().setFilter(null);
            getDecorator().setNodeId(null);
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#updateVisibleRange()
     */
    @Override
    protected void updateVisibleRange() {
        if (isLinking() && !isFiltering() && this.selected != null) {
            FormNodeModel formModel = (FormNodeModel) getModel();
            FormField element = formModel.getFormField(
                    formModel.getHandle(this.selected), getRevision());
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
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createOutlinePage()
     */
    @Override
    protected NodeOutlinePage createOutlinePage() {
        return new FormOutlinePage((Form) getRoot(), getModel());
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeDecorator()
     */
    @Override
    protected NodeTickDecorator createNodeDecorator() {
        FormTickDecorator decorator = new FormTickDecorator(getModel());
        decorator
                .setAuthorProvider(((FormTextAnnotateModel) getAnnotateModel()));
        return decorator;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#createNodeModel()
     */
    @Override
    protected IFilterNodeModel createNodeModel() {
        FormNodeModel model = new FormNodeModel(getRevisions(), this);
        ((FormTextAnnotateModel) getAnnotateModel())
                .setNodeAuthorProvider(model);
        return model;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getDocument(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected IDocument getDocument(IEditorInput input) {
        try {
            provider.connect(input);
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
        return provider.getDocument(input);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor#getNodeLabel()
     */
    @Override
    protected String getNodeLabel() {
        return "field"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getFilterLabel()
     */
    @Override
    protected String getFilterLabel() {
        return Messages.FormTimeLapseEditor_Fields;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTimeLapseEditor#getRange(java.lang.String)
     */
    @Override
    protected Range getRange(String filterKey) {
        Range range = null;
        FormField field = ((FormNodeModel) getModel()).getFormField(filterKey,
                getRevision());
        if (field != null) {
            range = new Range(field.getOffset(), field.getLength());
        }
        return range;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#getViewer()
     */
    @Override
    protected ITextViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createViewer(Composite parent) {
        CompositeRuler ruler = configureRulers(true);
        viewer = new ProjectionViewer(parent, ruler, null, false,
                SWT.LEFT_TO_RIGHT | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTextWidget().setBackground(
                getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        viewer.getTextWidget().setForeground(
                getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));

        if (!TextUtils
                .getEditorStore()
                .getBoolean(
                        AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND_SYSTEM_DEFAULT)) {
            viewer.getTextWidget()
                    .setSelectionBackground(
                            getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_BACKGROUND));
        }
        if (!TextUtils
                .getEditorStore()
                .getBoolean(
                        AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND_SYSTEM_DEFAULT)) {
            viewer.getTextWidget()
                    .setSelectionForeground(
                            getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_SELECTION_FOREGROUND));
        }
        this.provider = new TextFileDocumentProvider();
        configureViewer(JFaceResources.TEXT_FONT);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#dispose(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void dispose(IEditorInput input) {
        super.dispose(input);
        this.provider.disconnect(input);
    }

    @Override
    protected boolean isBuild() {
        return true;
    }
}
