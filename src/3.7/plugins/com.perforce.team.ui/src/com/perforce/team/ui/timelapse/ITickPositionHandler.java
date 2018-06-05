/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ITickPositionHandler {

    /**
     * Is there a next position in this handler's context
     * 
     * @param position
     * @return - true if next revision, false otherwise
     */
    boolean hasNextPosition(int position);

    /**
     * Is there a previous position in this handler's context
     * 
     * @param position
     * @return - true if previous revision, false otherwise
     */
    boolean hasPreviousPosition(int position);

    /**
     * Get previous position in this handler's context
     * 
     * @param position
     * @return - previous revision or -1 on error
     */
    int getPrevious(int position);

    /**
     * Get next position in this handler's context
     * 
     * @param position
     * @return - next revision or -1 on error
     */
    int getNext(int position);

    /**
     * Does this handler's context contain this position
     * 
     * @param position
     * @return - true if position is in context, false otherwise
     */
    boolean contains(int position);

    /**
     * Is this handler enabled
     * 
     * @return - true is enabled, false otherwise
     */
    boolean isEnabled();

    /**
     * Get total tick count
     * 
     * @return - positive integer or less than/equal 0 to show all ticks
     */
    int getTickCount();

    /**
     * Filter revisions to only proper context
     * 
     * @param revisions
     * @return - revisions
     */
    IP4Revision[] filter(IP4Revision[] revisions);

    /**
     * Get the new position that should be moved to. Returning -1 means the last
     * position should be used.
     * 
     * @return - int
     */
    int getNewPosition();

}
