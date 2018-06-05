/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ITickFormatter {

    /**
     * Format a tick
     * 
     * @param revision
     * @param tickNumber
     * @param event
     * @return - non-null color
     */
    Color format(IP4Revision revision, int tickNumber, PaintEvent event);

}
