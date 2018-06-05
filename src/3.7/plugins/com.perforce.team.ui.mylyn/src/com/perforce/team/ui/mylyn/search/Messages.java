/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.search;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.mylyn.search.messages"; //$NON-NLS-1$

    /**
     * FilterEntry_AddFilter
     */
    public static String FilterEntry_AddFilter;

    /**
     * FilterEntry_After
     */
    public static String FilterEntry_After;

    /**
     * FilterEntry_Before
     */
    public static String FilterEntry_Before;

    /**
     * FilterEntry_Contains
     */
    public static String FilterEntry_Contains;

    /**
     * FilterEntry_EndsWith
     */
    public static String FilterEntry_EndsWith;

    /**
     * FilterEntry_Equals
     */
    public static String FilterEntry_Equals;

    /**
     * FilterEntry_GreaterThan
     */
    public static String FilterEntry_GreaterThan;

    /**
     * FilterEntry_GreaterThanEqual
     */
    public static String FilterEntry_GreaterThanEqual;

    /**
     * FilterEntry_LessThan
     */
    public static String FilterEntry_LessThan;

    /**
     * FilterEntry_LessThanEqual
     */
    public static String FilterEntry_LessThanEqual;

    /**
     * FilterEntry_NotEquals
     */
    public static String FilterEntry_NotEquals;

    /**
     * FilterEntry_On
     */
    public static String FilterEntry_On;

    /**
     * FilterEntry_OnOrAfter
     */
    public static String FilterEntry_OnOrAfter;

    /**
     * FilterEntry_OnOrBefore
     */
    public static String FilterEntry_OnOrBefore;

    /**
     * FilterEntry_RemoveFilter
     */
    public static String FilterEntry_RemoveFilter;

    /**
     * FilterEntry_SelectField
     */
    public static String FilterEntry_SelectField;

    /**
     * FilterEntry_StartsWith
     */
    public static String FilterEntry_StartsWith;

    /**
     * FilterManager_all
     */
    public static String FilterManager_all;

    /**
     * FilterManager_any
     */
    public static String FilterManager_any;

    /**
     * FilterManager_ClearWords
     */
    public static String FilterManager_ClearWords;

    /**
     * FilterManager_Containing
     */
    public static String FilterManager_Containing;

    /**
     * FilterManager_FollowingWords
     */
    public static String FilterManager_FollowingWords;

    /**
     * FilterManager_MatchingConditions
     */
    public static String FilterManager_MatchingConditions;

    /**
     * FilterManager_NoPositiveRule
     */
    public static String FilterManager_NoPositiveRule;

    /**
     * JobFieldEntry_Append
     */
    public static String JobFieldEntry_Append;

    /**
     * JobFieldEntry_Prepend
     */
    public static String JobFieldEntry_Prepend;

    /**
     * JobFieldEntry_Replace
     */
    public static String JobFieldEntry_Replace;

    /**
     * P4JobQueryPage_JobQuery
     */
    public static String P4JobQueryPage_JobQuery;

    /**
     * P4JobQueryPage_LimitResults
     */
    public static String P4JobQueryPage_LimitResults;

    /**
     * P4JobQueryPage_NewSearch
     */
    public static String P4JobQueryPage_NewSearch;

    /**
     * P4JobQueryPage_QueryTitle
     */
    public static String P4JobQueryPage_QueryTitle;

    /**
     * P4JobQueryPage_SeachJobsWithQuery
     */
    public static String P4JobQueryPage_SeachJobsWithQuery;

    /**
     * P4JobQueryPage_SearchJobs
     */
    public static String P4JobQueryPage_SearchJobs;

    /**
     * P4JobQueryPage_SearchJobsWithRules
     */
    public static String P4JobQueryPage_SearchJobsWithRules;

	public static String P4JobQueryPage_OfflineError;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
