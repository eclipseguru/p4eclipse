/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedFile;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.editor.DepotFileEditorInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class OpenEditorAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4SubmittedFile) {
            IP4SubmittedFile submitted = (IP4SubmittedFile) resource;
            if (isEnabled(submitted)) {
                DepotFileEditorInput input = new DepotFileEditorInput(
                        submitted.getFile(), "#" + submitted.getRevision()); //$NON-NLS-1$
                P4UIUtils.openEditor(input);
            }
        }
    }

    private boolean isEnabled(IP4SubmittedFile file) {
        return file.getRevision() > 0
                && !P4File.isActionDelete(file.getAction());
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = false;
        IP4Resource resource = getSingleOnlineResourceSelection();
        if (resource instanceof IP4SubmittedFile) {
            enabled = isEnabled((IP4SubmittedFile) resource);
        }
        return enabled;
    }
}
