/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.processor.InterchangesProcessor;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart3;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraphPage extends IFormPage, IEditorPart,
        IWorkbenchPart3 {

    /**
     * Get interchanges processor
     * 
     * @return - processor
     */
    InterchangesProcessor getProcessor();

    /**
     * Get graph
     * 
     * @return - branch graph
     */
    IBranchGraph getGraph();

    /**
     * Get selection provider
     * 
     * @return - provider
     */
    ISelectionProvider getSelectionProvider();

    /**
     * Get graph selection provider
     * 
     * @return - selection provider
     */
    ISelectionProvider getGraphSelectionProvider();

    /**
     * Get control
     * 
     * @return - composite
     */
    Composite getControl();

    /**
     * Set selection
     * 
     * @param selection
     */
    void setSelection(ISelection selection);

    /**
     * Get branch graph builder
     * 
     * @return - builder
     */
    IBranchGraphBuilder getBuilder();

    /**
     * Refresh the page
     */
    void refresh();

}
