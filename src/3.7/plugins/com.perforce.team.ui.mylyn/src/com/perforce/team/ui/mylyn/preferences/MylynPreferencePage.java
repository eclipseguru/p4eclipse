/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.preferences;

import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MylynPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private Composite displayArea = null;
    private BooleanFieldEditor useTaskEditor = null;
    private BooleanFieldEditor sortJobValuesEditor = null;
    private BooleanFieldEditor groupFieldsByTypeEditor = null;
    private BooleanFieldEditor recreateChangelistsEditor = null;
    private BooleanFieldEditor useMylynTeamCommentEditor = null;

    /**
     * Create the preference page
     */
    public MylynPreferencePage() {

    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        this.displayArea = new Composite(parent, SWT.NONE);
        this.displayArea.setLayout(new GridLayout(1, true));
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        useTaskEditor = new BooleanFieldEditor(
                IPreferenceConstants.USE_TASK_EDITOR,
                Messages.MylynPreferencePage_UseTaskEditor, this.displayArea);
        useTaskEditor.setPreferenceStore(getPreferenceStore());
        useTaskEditor.load();

        sortJobValuesEditor = new BooleanFieldEditor(
                IPreferenceConstants.SORT_JOB_VALUES,
                Messages.MylynPreferencePage_SortJobFieldInSearch,
                this.displayArea);
        sortJobValuesEditor.setPreferenceStore(getPreferenceStore());
        sortJobValuesEditor.load();

        groupFieldsByTypeEditor = new BooleanFieldEditor(
                IPreferenceConstants.GROUP_FIELDS_BY_TYPE,
                Messages.MylynPreferencePage_DisplaySingleBeforeMulti,
                this.displayArea);
        groupFieldsByTypeEditor.setPreferenceStore(getPreferenceStore());
        groupFieldsByTypeEditor.load();

        recreateChangelistsEditor = new BooleanFieldEditor(
                IPreferenceConstants.RECREATE_CHANGELISTS,
                Messages.MylynPreferencePage_RecreatePendingChangelist,
                this.displayArea);
        recreateChangelistsEditor.setPreferenceStore(getPreferenceStore());
        recreateChangelistsEditor.load();

        useMylynTeamCommentEditor = new BooleanFieldEditor(
                IPreferenceConstants.USE_MYLYN_TEAM_COMMENT,
                Messages.MylynPreferencePage_UseMylynTeamComment,
                this.displayArea);
        useMylynTeamCommentEditor.setPreferenceStore(getPreferenceStore());
        useMylynTeamCommentEditor.load();

        return this.displayArea;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        useTaskEditor.store();
        sortJobValuesEditor.store();
        groupFieldsByTypeEditor.store();
        recreateChangelistsEditor.store();
        useMylynTeamCommentEditor.store();
        return super.performOk();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        useTaskEditor.loadDefault();
        sortJobValuesEditor.loadDefault();
        groupFieldsByTypeEditor.loadDefault();
        recreateChangelistsEditor.loadDefault();
        useMylynTeamCommentEditor.loadDefault();
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(PerforceUiMylynPlugin.getDefault()
                .getPreferenceStore());
    }

}
