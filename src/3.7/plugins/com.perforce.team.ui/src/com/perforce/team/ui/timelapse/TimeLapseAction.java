/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;
import com.perforce.team.ui.p4v.P4VTimeLapseAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentDescription;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseAction extends P4Action {

    private boolean enableBranchHistory = true;
    private boolean enableChangelistKeys = true;

    /**
     * @param enableBranchHistory
     *            the enableBranchHistory to set
     */
    public void setEnableBranchHistory(boolean enableBranchHistory) {
        this.enableBranchHistory = enableBranchHistory;
    }

    /**
     * @param enableChangelistKeys
     *            the enableChangelistKeys to set
     */
    public void setEnableChangelistKeys(boolean enableChangelistKeys) {
        this.enableChangelistKeys = enableChangelistKeys;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return revisionExists(getSingleFileSelection());
    }

    private void openEditor(final String id, final IP4File file) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                TimeLapseInput input = new TimeLapseInput(file,
                        enableBranchHistory, enableChangelistKeys);
                try {
                    IDE.openEditor(PerforceUIPlugin.getActivePage(), input, id);
                } catch (PartInitException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        });
    }

    private void showNotFoundMessage() {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openInformation(
                        P4UIUtils.getDialogShell(),
                        Messages.TimeLapseAction_NoTimelapseTitle,
                        Messages.TimeLapseAction_NoTimelapseMessage);
            }
        });
    }

    private IContentType getStorageType(final IP4File file) {

        // Refresh file is head revision in not greater than 0
        int head = file.getHeadRevision();
        if (head <= 1) {
            file.refresh();
        }

        IStorage storage = new P4Storage() {

            @Override
            public String getName() {
                return file.getName();
            }

            public InputStream getContents() throws CoreException {
                return file.getHeadContents();
            }
        };
        return P4UIUtils.getContentType(storage);
    }

    private IContentType getFileType(IFile localFile) {
        IContentType type = null;
        try {
            // Refresh or else content type lookup may fail
            // it resource is out of sync
            localFile.refreshLocal(IResource.DEPTH_ONE,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
        if (localFile.exists()) {
            try {
                IContentDescription description = localFile
                        .getContentDescription();
                if (description != null) {
                    type = description.getContentType();
                }
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return type;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        boolean internal = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.USE_INTERNAL_TIMELAPSE);
        if (internal) {
            final IP4File file = getSingleFileSelection();
            if (file != null && isEnabledEx()) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public String getTitle() {
                        return Messages.TimeLapseAction_OpeningTimelapse;
                    }

                    @Override
                    public void run(IProgressMonitor monitor) {
                        String editorId = null;
                        IContextHandler handler = null;
                        IContentType type = null;
                        IFile localFile = file.getLocalFileForLocation();
                        if (localFile != null) {
                            type = getFileType(localFile);
                        }
                        if (type == null) {
                            type = getStorageType(file);
                        }
                        if (type != null) {
                            editorId = TimeLapseRegistry.getRegistry()
                                    .getEditorId(type);
                            handler = TimeLapseRegistry.getRegistry()
                                    .getHandler(type);
                        }
                        if (editorId == null) {
                            editorId = TimeLapseRegistry.getRegistry()
                                    .getEditorId(
                                            "org.eclipse.core.runtime.text", //$NON-NLS-1$
                                            true);
                        }
                        if (editorId != null) {
                            boolean open = true;
                            if (handler != null) {
                                open = handler.timelapseRequested(type,
                                        editorId, file);
                            }
                            if (open) {
                                openEditor(editorId, file);
                            }
                        } else {
                            showNotFoundMessage();
                        }
                    }

                };
                runRunnable(runnable);

            }
        } else {
            P4VTimeLapseAction external = new P4VTimeLapseAction();
            external.setAsync(isAsync());
            external.setCollection(getResourceSelection());
            external.run(null);
        }
    }
}
