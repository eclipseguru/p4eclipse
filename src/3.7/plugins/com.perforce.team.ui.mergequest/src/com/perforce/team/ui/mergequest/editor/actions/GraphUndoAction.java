/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.actions;

import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.UndoAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphUndoAction extends UndoAction {

    private GraphEditorAction delegate;

    /**
     * @param editor
     */
    public GraphUndoAction(IBranchGraphEditor editor) {
        super(editor);
        this.delegate = new GraphEditorAction(editor, this);
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#dispose()
     */
    @Override
    public void dispose() {
        this.delegate.dispose();
        super.dispose();
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#getCommandStack()
     */
    @Override
    protected CommandStack getCommandStack() {
        return this.delegate.getCommandStack();
    }

}
