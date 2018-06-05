/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.editor.Proposal;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchProposal extends Proposal {

    private IP4Branch branch;
    private SharedResources resources;

    /**
     * @param branch
     * @param resources
     */
    public BranchProposal(IP4Branch branch, SharedResources resources) {
        super(branch.getName());
        this.branch = branch;
        this.resources = resources;
    }

    /**
     * Get underlying branch
     * 
     * @return branch
     */
    public IP4Branch getBranch() {
        return this.branch;
    }

    /**
     * @see com.perforce.team.ui.editor.Proposal#getImage()
     */
    @Override
    public Image getImage() {
        Image image = this.resources.getImage(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_BRANCH));
        if (this.branch.isLocked()) {
            OverlayIcon icon = new OverlayIcon(
                    image,
                    new ImageDescriptor[] { PerforceUIPlugin
                            .getDescriptor(IPerforceUIConstants.IMG_DEC_LOCK) },
                    new int[] { IPerforceUIConstants.ICON_TOP_RIGHT });
            image = this.resources.getOverlay(icon);
        }
        return image;
    }
    
    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof BranchProposal && super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
