/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class AsyncEnablementAction extends P4Action {

    private ISchedulingRule enablementRule = P4Runner.createRule();

    /**
     * Determine if any elements in the selection are associated with a
     * connection that is non-offline and non-connection. This determines
     * whether the {@link #isEnabledEx()} should be run on the UI-thread.
     * 
     * @return true to run {@link #isEnabledEx()} off the ui-thread, false to run
     *         off UI-thread
     */
    private boolean loadEnablementAsync() {
        boolean useAsync = false;
        if (PerforceUIPlugin.isUIThread() && this.getSelection() != null) {
            for (Object select : this.getSelection().toArray()) {
                IResource resource = getResource(select);
                if (resource != null) {
                    IP4Connection connection = P4ConnectionManager.getManager()
                            .getConnection(resource.getProject(), false);
                    if (isConnected(connection)) {
                        useAsync = true;
                        break;
                    }
                } else {
                    IP4Resource p4Resource = getP4Resource(select);
                    if (p4Resource != null
                            && isConnected(p4Resource.getConnection())) {
                        useAsync = true;
                        break;
                    }
                }
            }
        }
        return useAsync;
    }

    private boolean isConnected(IP4Connection connection) {
        return connection != null && !connection.isOffline()
                && !connection.isConnected();
    }

    /**
     * @see com.perforce.team.ui.actions.PerforceTeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
     */
    @Override
    protected void setActionEnablement(final IAction action) {
        if (!loadEnablementAsync()) {
            super.setActionEnablement(action);
        } else {
            action.setEnabled(false);
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    try {
                        action.setEnabled(isEnabledEx());
                    } catch (TeamException te) {
                        PerforceProviderPlugin.logError(te);
                    }
                }

            }, enablementRule);
        }
    }

}
