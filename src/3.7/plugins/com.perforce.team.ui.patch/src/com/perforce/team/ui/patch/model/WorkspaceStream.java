/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import com.perforce.p4java.CharsetDefs;
import com.perforce.team.core.P4CoreUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class WorkspaceStream extends FileStream {

    private IFile file;
    private OutputStream stream;

    /**
     * Set file
     * 
     * @param file
     */
    public void setFile(IFile file) {
        this.file = file;
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#initialize(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void initialize(final IProgressMonitor monitor) throws IOException {
        if (file == null) {
            throw new IOException("Missing workspace file"); //$NON-NLS-1$
        }
        File workspaceFile = file.getLocation().toFile();
        validateFile(workspaceFile, monitor);
        if (!monitor.isCanceled()) {
            this.stream = new FileOutputStream(workspaceFile);
        }
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#getStream()
     */
    public OutputStream getStream() throws IOException {
        return this.stream;
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#finish(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void finish(IProgressMonitor monitor) throws IOException {
        if (file != null) {
            try {
                file.refreshLocal(IResource.DEPTH_ONE,
                        new NullProgressMonitor());
            } catch (CoreException e) {
                // Ignore
            }
        }
        if (stream != null) {
            stream.close();
            stream = null;
        }
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#getCharset()
     */
    public Charset getCharset() {
        Charset charset = null;
        String name;
        try {
            name = file.getCharset(true);
            if (name != null) {
                charset = P4CoreUtils.charsetForName(name);
            }
        } catch (CoreException e) {
            charset = null;
        } catch (IllegalCharsetNameException e) {
            charset = null;
        } catch (UnsupportedCharsetException e) {
            charset = null;
        } catch (IllegalArgumentException e) {
            charset = null;
        }
        if (charset == null) {
            charset = CharsetDefs.LOCAL;
        }
        return charset;
    }

}
