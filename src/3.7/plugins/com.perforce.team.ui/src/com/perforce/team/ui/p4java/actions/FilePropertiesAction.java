/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.jface.preference.PreferenceDialog;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4UIUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FilePropertiesAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        openFileProperyPage(true);
    }

    /**
     * Open file property page on current selection
     * 
     * @param block
     * @return - preference dialog
     */
    public PreferenceDialog openFileProperyPage(boolean block) {
        PreferenceDialog dialog = null;
        IP4File file = getSingleFileSelection();
        if (file != null) {
            dialog = P4UIUtils.openPropertyPage(
                    "com.perforce.team.ui.dialogs.FilePropertiesDialog", file, //$NON-NLS-1$
                    block, false);
        }
        return dialog;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return getSingleFileSelection() != null;
    }

}
