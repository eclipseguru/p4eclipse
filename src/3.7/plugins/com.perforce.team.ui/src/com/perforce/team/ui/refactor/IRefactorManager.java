/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.refactor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IRefactorManager {

    /**
     * Is {@link #getPreference()} enabled?
     * 
     * @return - true if enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Is specified preference enabled
     * 
     * @param preference
     * @return - true if enabled, false otherwise
     */
    boolean isEnabled(String preference);

    /**
     * Get main preference of thie refactor manager
     * 
     * @return - non-null preference
     */
    String getPreference();

}
