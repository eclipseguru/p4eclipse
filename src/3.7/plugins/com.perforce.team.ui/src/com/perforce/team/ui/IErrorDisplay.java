/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

/**
 * Simple interface for controls that can display error messages to the user
 * through an interface.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IErrorDisplay {

    /**
     * Set the error message to display
     * 
     * @param message
     * @param provider
     */
    void setErrorMessage(String message, IErrorProvider provider);

    /**
     * Get the current error message
     * 
     * @return - mesasge
     */
    String getErrorMessage();

}
