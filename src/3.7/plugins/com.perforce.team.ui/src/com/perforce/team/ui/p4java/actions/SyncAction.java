/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Resource;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIProgressListener;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncAction extends P4Action {
	boolean force=false;

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return containsOnlineConnection();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        final P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
            sync(collection);
        }
    }

    private void sync(final P4Collection collection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.SyncAction_Syncing;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 300);
                monitor.subTask(generateTitle(null, collection));
                SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 100);
                P4UIProgressListener callback = new P4UIProgressListener(CmdSpec.SYNC, null, subMonitor);
                List<IFileSpec> specs = collection.sync(force, false, null, subMonitor, callback);// this is done using batch notifications.
                monitor.worked(100);
                monitor.setTaskName("Refresh local resources..."); 
                //collection.refresh();
                //collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                postSync(collection, specs);
                monitor.setTaskName("Update action state...");                
                monitor.worked(100);
                updateActionState();
                monitor.worked(100);
                monitor.done();
            }

        };
        runRunnable(runnable);
    }

    /*
     * try to refresh only resources have been synced. e.g, if folder A does not exist
     * any more after sync, we don't need refresh A, since that will cause another
     * error to handle, and since A does not exist, it make no sense to fetch
     * the metadata for A.
     */
	protected void postSync(P4Collection collection, List<IFileSpec> specs) {
		if(specs==null)
			return;
		P4Collection newList = new P4Collection();
		for(IP4Resource r:collection.members()){
			String p = r.getActionPath();
			if(p.endsWith("/..."))
				p=p.substring(0,p.length()-4);
			for(IFileSpec s: specs){
				FilePath cp = s.getClientPath();
				if(cp!=null){
					String cpp = cp.getPathString();
					if(cpp!=null && cpp.startsWith(p)){
						newList.add(r);
					}
				} else if (s.getStatusMessage() != null &&
						s.getStatusMessage().startsWith(p)) {
					newList.add(r);
				}
			}
		}
		newList.refresh();
        newList.refreshLocalResources(IResource.DEPTH_INFINITE);
	}

	public void setForce(boolean forceSync){
		force=forceSync;
	}
}
