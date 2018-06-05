/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IEnableDisplay {

    /**
     * Set the display as enabled
     * 
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Is this display enabled?
     * 
     * @return - true if enabled, false otherwise
     */
    boolean isEnabled();

}
