/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IErrorHandler {

    /**
     * Callback with a p4j exception occurs and an error handler may be able to
     * fix the problem. If true is returned the operation will be attempted
     * again.
     * 
     * @param connection
     * @param exception
     * @return - true to rety, false to not
     */
    boolean shouldRetry(IP4Connection connection, P4JavaException exception);

    /**
     * Handles how to display, log,Êetc. an array containing error specs
     * 
     * @param specs
     */
    void handleErrorSpecs(IFileSpec[] specs);

}
