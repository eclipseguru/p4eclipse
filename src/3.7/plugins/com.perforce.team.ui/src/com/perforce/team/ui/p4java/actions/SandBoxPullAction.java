package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Runnable;

public class SandBoxPullAction extends P4Action {

	@Override
	protected void runAction() {
        final P4Collection collection = getResourceSelection();
        if (collection.isEmpty()) {
        	return;
        }
        
		Object resource = getSelection().getFirstElement();
		if(resource instanceof IP4Resource){
			IP4Connection conn = ((IP4Resource) resource).getConnection();
			if(conn!=null){
		        IP4Runnable runnable = new P4Runnable() {

		            @Override
		            public String getTitle() {
		                return Messages.PullAction_Pulling;
		            }

		            @Override
		            public void run(IProgressMonitor monitor) {
		                monitor.beginTask(getTitle(), 1);
		                monitor.subTask(generateTitle(null, collection));
		                collection.pull();
		                monitor.worked(1);
		                monitor.done();
		                collection.refreshLocalResources(IResource.DEPTH_INFINITE);
		                updateActionState();
		            }

		        };
		        runRunnable(runnable);
			}
		}
	}

	@Override
	protected boolean isEnabledEx() throws TeamException {
        if (this.getSelection() != null) {
            Object[] selected = this.getSelection().toArray();
            if(selected!=null && selected.length==1 && selected[0] instanceof IP4Resource){
            	IP4Resource resource = (IP4Resource) selected[0];
            	IP4Connection conn=resource.getConnection();
            	return conn!=null && conn.isSandbox() && IP4Resource.MIRROR_FOLDER.equals(resource.getName()) && resource.getParent() instanceof P4Depot;
            }
        }
        return false;
	}
}
