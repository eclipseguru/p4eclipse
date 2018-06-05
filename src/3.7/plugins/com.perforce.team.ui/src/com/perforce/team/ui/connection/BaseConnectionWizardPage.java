/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.connection;

import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4BrowsableConnection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4ServerConnection;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseConnectionWizardPage extends WizardPage {

    /**
     * @param pageName
     */
    protected BaseConnectionWizardPage(String pageName) {
        super(pageName);
    }

    /**
     * Get current connection parameters
     * 
     * @return - connection parameters
     */
    protected ConnectionParameters getCurrentParams() {
        return getCurrentParams(false);
    }

    /**
     * Get current connection parameters
     * 
     * @param ignoreClient
     * 
     * @return - connection parameters
     */
    protected ConnectionParameters getCurrentParams(boolean ignoreClient) {
        ConnectionParameters params = new ConnectionParameters();
        params.setUser(getUser());
        if (!ignoreClient) {
            params.setClient(getClient());
        }
        params.setPort(getPort());
        params.setCharset(getCharset());
        params.setPassword(getPassword());
        params.setAuthTicket(((IConnectionWizard) getWizard()).getAuthTicket());
        return params;
    }

    /**
     * Create a connection for the current settings
     * 
     * @return - connection representing current state of wizard page
     */
    protected IP4Connection createConnection() {
        return createConnection(false, false);
    }

    /**
     * Create a connection for the current settings
     * 
     * @param ignoreClient
     * @param serverOnly
     * 
     * @return - connection representing current state of wizard page
     */
    protected IP4Connection createConnection(boolean ignoreClient,
            boolean serverOnly) {
        IP4Connection connection = null;
        ConnectionParameters params = getCurrentParams(ignoreClient);
        if (!serverOnly) {
            connection = new P4Connection(params);
        } else {
            connection = new P4ServerConnection(params);
        }
        return connection;
    }

    /**
     * Create a browseable connection from the current params
     * 
     * @return - browseable connection
     */
    protected P4BrowsableConnection createBrowseConnection() {
        ConnectionParameters params = getCurrentParams();
        P4BrowsableConnection connection = new P4BrowsableConnection(params);
        return connection;
    }

    /**
     * @see org.eclipse.jface.wizard.WizardPage#canFlipToNextPage()
     */
    @Override
    public boolean canFlipToNextPage() {
        return isPageComplete();
    }

    /**
     * Get the port
     * 
     * @return - server port
     */
    public String getPort() {
        return ((IConnectionWizard) getWizard()).getPort();
    }

    /**
     * Get the user
     * 
     * @return - perforce user
     */
    public String getUser() {
        return ((IConnectionWizard) getWizard()).getUser();
    }

    /**
     * Get the client workspace
     * 
     * @return - perforce client workspace
     */
    public String getClient() {
        return ((IConnectionWizard) getWizard()).getClient();
    }

    /**
     * Get the user password
     * 
     * @return - password
     */
    public String getPassword() {
        return ((IConnectionWizard) getWizard()).getPassword();
    }

    /**
     * Get the charset
     * 
     * @return - charset
     */
    public String getCharset() {
        return ((IConnectionWizard) getWizard()).getCharset();
    }

    /**
     * Update the wizard's cached auth ticket
     * 
     * @param connection
     */
    protected void updateAuthTicket(IP4Connection connection) {
        if (connection != null) {
            IServer server = connection.getServer();
            if (server != null) {
                String ticket = server.getAuthTicket();
                if (ticket != null) {
                    updateAuthTicket(ticket);
                }
            }
        }
    }

    /**
     * Update the wizard's cached auth ticket
     * 
     * @param ticket
     */
    protected void updateAuthTicket(String ticket) {
        ((IConnectionWizard) getWizard()).setAuthTicket(ticket);
    }

    protected IP4Connection getWizardConnection(){
    	IWizard wizard = getWizard();
    	if(wizard instanceof AbstractConnectionWizard){
    		return ((AbstractConnectionWizard)wizard).getConnection();
    	}
    	return null;
    }

    protected void setWizardConnection(IP4Connection conn){
    	IWizard wizard = getWizard();
    	if(wizard instanceof AbstractConnectionWizard){
    		((AbstractConnectionWizard)wizard).setConnection(conn);
    	}
    }
}
