/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import com.perforce.p4java.CharsetDefs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileSystemStream extends FileStream {

    private String path;
    private FileOutputStream stream;

    /**
     * Set path
     * 
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#initialize(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void initialize(final IProgressMonitor monitor) throws IOException {
        if (path == null) {
            throw new IOException("Path not set"); //$NON-NLS-1$
        }
        File file = new File(path);
        validateFile(file, monitor);
        if (!monitor.isCanceled()) {
            this.stream = new FileOutputStream(file);
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
        if (stream != null) {
            this.stream.close();
            this.stream = null;
        }
    }

    /**
     * @see com.perforce.team.ui.patch.model.IPatchStream#getCharset()
     */
    public Charset getCharset() {
        return CharsetDefs.LOCAL;
    }

}
