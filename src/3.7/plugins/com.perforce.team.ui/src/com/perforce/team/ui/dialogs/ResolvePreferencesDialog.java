package com.perforce.team.ui.dialogs;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.perforce.team.ui.IPerforceUIConstants;

public class ResolvePreferencesDialog extends PerforcePreferencesDialog {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.ResolvePreferencesDialog"; //$NON-NLS-1$

    private PerforceRadioGroupFieldEditor resolveMode;
    private PerforceRadioGroupFieldEditor resolveAction;
    private PerforceBooleanFieldEditor mergeBinaryAsText;
    private PerforceRadioGroupFieldEditor interactiveMergeTool;
    
    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 2;

    /**
     * Constructor.
     */
    public ResolvePreferencesDialog() {
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

    @Override
    protected Control createContents(Composite parent) {
        Composite composite = DialogUtils.createComposite(parent, NUM_COLS);

        resolveMode = createRadioGroupFieldEditor(
                IPerforceUIConstants.PREF_RESOLVE_DEFAULT_MODE,
                Messages.ResolvePreferencesDialog_DefaultMode, 1,
                new String[][] {
                        { Messages.ResolvePreferencesDialog_Automatically,
                                IPerforceUIConstants.RESOLVE_AUTO },
                        { Messages.ResolvePreferencesDialog_Interactively,
                                	IPerforceUIConstants.RESOLVE_INTERACTIVE },
                        { Messages.ResolvePreferencesDialog_Prompt,
                        		IPerforceUIConstants.PROMPT }
                                },composite, true);

        resolveAction = createRadioGroupFieldEditor(
                IPerforceUIConstants.PREF_RESOLVE_DEFAULT_ACTION,
                Messages.ResolvePreferencesDialog_AutoResolveOptions,
                1,
                new String[][] {
                        { Messages.ResolvePreferencesDialog_AcceptSource,
                                "accept_source" },//$NON-NLS-1$
                        { Messages.ResolvePreferencesDialog_AcceptTarget,
                                "accept_target" },//$NON-NLS-1$
                        { Messages.ResolvePreferencesDialog_AcceptMergeSafe,
                                "accept_merge_safe" },//$NON-NLS-1$
                        { Messages.ResolvePreferencesDialog_AcceptMergeNoConflicts,
                                "accept_merge_no_conflicts" },//$NON-NLS-1$
                        { Messages.ResolvePreferencesDialog_AcceptMergeWithConflicts,
                                "accept_merge_with_conflicts" }, }, composite,//$NON-NLS-1$
                true);

        mergeBinaryAsText = createBooleanFieldEditor(
                IPerforceUIConstants.PREF_RESOLVE_MERGE_BINARY_AS_TEXT,
                Messages.ResolvePreferencesDialog_MergeBinaryAsText, composite);
        
        interactiveMergeTool = createRadioGroupFieldEditor(
                IPerforceUIConstants.PREF_RESOLVE_INTERACTIVE_MERGE_TOOL,
                Messages.ResolvePreferencesDialog_InteractiveMergeTool, 1,
                new String[][] {
                        { Messages.ResolvePreferencesDialog_MergeWithP4Merge,
                                "p4merge" },//$NON-NLS-1$
                        { Messages.ResolvePreferencesDialog_MergeWithEclipse,
                                "eclipse_compare" } }, composite, true);//$NON-NLS-1$
        
        setFieldEditors(new FieldEditor[] { resolveMode, resolveAction,
                mergeBinaryAsText, interactiveMergeTool });
        initializeValues();
        return composite;
    }
}
