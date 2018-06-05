/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IChangelist;

/**
 * Wrapper class used to pass around the selected changelist and whether this
 * was an explicit selection by the user.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistSelection {

    private int id;
    private boolean explicitSelection;

    /**
     * Create a changelist selection that is not an explicit selection and is
     * for the {@link IChangelist#UNKNOWN} id.
     */
    public ChangelistSelection() {
        this(IChangelist.UNKNOWN, false);
    }

    /**
     * Create a changelist selection with the specified id
     * 
     * @param id
     * @param explicitSelection
     */
    public ChangelistSelection(int id, boolean explicitSelection) {
        this.id = id;
        this.explicitSelection = explicitSelection;
    }

    /**
     * Get the pending changelist id
     * 
     * @return the id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Get whether the use make an explicit changelist selection either via an
     * active pending changelist or selection from a combo box of possible
     * pending changelists.
     * 
     * @return the explicitSelection
     */
    public boolean isExplicitSelection() {
        return this.explicitSelection;
    }

}
