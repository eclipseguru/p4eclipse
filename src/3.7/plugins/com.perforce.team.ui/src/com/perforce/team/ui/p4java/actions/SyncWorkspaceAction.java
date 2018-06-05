package com.perforce.team.ui.p4java.actions;

import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4TeamUtils;

public class SyncWorkspaceAction extends P4Action {

	@Override
	protected void runAction() {
		Object connection = getSelection().getFirstElement();
		if(connection instanceof IP4Connection){
			P4TeamUtils.syncWorkspace((IP4Connection) connection,false);
		}
	}

	@Override
	protected boolean isEnabledEx() throws TeamException {
        if (this.getSelection() != null) {
            Object[] selected = this.getSelection().toArray();
            if(selected!=null && selected.length==1 && selected[0] instanceof IP4Connection)
            	return true;
        }
        return false;
	}
}
