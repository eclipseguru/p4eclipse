/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.actions;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;

import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.actions.SelectionAction;
import org.eclipse.gef.ui.actions.UpdateAction;
import org.eclipse.gef.ui.actions.WorkbenchPartAction;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphEditorAction extends WorkbenchPartAction implements
        IPageChangedListener {

    private UpdateAction wrapped;
    private IBranchGraphEditor editor;
    private IBranchGraphPage page;

    /**
     * @param editor
     * @param wrapped
     */
    public GraphEditorAction(IBranchGraphEditor editor, UpdateAction wrapped) {
        super(editor);
        this.wrapped = wrapped;
        this.editor = editor;
        this.editor.addPageChangedListener(this);
        this.page = this.editor.getActiveGraphPage();
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#dispose()
     */
    @Override
    public void dispose() {
        this.editor.removePageChangedListener(this);
        super.dispose();
    }

    /**
     * @see org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.eclipse.jface.dialogs.PageChangedEvent)
     */
    public void pageChanged(PageChangedEvent event) {
        this.page = this.editor.getActiveGraphPage();
        if (wrapped instanceof SelectionAction && this.page!=null) {
            ((SelectionAction) wrapped).setSelectionProvider(this.page
                    .getGraphSelectionProvider());
        }
        wrapped.update();
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#getCommandStack()
     */
    @Override
    protected CommandStack getCommandStack() {
        return P4CoreUtils.convert(this.page, CommandStack.class);
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#calculateEnabled()
     */
    @Override
    protected boolean calculateEnabled() {
        return false;
    }

}
