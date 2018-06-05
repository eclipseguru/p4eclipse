package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.preferences.RetrievePreferencePage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Changelists preferences dialog
 */
public class ChangesPreferencesDialog extends RetrievePreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.dialogs.ChangesPreferencesDialog"; //$NON-NLS-1$

    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 1;

    /**
     * Constructor.
     */
    public ChangesPreferencesDialog() {
        super(NUM_COLS);
    }

    /**
     * Create dialog controls
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        super.createRetrieveArea(displayArea);

        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(allElementsRadio,
                        IHelpContextIds.PREF_ALL_CHANGELISTS_RADIO);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(maxElementsRadio,
                        IHelpContextIds.PREF_MAX_CHANGELISTS_RADIO);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(maxElementsText,
                        IHelpContextIds.PREF_MAX_CHANGELISTS_TEXT);

        return displayArea;
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getName()
     */
    @Override
    protected String getName() {
        return "changelists"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getRetrievePref()
     */
    @Override
    protected String getRetrievePref() {
        return IPerforceUIConstants.PREF_RETRIEVE_NUM_CHANGES;
    }

}
