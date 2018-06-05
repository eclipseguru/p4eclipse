/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java.builder;

import java.io.File;
import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Connection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ClientBuilder {

    private ConnectionParameters params;
    private String location = null;
    private String stream;

    /**
     * Creates a new client builder with default params and location
     * 
     * @param params
     * @param location
     * @param stream 
     */
    public ClientBuilder(ConnectionParameters params, String location, String stream) {
        this.params = params;
        this.location = location;
        this.stream = stream;
    }

    public ClientBuilder(ConnectionParameters params, String location) {
        this(params,location,null);
    }

    /**
     * Build the client and send any exceptions to the specified non-null error
     * handler which can try to recover from the exceptions and retry
     * 
     * @param handler
     * @return - true if created
     */
    public boolean build(IErrorHandler handler) {
        boolean created = false;
        boolean retry = true;
        IP4Connection connection = new P4Connection(this.params);
        while (retry) {
            retry = false;
            try {
                created = build(connection);
            } catch (P4JavaException e) {
                retry = handler.shouldRetry(connection, e);
            }
        }
        return created;
    }

    /**
     * Builds the new client
     * 
     * @return - true if client was successfully created, false otherwise
     * @throws P4JavaException
     */
    public boolean build() throws P4JavaException {
        return build((IP4Connection) null);
    }

    /**
     * Get a client template
     * 
     * @param handler
     * @return - client template
     */
    public IClient getClientTemplate(IErrorHandler handler) {
        IClient template = null;
        boolean retry = true;
        IP4Connection connection = new P4Connection(this.params);
        while (retry) {
            retry = false;
            try {
                template = getClientTemplate(connection.getServer());
            } catch (P4JavaException e) {
                retry = handler.shouldRetry(connection, e);
            }
        }
        return template;
    }

    /**
     * Get a client template
     * 
     * @return - client template
     * @throws P4JavaException
     */
    public IClient getClientTemplate() throws P4JavaException {
        IP4Connection connection = new P4Connection(this.params);
        return getClientTemplate(connection.getServer());
    }

    private IClient getClientTemplate(IServer server) throws P4JavaException {
        IClient template = null;
        try {
            template = server.getClientTemplate(this.params.getClient());
        } catch (P4JavaError e) {
            PerforceProviderPlugin.logError(e);
        }
        return template;
    }

    /**
     * Builds the new client
     * 
     * @param connection
     * 
     * @return - true if client was successfully created, false otherwise
     * @throws P4JavaException
     */
    public boolean build(IP4Connection connection) throws P4JavaException {
        if (connection == null) {
            connection = new P4Connection(this.params);
        }
        IServer server = connection.getServer();
        if (server == null) {
            throw new P4JavaException(
                    Messages.ClientBuilder_RetrieveServerError);
        }
        IClient template = getClientTemplate(server);
        if (template == null) {
            return false;
        } else {
            IClient spec = new Client(server);

            // Get configured values
            StringBuilder root = new StringBuilder();
            root.append(this.location);
            if (root.length() == 0
                    || root.charAt(root.length() - 1) != File.separatorChar) {
                root.append(File.separatorChar);
            }
            
            // root.append(params.getClient());// not need any more, since we have append the client tothe name already in wizard.

            // make local dir
            try {
                File path = new File(root.toString());
                if(!path.exists()){
                    if(!path.mkdirs()){
                    	String msg=MessageFormat.format(
								Messages.ClientBuilder_CreateFolderError,
								path.getAbsolutePath());
						throw new Exception(msg);
                    }
                }
            } catch (Exception e) {
                throw new P4JavaException(e);
            }
            
            if(!StringUtils.isEmpty(stream))
            	spec.setStream(stream);
            
            spec.setRoot(root.toString());
            spec.setOwnerName(params.getUser());
            spec.setName(params.getClient());

            // Get templated values
            spec.setClientView(template.getClientView());
            spec.setHostName(template.getHostName());
            spec.setDescription(template.getDescription());
            spec.setSubmitOptions(template.getSubmitOptions());
            spec.setOptions(template.getOptions());
            spec.setLineEnd(template.getLineEnd());

            try {
                server.createClient(spec);
            } catch (P4JavaError e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        return true;
    }
}
