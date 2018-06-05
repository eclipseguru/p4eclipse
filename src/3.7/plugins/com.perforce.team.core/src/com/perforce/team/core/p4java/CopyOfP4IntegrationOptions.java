/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class CopyOfP4IntegrationOptions {

    /**
     * INTEGRATE_AROUND_DELETED - -d option (integ -2)
     */
    public static final String INTEGRATE_AROUND_DELETED_2 = "d"; //$NON-NLS-1$

    /**
     * INTEGRATE_AROUND_DELETED - -Di option (integ -3)
     */
    public static final String INTEGRATE_AROUND_DELETED_3 = "Di"; //$NON-NLS-1$

    // control after integ actions.
    private boolean trySafeResolve; // try to save resolve after integration
    private int maxFiles = -1;
    
    // shared integ options
    private boolean integrateAroundDeleted; // integrate around deleted revision (-d or -Di)
    private boolean useHaveRev; // merge have verison instead of target head (-h)
    private boolean displayBaseDetails; // output merge info (-o)
    private boolean dontCopyToClient; // do not copy target files to workspace (-v)
    private boolean force; // discard all integration history (-f)
    private boolean reverseMapping; // integrate branch view target to source (-r only with -b)

	// Integ -2 specific options
    private boolean baselessMerge; // allow merge source to targets even no common ancestor (-i)
    private boolean propagateType; // propagate the source file’s filetype to the target file. (-t)

    // Integ 3 specific options
    private boolean branchResolves; // Schedules 'branch resolves' instead of branching new target files automatically. A.k.a "-Rb"
    private boolean deleteResolves; // Schedules 'delete resolves' instead of deleting target files automatically. A.k.a "-Rd".
    private boolean skipIntegratedRevs; // Skips cherry-picked revisions already integrated. A.k.a "-Rs".

    /**
     * Creates a new integrate options with all set to false
     */
    public CopyOfP4IntegrationOptions() {
        this.useHaveRev = false;
        this.baselessMerge = false;
        this.displayBaseDetails = false;
        this.propagateType = false;
        this.dontCopyToClient = false;
        this.force = false;
        this.integrateAroundDeleted = false;
        this.reverseMapping = false;
        this.trySafeResolve = false;
        
        this.branchResolves=false;
        this.deleteResolves=false;
        this.skipIntegratedRevs=false;
        
    }

    /**
     * Creates a new integrate options with the specified options
     * 
     * @param useHaveRev
     * @param baselessMerge
     * @param displayBaseDetails
     * @param propagateType
     * @param dontCopyToClient
     * @param force
     * @param integrateAboutDeleted
     * @param reverseMapping
     * @param trySafeResolve
     */
    public CopyOfP4IntegrationOptions(boolean useHaveRev, boolean baselessMerge,
            boolean displayBaseDetails, boolean propagateType,
            boolean dontCopyToClient, boolean force,
            boolean integrateAboutDeleted, boolean reverseMapping,
            boolean trySafeResolve) {
        this.useHaveRev = useHaveRev;
        this.propagateType = propagateType;
        this.baselessMerge = baselessMerge;
        this.displayBaseDetails = displayBaseDetails;
        this.dontCopyToClient = dontCopyToClient;
        this.force = force;
        this.integrateAroundDeleted = integrateAboutDeleted;
        this.reverseMapping = reverseMapping;
        this.trySafeResolve = trySafeResolve;
    }

    /**
     * @return the trySafeResolve
     */
    public boolean isTrySafeResolve() {
        return this.trySafeResolve;
    }

    /**
     * @param trySafeResolve
     *            the trySafeResolve to set
     */
    public void setTrySafeResolve(boolean trySafeResolve) {
        this.trySafeResolve = trySafeResolve;
    }

    /**
     * @return the useHaveRev
     */
    public boolean isUseHaveRev() {
        return this.useHaveRev;
    }

    /**
     * @param useHaveRev
     *            the useHaveRev to set
     */
    public void setUseHaveRev(boolean useHaveRev) {
        this.useHaveRev = useHaveRev;
    }

    /**
     * @return the baselessMerge
     */
    public boolean isBaselessMerge() {
        return this.baselessMerge;
    }

    /**
     * @param baselessMerge
     *            the baselessMerge to set
     */
    public void setBaselessMerge(boolean baselessMerge) {
        this.baselessMerge = baselessMerge;
    }

    /**
     * @return the displayBaseDetails
     */
    public boolean isDisplayBaseDetails() {
        return this.displayBaseDetails;
    }

    /**
     * @param displayBaseDetails
     *            the displayBaseDetails to set
     */
    public void setDisplayBaseDetails(boolean displayBaseDetails) {
        this.displayBaseDetails = displayBaseDetails;
    }

    /**
     * @return the propagateType
     */
    public boolean isPropagateType() {
        return this.propagateType;
    }

    /**
     * @param propagateType
     *            the propagateType to set
     */
    public void setPropagateType(boolean propagateType) {
        this.propagateType = propagateType;
    }

    /**
     * @return the dontCopyToClient
     */
    public boolean isDontCopyToClient() {
        return this.dontCopyToClient;
    }

    /**
     * @param dontCopyToClient
     *            the dontCopyToClient to set
     */
    public void setDontCopyToClient(boolean dontCopyToClient) {
        this.dontCopyToClient = dontCopyToClient;
    }

    /**
     * @return the force
     */
    public boolean isForce() {
        return this.force;
    }

    /**
     * @param force
     *            the force to set
     */
    public void setForce(boolean force) {
        this.force = force;
    }

    /**
     * @return the integrateAroundDeleted
     */
    public boolean isIntegrateAroundDeleted() {
        return integrateAroundDeleted;
    }

    /**
     * @param integrateAroundDeleted
     *            the integrateAroundDeleted to set
     */
    public void setIntegrateAroundDeleted(boolean integrateAroundDeleted) {
        this.integrateAroundDeleted = integrateAroundDeleted;
    }

    /**
     * @return the reverseMapping
     */
    public boolean isReverseMapping() {
        return reverseMapping;
    }

    /**
     * @param reverseMapping
     *            the reverseMapping to set
     */
    public void setReverseMapping(boolean reverseMapping) {
        this.reverseMapping = reverseMapping;
    }

    /**
     * @return the maxFiles
     */
    public int getMaxFiles() {
        return this.maxFiles;
    }

	/**
     * @param maxFiles
     *            the maxFiles to set
     */
    public void setMaxFiles(int maxFiles) {
        this.maxFiles = maxFiles;
    }

    public boolean isBranchResolves() {
		return branchResolves;
	}

	public void setBranchResolves(boolean branchResolves) {
		this.branchResolves = branchResolves;
	}

	public boolean isDeleteResolves() {
		return deleteResolves;
	}

	public void setDeleteResolves(boolean deleteResolves) {
		this.deleteResolves = deleteResolves;
	}

	public boolean isSkipIntegratedRevs() {
		return skipIntegratedRevs;
	}

	public void setSkipIntegratedRevs(boolean skipIntegratedRevs) {
		this.skipIntegratedRevs = skipIntegratedRevs;
	}
    
}
