/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

/**
 * Interface for UI controls to transmit errors from child to parent controls.
 * Works in conjunction with a {@link IErrorDisplay} object.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IErrorProvider {

    /**
     * Set the error display object to report errors to
     * 
     * @param display
     */
    void setErrorDisplay(IErrorDisplay display);

    /**
     * Get the current error message for the provider's current state
     * 
     * @return - message
     */
    String getErrorMessage();

    /**
     * Validate the error provider
     */
    void validate();

}
