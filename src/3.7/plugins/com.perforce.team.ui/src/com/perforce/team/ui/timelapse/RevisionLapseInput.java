/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.editor.RevisionEditorInput;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.ui.editors.text.ILocationProvider;

/**
 * Revision lapse input that is a location provider
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class RevisionLapseInput extends RevisionEditorInput implements
        ILocationProvider, ITempFileInput {

    /**
     * PREFIX
     */
    public static final String PREFIX = "p4eclipse-tlv-revision"; //$NON-NLS-1$

    /**
     * Creates a new input
     * 
     * @param revision
     */
    public RevisionLapseInput(IP4Revision revision) {
        super(revision);
    }

    /**
     * @see org.eclipse.ui.editors.text.ILocationProvider#getPath(java.lang.Object)
     */
    public IPath getPath(Object element) {
        return getTempStoragePath();
    }

    /**
     * @see com.perforce.team.ui.editor.P4BaseEditorInput#getPrefix()
     */
    @Override
    protected String getPrefix() {
        return PREFIX;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITempFileInput#deletePostLoad()
     */
    public boolean deletePostLoad() {
        return true;
    }

    /**
     * @see com.perforce.team.ui.timelapse.ITempFileInput#getFile()
     */
    public File getFile() {
        IPath path = getTempStoragePath();
        if (path != null) {
            return path.toFile();
        }
        return null;
    }

}
