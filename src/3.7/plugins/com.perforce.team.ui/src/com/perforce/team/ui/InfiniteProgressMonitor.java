/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class InfiniteProgressMonitor extends SubProgressMonitor {

    int totalWork;
    int halfWay;
    int currentIncrement;
    int nextProgress;
    int worked;

    /**
     * Constructor for InfiniteProgressMonitor.
     * 
     * @param monitor
     * @param ticks
     */
    public InfiniteProgressMonitor(IProgressMonitor monitor, int ticks) {
        this(monitor, ticks, 0);
    }

    /**
     * Constructor for InfiniteProgressMonitor.
     * 
     * @param monitor
     * @param ticks
     * @param style
     */
    public InfiniteProgressMonitor(IProgressMonitor monitor, int ticks,
            int style) {
        super(monitor, ticks, style);
    }

    /**
     * @see org.eclipse.core.runtime.SubProgressMonitor#beginTask(java.lang.String,
     *      int)
     */
    @Override
    public void beginTask(String name, int totalWork) {
        super.beginTask(name, totalWork);
        this.totalWork = totalWork;
        this.halfWay = totalWork / 2;
        this.currentIncrement = 1;
        this.nextProgress = currentIncrement;
        this.worked = 0;
    }

    /**
     * @see org.eclipse.core.runtime.SubProgressMonitor#worked(int)
     */
    @Override
    public void worked(int work) {
        if (worked >= totalWork) {
            return;
        }
        if (--nextProgress <= 0) {
            super.worked(1);
            worked++;
            if (worked >= halfWay) {
                // we have passed the current halfway point, so double the
                // increment and reset the halfway point.
                currentIncrement *= 2;
                halfWay += (totalWork - halfWay) / 2;
            }
            // reset the progress counter to another full increment
            nextProgress = currentIncrement;
        }
    }

    /**
     * Don't allow clearing of the subtask. This will stop the flickering of the
     * subtask in the progress dialogs.
     * 
     * @see IProgressMonitor#subTask(String)
     */
    @Override
    public void subTask(String name) {
        if (name != null && !name.equals("")) { //$NON-NLS-1$
            super.subTask(name);
        }
    }
}