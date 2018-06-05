/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseErrorProvider implements IErrorProvider {

    /**
     * Error display
     */
    protected IErrorDisplay errorDisplay = null;

    /**
     * Error message
     */
    protected String errorMessage = null;

    /**
     * @see com.perforce.team.ui.IErrorProvider#getErrorMessage()
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#setErrorDisplay(com.perforce.team.ui.IErrorDisplay)
     */
    public void setErrorDisplay(IErrorDisplay display) {
        this.errorDisplay = display;
        if (this.errorDisplay != null) {
            this.errorDisplay.setErrorMessage(this.errorMessage, this);
        }
    }

    /**
     * @see com.perforce.team.ui.IErrorProvider#validate()
     */
    public void validate() {
        if (this.errorDisplay != null) {
            this.errorDisplay.setErrorMessage(getErrorMessage(), this);
        }
    }

}
