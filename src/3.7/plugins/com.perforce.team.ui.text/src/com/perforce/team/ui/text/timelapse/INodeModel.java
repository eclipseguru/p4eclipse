/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.p4java.IP4Revision;

import org.eclipse.ui.IEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface INodeModel {

    /**
     * Node change type
     */
    enum ChangeType {

        /**
         * Node added
         */
        ADD,

        /**
         * Node deleted
         */
        DELETE,

        /**
         * Node edited
         */
        EDIT,

        /**
         * Node unchanged
         */
        UNCHANGED

    }

    /**
     * Node record
     */
    interface IRecord {

        /**
         * Get type of change
         * 
         * @return - change type
         */
        ChangeType getType();

        /**
         * Get revision of record
         * 
         * @return - p4 revision
         */
        IP4Revision getRevision();

        /**
         * Get id of node
         * 
         * @return - node id
         */
        String getId();
    }

    /**
     * Build the node model for all revisions
     */
    void buildAll();

    /**
     * Build the node model for a revision
     * 
     * @param revision
     */
    void build(IP4Revision revision);

    /**
     * Clear all node records
     */
    void clear();

    /**
     * Clear node records for a revision
     * 
     * @param revision
     */
    void clear(IP4Revision revision);

    /**
     * Get records by id
     * 
     * @param id
     * @return - non-null array of records
     */
    ChangeType[] getRecords(String id);

    /**
     * Get revisions where a specified id exists
     * 
     * @param id
     * @return - non-null but possibly empty array of revisions
     */
    IP4Revision[] getRevisions(String id);

    /**
     * Get number of times an id has changes hashes across all revisions
     * 
     * @param id
     * @return - number of hash changes
     */
    int getChangeCount(String id);

    /**
     * Parse the editor input into a context specific object used to find nodes
     * 
     * @param input
     * @param revision
     * @return - node input
     */
    Object parseInput(IEditorInput input, IP4Revision revision);

    /**
     * Find nodes from a node input and a revision
     * 
     * @param nodeInput
     * @param revision
     */
    void findNodes(Object nodeInput, IP4Revision revision);

    /**
     * Get ordered revisions where the node at the specified id changes
     * 
     * @param id
     * @return - non-null array of changed revisions
     */
    IP4Revision[] getChangeRevisions(String id);

    /**
     * Get the total change count for an id
     * 
     * @param revision
     * @param id
     * @return - change
     */
    int getTotalChangeCount(IP4Revision revision, String id);

    /**
     * Get the relative change count for an id
     * 
     * @param revision
     * @param id
     * @return - change count
     */
    int getRelativeChangeCount(IP4Revision revision, String id);

    /**
     * Get a unique handle id for this element
     * 
     * @param element
     * @return - unique string id
     */
    String getHandle(Object element);

    /**
     * Get node records for an id
     * 
     * @param id
     * @return - non-null array of records
     */
    IRecord[] getNodeRecords(String id);

    /**
     * Get a node given a revision and id
     * 
     * @param id
     * @param revision
     * @return - node or null if none found
     */
    Object getNode(String id, IP4Revision revision);

    /**
     * Is the model complete, meaning does it contain node records for every
     * revision?
     * 
     * @return - true if a node record exists for every revision
     */
    boolean isComplete();

    /**
     * Did the specified node id change between the previous revision and the
     * current revision. This method will return true if the specified revision
     * is the first revision.
     * 
     * @param id
     * @param revision
     * @return - record or null if none found
     */
    boolean isChanged(String id, IP4Revision revision);

}
