/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model.registry;

import org.eclipse.core.runtime.Assert;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchType {

    /**
     * UNKNOWN
     */
    public static final BranchType UNKNOWN = new BranchType("unknown", //$NON-NLS-1$
            Messages.BranchType_1, -1);

    private int firmness;
    private String type;
    private String label;

    /**
     * Create a new branch type
     * 
     * @param type
     * @param label
     * @param firmness
     */
    public BranchType(String type, String label, int firmness) {
        Assert.isNotNull(type, "Type cannot be null"); //$NON-NLS-1$
        Assert.isTrue(type.length() > 0, "Type cannot be empty"); //$NON-NLS-1$
        Assert.isNotNull(label, "Label cannot be null"); //$NON-NLS-1$
        Assert.isTrue(label.length() > 0, "Label cannot be empty"); //$NON-NLS-1$
        this.type = type;
        this.label = label;
        this.firmness = firmness;
    }

    /**
     * @return the firmness
     */
    public int getFirmness() {
        return this.firmness;
    }

    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BranchType) {
            BranchType other = (BranchType) obj;
            return other.firmness == this.firmness
                    && other.type.equals(this.type);
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.type.hashCode();
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return this.label;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.type + " [" + this.label + "," + this.firmness + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
