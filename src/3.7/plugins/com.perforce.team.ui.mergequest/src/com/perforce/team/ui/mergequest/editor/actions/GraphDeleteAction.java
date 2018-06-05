/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor.actions;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;

import org.eclipse.gef.ui.actions.DeleteAction;
import org.eclipse.ui.IWorkbenchPart;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphDeleteAction extends DeleteAction {

    private GraphEditorAction delegate;

    /**
     * @param part
     */
    public GraphDeleteAction(IBranchGraphEditor part) {
        super((IWorkbenchPart) part);
        this.delegate = new GraphEditorAction(part, this);
    }

    /**
     * @see org.eclipse.gef.ui.actions.DeleteAction#init()
     */
    @Override
    protected void init() {
        super.init();
        setText(Messages.GraphDeleteAction_Title);
        setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_DELETE));
    }

    /**
     * @see org.eclipse.gef.ui.actions.WorkbenchPartAction#dispose()
     */
    @Override
    public void dispose() {
        this.delegate.dispose();
        super.dispose();
    }

}
