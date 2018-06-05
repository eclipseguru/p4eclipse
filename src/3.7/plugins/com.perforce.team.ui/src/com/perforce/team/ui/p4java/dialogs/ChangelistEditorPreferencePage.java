/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistEditorPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID - preference page id
     */
    public static final String ID = "com.perforce.team.ui.dialogs.ChangelistEditorPreferencePage"; //$NON-NLS-1$

    private static final String[] DISPLAY_STYLES = new String[] {
            Messages.ChangelistEditorPreferencePage_Solid,
            Messages.ChangelistEditorPreferencePage_Dotted,
            Messages.ChangelistEditorPreferencePage_Dashed };
    private static final int[] SWT_STYLES = new int[] { SWT.LINE_SOLID,
            SWT.LINE_DOT, SWT.LINE_DASH };

    private BooleanFieldEditor editorFontEditor;
    private BooleanFieldEditor autoActivateEditor;
    private BooleanFieldEditor rulerEditor;
    private IntegerFieldEditor rulerColumnEditor;
    private ColorFieldEditor rulerColorEditor;
    private Combo rulerStyleEditor;
    private BooleanFieldEditor sameStatusEditor;

    private Composite createEditorParent(Composite parent) {
        Composite editorParent = new Composite(parent, SWT.NONE);
        GridLayout epLayout = new GridLayout(1, true);
        epLayout.marginWidth = 0;
        epLayout.marginHeight = 0;
        editorParent.setLayout(epLayout);
        editorParent
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return editorParent;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        editorFontEditor = new BooleanFieldEditor(
                IPreferenceConstants.DESCRIPTION_EDITOR_FONT,
                Messages.ChangelistEditorPreferencePage_UseTextEditorFont,
                createEditorParent(displayArea));
        editorFontEditor.setPreferenceStore(getPreferenceStore());
        editorFontEditor.load();

        autoActivateEditor = new BooleanFieldEditor(
                IPreferenceConstants.DESCRIPTION_AUTO_ACTIVATE,
                Messages.ChangelistEditorPreferencePage_EnableAutoActivation,
                createEditorParent(displayArea));
        autoActivateEditor.setPreferenceStore(getPreferenceStore());
        autoActivateEditor.load();

        rulerEditor = new BooleanFieldEditor(
                IPreferenceConstants.DESCRIPTION_RULER,
                Messages.ChangelistEditorPreferencePage_ShowWrappingGuide,
                createEditorParent(displayArea));
        rulerEditor.setPreferenceStore(getPreferenceStore());
        rulerEditor.load();

        rulerColumnEditor = new IntegerFieldEditor(
                IPreferenceConstants.DESCRIPTION_RULER_COLUMN,
                Messages.ChangelistEditorPreferencePage_WrappingGuideColumn,
                createEditorParent(displayArea));
        rulerColumnEditor.setPreferenceStore(getPreferenceStore());
        rulerColumnEditor.setEmptyStringAllowed(false);
        rulerColumnEditor.load();

        rulerColorEditor = new ColorFieldEditor(
                IPreferenceConstants.DESCRIPTION_RULER_COLOR,
                Messages.ChangelistEditorPreferencePage_WrappingGuideColor,
                createEditorParent(displayArea));
        rulerColorEditor.setPreferenceStore(getPreferenceStore());
        rulerColorEditor.load();

        Composite styleArea = createEditorParent(displayArea);
        ((GridLayout) styleArea.getLayout()).numColumns = 2;
        ((GridLayout) styleArea.getLayout()).makeColumnsEqualWidth = false;

        Label rulerStyleLabel = new Label(styleArea, SWT.LEFT);
        rulerStyleLabel
                .setText(Messages.ChangelistEditorPreferencePage_WrappingGuideLineStyle);
        rulerStyleEditor = new Combo(styleArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        for (String display : DISPLAY_STYLES) {
            rulerStyleEditor.add(display);
        }
        rulerStyleEditor.select(0);
        int currentStyle = getPreferenceStore().getInt(
                IPreferenceConstants.DESCRIPTION_RULER_STYLE);
        setRulerStyle(currentStyle);

        sameStatusEditor = new BooleanFieldEditor(
                IPreferenceConstants.SAME_JOB_STATUS,
                Messages.ChangelistEditorPreferencePage_LeaveJobStatusUnchanged,
                createEditorParent(displayArea));
        sameStatusEditor.setPreferenceStore(getPreferenceStore());
        sameStatusEditor.load();

        return displayArea;
    }

    private void setRulerStyle(int style) {
        for (int i = 0; i < SWT_STYLES.length; i++) {
            if (style == SWT_STYLES[i]) {
                rulerStyleEditor.select(i);
                break;
            }
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        editorFontEditor.loadDefault();
        autoActivateEditor.loadDefault();
        rulerEditor.loadDefault();
        rulerColorEditor.loadDefault();
        rulerColumnEditor.loadDefault();
        sameStatusEditor.loadDefault();
        int defaultStyle = getPreferenceStore().getDefaultInt(
                IPreferenceConstants.DESCRIPTION_RULER_STYLE);
        setRulerStyle(defaultStyle);
        super.performDefaults();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        editorFontEditor.store();
        autoActivateEditor.store();
        rulerEditor.store();
        rulerColorEditor.store();
        rulerColumnEditor.store();
        sameStatusEditor.store();
        int index = rulerStyleEditor.getSelectionIndex();
        if (index > -1) {
            getPreferenceStore().setValue(
                    IPreferenceConstants.DESCRIPTION_RULER_STYLE,
                    SWT_STYLES[index]);
        }
        return super.performOk();
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUIPlugin.getPlugin().getPreferenceStore());
    }

}
