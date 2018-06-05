/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.p4java.IP4Connection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class GraphContext {

    private IP4Connection connection;
    private IBranchGraphPage page;

    /**
     * Create a graph context
     * 
     * @param page
     * @param connection
     */
    public GraphContext(IBranchGraphPage page, IP4Connection connection) {
        this.page = page;
        this.connection = connection;
    }

    /**
     * Get page
     * 
     * @return - page
     */
    public IBranchGraphPage getPage() {
        return this.page;
    }

    /**
     * Get connection
     * 
     * @return - connection
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * Get branch graph builder
     * 
     * @return - builder
     */
    public IBranchGraphBuilder getBuilder() {
        return this.page.getBuilder();
    }

    /**
     * Get branch graph
     * 
     * @return - graph
     */
    public IBranchGraph getGraph() {
        return this.page.getGraph();
    }

}
