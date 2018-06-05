/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.editor;

import org.eclipse.osgi.util.NLS;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "com.perforce.team.ui.search.editor.messages"; //$NON-NLS-1$

    public static String ChangelistSearchPage_CollapseAll;

    /**
     * ChangelistSearchPage_MultipleFiles
     */
    public static String ChangelistSearchPage_MultipleFiles;

    /**
     * ChangelistSearchPage_MultipleMatches
     */
    public static String ChangelistSearchPage_MultipleMatches;

    /**
     * ChangelistSearchPage_NoSearchResults
     */
    public static String ChangelistSearchPage_NoSearchResults;

    /**
     * ChangelistSearchPage_Pattern
     */
    public static String ChangelistSearchPage_Pattern;

    /**
     * ChangelistSearchPage_Search
     */
    public static String ChangelistSearchPage_Search;

    /**
     * ChangelistSearchPage_SearchChangelist
     */
    public static String ChangelistSearchPage_SearchChangelist;

    /**
     * ChangelistSearchPage_SearchExpressionDescription
     */
    public static String ChangelistSearchPage_SearchExpressionDescription;

    /**
     * ChangelistSearchPage_SearchExpressionTitle
     */
    public static String ChangelistSearchPage_SearchExpressionTitle;

    /**
     * ChangelistSearchPage_SearchOptionsTitle
     */
    public static String ChangelistSearchPage_SearchOptionsTitle;

    /**
     * ChangelistSearchPage_SearchPageTitle
     */
    public static String ChangelistSearchPage_SearchPageTitle;

    /**
     * ChangelistSearchPage_SearchResultsTitle
     */
    public static String ChangelistSearchPage_SearchResultsTitle;

    /**
     * ChangelistSearchPage_SingleFile
     */
    public static String ChangelistSearchPage_SingleFile;

    /**
     * ChangelistSearchPage_SingleMatch
     */
    public static String ChangelistSearchPage_SingleMatch;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
