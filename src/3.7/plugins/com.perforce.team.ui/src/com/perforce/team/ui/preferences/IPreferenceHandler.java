/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IPreferenceHandler {

    /**
     * Add controls
     * 
     * @param parent
     */
    void addControls(Composite parent);

    /**
     * Save preferences
     */
    void save();

    /**
     * Restore defaults
     */
    void defaults();

}
