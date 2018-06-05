/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.actions;

import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphActionRegistry extends PlatformObject implements
        CommandStackListener, ISelectionChangedListener {

    private ActionRegistry registry;
    private List<String> updateActions;
    private List<String> selectActions;
    private IBranchGraphEditor editor;

    /**
     * Create a graph action registry
     * 
     * @param editor
     */
    public GraphActionRegistry(IBranchGraphEditor editor) {
        this.registry = new ActionRegistry();
        this.updateActions = new ArrayList<String>();
        this.selectActions = new ArrayList<String>();
        this.editor = editor;
    }

    /**
     * Load the actions
     */
    public void loadActions() {
        UndoAction undo = new GraphUndoAction(this.editor);
        this.registry.registerAction(undo);
        this.updateActions.add(undo.getId());

        RedoAction redo = new GraphRedoAction(this.editor);
        this.registry.registerAction(redo);
        this.updateActions.add(redo.getId());

        DeleteAction delete = new GraphDeleteAction(this.editor);
        this.registry.registerAction(delete);
        this.selectActions.add(delete.getId());
    }

    /**
     * @see org.eclipse.gef.commands.CommandStackListener#commandStackChanged(java.util.EventObject)
     */
    public void commandStackChanged(EventObject event) {
        updateActions(this.updateActions);
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == ActionRegistry.class) {
            return this.registry;
        }
        return super.getAdapter(adapter);
    }

    private void updateActions(List<String> ids) {
        for (String id : ids) {
            IAction action = registry.getAction(id);
            if (action instanceof UpdateAction) {
                ((UpdateAction) action).update();
            }
        }
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        updateActions(this.selectActions);
    }
}
