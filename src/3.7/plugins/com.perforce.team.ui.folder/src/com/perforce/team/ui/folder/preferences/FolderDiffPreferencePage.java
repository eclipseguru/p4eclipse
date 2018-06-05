/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.preferences;

import com.perforce.team.ui.folder.PerforceUiFolderPlugin;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
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
public class FolderDiffPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.folder.diffPage"; //$NON-NLS-1$

    private ColorFieldEditor uniqueEditor;
    private ColorFieldEditor contentEditor;
    private ColorFieldEditor diffUniqueEditor;
    private ColorFieldEditor diffContentEditor;
    private BooleanFieldEditor diffLinkedResourcesEditor;

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(1)
                .create());
        displayArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());

        IPreferenceStore store = PerforceUiFolderPlugin.getDefault()
                .getPreferenceStore();

        Composite colorArea = new Composite(displayArea, SWT.NONE);
        GridLayout caLayout = new GridLayout(1, true);
        caLayout.marginWidth = 0;
        caLayout.marginHeight = 0;
        colorArea.setLayout(caLayout);
        colorArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        uniqueEditor = new ColorFieldEditor(IPreferenceConstants.UNIQUE_COLOR,
                Messages.FolderDiffPreferencePage_UniqueFileColor, colorArea);
        uniqueEditor.setPreferenceStore(store);
        uniqueEditor.load();

        contentEditor = new ColorFieldEditor(
                IPreferenceConstants.CONTENT_COLOR,
                Messages.FolderDiffPreferencePage_DifferingFileColor, colorArea);
        contentEditor.setPreferenceStore(store);
        contentEditor.load();

        diffUniqueEditor = new ColorFieldEditor(
                IPreferenceConstants.DIFF_UNIQUE_COLOR,
                Messages.FolderDiffPreferencePage_UniqueContentColor, colorArea);
        diffUniqueEditor.setPreferenceStore(store);
        diffUniqueEditor.load();

        diffContentEditor = new ColorFieldEditor(
                IPreferenceConstants.DIFF_CONTENT_COLOR,
                Messages.FolderDiffPreferencePage_DifferingContentColor,
                colorArea);
        diffContentEditor.setPreferenceStore(store);
        diffContentEditor.load();

        diffLinkedResourcesEditor = new BooleanFieldEditor(
                IPreferenceConstants.DIFF_LINKED_RESOURCES,
                Messages.FolderDiffPreferencePage_IncludeLinkedResources,
                displayArea);
        diffLinkedResourcesEditor.setPreferenceStore(store);
        diffLinkedResourcesEditor.load();

        return displayArea;
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        uniqueEditor.loadDefault();
        contentEditor.loadDefault();
        diffUniqueEditor.loadDefault();
        diffContentEditor.loadDefault();
        diffLinkedResourcesEditor.loadDefault();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        uniqueEditor.store();
        contentEditor.store();
        diffUniqueEditor.store();
        diffContentEditor.store();
        diffLinkedResourcesEditor.store();
        return super.performOk();
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

}
