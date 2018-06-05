/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mergequest.views.tasks.IntegrateTaskView;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ShowTasksAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        try {
            PerforceUIPlugin.getActivePage().showView(IntegrateTaskView.ID);
        } catch (PartInitException e) {
            PerforceProviderPlugin.logError(e);
        }
    }

}
