/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for running a p4 command
 *
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Runnable {//extends Runnable {

    /**
     * Gets the title of this runnable to be used as the job label.
     *
     * @return - string title or null
     */
    default String getTitle() {
    	return null;
    };

    /**
     * Run the runnable with access to the progress monitory
     *
     * @param monitor
     */
    void run(IProgressMonitor monitor);

}
