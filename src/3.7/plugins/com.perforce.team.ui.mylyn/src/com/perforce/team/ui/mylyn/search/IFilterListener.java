/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.search;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFilterListener {

    /**
     * Filter entry added
     * 
     * @param entry
     */
    void added(FilterEntry entry);

    /**
     * Filter entry removed
     * 
     * @param removed
     */
    void removed(FilterEntry removed);

    /**
     * Filter entry modified
     * 
     * @param modified
     */
    void modified(FilterEntry modified);

    /**
     * Filter words modified
     * 
     * @param words
     */
    void wordsModified(String words);

}
