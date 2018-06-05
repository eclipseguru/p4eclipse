/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.changeset;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.synchronize.P4ChangeSetManager;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.team.ui.AbstractActiveChangeSetProvider;
import org.eclipse.mylyn.team.ui.IContextChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ActiveChangeSetProvider extends AbstractActiveChangeSetProvider {

    /**
     * INACTIVE_PROXY_ID - copied from {@link ContextActiveChangeSetManager}
     * 
     * An inactive proxy is not needed since files will open in the default
     * changelist if opened without a changelist id.
     */
    public static final String INACTIVE_PROXY_ID = "org.eclipse.mylyn.team.ui.inactive.proxy"; //$NON-NLS-1$

    /**
     * P4 Active change set provider
     */
    public P4ActiveChangeSetProvider() {
    }

    /**
     * @see org.eclipse.mylyn.team.ui.AbstractActiveChangeSetProvider#getActiveChangeSetManager()
     */
    @Override
    public ActiveChangeSetManager getActiveChangeSetManager() {
        return P4ChangeSetManager.getChangeSetManager();
    }

    /**
     * @see org.eclipse.mylyn.team.ui.AbstractActiveChangeSetProvider#createChangeSet(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public IContextChangeSet createChangeSet(ITask task) {
    	String id = task.getTaskKey();
    	if (id != null && !INACTIVE_PROXY_ID.equals(id)) {
    		IP4Connection connection = P4MylynUiUtils.getConnection(task);
    		if (connection == null) {
    			IP4Connection[] connections = P4ConnectionManager
    					.getManager().getConnections();
    			if (connections.length == 1) {
    				connection = connections[0];
    			}
    		}
    		if (connection != null) {
    			return new P4ContextChangeSet(task, connection,
    					getActiveChangeSetManager());
    		}
    	}
        return super.createChangeSet(task);
    }

}
