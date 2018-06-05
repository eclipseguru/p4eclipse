/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.ui.PerforceUIPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ClientFileEditorInput extends PlatformObject implements
        IStorageEditorInput, IPathEditorInput {

    private File clientFile;
    private IStorage storage;

    /**
     * Create a new client file editor input
     * 
     * @param clientFile
     */
    public ClientFileEditorInput(File clientFile) {
        this.clientFile = clientFile;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClientFileEditorInput) {
            return this.clientFile
                    .equals(((ClientFileEditorInput) obj).clientFile);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.clientFile!=null)
    		return this.clientFile.hashCode();
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }

    /**
     * @see org.eclipse.ui.IStorageEditorInput#getStorage()
     */
    public IStorage getStorage() throws CoreException {
        if (storage == null) {
            final IPath fullPath = getPath();
            storage = new P4Storage() {

                @Override
                public IPath getFullPath() {
                    return fullPath;
                }

                @Override
                public String getName() {
                    return clientFile.getName();
                }

                public InputStream getContents() throws CoreException {
                    FileInputStream stream = null;
                    try {
                        stream = new FileInputStream(clientFile);
                    } catch (FileNotFoundException e) {
                        throw new CoreException(new Status(IStatus.ERROR,
                                PerforceUIPlugin.ID, IStatus.OK,
                                "Error creating file input stream", e));
                    }
                    return stream;
                }
            };
        }
        return storage;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return false;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return this.clientFile.getName();
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return this.clientFile.getAbsolutePath();
    }

    /**
     * @see org.eclipse.ui.IPathEditorInput#getPath()
     */
    public IPath getPath() {
        return Path.fromOSString(this.clientFile.getAbsolutePath());
    }

}
