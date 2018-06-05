/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.text.timelapse.TextAnnotateModel;
import com.perforce.team.ui.timelapse.IAuthorProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormTextAnnotateModel extends TextAnnotateModel implements
        IAuthorProvider {

    private IAuthorProvider nodeAuthorProvider;

    /**
     * @param file
     * @param prefix
     * @param useChangelistKey
     */
    public FormTextAnnotateModel(IP4File file, String prefix,
            boolean useChangelistKey) {
        super(file, prefix, useChangelistKey);
    }

    /**
     * Set node author provider
     * 
     * @param nodeAuthorProvider
     */
    public void setNodeAuthorProvider(IAuthorProvider nodeAuthorProvider) {
        this.nodeAuthorProvider = nodeAuthorProvider;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextAnnotateModel#getAuthor(int)
     */
    @Override
    public String getAuthor(int changelist) {
        String author = null;
        if (this.nodeAuthorProvider != null) {
            author = this.nodeAuthorProvider
                    .getAuthor(getRevisionById(changelist));
        }
        if (author == null) {
            author = super.getAuthor(changelist);
        }
        return author;
    }

    /**
     * @see com.perforce.team.ui.timelapse.IAuthorProvider#getAuthor(com.perforce.team.core.p4java.IP4Revision)
     */
    public String getAuthor(IP4Revision revision) {
        String author = null;
        if (revision != null) {
            author = getAuthor(getRevisionId(revision));
        }
        return author;
    }

}
