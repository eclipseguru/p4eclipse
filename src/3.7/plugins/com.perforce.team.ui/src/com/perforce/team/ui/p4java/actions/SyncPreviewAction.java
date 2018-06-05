/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIProgressListener;
import com.perforce.team.ui.views.ConsoleDocument;
import com.perforce.team.ui.views.ConsoleView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncPreviewAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return containsOnlineConnection();
    }

    /**
     * Run a sync preview against the specified non-null collection
     * 
     * @param collection
     * @param force
     * @param revision
     */
    public void runAction(final P4Collection collection, final boolean force,
            final String revision) {
        if (collection.isEmpty()) {
            return;
        }
        final ConsoleView view = ConsoleView.openInActivePerspective();
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.SyncPreviewAction_PreviewingSync;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                P4UIProgressListener callback = new P4UIProgressListener(CmdSpec.SYNC, collection.getConnection(),monitor);
                List<IFileSpec> previews = collection.sync(force, true,
                        revision, monitor, callback);
                if (view != null) {
                    for (IFileSpec spec : previews) {
                        if (spec.getOpStatus() == FileSpecOpStatus.VALID) {
                            StringBuffer buffer = new StringBuffer();
                            if (spec.getDepotPath() != null) {
                                buffer.append(spec.getDepotPath());
                                if (spec.getEndRevision() > 0) {
                                    buffer.append("#"); //$NON-NLS-1$
                                    buffer.append(spec.getEndRevision());
                                }
                                buffer.append(" "); //$NON-NLS-1$
                            }

                            if (spec.getAction() != null) {
                                buffer.append(spec.getAction().toString()
                                        .toLowerCase());
                                buffer.append(" "); //$NON-NLS-1$
                            }
                            String local = spec.getClientPathString();
                            if (local == null) {
                                local = spec.getLocalPathString();
                            }
                            if (local != null) {
                                buffer.append(local);
                            }
                            if (buffer.length() > 0) {
                                view.appendLine(ConsoleDocument.MESSAGE,
                                        buffer.toString());
                            }
                        }
                    }
                }
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        runAction(getResourceSelection(), false, null);
    }
}
