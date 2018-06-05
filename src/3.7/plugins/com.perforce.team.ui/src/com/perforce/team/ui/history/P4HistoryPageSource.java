/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.history;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4HistoryPageSource extends HistoryPageSource {

    private IP4File file;

    /**
     * Create a p4 history page source for a specified p4 file
     * 
     * @param file
     */
    public P4HistoryPageSource(IP4File file) {
        this.file = file;
    }

    /**
     * Create an empty p4 history page source
     */
    public P4HistoryPageSource() {
        this(null);
    }

    /**
     * Get file for this history page source
     * 
     * @return - p4 file this source was created with, may be null
     */
    public IP4File getFile() {
        return this.file;
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPageSource#canShowHistoryFor(java.lang.Object)
     */
    public boolean canShowHistoryFor(Object object) {
        boolean canShow = false;
        IResource resource = P4CoreUtils.getResource(object);
        if (resource instanceof IFile) {
            canShow = P4ConnectionManager.getManager().getConnection(
                    ((IFile) resource).getProject()) != null;
        } else if (object instanceof IP4File || getFile() != null) {
            canShow = true;
        } else if (object instanceof P4HistoryPageSource) {
            canShow = ((P4HistoryPageSource) object).getFile() != null;
        }
        return canShow;
    }

    /**
     * @see org.eclipse.team.ui.history.IHistoryPageSource#createPage(java.lang.Object)
     */
    public Page createPage(Object object) {
        if (this.file != null) {
            object = this.file;
        }
        return new P4HistoryPage(object);
    }

}
