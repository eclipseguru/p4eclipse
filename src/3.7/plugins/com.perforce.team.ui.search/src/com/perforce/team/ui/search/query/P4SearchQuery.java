/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.search.query.QueryOptions;
import com.perforce.team.ui.search.results.P4SearchResult;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SearchQuery extends PlatformObject implements ISearchQuery {

    private IP4Connection connection;
    private QueryOptions options;
    private P4SearchResult result;

    /**
     * 
     * @param connection
     * @param options
     */
    public P4SearchQuery(IP4Connection connection, QueryOptions options) {
        this.connection = connection;
        this.options = options;
    }

    /**
     * Get the text
     * 
     * @return - text
     */
    public String getText() {
        return this.options.getPattern();
    }

    /**
     * @see org.eclipse.search.ui.ISearchQuery#canRerun()
     */
    public boolean canRerun() {
        return true;
    }

    /**
     * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
     */
    public boolean canRunInBackground() {
        return true;
    }

    /**
     * @see org.eclipse.search.ui.ISearchQuery#getLabel()
     */
    public String getLabel() {
        return MessageFormat.format(
                Messages.P4SearchQuery_SearchingConnectionForPattern,
                this.connection.getParameters().getPort(),
                this.options.getPattern());
    }

    /**
     * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
     */
    public ISearchResult getSearchResult() {
        if (result == null) {
            result = new P4SearchResult(this);
        }
        return result;
    }

    /**
     * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus run(IProgressMonitor monitor)
            throws OperationCanceledException {
        P4SearchResult p4Result = (P4SearchResult) getSearchResult();
        p4Result.removeAll();
        IFileLineMatch[] matches = this.connection.searchDepot(
                this.options.getPattern(), this.options.createOptions(),
                this.options.getPaths());
        p4Result.addAll(matches);
        return Status.OK_STATUS;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IP4Connection.class || adapter == IP4Resource.class) {
            return this.connection;
        }
        return super.getAdapter(adapter);
    }

}
