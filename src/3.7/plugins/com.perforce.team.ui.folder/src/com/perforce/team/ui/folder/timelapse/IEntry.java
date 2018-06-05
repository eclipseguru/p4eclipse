/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IEntry {

    /**
     * Get earlier changelist that this entry appears in the history
     * 
     * @return - changelist id
     */
    int getFirst();

    /**
     * Complete the entry meaning all revision data has been processed
     */
    void complete();

    /**
     * Get revisions
     * 
     * @return - all revisions
     */
    IP4Revision[] getRevisions();

}
