/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4ConnectionProvider {

    /**
     * Get connection
     * 
     * @return p4 connection
     */
    IP4Connection getConnection();

}
