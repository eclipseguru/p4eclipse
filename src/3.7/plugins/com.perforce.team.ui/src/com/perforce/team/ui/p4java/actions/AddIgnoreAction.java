/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.PerforceMarkerManager;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AddIgnoreAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        List<IResource> resources = getLocalResourceSelection();
        if (null!=resources && !resources.isEmpty()) {
            ignore(resources);
        }
    }

    private void ignore(final List<IResource> resources) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.AddIgnoreAction_IgnoringSelectedFoldersFiles;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                for (IResource resource : resources) {
                    IgnoredFiles.addIgnore(resource);
                    try {
                        resource.deleteMarkers(
                                PerforceMarkerManager.ADDITION_MARKER, false,
                                IResource.DEPTH_ZERO);
                    } catch (CoreException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
                updateActionState();
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            if (containsContainers()) {
                enabled = true;
            } else {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                if (resources.length > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource.getRemotePath() == null) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

}
