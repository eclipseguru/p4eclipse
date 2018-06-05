/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.p4java.IP4File;

import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class HaveQuickDiffProvider extends P4QuickDiffProvider {

    private int lastHave = Integer.MIN_VALUE;

    /**
     * @see com.perforce.team.ui.editor.P4QuickDiffProvider#getInputStream(com.perforce.team.core.p4java.IP4File,
     *      org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    protected InputStream getInputStream(IP4File file, IProgressMonitor monitor) {
        this.lastHave = file.getHaveRevision();
        InputStream stream = null;
        if (this.lastHave > 0) {
            stream = file.getHaveContents();
        }
        return stream;
    }

    /**
     * @see com.perforce.team.ui.editor.P4QuickDiffProvider#shouldRefresh(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean shouldRefresh(IP4File file) {
        return this.lastHave != file.getHaveRevision();
    }
}
