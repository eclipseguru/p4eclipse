/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.history.P4HistoryPageSource;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.ui.history.IHistoryView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ShowHistoryAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleResourceSelection();
        final IP4File file = P4CoreUtils.convert(resource, IP4File.class);
        if (file != null) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4HistoryPageSource source = new P4HistoryPageSource(file);
                    IViewPart part;
                    try {
                        part = PerforceUIPlugin.getActivePage().showView(IHistoryView.VIEW_ID);
                        if (part instanceof IHistoryView) {
                            ((IHistoryView) part).showHistoryFor(source);
                        } else {
                            PerforceProviderPlugin
                                    .logError("Error opening History view. Class of view was not expected: GenericHistoryView");
                        }
                    } catch (PartInitException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            });
        } else if (resource instanceof IP4Folder) {
            final IP4Folder folder = (IP4Folder) resource;
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    SubmittedView view = SubmittedView.showView();
                    if (view != null) {
                        view.scheduleShowChangelists(folder);
                    }
                }
            });

        }
    }

    /**
     * 
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = false;
        // Only enable for single object selections
        if (containsOnlineConnection() && getSelectionSize() == 1) {
            enabled = containsContainers();
            if (!enabled) {
                IP4Resource resource = getSingleResourceSelection();
                enabled = resource instanceof IP4Folder
                        || P4CoreUtils.convert(resource, IP4File.class) != null;
            }
        }
        return enabled;
    }
}
