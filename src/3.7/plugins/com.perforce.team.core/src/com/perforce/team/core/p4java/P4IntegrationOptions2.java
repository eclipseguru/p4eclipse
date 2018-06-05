/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.option.client.IntegrateFilesOptions;

/**
 * Integrate options for integEngine 2.
 * 
 * @author ali
 *
 */
public class P4IntegrationOptions2 extends P4IntegrationOptions{

	// Integ -2 specific options
    private boolean baselessMerge; // allow merge source to targets even no common ancestor (-i)
    private boolean propagateType; // propagate the source file’s filetype to the target file. (-t)

    /**
     * Creates a new integrate options with all set to false
     */
    public P4IntegrationOptions2() {
    	super();
        
        this.baselessMerge = false;
        this.propagateType = false;
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
    public P4IntegrationOptions2(boolean useHaveRev, 
            boolean displayBaseDetails, 
            boolean dontCopyToClient, boolean force,
            boolean reverseMapping,
            boolean trySafeResolve,
            boolean integrateAboutDeleted,
            boolean baselessMerge,
            boolean propagateType
            ) {
    	super(useHaveRev, displayBaseDetails, dontCopyToClient, force, reverseMapping, trySafeResolve);

    	this.baselessMerge = baselessMerge;
        this.propagateType = propagateType;
        setIntegrateAroundDeleted(integrateAboutDeleted);
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

	@Override
	public IntegrateFilesOptions createIntegrateFilesOptions(int changeListId,
			boolean showActionsOnly) {
		
		IntegrateFilesOptions opt = super.createIntegrateFilesOptions(changeListId, showActionsOnly);
		opt.setIntegrateAroundDeletedRevs(isIntegrateAroundDeleted());
		opt.setDoBaselessMerge(isBaselessMerge());
		opt.setPropagateType(isPropagateType());
		
		return opt;
		
	}
    
}
