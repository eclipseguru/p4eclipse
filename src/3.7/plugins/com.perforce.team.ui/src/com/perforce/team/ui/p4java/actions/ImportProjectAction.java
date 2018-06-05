/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.PerforceProjectSetSerializer;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportProjectAction extends P4Action {

    /**
     * Runs an import passing on the monitor to the workspace model operation
     * 
     * @param monitor
     */
    public void runAction(IProgressMonitor monitor) {
        runAction(monitor, true);
    }

    /**
     * Runs an import passing on the monitor to the workspace model operation
     * and optionally shows an error dialog for any projects that were attempted
     * to be imported but not in the client view
     * 
     * @param monitor
     * @param showErrors
     */
    public void runAction(IProgressMonitor monitor, boolean showErrors) {
        final P4Collection collection = getResourceSelection();
        PerforceProjectSetSerializer.createProjects(collection, getShell(),
                showErrors, isAsync());
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        runAction(null);
    }

}
