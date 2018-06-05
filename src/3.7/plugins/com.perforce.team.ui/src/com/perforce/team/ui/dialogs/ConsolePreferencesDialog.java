package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.ui.IPerforceUIConstants;

/**
 * Console Preferences dialog.
 */
public class ConsolePreferencesDialog extends PerforcePreferencesDialog {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.ConsolePreferencesDialog"; //$NON-NLS-1$

    // Colour editors
    private PerforceColorFieldEditor commandColorEditor;
    private PerforceColorFieldEditor messageColorEditor;
    private PerforceColorFieldEditor errorColorEditor;
    private IntegerFieldEditor commandsEditor;
    private BooleanFieldEditor timestampEditor;
	private PerforceBooleanFieldEditor hideOutputEditor;

    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 2;

    /**
     * Constructor.
     */
    public ConsolePreferencesDialog() {
        super(NUM_COLS);
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#setErrorMessage(java.lang.String)
     */
    @Override
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        // Set valid if error message is null
        this.setValid(newMessage == null);
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     * @return - main control
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = DialogUtils.createComposite(parent, NUM_COLS);

        Label label = DialogUtils.createLabel(composite,
                Messages.ConsolePreferencesDialog_ConsoleSettings);
        ((GridData) label.getLayoutData()).horizontalSpan = NUM_COLS;

        commandColorEditor = createColorFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_COMMAND_COLOUR,
                Messages.ConsolePreferencesDialog_CommandLine, composite);
        messageColorEditor = createColorFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_MESSAGE_COLOUR,
                Messages.ConsolePreferencesDialog_Message, composite);
        errorColorEditor = createColorFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_ERROR_COLOUR,
                Messages.ConsolePreferencesDialog_Error, composite);
        commandsEditor = new IntegerFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_COMMANDS,
                Messages.ConsolePreferencesDialog_CommandHistorySize, composite);
        commandsEditor.setValidRange(0, Integer.MAX_VALUE);
        commandsEditor.setPage(this);
        commandsEditor.setPreferenceStore(getPreferenceStore());
        timestampEditor = createBooleanFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_TIMESTAMP,
                Messages.ConsolePreferencesDialog_ShowTimestamp, composite);
        hideOutputEditor = createBooleanFieldEditor(
                IPerforceUIConstants.PREF_CONSOLE_COMMAND_OUPUT_HIDE_LARGE,
                Messages.ConsolePreferencesDialog_HideLargeSizeOutput, composite);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(commandColorEditor.getHelpControl(),
                        IHelpContextIds.PREF_CONSOLE_COMMAND_COLOUR);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(messageColorEditor.getHelpControl(),
                        IHelpContextIds.PREF_CONSOLE_MESSAGE_COLOUR);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(errorColorEditor.getHelpControl(),
                        IHelpContextIds.PREF_CONSOLE_ERROR_COLOUR);

        setFieldEditors(new FieldEditor[] { commandColorEditor,
                messageColorEditor, errorColorEditor, commandsEditor,
                timestampEditor, hideOutputEditor });
        initializeValues();
        return composite;
    }
}
