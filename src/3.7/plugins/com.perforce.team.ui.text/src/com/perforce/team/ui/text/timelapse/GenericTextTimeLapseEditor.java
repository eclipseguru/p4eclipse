/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.text.TextUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class GenericTextTimeLapseEditor extends TextTimeLapseEditor {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.text.timelapse.GenericTextTimeLapseEditor"; //$NON-NLS-1$

    private TextFileDocumentProvider provider;
    private SourceViewer viewer;

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#getViewer()
     */
    @Override
    protected ITextViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#refresh(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void refresh(IEditorInput input) {
        try {
            provider.connect(input);
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
        this.viewer.setRedraw(false);

        IDocument currDoc = this.viewer.getDocument();
        if (currDoc != null) {
            currDoc.set(provider.getDocument(input).get());
        } else {
            currDoc = new Document(provider.getDocument(input).get());
            this.viewer.setDocument(currDoc);
        }
        this.viewer.setRedraw(true);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#dispose(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void dispose(IEditorInput input) {
        super.dispose(input);
        this.provider.disconnect(input);
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createViewer(Composite parent) {
        CompositeRuler ruler = configureRulers(true);
        viewer = new SourceViewer(parent, ruler, SWT.LEFT_TO_RIGHT
                | SWT.H_SCROLL | SWT.V_SCROLL);
        viewer.getTextWidget().setBackground(
                getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND));
        viewer.getTextWidget().setForeground(
                getEditorColor(AbstractTextEditor.PREFERENCE_COLOR_FOREGROUND));
        provider = new TextFileDocumentProvider();
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
        configureViewer(JFaceResources.TEXT_FONT);
    }

}
