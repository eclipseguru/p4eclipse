/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class Proposal implements IProposal {

    /**
     * EMPTY
     */
    public static final String EMPTY = Messages.Proposal_EmptyProposal;

    private String display = null;
    private String value = null;

    /**
     * Create a proposal with a specified value
     * 
     * @param value
     */
    public Proposal(String value) {
        this(value, null);
    }

    /**
     * Create a proposal with a specified value and display
     * 
     * @param value
     * @param display
     */
    public Proposal(String value, String display) {
        this.value = value;
        this.display = display;
        if (this.value == null || this.value.length() == 0) {
            this.value = EMPTY;
        }
        if (this.display != null && this.display.length() == 0) {
            this.display = null;
        }
    }

    /**
     * @see com.perforce.team.ui.editor.IProposal#getDisplay()
     */
    public String getDisplay() {
        return this.display != null ? this.display : this.value;
    }

    /**
     * @see com.perforce.team.ui.editor.IProposal#getValue()
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof IProposal) {
            return this.value.equals(((IProposal) obj).getValue());
        }
        return false;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * Compare a proposal to this one
     * 
     * @param proposal
     * @return - compare int
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IProposal proposal) {
        return getDisplay().compareToIgnoreCase(proposal.getDisplay());
    }

    /**
     * @see com.perforce.team.ui.editor.IProposal#getImage()
     */
    public Image getImage() {
        return null;
    }

}
