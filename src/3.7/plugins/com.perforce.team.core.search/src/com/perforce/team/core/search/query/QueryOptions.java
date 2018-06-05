/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.search.query;

import com.perforce.p4java.option.server.MatchingLinesOptions;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class QueryOptions {

    private boolean allRevisions = false;
    private Set<String> paths;
    private String pattern;
    private boolean caseInsensitive = false;
    private boolean searchBinaries = false;
    private int leadingContext = -1;
    private int trailingContext = -1;

    /**
     * Create query options with a non-null specified patter
     * 
     * @param pattern
     *            - non-null
     */
    public QueryOptions(String pattern) {
        Assert.isNotNull(pattern, "Pattern cannot be null"); //$NON-NLS-1$
        this.pattern = pattern;
        this.paths = new HashSet<String>();
    }

    /**
     * @return the pattern
     */
    public String getPattern() {
        return this.pattern;
    }

    /**
     * @param pattern
     *            the pattern to set
     */
    public void setPattern(String pattern) {
        if (pattern != null) {
            this.pattern = pattern;
        }
    }

    /**
     * Add paths to query
     * 
     * @param paths
     */
    public void addPaths(String[] paths) {
        if (paths != null) {
            for (String path : paths) {
                addPath(path);
            }
        }
    }

    /**
     * Add path to query
     * 
     * @param path
     */
    public void addPath(String path) {
        if (path != null) {
            paths.add(path);
        }
    }

    /**
     * Create grep options with current query options set
     * 
     * @return - grep options
     */
    public MatchingLinesOptions createOptions() {
        MatchingLinesOptions options = new MatchingLinesOptions();
        options.setIncludeLineNumbers(true);
        options.setAllRevisions(allRevisions);
        options.setCaseInsensitive(caseInsensitive);
        options.setSearchBinaries(searchBinaries);
        options.setLeadingContext(leadingContext);
        options.setTrailingContext(trailingContext);
        return options;
    }

    /**
     * Get paths to search
     * 
     * @return non-null but possibly empty array of strings
     */
    public String[] getPaths() {
        return this.paths.toArray(new String[paths.size()]);
    }

    /**
     * @return true if all revisions, false otherwise
     */
    public boolean isAllRevisions() {
        return this.allRevisions;
    }

    /**
     * @param allRevisions
     *            the allRevisions to set
     */
    public void setAllRevisions(boolean allRevisions) {
        this.allRevisions = allRevisions;
    }

    /**
     * @return true if case insenstive, false otherwise
     */
    public boolean isCaseInsensitive() {
        return this.caseInsensitive;
    }

    /**
     * @param caseInsensitive
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * @param searchBinaries
     */
    public void setSearchBinaries(boolean searchBinaries) {
        this.searchBinaries = searchBinaries;
    }

    /**
     * @return true if searching binaries, false otherwise
     */
    public boolean isSearchBinaries() {
        return this.searchBinaries;
    }

    /**
     * @return the leadingContext
     */
    public int getLeadingContext() {
        return this.leadingContext;
    }

    /**
     * @param leadingContext
     *            the leadingContext to set
     */
    public void setLeadingContext(int leadingContext) {
        this.leadingContext = leadingContext;
    }

    /**
     * @return the trailingContext
     */
    public int getTrailingContext() {
        return this.trailingContext;
    }

    /**
     * @param trailingContext
     *            the trailingContext to set
     */
    public void setTrailingContext(int trailingContext) {
        this.trailingContext = trailingContext;
    }

}
