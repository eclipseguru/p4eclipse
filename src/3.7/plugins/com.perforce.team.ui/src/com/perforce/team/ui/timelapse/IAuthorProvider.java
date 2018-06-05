/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IAuthorProvider {

    /**
     * Get author of revision
     * 
     * @param revision
     * @return author
     */
    String getAuthor(IP4Revision revision);

}
