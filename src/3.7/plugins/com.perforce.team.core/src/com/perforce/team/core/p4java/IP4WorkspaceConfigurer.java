/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * Interface for allowing the p4 workspace object to be configured before it
 * loads connections. This should be used to set the error handler or custom
 * server properties before servers are actually loaded so their initial state
 * will be correct and they won't need to be re-configured after they are
 * created.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4WorkspaceConfigurer {

    /**
     * Callback to configure the workspace
     * 
     * @param workspace
     */
    void configure(P4Workspace workspace);

}
