/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.swt.graphics.GC;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ITickDecorator {

    /**
     * Decorate the graphic context with the decoration for this revision
     * 
     * @param revision
     * @param x
     * @param y
     * @param gc
     */
    void decorate(IP4Revision revision, int x, int y, GC gc);

    /**
     * Dispose of the decorator
     */
    void dispose();

}
