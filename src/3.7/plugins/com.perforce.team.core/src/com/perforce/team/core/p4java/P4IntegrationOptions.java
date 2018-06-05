/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.option.client.IntegrateFilesOptions;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.PerforceProviderPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4IntegrationOptions {
    // control after integ actions.
    private boolean trySafeResolve; // try to save resolve after integration
    private int maxFiles = -1;
    
    // shared integ options
    private boolean integrateAroundDeleted; // integrate around deleted revision (-d (2) or -Di (3))
    private boolean useHaveRev; // merge have verison instead of target head (-h)
    private boolean displayBaseDetails; // output merge info (-o)
    private boolean dontCopyToClient; // do not copy target files to workspace (-v)
    private boolean force; // discard all integration history (-f)
    private boolean reverseMapping; // integrate branch view target to source (-r only with -b)

    public IntegrateFilesOptions createIntegrateFilesOptions(int changeListId, boolean showActionsOnly){
		IntegrateFilesOptions opt = new IntegrateFilesOptions();
		opt.setChangelistId(changeListId);
		opt.setShowActionsOnly(showActionsOnly);
		
		// shared attributes
		opt.setForceIntegration(isForce());
		opt.setUseHaveRev(isUseHaveRev());
		opt.setDisplayBaseDetails(isDisplayBaseDetails());
		opt.setReverseMapping(isReverseMapping());
		opt.setDontCopyToClient(isDontCopyToClient());
		opt.setMaxFiles(getMaxFiles());

		return opt;
    }
    
    /**
     * Creates a new integrate options with all set to false
     */
    public P4IntegrationOptions() {
        this.useHaveRev = false;
        this.dontCopyToClient = false;
        this.force = false;
        this.displayBaseDetails = false;
        this.reverseMapping = false;
        this.trySafeResolve = false;

        this.integrateAroundDeleted = false;

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
    public P4IntegrationOptions(boolean useHaveRev, 
            boolean displayBaseDetails, 
            boolean dontCopyToClient, boolean force,
            boolean reverseMapping,
            boolean trySafeResolve) {
        this.useHaveRev = useHaveRev;
        this.displayBaseDetails = displayBaseDetails;
        this.dontCopyToClient = dontCopyToClient;
        this.force = force;
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

    public static P4IntegrationOptions createInstance(IServer server){
    	int serverVer = server.getServerVersionNumber();
    	String level=null;
		try {
			IServerInfo info = server.getServerInfo();
			level = info.getIntegEngine();
		} catch (Exception e) {
			PerforceProviderPlugin.logError(e.getLocalizedMessage());
		}
    	if(level==null){
    		if(serverVer<IP4ServerConstants.INTEG3_SERVERID_VERSION){ 
    			return new P4IntegrationOptions2(); // older server default to integEngine 2
    		}else{
    			return new P4IntegrationOptions3(); // newer server default to integEngine 3
    		}
    	}else{
    		if("2".equals(level.trim())){
    			return new P4IntegrationOptions2();
    		}
    		if("3".equals(level.trim())){
    			return new P4IntegrationOptions3();
    		}
    	}
    	return new P4IntegrationOptions3();
    }
    
}
