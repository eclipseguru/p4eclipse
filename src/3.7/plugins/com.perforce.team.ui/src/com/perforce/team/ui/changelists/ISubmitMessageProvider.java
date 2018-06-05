/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4PendingChangelist;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface ISubmitMessageProvider {

    /**
     * Get changelist description
     * 
     * @param list
     * @return - changelist description
     */
    String getDescription(IP4PendingChangelist list);

}
