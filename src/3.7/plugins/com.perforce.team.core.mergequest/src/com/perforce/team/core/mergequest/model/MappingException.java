/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingException extends Exception {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Create an empty mapping exception
     */
    public MappingException() {
        super();
    }

    /**
     * Create a new mapping exception with the specified message
     * 
     * @param message
     */
    public MappingException(String message) {
        super(message);
    }

    /**
     * Create a new mapping exception with the specified throwable
     * 
     * @param throwable
     */
    public MappingException(Throwable throwable) {
        super(throwable);
    }

}
