/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IProposal extends Comparable<IProposal> {

    /**
     * Get display string of this proposal. Returned value must be non-null and
     * non-empty.
     * 
     * @return - non-null and non-empty string for the display of this proposal
     */
    String getDisplay();

    /**
     * Get the replacement string for this proposal. Returned value must be
     * non-null and non-empty.
     * 
     * @return - non-null and non-empty string for the value of this proposal
     */
    String getValue();

    /**
     * Get the image for this proposal.
     * 
     * @return - image or null if no image should be used
     */
    Image getImage();

}
