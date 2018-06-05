/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.net.URI;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;

/**
 * 
 * File revision wrapper around a {@link IFileState} object. Implements
 * {@link ILocalRevision} to add access to the workspace file that the
 * {@link IFileState} is referring to.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class LocalRevision extends FileRevision implements ILocalRevision {

    private IFile localFile = null;
    private IFileState state = null;

    /**
     * 
     * @param localFile
     * @param state
     */
    public LocalRevision(IFile localFile, IFileState state) {
        this.localFile = localFile;
        this.state = state;
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        if (this.state != null) {
            return this.state.getModificationTime();
        } else {
            return this.localFile.getLocalTimeStamp();
        }
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getName()
     */
    public String getName() {
        if (this.state != null) {
            return this.state.getName();
        } else {
            return this.localFile.getName();
        }
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getStorage(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
        if (this.state != null) {
            return this.state;
        } else {
            return this.localFile;
        }
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getURI()
     */
    @Override
    public URI getURI() {
        return this.localFile.getLocationURI();
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#isPropertyMissing()
     */
    public boolean isPropertyMissing() {
        return false;
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#withAllProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IFileRevision withAllProperties(IProgressMonitor monitor)
            throws CoreException {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.ILocalRevision#getFile()
     */
    public IFile getFile() {
        return this.localFile;
    }

    /**
     * @see com.perforce.team.core.p4java.ILocalRevision#isCurrent()
     */
    public boolean isCurrent() {
        return this.state == null;
    }

    /**
     * @see com.perforce.team.core.p4java.ILocalRevision#getLocalPath()
     */
    public String getLocalPath() {
        return getURI().getPath();
    }

}
