/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4merge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.P4ClientUtil;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.p4java.actions.P4Action;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4MergeResolveAction extends P4Action {

    /**
     * Run the p4 merge for each file in the selection
     * 
     * @return - array of running p4 merge application wrappers
     */
    public MergeRunner[] runApplication() {
        List<MergeRunner> runners = new ArrayList<MergeRunner>();
        P4Collection collection = getResourceSelection();
        for (IP4Resource resource : collection.members()) {
            if (resource instanceof IP4File) {
                IP4File file = (IP4File) resource;
                MergeRunner runner = runApplication(file, 0);
                if (runner != null)
                    runners.add(runner);
            }
        }
        return runners.toArray(new MergeRunner[0]);
    }

    /**
     * Run the p4 merge for the specified integ spec of the specified file
     * 
     * @return - running p4 merge application wrapper
     */
    public MergeRunner runApplication(IP4File file, int whichIntegSpec) {
        if (file == null)
            return null;
        createCollection(new IP4Resource[] { file }).resolve(
                new ResolveFilesAutoOptions().setShowActionsOnly(true)
                        .setShowBase(true).setResolveFileContentChanges(true));
        
        if (file.getIntegrationSpecs().length == 0)
            return null;
        IFileSpec integSpec = file.getIntegrationSpecs()[whichIntegSpec];
        // for non-text type, no baseFile and baseRev, replace them
        int baseRev = P4ClientUtil.getBaseRev(integSpec);
        String basePath = P4ClientUtil.getBaseFile(integSpec)+"#" + baseRev; //$NON-NLS-1$
        
        int startFromRev = Math.max(1, integSpec.getStartFromRev());
        int endFromRev = integSpec.getEndFromRev();
        String theirsPath = integSpec.getFromFile() + P4ClientUtil.computeTheirRev(integSpec);
        String yoursPath = file.getLocalPath();
        
        MergeRunner runner = new MergeRunner(file.getConnection(), file,
                basePath, true, theirsPath, true, yoursPath, false, startFromRev, endFromRev);
        runner.setAsync(isAsync());
        runner.run();
        return runner;
    }

    /**
     * Run a resolve and return the resources. This method will only return a
     * non-empty array if {@link #isAsync()} returns true.
     * 
     * @return - array of resolved resources
     */
    public IP4Resource runResolve(IP4File file, int whichIntegSpec) {
        MergeRunner runner = runApplication(file, whichIntegSpec);
        if (runner == null)
            return null;
        return runner.getResolved();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        // runApplication();
    }

}
