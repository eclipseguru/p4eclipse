/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.synchronize;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Revision;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.core.variants.IResourceVariantComparator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSyncInfo extends SyncInfo {

    private IP4File p4File;

    /**
     * @param local
     * @param base
     * @param remote
     * @param comparator
     * @param p4File
     */
    public PerforceSyncInfo(IResource local, IResourceVariant base,
            IResourceVariant remote, IResourceVariantComparator comparator,
            IP4File p4File) {
        super(local, base, remote, comparator);
        this.p4File = p4File;
    }

    /**
     * Gets the p4 file
     * 
     * @return - p4 file
     */
    public IP4File getP4File() {
        return this.p4File;
    }

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	boolean eq=obj instanceof PerforceSyncInfo && super.equals(obj);
    	return eq && P4CoreUtils.equals(getP4File(), ((PerforceSyncInfo) obj).getP4File());
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return this.p4File==null?super.hashCode():super.hashCode()+this.p4File.hashCode();
    }

}
