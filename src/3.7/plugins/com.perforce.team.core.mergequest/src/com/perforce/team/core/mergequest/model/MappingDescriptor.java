/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.util.Locale;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingDescriptor {

    /**
     * Change type between branches
     */
    public static enum ChangeType {

        /**
         * No permission to know what needs to be integrate
         */
        NO_PERMISSION,

        /**
         * Changes exist visible from the selected client
         */
        VISIBLE_CHANGES,

        /**
         * No changes
         */
        NO_CHANGES,

        /**
         * Request failed or never run
         */
        UNKNOWN;

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

    }

    private int sourceCount = 0;
    private int targetCount = 0;
    private int latestSource = -1;
    private int latestTarget = -1;
    private ChangeType sourceChangeType = ChangeType.UNKNOWN;
    private ChangeType targetChangeType = ChangeType.UNKNOWN;

    /**
     * Create a mapping descriptor
     */
    public MappingDescriptor() {
    }

    /**
     * @return the sourceCount
     */
    public int getSourceCount() {
        return this.sourceCount;
    }

    /**
     * @param sourceCount
     *            the sourceCount to set
     * @return true if set, false otherwise
     */
    public boolean setSourceCount(int sourceCount) {
        boolean set = false;
        if (sourceCount >= 0 && sourceCount != this.sourceCount) {
            this.sourceCount = sourceCount;
            set = true;
        }
        return set;
    }

    /**
     * @return the targetCount
     */
    public int getTargetCount() {
        return this.targetCount;
    }

    /**
     * @param targetCount
     *            the targetCount to set
     * @return true if set, false otherwise
     */
    public boolean setTargetCount(int targetCount) {
        boolean set = false;
        if (targetCount >= 0 && targetCount != this.targetCount) {
            this.targetCount = targetCount;
            set = true;
        }
        return set;
    }

    /**
     * @return the latestSource
     */
    public int getLatestSource() {
        return this.latestSource;
    }

    /**
     * @param latestSource
     *            the latestSource to set
     * @return - true if set, false otherwise
     */
    public boolean setLatestSource(int latestSource) {
        boolean set = false;
        if (latestSource > 0 && latestSource != this.latestSource) {
            this.latestSource = latestSource;
            set = true;
        }
        return set;
    }

    /**
     * @return the latestTarget
     */
    public int getLatestTarget() {
        return this.latestTarget;
    }

    /**
     * @param latestTarget
     *            the latestTarget to set
     * @return - true if set, false otherwise
     */
    public boolean setLatestTarget(int latestTarget) {
        boolean set = false;
        if (latestTarget > 0 && latestTarget != this.latestTarget) {
            this.latestTarget = latestTarget;
            set = true;
        }
        return set;
    }

    /**
     * @return the sourceChangeType
     */
    public ChangeType getSourceChangeType() {
        return this.sourceChangeType;
    }

    /**
     * @param sourceChangeType
     *            the sourceChangeType to set
     * @return - true if set, false otherwise
     */
    public boolean setSourceChangeType(ChangeType sourceChangeType) {
        boolean set = false;
        if (sourceChangeType != null
                && sourceChangeType != this.sourceChangeType) {
            this.sourceChangeType = sourceChangeType;
            set = true;
        }
        return set;
    }

    /**
     * @return the targetChangeType
     */
    public ChangeType getTargetChangeType() {
        return this.targetChangeType;
    }

    /**
     * @param targetChangeType
     *            the targetChangeType to set
     * @return - true if set, false otherwise
     */
    public boolean setTargetChangeType(ChangeType targetChangeType) {
        boolean set = false;
        if (targetChangeType != null
                && targetChangeType != this.targetChangeType) {
            this.targetChangeType = targetChangeType;
            set = true;
        }
        return set;
    }

}
