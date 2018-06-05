/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.editor.DepotFileEditorInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffOpenAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        return getSingleOnlineResourceSelection() instanceof IP4DiffFile;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4DiffFile) {
            IP4DiffFile diff = (IP4DiffFile) resource;
            DepotFileEditorInput input = new DepotFileEditorInput(
                    diff.getFile(), "#" + diff.getRevision()); //$NON-NLS-1$
            P4UIUtils.openEditor(input);
        }

    }

}
