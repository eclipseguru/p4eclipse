package com.perforce.team.ui.streams.wizard;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * A WziardDialog with default width and height and centered on display.
 * <p/>
 * 
 * @author ali
 */
public class EditStreamWizardDialog extends WizardDialog {
    private static final String EDIT_STREAM_WIZARD_SETTINGS_SECTION = "EditStreamWizard"; //$NON-NLS-1$

    public EditStreamWizardDialog(Shell parentShell, IWizard newWizard) {
        super(parentShell, newWizard);
    }

    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        IDialogSettings section = settings
                .getSection(EDIT_STREAM_WIZARD_SETTINGS_SECTION);
        if (section == null) {
            section = settings.addNewSection(EDIT_STREAM_WIZARD_SETTINGS_SECTION);
        }
        return section;
    }


}
