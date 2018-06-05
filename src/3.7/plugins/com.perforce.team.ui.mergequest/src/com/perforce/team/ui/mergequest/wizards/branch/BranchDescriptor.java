/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.registry.BranchType;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchDescriptor {

    /**
     * Branch name
     */
    private String name;

    /**
     * Branch type
     */
    private BranchType type;

    /**
     * Create empty branch descriptor
     */
    public BranchDescriptor() {
        this.name = ""; //$NON-NLS-1$
        this.type = P4BranchGraphCorePlugin.getDefault().getBranchRegistry()
                .getDefaultType();
    }

    /**
     * Create a branch descriptor from the specified descriptor
     * 
     * @param descriptor
     */
    public BranchDescriptor(BranchDescriptor descriptor) {
        this();
        if (descriptor != null) {
            setName(descriptor.getName());
            setType(descriptor.getType());
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    /**
     * @return the type
     */
    public BranchType getType() {
        return this.type;
    }

    /**
     * Set type
     * 
     * @param type
     */
    public void setType(String type) {
        if (type != null) {
            setType(P4BranchGraphCorePlugin.getDefault().getBranchRegistry()
                    .getType(type));
        }
    }

    /**
     * @param type
     *            the type to set
     */
    public void setType(BranchType type) {
        if (type != null) {
            this.type = type;
        }
    }

}
