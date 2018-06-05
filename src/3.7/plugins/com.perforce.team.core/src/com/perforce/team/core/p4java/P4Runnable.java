/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Base implementation of an {@link IP4Runnable}. Subclasses need only override
 * the run method.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4Runnable implements IP4Runnable {

    /**
     * @see com.perforce.team.core.p4java.IP4Runnable#getTitle()
     */
    public String getTitle() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Runnable#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) {
    }

}
