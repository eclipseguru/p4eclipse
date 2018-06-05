/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface ISearchResultProvider {

    /**
     * Get search result
     * 
     * @return - search result
     */
    P4SearchResult getResult();

}
