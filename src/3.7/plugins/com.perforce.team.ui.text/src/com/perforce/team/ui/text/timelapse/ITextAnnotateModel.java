/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.timelapse.IAnnotateModel;

import org.eclipse.jface.text.source.ILineRange;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface ITextAnnotateModel extends IAnnotateModel {

    /**
     * Line data
     */
    public class Line {

        /**
         * Upper range identifier
         */
        public int upper;

        /**
         * Lower range identifier
         */
        public int lower;

        /**
         * Line text
         */
        public String data;
    }

    /**
     * Annotation
     */
    public class Annotation {

        Line[] lines;
        IP4Revision current;
        IP4Revision next;
    }

    /**
     * Get line ranges for the specified revision
     * 
     * @param revision
     * @return - array of line ranges
     */
    ILineRange[] getLineRanges(IP4Revision revision);

    /**
     * Get lines in revision
     * 
     * @param revision
     * @return - revision lines
     */
    Line[] getLines(IP4Revision revision);

    /**
     * Is latest revision line different than specified line/revision.
     * 
     * @param revision
     * @param line
     * @param number
     * @return - true if different, false if same or not present in latest
     */
    boolean isLatestDifferent(IP4Revision revision, Line line, int number);

    /**
     * Get number of positions from first position to the specified revision of
     * the specified line number in the specified revision
     * 
     * @param lineNumber
     * @param revision
     * @return - position that will be greater or equal to zero
     */
    int getPositionTo(int lineNumber, IP4Revision revision);

}
