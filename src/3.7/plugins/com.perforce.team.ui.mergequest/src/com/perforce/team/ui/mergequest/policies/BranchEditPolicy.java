/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.policies;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.ui.mergequest.commands.BranchDeleteCommand;

import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ComponentEditPolicy;
import org.eclipse.gef.requests.GroupRequest;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchEditPolicy extends ComponentEditPolicy {

    /**
     * @see org.eclipse.gef.editpolicies.ComponentEditPolicy#createDeleteCommand(org.eclipse.gef.requests.GroupRequest)
     */
    @Override
    protected Command createDeleteCommand(GroupRequest deleteRequest) {
        Object parent = getHost().getParent().getModel();
        Object child = getHost().getModel();
        if (parent instanceof BranchGraph && child instanceof Branch) {
            return new BranchDeleteCommand((BranchGraph) parent, (Branch) child);
        }
        return super.createDeleteCommand(deleteRequest);
    }

}
