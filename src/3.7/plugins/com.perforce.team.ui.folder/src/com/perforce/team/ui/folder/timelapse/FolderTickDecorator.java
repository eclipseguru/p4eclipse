/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.timelapse.ActionTickDecorator;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderTickDecorator extends ActionTickDecorator {

    private FileEntry entry;

    /**
     * Set file to draw decorations for
     * 
     * @param file
     */
    public void setFile(FileEntry file) {
        this.entry = file;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ActionTickDecorator#getAction(com.perforce.team.core.p4java.IP4Revision)
     */
    @Override
    protected FileAction getAction(IP4Revision revision) {
        if (this.entry != null) {
            IFileRevisionData data = this.entry.getData(revision
                    .getChangelist());
            if (data != null
                    && data.getChangelistId() == revision.getChangelist()) {
                return data.getAction();
            }
        }
        return null;
    }

}
