/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.editor.CompareUtils;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffCompareAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        IP4Resource resource = getSingleOnlineResourceSelection();
        return resource instanceof IP4DiffFile
                && ((IP4DiffFile) resource).getPair() != null;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4DiffFile) {
            IP4DiffFile file = (IP4DiffFile) resource;
            IP4DiffFile pair = file.getPair();
            if (pair != null) {
                IFileRevision left = null;
                IFileRevision right = null;
                if (file.isFile1()) {
                    left = P4CoreUtils.convert(file, IFileRevision.class);
                    right = P4CoreUtils.convert(pair, IFileRevision.class);
                } else {
                    left = P4CoreUtils.convert(pair, IFileRevision.class);
                    right = P4CoreUtils.convert(file, IFileRevision.class);
                }
                if (left != null && right != null) {
                    CompareUtils.openCompare(left, right);
                }
            }
        }
    }

}
