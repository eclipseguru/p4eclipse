/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PatchException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create empty patch exception
     */
    public PatchException() {
        super();
    }

    /**
     * Create patch exception with message
     * 
     * @param message
     */
    public PatchException(String message) {
        super(message);
    }

}
