/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.timelapse.IContextHandler;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.content.IContentType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CContextHandler implements IContextHandler {

    /**
     * @see com.perforce.team.ui.timelapse.IContextHandler#timelapseRequested(org.eclipse.core.runtime.content.IContentType,
     *      java.lang.String, com.perforce.team.core.p4java.IP4File)
     */
    public boolean timelapseRequested(IContentType type, String editorId,
            IP4File file) {
        final boolean[] open = new boolean[] { false };
        try {
            ICProject[] cProjects = CoreModel.getDefault().getCModel()
                    .getCProjects();
            if (cProjects == null || cProjects.length == 0) {
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        open[0] = P4ConnectionManager
                                .getManager()
                                .openConfirm(
                                        Messages.CContextHandler_NoCProjectsFoundTitle,
                                        Messages.CContextHandler_NoCProjectsFoundMessage);
                    }
                });
            } else {
                open[0] = true;
            }
        } catch (CModelException e) {
            PerforceProviderPlugin.logError(e);
        }
        return open[0];
    }
}
