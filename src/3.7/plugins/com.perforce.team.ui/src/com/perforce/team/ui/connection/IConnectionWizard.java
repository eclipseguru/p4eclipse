/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import org.eclipse.jface.wizard.IWizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IConnectionWizard extends IWizard {

    /**
     * Get the port string of the server
     * 
     * @return - perforce server port string
     */
    String getPort();

    /**
     * Get the user for the server
     * 
     * @return - perforce user
     */
    String getUser();

    /**
     * Get the client for the server
     * 
     * @return - perforce client workspace name
     */
    String getClient();

    /**
     * Get the charset for the connection
     * 
     * @return - charset
     */
    String getCharset();

    /**
     * Get the password for the connection
     * 
     * @return - password
     */
    String getPassword();

    /**
     * Get the current authentication ticket to use
     * 
     * @return - auth ticket
     */
    String getAuthTicket();

    /**
     * Set the auth ticket to use for connections created in the wizard
     * 
     * @param authTicket
     */
    void setAuthTicket(String authTicket);
    
    String getStream();
}
