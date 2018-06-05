package com.perforce.team.core;

/*
 * Copyright (c) 2003, 2006 Perforce Software.  All rights reserved.
 *
 */

import java.nio.charset.Charset;
import java.util.StringTokenizer;

import org.eclipse.equinox.security.storage.StorageException;

import com.perforce.p4java.CharsetDefs;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;

/**
 * Holds the connection parameters for a Perforce connection
 */
public class ConnectionParameters {

    /**
     * P4PORT
     */
    public static final String P4PORT = "P4PORT"; //$NON-NLS-1$

    /**
     * P4CLIENT
     */
    public static final String P4CLIENT = "P4CLIENT"; //$NON-NLS-1$

    /**
     * P4USER
     */
    public static final String P4USER = "P4USER"; //$NON-NLS-1$

    /**
     * P4CHARSET
     */
    public static final String P4CHARSET = "P4CHARSET"; //$NON-NLS-1$

    /**
     * P4PASSWD
     */
    public static final String P4PASSWD = "P4PASSWD"; //$NON-NLS-1$

    /**
     * SAVEPASS
     */
    public static final String SAVEPASS = "SAVEPASS"; //$NON-NLS-1$

    /**
     * OFFLINE
     */
    public static final String OFFLINE = "OFFLINE"; //$NON-NLS-1$

    /**
     * TRUE
     */
    public static final String TRUE = "true"; //$NON-NLS-1$

    /**
     * FALSE
     */
    public static final String FALSE = "false"; //$NON-NLS-1$

    private String port;
    private String client;
    private String user;
    private String charset;
    private String password;
    private String authTicket = null;
    private boolean savePassword = false;
    private boolean offline = false;

    /**
     * Create an empty object
     */
    public ConnectionParameters() {
    }

    /**
     * Constructor. Create object from the connection string which is a string
     * of the form "P4PORT 1666 P4CLIENT someclient P4USER robin"
     * 
     * @param connectString
     *            the connection string
     */
    public ConnectionParameters(String connectString) {
        StringTokenizer tokens = new StringTokenizer(connectString, " "); //$NON-NLS-1$
        while (tokens.hasMoreTokens()) {
            String key = tokens.nextToken();
            String value = unescapeSpaces(tokens.nextToken());
            if (P4PORT.equals(key)) {
                setPort(value);
            } else if (P4CLIENT.equals(key)) {
                setClient(value);
            } else if (P4USER.equals(key)) {
                setUser(value);
            } else if (P4CHARSET.equals(key)) {
                setCharset(value);
            } else if (P4PASSWD.equals(key)) {
                setPassword(value);
            } else if (SAVEPASS.equals(key)) {
                if (TRUE.equals(value)) {
                    savePassword = true;
                } else {
                    savePassword = false;
                }
            } else if (OFFLINE.equals(key)) {
                if (TRUE.equals(value)) {
                    offline = true;
                } else {
                    offline = false;
                }
            }

        }
    }

    /**
     * @return the offline
     */
    public boolean isOffline() {
        return offline;
    }

    /**
     * @param offline
     *            the offline to set
     */
    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    /**
     * Set save password
     * 
     * @param savePassword
     */
    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    /**
     * @return - true if saving password
     */
    public boolean savePassword() {
        return savePassword;
    }

    /**
     * Set port
     * 
     * @param port
     */
    public void setPort(String port) {
        this.port = checkValue(port);
    }

    /**
     * @return - port
     */
    public String getPort() {
        return port;
    }

    /**
     * @return - port or empty string if port is null
     */
    public String getPortNoNull() {
        return port == null ? "" : port; //$NON-NLS-1$
    }

    /**
     * Set client
     * 
     * @param client
     */
    public void setClient(String client) {
        this.client = checkValue(client);
    }

    /**
     * @return - client
     */
    public String getClient() {
        return client;
    }

    /**
     * @return - client or empty string if client is null
     */
    public String getClientNoNull() {
        return client == null ? "" : client; //$NON-NLS-1$
    }

    /**
     * Set user
     * 
     * @param user
     */
    public void setUser(String user) {
        this.user = checkValue(user);
    }

    /**
     * @return - user
     */
    public String getUser() {
        return user;
    }

    /**
     * @return - user or empty string if user is null
     */
    public String getUserNoNull() {
        return user == null ? "" : user; //$NON-NLS-1$
    }

    /**
     * Set charset
     * 
     * @param charset
     */
    public void setCharset(String charset) {
        this.charset = checkValue(charset);
    }

    /**
     * @return - charset
     */
    public String getCharset() {
        return charset;
    }

    /**
     * @return - charset if not 'none', will return null if 'none'
     */
    public String getCharsetNoNone() {
        if (!"none".equals(charset)) { //$NON-NLS-1$
            return charset;
        } else {
            return null;
        }
    }

    /**
     * @return - charset or empty string if charset is null
     */
    public String getCharsetNoNull() {
        return charset == null ? "" : charset; //$NON-NLS-1$
    }

    /**
     * Set password
     * 
     * @param password
     */
    public void setPassword(String password) {
        this.password = checkValue(password);
    }

    /**
     * @return - password
     */
    public String getPassword() {
        if ((password==null || password.isEmpty()) &&savePassword) {
            try {
                setPassword(P4SecureStore.INSTANCE.get(getStorageKey(), IConstants.EMPTY_STRING));
            } catch (StorageException e) {
                PerforceProviderPlugin.logError(e);
            }
        }

        return password;
    }
    
    public String peekPassword() {
        return password;
    }
    

    /**
     * @return - password or empty string if password is null
     */
    public String getPasswordNoNull() {
        String pw = getPassword();
        return pw == null ? "" : pw; //$NON-NLS-1$
    }

    /**
     * Gets the authentication ticket
     * 
     * @return the authTicket
     */
    public String getAuthTicket() {
        return authTicket;
    }

    /**
     * Sets the authentication ticket
     * 
     * @param authTicket
     *            the authTicket to set
     */
    public void setAuthTicket(String authTicket) {
        this.authTicket = authTicket;
    }

    private String checkValue(String value) {
        if (value != null) {
            value = value.trim();
            if (value.equals("")) { //$NON-NLS-1$
                value = null;
            }
        }
        return value;
    }

    /**
     * Get connection params as display strings
     * 
     * @return - display string
     */
    public String getDisplayString() {
        final StringBuilder display = new StringBuilder(getPortNoNull());
        display.append(',');
        display.append(' ');
        display.append(getClientNoNull());
        display.append(',');
        display.append(' ');
        display.append(getUserNoNull());

        String charset = getCharsetNoNone();
        if (charset != null) {
            display.append(',');
            display.append(' ');
            display.append(charset);
        }
        return display.toString();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        if (port != null) {
            s.append(P4PORT + " " + escapeSpaces(port) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (client != null) {
            s.append(P4CLIENT + " " + escapeSpaces(client) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (user != null) {
            s.append(P4USER + " " + escapeSpaces(user) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (charset != null) {
            s.append(P4CHARSET + " " + escapeSpaces(charset) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // if (password != null && savePassword) {
        //	    s.append(P4PASSWD + " " + escapeSpaces(password) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        // }
        s.append(SAVEPASS + " " + (savePassword ? TRUE : FALSE) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        s.append(OFFLINE + " " + (offline ? TRUE : FALSE) + " "); //$NON-NLS-1$ //$NON-NLS-2$
        return s.toString();
    }

    /**
     * Copy the connection params
     * 
     * @param params
     */
    public void copy(ConnectionParameters params) {
        params.setPort(getPort());
        params.setClient(getClient());
        params.setUser(getUser());
        params.setCharset(getCharset());
        params.setPassword(getPassword());
        params.setSavePassword(savePassword());
        params.setOffline(isOffline());
        params.setAuthTicket(getAuthTicket());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        String s = getPortNoNull() + getClientNoNull() + getUserNoNull()
                + getCharsetNoNull();
        return s.hashCode();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConnectionParameters)) {
            return false;
        }
        ConnectionParameters params = (ConnectionParameters) obj;
        if (!params.getPortNoNull().equals(getPortNoNull())) {
            return false;
        }
        if (!params.getClientNoNull().equals(getClientNoNull())) {
            return false;
        }
        if (!params.getUserNoNull().equals(getUserNoNull())) {
            return false;
        }
        if (!params.getCharsetNoNull().equals(getCharsetNoNull())) {
            return false;
        }
        return true;
    }

    private String escapeSpaces(String s) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
            case ' ':
                buff.append("\\s"); //$NON-NLS-1$
                break;
            case '\\':
                buff.append("\\\\"); //$NON-NLS-1$
                break;
            default:
                buff.append(ch);
                break;
            }
        }
        return buff.toString();
    }

    private String unescapeSpaces(String s) {
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch == '\\') {
                i++;
                if (s.charAt(i) == '\\') {
                    buff.append('\\');
                } else {
                    buff.append(' ');
                }
            } else {
                buff.append(ch);
            }
        }
        return buff.toString();
    }

    /**
     * 
     * @return secure storage key
     */
    public String getStorageKey() {
        return getUserNoNull()
        + IConstants.AT + getPort();
    }
    
    public static String getTicketUser(ConnectionParameters param, IServer server){
    	if(server==null)
    		return param.getUserNoNull();
        if(server.isCaseSensitive()){
        	return param.getUserNoNull();
        }else{// always convert to lowercase for case insensitive server
        	return param.getUserNoNull().toLowerCase();
        }

    }

    /**
     * @return - a valid Java charset
     */
    public static Charset getJavaCharset(IP4Resource resource) {
    	try {
    		IP4Connection conn = resource.getConnection();
    		if(conn!=null){
	    		ConnectionParameters p = conn.getParameters();
	    		String cs = PerforceCharsets.getJavaCharsetName(p.getCharset());
	    		return Charset.forName(cs);
    		}
		} catch (Exception e) {
			PerforceProviderPlugin.logWarning(e);
		}
    	return CharsetDefs.DEFAULT;
    }
}
