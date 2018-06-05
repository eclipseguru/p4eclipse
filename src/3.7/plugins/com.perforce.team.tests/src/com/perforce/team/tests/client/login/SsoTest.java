package com.perforce.team.tests.client.login;

import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4JavaCallback;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SsoTest extends ConnectionBasedTestCase {

    private String p4LoginSso = null;

    /**
     * Creates the parameters
     */
    @Override
    protected void initParameters() {
        assertNotNull(System.getProperty("p4.client.sso"));
        assertNotNull(System.getProperty("p4.user.sso"));
        assertNotNull(System.getProperty("p4.port.sso"));
        assertNotNull(System.getProperty("p4.login.sso"));
        parameters = new ConnectionParameters();
        parameters.setClient(System.getProperty("p4.client.sso"));
        parameters.setUser(System.getProperty("p4.user.sso"));
        parameters.setPort(System.getProperty("p4.port.sso"));
        parameters.setPassword(System.getProperty("p4.password.sso"));
        parameters.setCharset(System.getProperty("p4.charset.sso"));
        this.p4LoginSso = System.getProperty("p4.login.sso");
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        P4Workspace.getWorkspace().getAdvancedProperties()
                .setProperty(P4JavaCallback.SSO_CMD_ENV_KEY, this.p4LoginSso);
    }
    
    protected void startServer() {};

    /**
     * @see com.perforce.team.tests.P4TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        P4Workspace.getWorkspace().getAdvancedProperties()
                .remove(P4JavaCallback.SSO_CMD_ENV_KEY);
    }

    /**
     * Test sso from rpc server impl
     */
    public void testSsoRpcProp() {
    	P4Workspace.getWorkspace().getAdvancedProperties()
        .setProperty(P4JavaCallback.SSO_CMD_ENV_KEY, this.p4LoginSso);
        IP4Connection connection = createConnection(this.parameters, true,
                false);
        assertNotNull(connection.getServer());
        connection.logout();
        connection.connect();
        assertNull(connection.getServer().getAuthTicket());
        connection.login("");
        // Connect required since the next client crypto the ticket will be read
        // since rpc login does not add the -p arg.
        connection.connect();
        assertNotNull(connection.getServer());
        assertNotNull(connection.getServer().getAuthTicket());
    }

    /**
     * Test not setting P4LOGINSSO and failing back to other auth trigger
     */
    public void testFallbackAuthRpc() {
        P4Workspace.getWorkspace().getAdvancedProperties()
                .remove(P4JavaCallback.SSO_CMD_ENV_KEY);
        IP4Connection connection = createConnection(this.parameters, true,
                false);
        assertNotNull(connection.getServer());
        connection.logout();
        connection.connect();
        assertNull(connection.getServer().getAuthTicket());
        connection.login("secret");
        // Connect required since the next client crypto the ticket will be read
        // since rpc login does not add the -p arg.
        connection.connect();
        assertNotNull(connection.getServer());
        assertNotNull(connection.getServer().getAuthTicket());
    }

}
