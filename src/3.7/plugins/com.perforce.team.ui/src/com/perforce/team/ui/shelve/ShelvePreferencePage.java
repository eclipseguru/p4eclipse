/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.preferences.RetrievePreferencePage;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShelvePreferencePage extends RetrievePreferencePage {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.shelve.ShelvePreferencePage"; //$NON-NLS-1$

    /**
     * Create a new shelve preference page
     */
    public ShelvePreferencePage() {
        super(1);
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
        return "shelved changelists"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.preferences.RetrievePreferencePage#getRetrievePref()
     */
    @Override
    protected String getRetrievePref() {
        return IPreferenceConstants.NUM_SHELVES_RETRIEVE;
    }

}
