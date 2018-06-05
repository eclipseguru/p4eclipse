/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IJobProxy extends IWorkbenchAdapter, IAdaptable {

    /**
     * Get connection
     * 
     * @return - non-null connection
     */
    IP4Connection getConnection();

    /**
     * Get id
     * 
     * @return - non-null id
     */
    String getId();

}
