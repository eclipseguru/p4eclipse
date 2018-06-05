/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import com.perforce.team.ui.patch.P4PatchUiPlugin;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CreatePatchWizardDialog extends WizardDialog {

    /**
     * DIALOG_SETTINGS
     */
    public static final String DIALOG_SETTINGS = "CreatePatchWizardDialog"; //$NON-NLS-1$

    /**
     * @param parentShell
     * @param newWizard
     */
    public CreatePatchWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings wizardSection = null;
        IDialogSettings settings = P4PatchUiPlugin.getDefault()
                .getDialogSettings();
        if (settings != null) {
            wizardSection = settings.getSection(DIALOG_SETTINGS);
            if (wizardSection == null) {
                wizardSection = settings.addNewSection(DIALOG_SETTINGS);
            }
        }
        return wizardSection;
    }

}
