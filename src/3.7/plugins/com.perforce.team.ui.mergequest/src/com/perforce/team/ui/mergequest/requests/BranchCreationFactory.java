/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.requests;

import com.perforce.team.core.mergequest.model.registry.BranchType;

import org.eclipse.gef.requests.CreationFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchCreationFactory implements CreationFactory {

    /**
     * DEFAULT_BRANCH_NAME
     */
    public static final String DEFAULT_BRANCH_NAME = "branch"; //$NON-NLS-1$

    private BranchType type;

    /**
     * Create a branch creation factory for a specified branch type
     * 
     * @param type
     */
    public BranchCreationFactory(BranchType type) {
        this.type = type;
    }

    /**
     * @see org.eclipse.gef.requests.CreationFactory#getNewObject()
     */
    public Object getNewObject() {
        return null;
    }

    /**
     * @see org.eclipse.gef.requests.CreationFactory#getObjectType()
     */
    public Object getObjectType() {
        return this.type;
    }

}
