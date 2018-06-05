/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4Storage;

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class RevisionEditorInput extends P4BaseEditorInput {

    /**
     * Revision
     */
    protected IP4Revision revision = null;

    /**
     * Create a new file editor input around the specified revision.
     * 
     * @param revision
     */
    public RevisionEditorInput(IP4Revision revision) {
        this.revision = revision;
    }

    /**
     * @see com.perforce.team.ui.editor.P4BaseEditorInput#getSuffix()
     */
    @Override
    protected String getSuffix() {
        String name = this.revision.getRemotePath();
        if (name != null) {
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                return name.substring(lastDot);
            }
        }
        return super.getSuffix();
    }

    /**
     * @see com.perforce.team.ui.editor.P4BaseEditorInput#getPrefix()
     */
    @Override
    protected String getPrefix() {
        String name = this.revision.getName();
        if (name != null) {
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                return name.substring(0, lastDot);
            }
        }
        return super.getSuffix();
    }

    /**
     * Get wrapped storage
     * 
     * @return - storage
     * @throws CoreException
     */
    protected IStorage getWrappedStorage() throws CoreException {
        return revision.getStorage(new NullProgressMonitor());
    }

    /**
     * @see org.eclipse.ui.IStorageEditorInput#getStorage()
     */
    public IStorage getStorage() throws CoreException {
        if (storage == null) {
            final IStorage wrapStorage = getWrappedStorage();
            storage = new P4Storage() {

                @Override
                public IPath getFullPath() {
                    return getTempStoragePath();
                }

                @Override
                public String getName() {
                    return wrapStorage.getName();
                }

                public InputStream getContents() throws CoreException {
                    return getStorageContents(wrapStorage);
                }
            };
        }
        return this.storage;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return this.revision.getName() + " #" + this.revision.getRevision(); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return this.revision.getContentIdentifier();
    }

}
