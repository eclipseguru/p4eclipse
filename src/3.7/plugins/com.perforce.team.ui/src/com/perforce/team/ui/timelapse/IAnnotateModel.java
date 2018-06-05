/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IAnnotateModel {

    /**
     * Annotation type
     */
    enum Type {

        /**
         * REVISION
         */
        REVISION,

        /**
         * CHANGELIST
         */
        CHANGELIST,

        /**
         * DATE
         */
        DATE
    }

    /**
     * Model listener
     */
    public interface IModelListener {

        /**
         * Model loaded
         * 
         * @param model
         */
        void loaded(IAnnotateModel model);

    }

    /**
     * Generate an editor input from a revision
     * 
     * @param revision
     * @return - storage editor input
     */
    IStorageEditorInput generateInput(IP4Revision revision);

    /**
     * Load the annotation model
     * 
     * @param revisions
     * @param includeBranches
     * @param ignoreType
     */
    void load(IP4Revision[] revisions, boolean includeBranches,
            IP4File.WhitespaceIgnoreType ignoreType);

    /**
     * Add a load listener
     * 
     * @param listener
     */
    void addListener(IModelListener listener);

    /**
     * Remove a load listener
     * 
     * @param listener
     */
    void removeListener(IModelListener listener);

    /**
     * Get author of changelist
     * 
     * @param changelist
     * @return - author or null if not found for specified changelist.
     */
    String getAuthor(int changelist);

    /**
     * Get date of changelist
     * 
     * @param changelist
     * @return - date string or null if not found for specified changelist.
     */
    String getDate(int changelist);

    /**
     * Get revision number of changelist
     * 
     * @param changelist
     * @return - revision or -1 if not found for specified changelist.
     */
    int getRevision(int changelist);

    /**
     * Latest revision
     * 
     * @return - latest revision
     */
    IP4Revision getLatest();

    /**
     * Earliest revision
     * 
     * @return - earliest revision
     */
    IP4Revision getEarliest();

    /**
     * Get following revision
     * 
     * @param revision
     * @return - next revision of null if none
     */
    IP4Revision getNext(IP4Revision revision);

    /**
     * Get number of revisions
     * 
     * @return - revision count
     */
    int getRevisionCount();

    /**
     * Clear the annotate model
     */
    void clear();

    /**
     * Get key value for specified revision. Keys could be revision number or
     * changelist id.
     * 
     * @param revision
     * @return - key to use for revision
     */
    int getRevisionId(IP4Revision revision);

    /**
     * Get a revision by id
     * 
     * @param id
     * @return - p4 revision
     */
    IP4Revision getRevisionById(int id);

}
