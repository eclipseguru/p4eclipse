/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.BranchSpec;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.Label;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.ImportProjectAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ConnectionBasedTestCase extends P4TestCase {

    /**
     * Connection parameters object
     */
    protected ConnectionParameters parameters = null;

    /**
     * p4d process
     */
    protected Process p4d = null;

    /**
     * Server root
     */
    protected File serverRoot = null;

    /**
     * Unicode server?
     */
    protected boolean unicode = false;

    /**
     * Creates a connection from the params
     * 
     * @return - valid connection
     */
    protected IP4Connection createConnection() {
        return createConnection(useRpc());
    }

    /**
     * Creates a connection from the params
     * 
     * @param rpc
     * @return - valid connection
     */
    protected IP4Connection createConnection(boolean rpc) {
        return createConnection(parameters, rpc, true);
    }

    /**
     * Creates a connection from the params
     * 
     * @param params
     * 
     * @return - valid connection
     */
    protected IP4Connection createConnection(ConnectionParameters params) {
        return createConnection(params, useRpc(), true);
    }

    /**
     * Creates a connection from the params
     * 
     * @param params
     * 
     * @param rpc
     * @param check
     * @return - valid connection
     */
    protected IP4Connection createConnection(ConnectionParameters params,
            boolean rpc, boolean check) {
        IP4Connection connection = null;
        if (!rpc) {
            connection = new P4Connection(params);
        } else {
            connection = new P4Connection(params) {

                @Override
                protected String getProtocol() {
                    return "p4jrpc";
                }

            };
        }
        connection.login(params.getPassword());
        connection.connect();

        if (check) {
            assertTrue(connection.isConnected());
            assertNotNull(connection.getClient());
        }
        return connection;
    }

    /**
     * Creates a project with the specified path
     * 
     * @param path
     * @return - project
     */
    protected IProject createProject(String path) {
        ImportProjectAction checkout = new ImportProjectAction();
        IP4Connection connection = createConnection();
        connection.setOffline(false);
        assertFalse(connection.isOffline());
        IP4Folder projectP4Folder = new P4Folder(connection, null, path);
        assertNotNull(projectP4Folder.getClient());
        assertNotNull(projectP4Folder.getRemotePath());
        projectP4Folder.updateLocation();
        assertNotNull(projectP4Folder.getLocalPath());
        StructuredSelection selection = new StructuredSelection(projectP4Folder);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        checkout.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());

        checkout.runAction(new NullProgressMonitor(), false);

        String name = projectP4Folder.getName();

        P4Collection collection = new P4Collection(
                new IP4Resource[] { projectP4Folder });
        collection.forceSync(new NullProgressMonitor());

        IProject project = ResourcesPlugin.getWorkspace().getRoot()
                .getProject(name);
        assertNotNull(project);
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e1) {
            assertFalse(true);
        }
        assertTrue(project.exists());
        assertTrue(project.isAccessible());
        assertTrue(project.isOpen());
        return project;
    }

    /**
     * Creates the parameters
     */
    protected void initParameters() {
        String user = System.getProperty("p4.user.live");
        if (user == null) {
            user = "user";
        }
        String client = System.getProperty("p4.client.live");
        if (client == null) {
            client = "client";
        }
        String port = System.getProperty("p4.port.live");
        if (port == null) {
            port = "localhost:1666";
        }
        parameters = new ConnectionParameters();
        parameters.setClient(client);
        parameters.setUser(user);
        parameters.setPort(port);
        parameters.setPassword(System.getProperty("p4.password.live"));
        if (this.unicode) {
            parameters.setCharset("utf8");
        }
    }

    /**
     * Get absolute path to p4d to run for this test
     * 
     * @return path to p4d to exec
     * @throws IOException
     */
    protected String getP4dPath() throws IOException {
        String path = "servers/2013.1/"
                + Platform.getOS().toLowerCase(Locale.ENGLISH) + "/"
                + Platform.getOSArch().toLowerCase(Locale.ENGLISH) + "/p4d";
        path = Utils.getBundlePath(path);
        assertNotNull(path);
        new P4JavaSysFileCommandsHelper().setExecutable(path, true, false);
        return path;
    }

    /**
     * Start the p4d server required for this test
     */
    protected void startServer() {
        BufferedReader reader = null;
        try {
            File testsDir = getTestDir();
            serverRoot = new File(testsDir, "p4d" + System.nanoTime());
            assertFalse(serverRoot.exists());
            assertTrue(serverRoot.mkdirs());
            assertTrue(serverRoot.exists());
            serverRoot.deleteOnExit();
            String p4dPath = getP4dPath();

            if (this.unicode) {
                ProcessBuilder builder = new ProcessBuilder(p4dPath, "-r",
                        serverRoot.getAbsolutePath(), "-p",
                        parameters.getPort(), "-xi", "-L","/tmp/"+serverRoot.getAbsolutePath().replace("/", "_")+".log", "-vserver=5");
                Process unicodeP4d = builder.start();
                unicodeP4d.waitFor();
            }

            ProcessBuilder builder = new ProcessBuilder(p4dPath, "-r",
                    serverRoot.getAbsolutePath(), "-p", parameters.getPort(), "-L","/tmp/"+serverRoot.getAbsolutePath().replace("/", "_")+".log", "-vserver=5");
            this.p4d = builder.start();
            assertNotNull(this.p4d);
            reader = new BufferedReader(new InputStreamReader(
                    p4d.getInputStream()));
            String line = reader.readLine();
            if (line == null) {
                throw new Exception("P4D not producing output");
            }
            if (!line.endsWith("starting...")) {
                String first = line;
                line = reader.readLine();
                if (line == null || !line.endsWith("starting...")) {
                    throw new Exception(
                            "P4D not producing 'starting...' message: " + first
                                    + "\n" + line);
                }
            }
            IServer server = getServer(this.parameters);
            createClient(server, this.parameters);
        } catch (Throwable e) {
            try {
                StringWriter writer = new StringWriter();
                e.printStackTrace(new PrintWriter(writer));
                handle(writer.toString(), e);
            } finally {
                stopServer();
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Get test dir
     * 
     * @return test dir root
     */
    protected File getTestDir() {
        String dir = System.getProperty("tests.tmpdir");
        if (dir == null) {
            dir = System.getProperty("java.io.tmpdir");
        }
        assertNotNull(dir);
        File testsDir = new File(dir);
        if (!testsDir.exists()) {
            testsDir.mkdirs();
        }
        assertTrue(testsDir.exists());
        assertTrue(testsDir.isDirectory());
        return testsDir;
    }

    /**
     * Get test server
     * 
     * @param parameters
     * @return test server
     * @throws Exception
     */
    protected IServer getServer(ConnectionParameters parameters)
            throws Exception {
        IServer server = ServerFactory.getServer(
                "p4java://" + parameters.getPort(), null);
        assertNotNull(server);
        server.setUserName(parameters.getUser());
        server.setCharsetName(parameters.getCharsetNoNone());
        server.connect();
        assertTrue(server.isConnected());
        return server;
    }

    /**
     * Create a client
     * 
     * @param server
     * @param parameters
     * @param root
     * @return client
     * @throws Exception
     */
    protected IClient createClient(IServer server,
            ConnectionParameters parameters, String root) throws Exception {
        IClient client = server
                .getClientTemplate(parameters.getClient(), false);
        File clientRoot = new File(root);
        if (!clientRoot.exists()) {
            assertTrue(clientRoot.mkdirs());
        }
        assertTrue(clientRoot.isDirectory());
        assertTrue(clientRoot.exists());
        clientRoot.deleteOnExit();
        client.setRoot(clientRoot.getAbsolutePath());
        String created = server.createClient(client);
        assertNotNull(created);
        assertTrue(created.contains(parameters.getClient()));
        server.setCurrentClient(client);
        return server.getClient(parameters.getClient());
    }

    /**
     * Create a client
     * 
     * @param server
     * @param parameters
     * @return client
     * @throws Exception
     */
    protected IClient createClient(IServer server,
            ConnectionParameters parameters) throws Exception {
        return createClient(server, parameters, getTestDir() + File.separator
                + parameters.getClient() + System.nanoTime());
    }

    /**
     * Stop the server
     */
    protected void stopServer() {
        if (p4d != null) {
            p4d.destroy();
        }
        if (serverRoot != null) {
            serverRoot.deleteOnExit();
        }
    }

    /**
     * @see com.perforce.team.tests.P4TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        try {
            for (IP4Connection connection : P4ConnectionManager.getManager()
                    .getConnections()) {
                P4ConnectionManager.getManager().removeConnection(connection);
            }
            assertEquals(0,
                    P4ConnectionManager.getManager().getConnections().length);
        } finally {
            try {
                stopServer();
            } finally {
                super.tearDown();
            }
        }
    }

    /**
     * Configure properties
     */
    protected void configureProperties() {
        P4Workspace.getWorkspace().getAdvancedProperties()
                .setProperty("socketPoolSize", "5");
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configureProperties();
        initParameters();
        startServer();
    }

    /**
     * @see junit.framework.TestCase#runBare()
     */
    @Override
    public void runBare() throws Throwable {
        Throwable exception = null;
        try {
            setUp();
        } catch (Throwable t) {
            try {
                tearDown();
            } catch (Throwable t1) {
                // Ignore
            }
            throw t;
        }
        try {
            runTest();
        } catch (Throwable running) {
            exception = running;
        } finally {
            try {
                tearDown();
            } catch (Throwable tearingDown) {
                if (exception == null) {
                    exception = tearingDown;
                }
            }
        }
        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Create job
     * 
     * @return job
     * @throws Exception
     */
    protected IP4Job createJob() throws Exception {
        IP4Connection connection = createConnection();
        Map<String, Object> jobFields = new HashMap<String, Object>();
        jobFields.put("Job", "new");
        jobFields.put("Description", "Test Job " + System.nanoTime());
        jobFields.put("Status", "open");
        jobFields.put("User", connection.getParameters().getUser());
        IP4Job created = connection.createJob(jobFields);
        assertNotNull(created);
        return created;
    }

    /**
     * Create label
     * 
     * @return label
     * @throws Exception
     */
    protected ILabel createLabel() throws Exception {
        IP4Connection connection = createConnection();
        ILabel label = new Label();
        String name = "label" + System.nanoTime();
        label.setOwnerName(connection.getParameters().getUser());
        ViewMap<ILabelMapping> viewMapping = new ViewMap<ILabelMapping>();
        viewMapping.addEntry(new Label.LabelMapping(0, "//depot/..."));
        label.setViewMapping(viewMapping);
        label.setName(name);
        label.setDescription("test label " + System.nanoTime());
        String output = connection.getServer().createLabel(label);
        assertNotNull(output);
        return connection.getServer().getLabel(name);
    }

    /**
     * Create branch
     * 
     * @return branch
     * @throws Exception
     */
    protected IBranchSpec createBranch() throws Exception {
        IP4Connection connection = createConnection();
        IBranchSpec branch = new BranchSpec();
        String name = "branch" + System.nanoTime();
        branch.setOwnerName(connection.getParameters().getUser());
        ViewMap<IBranchMapping> viewMapping = new ViewMap<IBranchMapping>();
        viewMapping.addEntry(new BranchSpec.BranchViewMapping(0, "//depot/...",
                "//depot/a/..."));
        branch.setBranchView(viewMapping);
        branch.setName(name);
        branch.setDescription("test branch " + System.nanoTime());
        String output = connection.getServer().createBranchSpec(branch);
        assertNotNull(output);
        return connection.getServer().getBranchSpec(name);
    }

    /**
     * Create second user and client
     * 
     * @return client
     * @throws Exception
     */
    protected IClient createSecondUserAndClient() throws Exception {
        ConnectionParameters parameters2 = new ConnectionParameters();
        parameters2.setClient(parameters.getClient() + "2");
        parameters2.setUser(parameters.getUser() + "a");
        parameters2.setPort(parameters.getPort());
        parameters2.setPassword(parameters.getPassword());
        return createClient(getServer(parameters2), parameters2);
    }

    /**
     * Create pending changelist
     * 
     * @param client
     * @return changelist
     * @throws Exception
     */
    protected IChangelist createPendingChangelist(IClient client)
            throws Exception {
        IChangelist cl = new Changelist();
        cl.setId(IChangelist.UNKNOWN);
        cl.setClientId(client.getName());
        cl.setDescription("test");
        cl.setUsername(client.getServer().getUserName());
        cl = client.createChangelist(cl);
        assertNotNull(cl);
        return cl;
    }

    /**
     * Add file to depot with specified name from current project
     * 
     * @param client
     * @param cl
     * @param location
     * @param content
     * @return changelist
     * @throws Exception
     */
    protected IChangelist openFile(IClient client, IChangelist cl,
            String location, InputStream content) throws Exception {
        assertNotNull(client);
        assertNotNull(cl);
        assertNotNull(location);
        List<IFileSpec> fileSpec = P4FileSpecBuilder.makeFileSpecList(location);
        List<IExtendedFileSpec> status = client.getServer().getExtendedFiles(
                fileSpec, -1, -1, -1, null, null);
        assertNotNull(status);
        assertEquals(1, status.size());
        boolean add = status.get(0).getOpStatus() == FileSpecOpStatus.ERROR;
        File file = new File(location);
        List<IFileSpec> specs = null;
        if (add) {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                assertTrue(file.createNewFile());
            }
            assertTrue(file.exists());
            specs = client.addFiles(fileSpec, false, cl.getId(), this.unicode
                    ? "unicode"
                    : null, true);
        } else {
            client.sync(fileSpec, true, false, false, false);
            assertTrue(file.exists());
            specs = client.editFiles(fileSpec, false, false, cl.getId(), null);
        }
        File temp = P4CoreUtils.createFile(content);
        assertNotNull(temp);
        assertTrue(temp.exists());
        temp.deleteOnExit();
        P4CoreUtils.copyFile(temp, file);
        assertNotNull(specs);
        specs = P4FileSpecBuilder.getValidFileSpecs(specs);
        assertNotNull(specs);
        assertEquals(1, specs.size());
        
        try {
			content.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
        return cl;
    }

    /**
     * Delete file in depot
     * 
     * @param client
     * @param cl
     * @param location
     * @return changelist
     * @throws Exception
     */
    protected IChangelist deleteFile(IClient client, IChangelist cl,
            String location) throws Exception {
        assertNotNull(client);
        assertNotNull(cl);
        assertNotNull(location);
        List<IFileSpec> fileSpec = P4FileSpecBuilder.makeFileSpecList(location);
        List<IExtendedFileSpec> status = client.getServer().getExtendedFiles(
                fileSpec, -1, -1, -1, null, null);
        assertNotNull(status);
        assertEquals(1, status.size());
        boolean exists = status.get(0).getOpStatus() == FileSpecOpStatus.ERROR;
        File file = new File(location);
        List<IFileSpec> specs = null;
        if (exists) {
            client.sync(fileSpec, true, false, false, false);
            assertTrue(file.exists());
        }
        specs = client.deleteFiles(fileSpec, cl.getId(), false);
        assertNotNull(specs);
        specs = P4FileSpecBuilder.getValidFileSpecs(specs);
        assertNotNull(specs);
        assertEquals(1, specs.size());

        cl = client.getServer().getChangelist(cl.getId());
        cl.refresh();
        cl.submit(false);

        return cl;
    }

    /**
     * Add file to depot with specified name from current project
     * 
     * @param client
     * 
     * @param location
     * @param content
     * @return changelist
     * @throws Exception
     */
    protected IChangelist openFile(IClient client, String location,
            InputStream content) throws Exception {
        return openFile(client, createPendingChangelist(client), location,
                content);
    };

    /**
     * Open specified file at location for add
     * 
     * @param client
     * @param list
     * @param location
     * @return changelist
     * @throws Exception
     */
    protected IChangelist openFile(IClient client, IChangelist list,
            String location) throws Exception {
        return openFile(client, list, location, new ByteArrayInputStream(
                getClass().getName().getBytes()));
    }

    /**
     * Open specified file at location for add
     * 
     * @param client
     * @param location
     * @return changelist
     * @throws Exception
     */
    protected IChangelist openFile(IClient client, String location)
            throws Exception {
        return openFile(client, location, new ByteArrayInputStream(getClass().getSimpleName().getBytes()));
    }

    /**
     * Open specified file at location for add
     * 
     * @param client
     * @param location
     * @return changelist
     * @throws Exception
     */
    protected IChangelist deleteFile(IClient client, String location)
            throws Exception {
        return deleteFile(client, createPendingChangelist(client), location);
    }

    /**
     * Open for and and submit specified file at location
     * 
     * @param client
     * 
     * @param location
     * @param content
     * @throws Exception
     */
    protected void addFile(IClient client, String location, InputStream content)
            throws Exception {
        IChangelist cl = openFile(client, location, content);
        cl = client.getServer().getChangelist(cl.getId());
        cl.refresh();
        cl.submit(false);
        cl.refresh();
    };

    /**
     * Open for and and submit specified file at location
     * 
     * @param client
     * @param location
     * @throws Exception
     */
    protected void addFile(IClient client, String location) throws Exception {
        addFile(client, location, new ByteArrayInputStream(getClass().getName().getBytes()));
    }

    /**
     * Open for and and submit specified file at location
     * 
     * @param client
     * @param location
     * @throws Exception
     */
    protected void addDepotFile(IClient client, String location)
            throws Exception {
        addDepotFile(client, location, new ByteArrayInputStream(getClass().getName().getBytes()));
    }

    /**
     * Open for and and submit specified file at location
     * 
     * @param client
     * @param location
     * @param content
     * @throws Exception
     */
    protected void addDepotFile(IClient client, String location,
            InputStream content) throws Exception {
        List<IFileSpec> specs = client.where(P4FileSpecBuilder
                .makeFileSpecList(location));
        assertNotNull(specs);
        specs = P4FileSpecBuilder.getValidFileSpecs(specs);
        assertNotNull(specs);
        assertEquals(1, specs.size());
        String local = specs.get(0).getLocalPathString();
        addFile(client, local, content);
    }
}
