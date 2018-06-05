/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.team.core.p4java.IP4File;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4ReusableChangeSet {

    /**
     * Re-activate the change set. This method will be called when files are
     * opened and a changeset is no longer valid but still registered as the
     * default set. This gives the changeset an opportunity to rebuild itself
     * and reopened the newly opened files into itself if re-activation
     * succeeds.
     * 
     * @param openedFiles
     * @return true is re-use succeeds
     */
    boolean activate(IP4File[] openedFiles);

}
