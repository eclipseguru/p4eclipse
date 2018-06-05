/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IMappingVisitor {

    /**
     * Visit the specified mapping
     * 
     * @param mapping
     * @param branch
     * @return true to continue, false to end visiting
     */
    boolean visit(Mapping mapping, Branch branch);

}
