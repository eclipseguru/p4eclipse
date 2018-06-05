/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateDiffAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return getSingleOnlineResourceSelection() instanceof IP4DiffFile;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4DiffFile) {
            final IP4DiffFile diff = (IP4DiffFile) resource;
            IP4DiffFile pair = diff.getPair();
            final String source = diff.getActionPath();
            final String target = pair != null ? pair.getActionPath() : source;
            UIJob job = new UIJob(Messages.IntegrateDiffAction_IntegratingFile) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    IntegrateAction action = new IntegrateAction();
                    action.integratePaths(diff.getConnection(), source, target);
                    return Status.OK_STATUS;
                }
            };
            job.setSystem(true);
            job.schedule();
        }
    }
}
