/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.results;

import com.perforce.p4java.core.IFileLineMatch;
import com.perforce.p4java.core.IFileLineMatch.MatchType;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.changelists.Folder;
import com.perforce.team.ui.changelists.Folder.FolderBuilder;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.search.P4UiSearchPlugin;
import com.perforce.team.ui.search.query.P4SearchQuery;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.text.AbstractTextSearchResult;
import org.eclipse.search.ui.text.IEditorMatchAdapter;
import org.eclipse.search.ui.text.IFileMatchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SearchResult extends AbstractTextSearchResult implements
        IWorkbenchAdapter {

    private IP4Connection connection;
    private Set<IP4File> files;
    private P4SearchQuery query;
    private int trueCount = 0;
    private Folder[] folders = new Folder[0];
    private Folder[] compressed = new Folder[0];

    /**
     * Create a new p4 search result for the specified query
     * 
     * @param query
     */
    public P4SearchResult(P4SearchQuery query) {
        this.query = query;
        this.files = new HashSet<IP4File>();
        this.connection = P4CoreUtils.convert(query, IP4Connection.class);
    }

    /**
     * 
     * @see org.eclipse.search.ui.text.AbstractTextSearchResult#removeAll()
     */
    @Override
    public void removeAll() {
        trueCount = 0;
        super.removeAll();
        this.folders = new Folder[0];
        this.compressed = new Folder[0];
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object object) {
        return getElements();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object object) {
        return ((P4SearchResult) object).getLabel();
    }

    /**
     * @see org.eclipse.search.ui.ISearchResult#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return P4UiSearchPlugin.getDescriptor("icons/depot_search.png"); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.search.ui.ISearchResult#getLabel()
     */
    public String getLabel() {
        return MessageFormat.format(
                Messages.P4SearchResult_TextNumMatchesInConnection, this.query
                        .getText(), trueCount, this.connection.getParameters()
                        .getPort());
    }

    /**
     * @see org.eclipse.search.ui.ISearchResult#getQuery()
     */
    public ISearchQuery getQuery() {
        return this.query;
    }

    /**
     * @see org.eclipse.search.ui.ISearchResult#getTooltip()
     */
    public String getTooltip() {
        return getLabel();
    }

    /**
     * Add file line matches to search result
     * 
     * @param matches
     */
    public void addAll(IFileLineMatch[] matches) {
        if (matches != null) {
            Arrays.sort(matches, new Comparator<IFileLineMatch>() {

                public int compare(IFileLineMatch o1, IFileLineMatch o2) {
                    MatchType t1 = o1.getType();
                    MatchType t2 = o2.getType();
                    int diff = 0;
                    if (t1 == t2) {
                        diff = o1.getLineNumber() - o2.getLineNumber();
                    } else if (MatchType.MATCH == t1) {
                        diff = -1;
                    } else if (MatchType.MATCH == t2) {
                        diff = 1;
                    }
                    return diff;
                }
            });
            for (IFileLineMatch match : matches) {
                add(match);
            }

            Object[] files = getFiles();
            this.folders = new FolderBuilder(files, Type.TREE).build(false);
            this.compressed = new FolderBuilder(files, Type.COMPRESSED)
                    .build(true);
        }
    }

    /**
     * Add file line match to search result
     * 
     * @param match
     */
    private void add(IFileLineMatch match) {
        if (match != null) {
            IP4File file = this.connection.getFile(match.getDepotFile());
            if (file != null) {
                this.files.add(file);
                RevisionMatch revision = new RevisionMatch(file,
                        match.getRevision());
                if (MatchType.MATCH == match.getType()) {
                    trueCount++;
                }
                addMatch(new FileMatch(this.connection, revision, match));
            }
        }
    }

    /**
     * Get revision matches for specified file
     * 
     * @param file
     * @return - revision matches
     */
    public RevisionMatch[] getRevisions(IP4File file) {
        List<RevisionMatch> revs = new ArrayList<RevisionMatch>();
        if (file != null) {
            for (Object element : getElements()) {
                if (file.equals(((RevisionMatch) element).getFile())) {
                    revs.add((RevisionMatch) element);
                }
            }
        }
        return revs.toArray(new RevisionMatch[revs.size()]);
    }

    /**
     * Get all files with one or more matches
     * 
     * @return non-null but possibly empty array of files
     */
    public IP4File[] getFiles() {
        return this.files.toArray(new IP4File[this.files.size()]);
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getEditorMatchAdapter()
     */
    @Override
    public IEditorMatchAdapter getEditorMatchAdapter() {
        return null;
    }

    /**
     * @see org.eclipse.search.ui.text.AbstractTextSearchResult#getFileMatchAdapter()
     */
    @Override
    public IFileMatchAdapter getFileMatchAdapter() {
        return null;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return getImageDescriptor();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * Get root folders
     * 
     * @return non-null but possibly empty array of folders
     */
    public Folder[] getFolders() {
        return this.folders;
    }

    /**
     * Get compressed folders
     * 
     * @return non-null but possibly empty array of folders
     */
    public Folder[] getCompressed() {
        return this.compressed;
    }

}
