/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.team.core.p4java.IP4Changelist;

/**
 * Interface for a editor part that is configured with an array of changelist
 * fixes
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IFixEditorPart {

    /**
     * Set the fixes for the editor's task. This method is not guaranteed to be
     * called on the UI-thread.
     * 
     * @param fixes
     * @return - the number of fixes used by this part from the specified
     *         changelist array
     */
    int setFixes(IP4Changelist[] fixes);

}
