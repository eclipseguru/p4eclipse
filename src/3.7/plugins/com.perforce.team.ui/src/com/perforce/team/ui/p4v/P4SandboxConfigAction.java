/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import java.util.ArrayList;
import java.util.List;

import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;

public class P4SandboxConfigAction extends P4VAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        runApplication();
    }

    /**
     * Run the p4v streams view
     * 
     * @return - array of running p4v streams view application wrappers
     */
    public P4SandboxConfigRunner[] runApplication() {
        P4Collection collection = super.getResourceSelection();
        List<P4SandboxConfigRunner> runners = new ArrayList<P4SandboxConfigRunner>();
        if (!collection.isEmpty()) {
            IP4Resource[] resources = collection.members();
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4Connection) {
                IP4Connection connection = (IP4Connection) resource;
                P4SandboxConfigRunner runner = new P4SandboxConfigRunner(connection);
                runner.run();
                runners.add(runner);
                }
            }
        }
        return runners.toArray(new P4SandboxConfigRunner[0]);
    }

    /**
     * Enable only when connected to a server with streams support.
     * 
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        
        boolean enabled = false;
        if (containsOnlineConnection()){
            P4Collection connections = getConnectionSelection();
            IP4Resource[] resources = connections.members();
            for(IP4Resource resource: resources){
                IServer server=resource.getServer();
                if(server!=null && server.getServerVersionNumber() >= 20111){
                    enabled=true;
                    break;
                }
            }
        }
        return enabled;
    }

    /**
     * @see com.perforce.team.ui.p4v.P4VAction#enableFor(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean enableFor(IP4File file) {
        return true;
    }

}
