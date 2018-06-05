/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4JobUiConfiguration2 extends IP4JobUiConfiguration {

    /**
     * Get pending changelist locator
     * 
     * @return locator or null if legacy matching should be used
     */
    IPendingChangelistLocator getPendingChangelistLocator();

}
