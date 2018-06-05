/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4SubmittedChangelist;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface ISubmittedChangelistListener {

    /**
     * Callback when a set of changelists are loaded
     * 
     * @param lists
     */
    void changelistsLoaded(IP4SubmittedChangelist[] lists);

}
