/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import org.eclipse.jface.viewers.ColumnViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IIntegrateTaskContainer {

    /**
     * Display mode
     */
    public enum Mode {

        /**
         * FLAT
         */
        FLAT,

        /**
         * USER
         */
        USER,

        /**
         * DATE
         */
        DATE,

        /**
         * TASK
         */
        TASK

    }

    /**
     * Get current mode
     * 
     * @return - non-null mode
     */
    Mode getMode();

    /**
     * Get viewer
     * 
     * @return - viewer
     */
    ColumnViewer getViewer();

}
