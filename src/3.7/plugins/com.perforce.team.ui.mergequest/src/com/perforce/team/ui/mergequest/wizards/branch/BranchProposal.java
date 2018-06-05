/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.ui.editor.Proposal;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchProposal extends Proposal {

    private Branch branch;
    private SharedResources resources;

    /**
     * @param branch
     * @param resources
     */
    public BranchProposal(Branch branch, SharedResources resources) {
        super(branch.getName());
        this.branch = branch;
        this.resources = resources;
    }

    /**
     * Get underlying branch
     * 
     * @return branch
     */
    public Branch getBranch() {
        return this.branch;
    }

    public SharedResources getResources() {
		return this.resources;
	}

	/**
     * @see com.perforce.team.ui.editor.Proposal#getImage()
     */
    @Override
    public Image getImage() {
        IWorkbenchAdapter adapter = P4CoreUtils.convert(branch,
                IWorkbenchAdapter.class);
        if (adapter != null) {
            return this.resources.getImage(adapter.getImageDescriptor(branch));
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof BranchProposal){
    		boolean eq=super.equals(obj);
			return eq
					&& P4CoreUtils.equals(branch,
							((BranchProposal) obj).getBranch())
					&& P4CoreUtils.equals(resources,
							((BranchProposal) obj).getResources());
    	}
		return false;
    }
    
    @Override
    public int hashCode() {
    	return super.hashCode()+P4CoreUtils.hashCode(branch)*31+P4CoreUtils.hashCode(resources)*11;
    }
}
