package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.preferences.RetrievePreferencePage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Jobs preferences dialog
 */
public class JobsPreferencesDialog extends RetrievePreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.JobsPreferencesDialog"; //$NON-NLS-1$

    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 1;

    /**
     * Constructor.
     */
    public JobsPreferencesDialog() {
        super(NUM_COLS);
    }

    /**
     * Create dialog controls
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = DialogUtils.createComposite(parent);

        super.createRetrieveArea(composite);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(allElementsRadio, IHelpContextIds.PREF_ALL_JOBS_RADIO);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(maxElementsRadio, IHelpContextIds.PREF_MAX_JOBS_RADIO);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(maxElementsText, IHelpContextIds.PREF_MAX_JOBS_TEXT);

        return composite;
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getName()
     */
    @Override
    protected String getName() {
        return "jobs"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getRetrievePref()
     */
    @Override
    protected String getRetrievePref() {
        return IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS;
    }
}
