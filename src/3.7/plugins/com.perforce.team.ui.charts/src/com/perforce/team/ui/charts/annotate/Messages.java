/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.charts.annotate.messages"; //$NON-NLS-1$

    /**
     * AnnotateChart_LineCountHistory
     */
    public static String AnnotateChart_LineCountHistory;

    /**
     * AnnotateChartView_AnnotateChartMessage
     */
    public static String AnnotateChartView_AnnotateChartMessage;

    /**
     * TimeLapseStatsPage_Branches
     */
    public static String TimeLapseStatsPage_Branches;

    /**
     * TimeLapseStatsPage_ChangedBy
     */
    public static String TimeLapseStatsPage_ChangedBy;

    /**
     * TimeLapseStatsPage_ChangesPerYear
     */
    public static String TimeLapseStatsPage_ChangesPerYear;

    /**
     * TimeLapseStatsPage_Edits
     */
    public static String TimeLapseStatsPage_Edits;

    /**
     * TimeLapseStatsPage_LastChanged
     */
    public static String TimeLapseStatsPage_LastChanged;

    /**
     * TimeLapseStatsPage_LinesByUser
     */
    public static String TimeLapseStatsPage_LinesByUser;

    /**
     * TimeLapseStatsPage_Merges
     */
    public static String TimeLapseStatsPage_Merges;

    /**
     * TimeLapseStatsPage_Moves
     */
    public static String TimeLapseStatsPage_Moves;

    /**
     * TimeLapseStatsPage_Others
     */
    public static String TimeLapseStatsPage_Others;

    /**
     * TimeLapseStatsPage_RangeMultiple
     */
    public static String TimeLapseStatsPage_RangeMultiple;

    /**
     * TimeLapseStatsPage_RangeSingle
     */
    public static String TimeLapseStatsPage_RangeSingle;

    /**
     * TimeLapseStatsPage_Renames
     */
    public static String TimeLapseStatsPage_Renames;

    /**
     * TimeLapseStatsPage_RevisionsByUser
     */
    public static String TimeLapseStatsPage_RevisionsByUser;

    /**
     * TimeLapseStatsView_ViewMessage
     */
    public static String TimeLapseStatsView_ViewMessage;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
