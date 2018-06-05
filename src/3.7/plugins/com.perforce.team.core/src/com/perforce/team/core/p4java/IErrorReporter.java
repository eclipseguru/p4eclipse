/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IErrorReporter {

    /**
     * Sets this reporter's error handler
     * 
     * @param handler
     */
    void setErrorHandler(IErrorHandler handler);

    /**
     * Gets this reporter's error handler
     * 
     * @return - error handler
     */
    IErrorHandler getErrorHandler();

    /**
     * Handles a p4j exception
     * 
     * @param exception
     * @return - true to retry the operation, false to continue and do nothing
     */
    boolean handleError(P4JavaException exception);

    /**
     * Handles an array of at least one error p4j file spec
     * 
     * @param specs
     */
    void handleErrors(IFileSpec[] specs);

}
