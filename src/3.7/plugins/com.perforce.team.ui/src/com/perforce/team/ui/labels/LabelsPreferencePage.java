/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.preferences.RetrievePreferencePage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelsPreferencePage extends RetrievePreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.labels.LabelsPreferencePage"; //$NON-NLS-1$

    // Numbers of columns in GridLayout
    private static final int NUM_COLS = 1;

    /**
     * Constructor.
     */
    public LabelsPreferencePage() {
        super(NUM_COLS);
    }

    /**
     * /**
     * 
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = DialogUtils.createComposite(parent);
        super.createRetrieveArea(composite);

        return composite;
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getName()
     */
    @Override
    protected String getName() {
        return "labels"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getRetrievePref()
     */
    @Override
    protected String getRetrievePref() {
        return IPreferenceConstants.NUM_LABELS_RETRIEVE;
    }
}
