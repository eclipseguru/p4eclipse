/**
 * Copyright (c) 2013 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.dialogs.IntegrateDialog;
import com.perforce.team.ui.p4java.dialogs.PopulateDialog;

public class PopulateAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            enabled = this.getSelection() != null && this.getSelection().size() == 1;
            if (enabled && !containsContainers()) {
                IP4Resource[] resources = getResourceSelection().members();
                if (resources.length == 1) {
                    if (resources[0] instanceof IP4Connection
                            || resources[0] instanceof IP4SubmittedChangelist) {
                        enabled = true;
                    } else if (resources[0] instanceof IP4File) {
                        IP4File resource = (IP4File) resources[0];
                        enabled = resource.getHeadRevision() > 0;
                    }
                } else {
                    enabled = false;
                }
            }
        }
        return enabled;
    }

    public void populate(IP4Connection connection, String targetPath) {
        if (targetPath != null) {
        	PopulateDialog dialog = new PopulateDialog(getShell(), connection, null, targetPath);
            if (IntegrateDialog.OK == dialog.open()) {
            	populate(connection, dialog.getSourcePaths(), dialog.getTargetPaths(), false, dialog.getDescription());
            }
        }
    }

    public void populate(final IP4Connection connection,
    		final List<String> sourcePaths, final List<String> targetPaths, 
            final boolean preview, final String description) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
            	
            	for(int i=0;i<sourcePaths.size();i++){
	                IP4Resource[] integrated = connection.populate(sourcePaths.get(i),
	                        targetPaths.get(i), preview, description);
	                if (integrated!=null && integrated.length > 0) {
	                    P4Collection collection = createCollection(integrated);
	                    collection.refreshLocalResources(IResource.DEPTH_INFINITE);
	                }
            	}
            }

            @Override
            public String getTitle() {
                return Messages.PopulateAction_PopulatingFiles;
            }

        };
        runRunnable(runnable);
    }
    
	@Override
	protected void runAction() {
		// TODO Auto-generated method stub
		
	}
}
