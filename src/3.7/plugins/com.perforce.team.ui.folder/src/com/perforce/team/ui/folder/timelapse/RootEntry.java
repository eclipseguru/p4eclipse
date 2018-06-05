/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class RootEntry extends FolderEntry {

    private IP4Revision[] revisions=new IP4Revision[0];

    /**
     * @param revisions
     */
    public RootEntry(IP4Revision[] revisions) {
        super("", null); //$NON-NLS-1$
        this.revisions = revisions;
    }

    /**
     * @see com.perforce.team.ui.folder.timelapse.IEntry#getRevisions()
     */
    @Override
    public IP4Revision[] getRevisions() {
        return this.revisions;
    }

    @Override
    public boolean equals(Object obj) {
    	// coverity FB.EQ_DOESNT_OVERRIDE_EQUALS
    	return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// coverity FB.EQ_DOESNT_OVERRIDE_EQUALS
    	return super.hashCode();
    }
}
