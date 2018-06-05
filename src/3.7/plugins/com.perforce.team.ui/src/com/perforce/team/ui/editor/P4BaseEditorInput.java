/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4BaseEditorInput extends PlatformObject implements
        IWorkbenchAdapter, IStorageEditorInput, IPathEditorInput {

    /**
     * Storage field
     */
    protected IStorage storage = null;

    /**
     * Temp file storage
     */
    protected File tempFile;

    /**
     * Get temp storage path
     * 
     * @return - temp storage path or null if temp file is null
     */
    protected IPath getTempStoragePath() {
        IPath path = null;
        if (tempFile != null) {
            path = Path.fromOSString(tempFile.getAbsolutePath());
        }
        return path;
    }

    /**
     * Get suffix to use for storage
     * 
     * @return - suffix
     */
    protected String getSuffix() {
        return ".txt"; //$NON-NLS-1$
    }

    /**
     * Get prefix to use for storage
     * 
     * @return - prefix
     */
    protected String getPrefix() {
        return "p4depotfile"; //$NON-NLS-1$
    }

    /**
     * Get storage contents that will cache the entry in a temp file
     * 
     * @param wrapStorage
     * @return - input stream
     * @throws CoreException
     */
    protected InputStream getStorageContents(IStorage wrapStorage)
            throws CoreException {
        boolean useDirect = false;
        InputStream stream = null;

        // Put contents in a temp file so a p4 print only needs to
        // be done once per editor opening
        if (tempFile == null || !tempFile.exists()) {
            stream = wrapStorage.getContents();
            if (stream != null) {
                FileOutputStream output = null;
                InputStream input = stream;
                try {
                    String prefix = getPrefix();
                    if (prefix.length() < 3) {
                        prefix = new StringBuilder("___").replace(0, //$NON-NLS-1$
                                prefix.length(), prefix).toString();
                    }
                    tempFile = File.createTempFile(prefix, getSuffix());
                    tempFile.deleteOnExit();
                    output = new FileOutputStream(tempFile);

                    byte[] buffer = new byte[4096];
                    int read = stream.read(buffer);
                    while (read > 0) {
                        output.write(buffer, 0, read);
                        read = stream.read(buffer);
                    }
                    stream = new FileInputStream(tempFile);
                } catch (FileNotFoundException e) {
                    useDirect = true;
                } catch (IOException e) {
                    useDirect = true;
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        // Ignore
                    }
                    if (output != null) {
                        try {
                            output.flush();
                            output.close();
                        } catch (IOException e) {
                            // Ignore
                        }
                    }
                }
            }
        } else {
            try {
                stream = new FileInputStream(tempFile);
            } catch (FileNotFoundException e) {
                tempFile = null;
                useDirect = true;
            }
        }
        if (useDirect) {
            stream = wrapStorage.getContents();
        }
        return stream;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IStorage.class == adapter) {
            return this.storage;
        }
        if (IWorkbenchAdapter.class == adapter) {
            return this;
        }
        return super.getAdapter(adapter);
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
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        return getImageDescriptor();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return getName();
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * @see org.eclipse.ui.IPathEditorInput#getPath()
     */
    public IPath getPath() {
        return getTempStoragePath();
    }

}
