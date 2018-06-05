/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;

/**
 * Base error handler class. Classes should subclass this class instead of
 * implementing {@link IErrorHandler} directly in order to shield against future
 * changes to that interface.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ErrorHandler implements IErrorHandler {

    /**
     * @see com.perforce.team.core.p4java.IErrorHandler#handleErrorSpecs(com.perforce.p4java.core.file.IFileSpec[])
     */
    public void handleErrorSpecs(IFileSpec[] specs) {
        // Does nothing by default, subclasses should override
    }

    /**
     * @see com.perforce.team.core.p4java.IErrorHandler#shouldRetry(com.perforce.team.core.p4java.IP4Connection,
     *      com.perforce.p4java.exception.P4JavaException)
     */
    public boolean shouldRetry(IP4Connection connection,
            P4JavaException exception) {
        // Returns false by default, subclasses should override
        return false;
    }

}
