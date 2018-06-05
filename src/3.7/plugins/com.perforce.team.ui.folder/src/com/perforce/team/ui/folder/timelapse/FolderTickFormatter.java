/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.ui.timelapse.ITickFormatter;

import java.util.Collection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderTickFormatter implements ITickFormatter {

    private Color color;
    private FileEntry entry;
    private Collection<Integer> aheadLists;

    /**
     * Set the changelists ahead of the current have list
     * 
     * @param aheads
     */
    public void setAheads(Collection<Integer> aheads) {
        this.aheadLists = aheads;
    }

    /**
     * Set file to draw decorations for
     * 
     * @param file
     */
    public void setFile(FileEntry file) {
        this.entry = file;
    }

    /**
     * Set the color
     * 
     * @param color
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITickFormatter#format(com.perforce.team.core.p4java.IP4Revision,
     *      int, org.eclipse.swt.events.PaintEvent)
     */
    public Color format(IP4Revision revision, int tickNumber, PaintEvent event) {
        if (color != null) {
            if (entry != null) {
                IFileRevisionData data = this.entry.getData(revision
                        .getChangelist());
                if (data != null
                        && data.getChangelistId() == revision.getChangelist()) {
                    boolean outOfSync = false;
                    if (entry.getHaveRevision() == 0) {
                        outOfSync = !P4File.isActionDelete(entry.getLast()
                                .getAction());
                    } else if (data.getRevision() > entry.getHaveRevision()) {
                        outOfSync = true;
                    }
                    if (outOfSync) {
                        return event.display.getSystemColor(SWT.COLOR_DARK_RED);
                    } else if (data.getChangelistId() == revision
                            .getChangelist()) {
                        return this.color;
                    }
                }
            } else if (this.aheadLists != null) {
                if (this.aheadLists.contains(revision.getChangelist())) {
                    return event.display.getSystemColor(SWT.COLOR_DARK_RED);
                }
            }
        }
        return event.display.getSystemColor(SWT.COLOR_DARK_GRAY);
    }

}
