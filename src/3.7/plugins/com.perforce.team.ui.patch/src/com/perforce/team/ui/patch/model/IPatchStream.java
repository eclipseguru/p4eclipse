/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IPatchStream {

    /**
     * Initialize the patch stream
     * 
     * @param monitor
     * @throws IOException
     */
    void initialize(IProgressMonitor monitor) throws IOException;

    /**
     * Get output stream. This method may return null if initialization fails or
     * is cancelled
     * 
     * @return output stream
     * @throws IOException
     */
    OutputStream getStream() throws IOException;

    /**
     * Get character set
     * 
     * @return charset
     */
    Charset getCharset();

    /**
     * Finish the creation of the patch
     * 
     * @param monitor
     * @throws IOException
     */
    void finish(IProgressMonitor monitor) throws IOException;

}
