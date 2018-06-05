/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IErrorCollector {

    /**
     * Collect the throwable
     * 
     * @param throwable
     */
    void collect(Throwable throwable);

    /**
     * Get error count
     * 
     * @return error count
     */
    int getErrorCount();

    /**
     * Get non-null but possibly empty array of throwable errors
     * 
     * @return throwable array
     */
    Throwable[] getErrors();

    /**
     * Error collecting completed
     */
    void done();

}
