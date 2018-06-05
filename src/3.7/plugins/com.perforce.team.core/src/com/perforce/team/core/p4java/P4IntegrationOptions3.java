/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.option.client.IntegrateFilesOptions;

/**
 * Integrate options for integEngine 3.
 * 
 * @author ali
 *
 */
public class P4IntegrationOptions3 extends P4IntegrationOptions {
    // Integ 3 specific options
    private boolean branchResolves; // Schedules 'branch resolves' instead of branching new target files automatically. A.k.a "-Rb"
    private boolean deleteResolves; // Schedules 'delete resolves' instead of deleting target files automatically. A.k.a "-Rd".
    private boolean skipIntegratedRevs; // Skips cherry-picked revisions already integrated. A.k.a "-Rs".

    public P4IntegrationOptions3(){
    	super();
        this.branchResolves=false;
        this.deleteResolves=false;
        this.skipIntegratedRevs=false;
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

	@Override
	public IntegrateFilesOptions createIntegrateFilesOptions(int changeListId,
			boolean showActionsOnly) {
		
		IntegrateFilesOptions opt = super.createIntegrateFilesOptions(changeListId, showActionsOnly);
		opt.setIntegrateAllAfterReAdd(isIntegrateAroundDeleted()); // -Di
		opt.setDeleteResolves(isDeleteResolves());
		opt.setSkipIntegratedRevs(isSkipIntegratedRevs());
		opt.setBranchResolves(isBranchResolves());
		
		return opt;
		
	}

}
