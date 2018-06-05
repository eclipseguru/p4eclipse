/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.editor.GraphContext;
import com.perforce.team.ui.mergequest.editor.IBranchGraphPage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingDisplayAction extends Action {

    private GraphContext context;
    private Mapping mapping;

    /**
     * Create a mapping display action
     * 
     * @param context
     * @param mapping
     */
    public MappingDisplayAction(GraphContext context, Mapping mapping) {
        this.context = context;
        this.mapping = mapping;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (mapping != null) {
            new UIJob(Messages.MappingDisplayAction_DisplayingIntegrateTasks) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    IBranchGraphPage editor = context.getPage();
                    if (P4UIUtils.okToUse(editor.getControl())) {
                        editor.setSelection(new StructuredSelection(mapping));
                        new ShowTasksAction().runAction();
                    }
                    return Status.OK_STATUS;
                }
            }.schedule();
        }
    }
}
