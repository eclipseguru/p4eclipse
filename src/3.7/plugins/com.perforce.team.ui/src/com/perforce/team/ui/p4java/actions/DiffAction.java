/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.actions.Messages;
import com.perforce.team.ui.editor.CompareUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class DiffAction extends P4Action {

    private static final int MAX_DIFF_CHECK = 5;

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (file.getHeadRevision() > 0) {
                                enabled = true;
                                break;
                            }
                        } else {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    /**
     * Get compare stream for this diff action
     * 
     * @param file
     * @return - input stream
     */
    protected abstract InputStream getCompareStream(IP4File file);

    /**
     * Get the descriptive string for this comparison version
     * 
     * @param file
     * @return - compare string
     */
    protected abstract String getCompareString(IP4File file);

    /**
     * Get editor title for this diff
     * 
     * @param file
     * @return - editor title
     */
    protected abstract String getEditorTitle(IP4File file);

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        boolean doDiff = true;
        P4Collection collection = getFileSelection();
        List<IP4File> files = new ArrayList<IP4File>();
        List<IFile> localFiles = new ArrayList<IFile>();
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                files.add(file);
                IFile[] p4LocalFiles = file.getLocalFiles();
                for (IFile localFile : p4LocalFiles) {
                    localFiles.add(localFile);
                }
            }
        }
        if (files.size() > MAX_DIFF_CHECK) {
            doDiff = MessageDialog.openQuestion(getShell(),
                    Messages.DiffDepotAction_DIALOGTITLE,
                    NLS.bind(Messages.DiffDepotAction_M, files.size()));
        }
        if (doDiff) {
            PerforceUIPlugin.saveDirtyResources(getTargetPage(),
                    localFiles.toArray(new IFile[0]));
            for (IP4File file : files) {
                try {
                    File depotFile = P4CoreUtils
                            .createFile(getCompareStream(file));
                    if (depotFile != null) {
                        String editorTitle = NLS.bind(getEditorTitle(file),
                                file.getName());
                        String compareSide = getCompareString(file);

                        IFile localFile = file.getLocalFileForLocation();

                        if (localFile == null) {
                            CompareUtils.openLocalCompare(
                                    new File(file.getLocalPath()), depotFile,
                                    Messages.DiffDepotAction_DIFF4,
                                    compareSide, editorTitle);
                        } else {
                            CompareUtils.openLocalCompare(localFile, depotFile,
                                    Messages.DiffDepotAction_DIFF2,
                                    compareSide, editorTitle);
                        }

                    }
                } catch (Exception e) {
                    PerforceProviderPlugin.logError(e);
                } catch (Error e) {
                    PerforceProviderPlugin.logError(e);
                }

            }
        }
    }
}