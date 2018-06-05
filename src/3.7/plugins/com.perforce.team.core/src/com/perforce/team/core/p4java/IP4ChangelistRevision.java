/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileRevisionData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IP4ChangelistRevision extends IP4Revision {

    /**
     * Get files affected by this changelist revision
     * 
     * @return - files at this changelist revision
     */
    IFileRevisionData[] getRevisions();

}
