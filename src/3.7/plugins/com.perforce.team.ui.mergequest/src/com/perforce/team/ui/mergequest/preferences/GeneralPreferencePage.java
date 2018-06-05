/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.preferences;

import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GeneralPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private BooleanFieldEditor showTooltips;

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
        setPreferenceStore(P4BranchGraphPlugin.getDefault()
                .getPreferenceStore());
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        super.performDefaults();
        showTooltips.loadDefault();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        showTooltips.store();
        return super.performOk();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().applyTo(displayArea);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(displayArea);

        showTooltips = new BooleanFieldEditor(
                IPreferenceConstants.SHOW_TOOLTIPS,
                Messages.GeneralPreferencePage_ShowTooltips, displayArea);
        showTooltips.setPreferenceStore(getPreferenceStore());
        showTooltips.load();

        return displayArea;
    }

}
