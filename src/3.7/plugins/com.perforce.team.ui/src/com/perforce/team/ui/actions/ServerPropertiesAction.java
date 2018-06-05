package com.perforce.team.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.dialogs.SetConnectionDialog;

/**
 * Copyright (c) 2003 Perforce Software. All rights reserved.
 * 
 */
public class ServerPropertiesAction extends PerforceTeamAction {

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        if (getSelection() != null) {
            for (Object obj : getSelection().toArray()) {
                if (obj instanceof IP4Connection) {
                    IP4Connection connection = (IP4Connection) obj;
                    execute(connection);
                }
            }
        }
    }

    /**
     * Edits the properties of a connection
     * 
     * @param connection
     * @return - true if edited
     */
    public boolean execute(final IP4Connection connection) {
        final boolean[] changed=new boolean[]{false};
        final SetConnectionDialog dlg = new SetConnectionDialog(getShell());
        dlg.setConnectionParams(connection.getParameters());
        if (dlg.open() == Window.OK) {

        	Job job=new Job(Messages.ChangePropertyAction_UpdateConnection){

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					monitor.beginTask(Messages.ChangePropertyAction_UpdateConnection, 100);
					monitor.worked(50);
		            if (!connection.getParameters().equals(dlg.getConnectionParams())) {
		                P4ConnectionManager.getManager().editConnection(connection,
		                        dlg.getConnectionParams());
		                changed[0] = true;
		            }
		            monitor.done();
					return Status.OK_STATUS;
				}
        		
        	};
        	job.schedule();
        	
        }
        return changed[0];
    }

    /**
     * @see com.perforce.team.ui.actions.PerforceTeamAction#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        return true;
    }
}
