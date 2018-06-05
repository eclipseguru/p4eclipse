/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import com.perforce.team.core.p4java.IP4Connection;

import java.awt.Point;
import java.util.Collection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IBranchGraph extends IBranchGraphElement {

    /**
     * ELEMENT_ADDED
     */
    String ELEMENT_ADDED = "ELEMENT_ADDED"; //$NON-NLS-1$

    /**
     * ELEMENT_REMOVED
     */
    String ELEMENT_REMOVED = "ELEMENT_REMOVED"; //$NON-NLS-1$

    /**
     * Get the connection of this graph
     * 
     * @return p4 connection or null if unset
     */
    IP4Connection getConnection();

    /**
     * Set the connection for this graph to use
     * 
     * @param connection
     */
    void setConnection(IP4Connection connection);

    /**
     * Create branch
     * 
     * @param id
     * @return branch
     */
    Branch createBranch(String id);

    /**
     * Create depot path mapping
     * 
     * @param id
     * @return depot path mapping
     */
    DepotPathMapping createDepotPathMapping(String id);

    /**
     * Create branch spec mapping
     * 
     * @param id
     * @return branch spec mapping
     */
    BranchSpecMapping createBranchSpecMapping(String id);

    /**
     * Add an element to this graph
     * 
     * @param element
     * @return true if added, false otherwise
     */
    boolean add(IBranchGraphElement element);

    /**
     * Remove an element from this graph
     * 
     * @param element
     * @return true if removed, false otherwise
     */
    boolean remove(IBranchGraphElement element);

    /**
     * Get element by id
     * 
     * @param id
     * @return branch graph element
     */
    IBranchGraphElement getElementById(String id);

    /**
     * Get element by id that matches specified element type
     * 
     * @param <ElementType>
     * @param id
     * @param elementClass
     * @return element or null if not found or not matching specified type
     */
    <ElementType extends IBranchGraphElement> ElementType getElementById(
            String id, Class<ElementType> elementClass);

    /**
     * Get branch by id
     * 
     * @param id
     * @return branch or null if not found in graph
     */
    Branch getBranch(String id);

    /**
     * Get mapping by id
     * 
     * @param id
     * @return mapping or null if not found in graph
     */
    Mapping getMapping(String id);

    /**
     * Does this graph contain an element with the specified id
     * 
     * @param id
     * @return true is contains, false otherwise
     */
    boolean containsElement(String id);

    /**
     * Get all elements of the specified type
     * 
     * @param <ElementType>
     * @param elementClass
     * @return non-null but possibly empty collection
     */
    <ElementType extends IBranchGraphElement> Collection<ElementType> getElements(
            Class<ElementType> elementClass);

    /**
     * Get all elements in this graph
     * 
     * @return non-null but possibly empty array of branch graph elements
     */
    IBranchGraphElement[] getElements();

    /**
     * Get branches in graph
     * 
     * @return non-null but possibly empty array of branch
     */
    Branch[] getBranches();

    /**
     * Get mappings in graph
     * 
     * @return non-null but possibly empty array of mappings
     */
    Mapping[] getMappings();

    /**
     * Is the branch graph empty of branches?
     * 
     * @return - true if no branches, false otherwise
     */
    boolean isEmpty();

    /**
     * Update the element factories with the latest state of the elements in the
     * graph. This should only be called when importing graphs.
     */
    void updateFactories();

    /**
     * Find empty location given a starting point where a branch is not located.
     * 
     * @param startingLocation
     * @param increment
     * @return point
     */
    Point findEmptyLocation(Point startingLocation, int increment);

}
