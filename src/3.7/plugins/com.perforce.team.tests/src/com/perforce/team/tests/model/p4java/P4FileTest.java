/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.ServerFactory;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IEventObject;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.tests.ProjectBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4FileTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addFile(project.getFile("plugin.xml"));
    }

    private IP4File createNewFile() {
        return createNewFile("plugin.xml");
    }

    private IP4File createNewFile(String name) {
        IP4File file = null;
        try {
            IP4Connection connection = createConnection();
            String folderPath = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
            P4Folder folder = new P4Folder(connection, null, folderPath);
            folder.updateLocation();
            assertNotNull(folder.getLocalPath());
            String path = folder.getLocalPath() + "/" + name;
            file = new P4File(connection, path);
        } catch (Exception e) {
            assertFalse(true);
        }
        return file;
    }

    /**
     * Tests the {@link IEventObject} interface through the {@link P4File} class
     */
    public void testListener() {
        final List<P4Event> events = new ArrayList<P4Event>();
        IP4File file = new P4File(createConnection(), null);
        IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                events.add(event);
                if (events.size() % 2 == 0) {
                    throw new Error("Test error");
                } else {
                    throw new NullPointerException("Test exception");
                }
            }

			public String getName() {
				return P4FileTest.this.getClass().getSimpleName();
			}            
        };
        try {
            file.addListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        try {
            file.addListeners(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        file.addListener(listener);
        file.addListener(listener);
        file.addListeners(new IP4Listener[] { listener });
        try {
            file.addListeners(new IP4Listener[] { listener, null });
        } catch (Exception e) {
            assertFalse(true);
        }
        P4Event event1 = new P4Event(EventType.CHANGED, file);
        file.notifyListeners(event1);
        try {
            file.removeListener(null);
        } catch (Exception e) {
            assertFalse(true);
        }
        file.removeListener(listener);
        P4Event event2 = new P4Event(EventType.SUBMITTED, file);
        file.notifyListeners(event2);

        assertEquals(1, events.size());
        assertSame(event1, events.get(0));

        file.addListener(listener);
        file.notifyListeners(event2);
        file.clearListeners();
        P4Event event3 = new P4Event(EventType.SUBMITTED, file);
        file.notifyListeners(event3);

        assertEquals(2, events.size());
        assertSame(event1, events.get(0));
        assertSame(event2, events.get(1));
    }

    /**
     * Tests the supported content type of a p4 file, currently none. This is a
     * placeholder for now
     */
    public void testContentTypes() {
        IP4File file = createNewFile();
        assertNotNull(file);
        String[] types = file.getSupportedTypes();
        assertNotNull(types);
        for (String type : types) {
            assertNotNull(type);
            assertNotNull(file.getAs(type));
        }
        assertNull(file.getAs(null));
    }

    /**
     * Tests the p4j exceptions don't bubble up when connection failure occurs
     */
    public void testGracefulFailure() {
        IFileSpec spec = new FileSpec();
        ConnectionParameters params = new ConnectionParameters();
        params.setClient("bad");
        params.setUser("bad");
        params.setPort("bad");
        IP4Connection connection = new P4Connection(params);
        IP4File file = new P4File(spec, connection);
        try {
            file.add();
            file.delete();
            file.edit();
            file.ignore();
            file.refresh();
            file.refresh(IResource.DEPTH_ZERO);
            file.refresh(IResource.DEPTH_ONE);
            file.refresh(IResource.DEPTH_INFINITE);
            file.revert();
            file.sync(new NullProgressMonitor());
            file.getHistory();
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse(true);
        } catch (Error e) {
            e.printStackTrace();
            assertFalse(true);
        }
    }

    /**
     * Tests file add
     */
    public void testAdd() {
        IP4File file = createNewFile("plugin2.xml");
        assertNotNull(file);
        assertFalse(file.isOpened());
        assertFalse(file.openedForAdd());
        file.add();
        file.refresh();
        assertFalse(file.isRemote());
        assertTrue(file.isLocal());
        assertTrue(file.isOpened());
        assertTrue(file.openedForAdd());
        assertSame(FileAction.ADD, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForAdd());
        file.add(0);
        file.refresh();
        assertTrue(file.isOpened());
        assertTrue(file.openedForAdd());
        assertSame(FileAction.ADD, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForAdd());
    }

    /**
     * Tests file delete
     */
    public void testDelete() {
        IP4File file = createNewFile();
        assertNotNull(file);
        assertFalse(file.isOpened());
        assertFalse(file.openedForDelete());
        file.delete();
        file.refresh();
        assertTrue(file.isLocal());
        assertTrue(file.isOpened());
        assertTrue(file.openedForDelete());
        assertSame(FileAction.DELETE, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForDelete());
        file.delete(0);
        file.refresh();
        assertTrue(file.isOpened());
        assertTrue(file.openedForDelete());
        assertSame(FileAction.DELETE, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForDelete());
    }

    /**
     * Tests file edit
     */
    public void testEdit() {
        IP4File file = createNewFile();
        assertNotNull(file);
        assertFalse(file.isOpened());
        assertFalse(file.openedForEdit());
        file.edit();
        file.refresh();
        assertTrue(file.isRemote());
        assertNotNull(file.getRemoteContents());
        assertTrue(file.isLocal());
        assertTrue(file.isOpened());
        assertTrue(file.openedForEdit());
        assertSame(FileAction.EDIT, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForEdit());
        file.edit(0);
        file.refresh();
        assertTrue(file.isOpened());
        assertTrue(file.openedForEdit());
        assertSame(FileAction.EDIT, file.getAction());
        file.revert();
        file.refresh();
        assertFalse(file.isOpened());
        assertFalse(file.openedForEdit());
    }

    /**
     * Tests the p4 file object
     */
    public void testP4File() {
        try {
            IServer p4jServer = ServerFactory.getServer("p4java://"
                    + parameters.getPortNoNull(), null);
            assertNotNull(p4jServer);
            p4jServer.setUserName(parameters.getUserNoNull());
            p4jServer.connect();
            p4jServer.login(parameters.getPassword());
            IClient client = p4jServer.getClient(parameters.getClient());
            assertNotNull(client);
            String path = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml";
            P4Folder folder = new P4Folder(new P4Connection(parameters),
                    "com.perforce.team.plugin");
            List<IExtendedFileSpec> specs = p4jServer.getExtendedFiles(
                    P4FileSpecBuilder.makeFileSpecList(new String[] { path }), 0,
                    -1, -1, null, null);
            assertNotNull(specs);
            assertEquals(1, specs.size());
            P4File file = new P4File(specs.get(0), folder);
            assertNotNull(file.getActionPath());
            assertNotNull(file.getClient());
            assertNotNull(file.getParent());
            assertSame(folder, file.getParent());
            assertNotNull(file.getRemotePath());
            assertEquals(path, file.getRemotePath());
            assertNotNull(file.getP4JFile());
            assertSame(specs.get(0), file.getP4JFile());
            assertNotNull(file.getOtherActions());
            assertEquals(0, file.getOtherActions().size());
            assertNotNull(file.getOtherChangelists());
            assertEquals(0, file.getOtherChangelists().size());
            assertNotNull(file.getOtherEditors());
            assertEquals(0, file.getOtherEditors().size());
            assertTrue(file.getHeadChange() > 0);
            assertTrue(file.getHeadTime() > 0);
            assertNotNull(file.getStatus());
            assertNull(file.getStatusMessage());
            assertFalse(file.isUnresolved());
            assertFalse(file.openedElsewhere());
        } catch (URISyntaxException e) {
            handle(e);
        } catch (P4JavaException e) {
            handle(e);
        }
    }

    /**
     * Tests an empty file
     */
    public void testEmptyFile() {
        IP4File file = new P4File((IP4Connection) null, null);
        assertNull(file.getServer());
        assertNull(file.getAction());
        assertNull(file.getActionPath());
        assertNull(file.getChangelist());
        assertNull(file.getClient());
        assertNull(file.getClientPath());
        assertNull(file.getConnection());
        assertNull(file.getErrorHandler());
        assertNull(file.getHeadAction());
        assertNull(file.getHeadType());
        assertEquals(0, file.getHaveRevision());
        assertEquals(0, file.getHeadRevision());
        assertNotNull(file.getHistory());
        assertEquals(0, file.getHistory().length);
        assertNotNull(file.getLocalFiles());
        assertEquals(0, file.getLocalFiles().length);
        assertNull(file.getLocalPath());
        assertNull(file.getName());
        assertNull(file.getOpenedType());
        assertNull(file.getP4JFile());
        assertNull(file.getParent());
        assertFalse(file.isOpened());
        assertFalse(file.openedForAdd());
        assertFalse(file.openedForDelete());
        assertFalse(file.openedForEdit());
        assertNull(file.getRemoteContents());
        assertNull(file.getRemoteContents(1));
        assertFalse(file.isLocal());
        assertFalse(file.isIgnored());
        assertFalse(file.openedElsewhere());
        assertNotNull(file.getOtherActions());
        assertEquals(0, file.getOtherActions().size());
        assertNotNull(file.getOtherChangelists());
        assertEquals(0, file.getOtherChangelists().size());
        assertNotNull(file.getOtherEditors());
        assertEquals(0, file.getOtherEditors().size());
        assertNull(file.getIntegrationSpec());
        assertEquals(0, file.getHeadTime());
        assertEquals(0, file.getHeadChange());
        assertNull(file.getUserName());
        assertNull(file.getClientName());
        assertEquals(IChangelist.UNKNOWN, file.getChangelistId());
        assertFalse(file.isUnresolved());
        assertNull(file.getResolvePath());
        assertFalse(file.isRemote());
        assertFalse(file.handleError(null));
        assertFalse(file.isHeadActionAdd());
        assertFalse(file.isHeadActionDelete());
        assertFalse(file.isHeadActionEdit());

        IP4Connection connection = createConnection();
        file.setParent(connection);
        assertNotNull(file.getParent());
        assertSame(connection, file.getParent());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }
}
