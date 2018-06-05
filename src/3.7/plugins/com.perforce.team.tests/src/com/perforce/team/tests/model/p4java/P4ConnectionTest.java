/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4TeamUtils;

import java.net.URI;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IPathVariableManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4ConnectionTest extends ConnectionBasedTestCase {

    /**
     * @throws Exception
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        IClient client = createConnection().getClient();
        for (int i = 0; i < 11; i++) {
            addDepotFile(client, "//depot/submitted/file" + i + ".txt");
        }
        addDepotFile(client,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml");
    }

    /**
     * Test the path conversion added for job032661
     */
    public void testPathConversion() {
        assertNull(P4Connection.convertPath(null));
        assertNotNull(P4Connection.convertPath("test"));
        assertFalse("test".equals(P4Connection.convertPath("test")));
    }

    /**
     * Tests a p4 connection that will fail to obtain a valid p4j server and
     * client
     */
    public void testBadP4Connection() {
        ConnectionParameters params = new ConnectionParameters();
        params.setPort("bad.server.####");
        params.setClient("bad_client_111");
        params.setUser("bad_user_222");
        IP4Connection connection = new P4Connection(params);
        assertNotNull(connection.getUser());
        assertNotNull(connection.getAddress());
        assertNotNull(connection.getClientName());
        assertEquals(params.getPort(), connection.getAddress());
        assertEquals(params.getUser(), connection.getUser());
        assertEquals(params.getClient(), connection.getClientName());
    }

    /**
     * Test empty p4 connection
     */
    public void testEmptyP4Connection() {
        P4Connection connection = new P4Connection(null);
        assertFalse(connection.isConnected());
        assertFalse(connection.isLoggedIn());
        assertFalse(connection.isOffline());
        try {
            connection.refresh();
            connection.refresh(-1);
            connection.refresh(1);
        } catch (Throwable e) {
            handle(e);
        }
        assertFalse(connection.isSupported());
        assertFalse(connection.isMoveSupported());
        assertNotNull(connection.getConnection());
        assertSame(connection, connection.getConnection());
        assertNotNull(connection.getName());
        assertNull(connection.getServer());
        assertNull(connection.getClient());
        assertNotNull(connection.getUser());
        assertNotNull(connection.toString());
        assertFalse(connection.isSecure());
        assertTrue(connection.isContainer());
        assertNull(connection.getParent());
        assertNotNull(connection.getParameters());
        assertNull(connection.getDate());
        assertNotNull(connection.getAddress());
        assertNull(connection.getLicense());
        assertNull(connection.getRoot());
        assertNull(connection.getUptime());
        assertNull(connection.getVersion());
        assertNotNull(connection.getClientName());
        assertNull(connection.getClientPath());
        assertNull(connection.getClientRoot());
        assertNotNull(connection.getActionPath());
        assertNotNull(connection.getActionPath(Type.LOCAL));
        assertNotNull(connection.getActionPath(Type.REMOTE));
        assertNull(connection.getServerInfoClientAddress());
        assertNull(connection.getServerInfoClientHost());
        assertNull(connection.getServerInfoClientRoot());
        assertNull(connection.getServerInfoClientName());
        assertNotNull(connection.getAllLocalFiles());
        assertEquals(0, connection.getAllLocalFiles().length);
        assertNotNull(connection.getSubmittedChangelists());
        assertEquals(0, connection.getSubmittedChangelists().length);
        assertNotNull(connection.getAllClients());
        assertEquals(0, connection.getAllClients().length);
        assertNotNull(connection.getOwnedClients());
        assertEquals(0, connection.getOwnedClients().length);
        assertNotNull(connection.getOwnedLocalClients());
        assertEquals(0, connection.getOwnedLocalClients().length);
        assertNull(connection.getResource((IResource) null));
        assertNull(connection.getCurrentDirectory());
        IResource resource = new IResource() {

            public boolean isConflicting(ISchedulingRule rule) {
                return false;
            }

            public boolean contains(ISchedulingRule rule) {
                return false;
            }

            public Object getAdapter(Class adapter) {
                return null;
            }

            public void touch(IProgressMonitor monitor) throws CoreException {

            }

            public void setTeamPrivateMember(boolean isTeamPrivate)
                    throws CoreException {

            }

            public void setSessionProperty(QualifiedName key, Object value)
                    throws CoreException {

            }

            public void setResourceAttributes(ResourceAttributes attributes)
                    throws CoreException {

            }

            public void setReadOnly(boolean readOnly) {

            }

            public void setPersistentProperty(QualifiedName key, String value)
                    throws CoreException {

            }

            public long setLocalTimeStamp(long value) throws CoreException {
                return 0;
            }

            public void setLocal(boolean flag, int depth,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void setHidden(boolean isHidden) throws CoreException {

            }

            public void setDerived(boolean isDerived) throws CoreException {

            }

            public void revertModificationStamp(long value)
                    throws CoreException {

            }

            public void refreshLocal(int depth, IProgressMonitor monitor)
                    throws CoreException {

            }

            public void move(IProjectDescription description, boolean force,
                    boolean keepHistory, IProgressMonitor monitor)
                    throws CoreException {

            }

            public void move(IProjectDescription description, int updateFlags,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void move(IPath destination, int updateFlags,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void move(IPath destination, boolean force,
                    IProgressMonitor monitor) throws CoreException {

            }

            public boolean isTeamPrivateMember() {
                return false;
            }

            public boolean isSynchronized(int depth) {
                return false;
            }

            public boolean isReadOnly() {
                return false;
            }

            public boolean isPhantom() {
                return false;
            }

            public boolean isLocal(int depth) {
                return false;
            }

            public boolean isLinked(int options) {
                return false;
            }

            public boolean isLinked() {
                return false;
            }

            public boolean isHidden() {
                return false;
            }

            public boolean isDerived(int options) {
                return false;
            }

            public boolean isDerived() {
                return false;
            }

            public boolean isAccessible() {
                return false;
            }

            public IWorkspace getWorkspace() {
                return null;
            }

            public int getType() {
                return 0;
            }

            public Object getSessionProperty(QualifiedName key)
                    throws CoreException {
                return null;
            }

            /*
             * Do not use template, since eclipse 3.6/3.7 API is
             * different(non-Javadoc) 3.6 Map<Object, Object> 3.7
             * Map<QualifiedName,String>
             * 
             * @see org.eclipse.core.resources.IResource#getSessionProperties()
             */
            @SuppressWarnings({ "rawtypes" })
            public Map getSessionProperties() throws CoreException {
                return null;
            }

            public ResourceAttributes getResourceAttributes() {
                return null;
            }

            public URI getRawLocationURI() {
                return null;
            }

            public IPath getRawLocation() {
                return null;
            }

            public IPath getProjectRelativePath() {
                return null;
            }

            public IProject getProject() {
                return null;
            }

            public String getPersistentProperty(QualifiedName key)
                    throws CoreException {
                return null;
            }

            /*
             * Do not use template, since eclipse 3.6/3.7 API is
             * different(non-Javadoc) 3.6 Map<Object, Object> 3.7
             * Map<QualifiedName,String>
             * 
             * @see
             * org.eclipse.core.resources.IResource#getPersistentProperties()
             */
            @SuppressWarnings("rawtypes")
            public Map getPersistentProperties() throws CoreException {
                return null;
            }

            public IContainer getParent() {
                return null;
            }

            public String getName() {
                return null;
            }

            public long getModificationStamp() {
                return 0;
            }

            public IMarker getMarker(long id) {
                return null;
            }

            public URI getLocationURI() {
                return null;
            }

            public IPath getLocation() {
                return null;
            }

            public long getLocalTimeStamp() {
                return 0;
            }

            public IPath getFullPath() {
                return null;
            }

            public String getFileExtension() {
                return null;
            }

            public int findMaxProblemSeverity(String type,
                    boolean includeSubtypes, int depth) throws CoreException {
                return 0;
            }

            public IMarker[] findMarkers(String type, boolean includeSubtypes,
                    int depth) throws CoreException {
                return null;
            }

            public IMarker findMarker(long id) throws CoreException {
                return null;
            }

            public boolean exists() {
                return false;
            }

            public void deleteMarkers(String type, boolean includeSubtypes,
                    int depth) throws CoreException {

            }

            public void delete(int updateFlags, IProgressMonitor monitor)
                    throws CoreException {

            }

            public void delete(boolean force, IProgressMonitor monitor)
                    throws CoreException {

            }

            public IResourceProxy createProxy() {
                return null;
            }

            public IMarker createMarker(String type) throws CoreException {
                return null;
            }

            public void copy(IProjectDescription description, int updateFlags,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void copy(IProjectDescription description, boolean force,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void copy(IPath destination, int updateFlags,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void copy(IPath destination, boolean force,
                    IProgressMonitor monitor) throws CoreException {

            }

            public void clearHistory(IProgressMonitor monitor)
                    throws CoreException {

            }

            public void accept(IResourceVisitor visitor, int depth,
                    int memberFlags) throws CoreException {

            }

            public void accept(IResourceVisitor visitor, int depth,
                    boolean includePhantoms) throws CoreException {

            }

            public void accept(IResourceProxyVisitor visitor, int memberFlags)
                    throws CoreException {

            }

            public void accept(IResourceVisitor visitor) throws CoreException {

            }

            public boolean isHidden(int options) {
                return false;
            }

            public boolean isTeamPrivateMember(int options) {
                return false;
            }

            public IPathVariableManager getPathVariableManager() {
                return null;
            }

            public boolean isVirtual() {
                return false;
            }

            public void setDerived(boolean isDerived, IProgressMonitor monitor)
                    throws CoreException {

            }

			public void accept(IResourceProxyVisitor visitor, int depth,
					int memberFlags) throws CoreException {
				// TODO Auto-generated method stub
				
			}
        };
        assertNull(connection.getResource(resource));
        assertNull(connection.getSubmittedChangelistById(1));
        assertNull(connection.getPendingChangelistById(0));
    }

    /**
     * Tests the p4 connection object
     */
    public void testP4Connection() {
        P4Connection connection = new P4Connection(parameters);
        assertNotNull(connection.getServer());
        connection.login(parameters.getPassword());
        connection.connect();
        assertTrue(connection.isConnected());
        assertFalse(connection.isOffline());
        assertNotNull(connection.getConnection());
        assertSame(connection, connection.getConnection());
        assertNotNull(connection.getName());
        assertNotNull(connection.getServer());
        assertNotNull(connection.getClient());
        assertNotNull(connection.getUser());
        assertNotNull(connection.toString());
        assertFalse(connection.isSecure());
        assertTrue(connection.isContainer());
        assertNull(connection.getParent());
        assertNotNull(connection.getParameters());
        assertNotNull(connection.getDate());
        assertNotNull(connection.getAddress());
        assertNotNull(connection.getLicense());
        assertNotNull(connection.getRoot());
        assertNotNull(connection.getUptime());
        assertNotNull(connection.getVersion());
        assertNotNull(connection.getClientName());
        assertNull(connection.getClientPath());
        assertNotNull(connection.getClientRoot());
        assertNotNull(connection.getAllLocalFiles());
        assertNotNull(connection.getServerInfoClientAddress());
        assertNotNull(connection.getServerInfoClientHost());
        assertNotNull(connection.getServerInfoClientName());
        assertNotNull(connection.getServerInfoClientRoot());
        assertEquals(0, connection.getAllLocalFiles().length);
        IClientSummary[] specs = connection.getAllClients();
        assertNotNull(specs);
        assertTrue(specs.length > 0);
        specs = connection.getOwnedClients();
        assertNotNull(specs);
        assertTrue(specs.length > 0);
        for (IClientSummary spec : specs) {
            assertEquals(parameters.getUser(), spec.getOwnerName());
        }

        P4Connection connection2 = new P4Connection(parameters);
        assertEquals(connection, connection2);

        IP4Resource[] members = connection.members();
        assertNotNull(members);
        assertTrue(members.length > 0);
        assertSame(members, connection.members());

        for (IP4Resource resource : members) {
            assertTrue(resource instanceof IP4Container);
            assertTrue(resource instanceof P4Depot);
        }

        connection.markForRefresh();
        assertTrue(connection.needsRefresh());
        connection.setOffline(true);
        assertTrue(connection.isOffline());
        assertFalse(connection.isConnected());
    }

    /**
     * Test the overloaded getSubmittedChangelists methods
     */
    public void testOverloadedSubmittedChangelists() {
        IP4Connection connection = createConnection();
        IP4Changelist[] submitted1 = connection.getSubmittedChangelists(1);
        assertNotNull(submitted1);
        assertEquals(1, submitted1.length);

        IP4Changelist[] submitted2 = connection.getSubmittedChangelists(
                (String) null, 1);
        assertNotNull(submitted2);
        assertEquals(submitted1.length, submitted2.length);
        assertEquals(submitted1[0], submitted2[0]);

        submitted2 = connection.getSubmittedChangelists((String[]) null, 1);
        assertNotNull(submitted2);
        assertEquals(submitted1.length, submitted2.length);
        assertEquals(submitted1[0], submitted2[0]);
    }

    /**
     * Test retrieving 10 submitted changelists
     */
    public void test10SubmittedChangelists() {
        IP4Connection connection = createConnection();

        IP4Changelist[] submitted = connection.getSubmittedChangelists(10);
        assertNotNull(submitted);
        assertEquals(10, submitted.length);
        for (IP4Changelist list : submitted) {
            assertNotNull(list);
            assertTrue(list.getId() > 0);
            assertNotNull(list.getId() + " failed", list.getDescription());
            assertNotNull(list.getId() + " failed", list.getDate());
            assertNotNull(list.getId() + " failed", list.getClient());
            assertNotNull(list.getId() + " failed", list.getClientName());
            assertNotNull(list.getId() + " failed", list.getUserName());
            assertNotNull(list.getId() + " failed", list.getShortDescription());
            assertSame(list.getId() + " failed", connection,
                    list.getConnection());
            if (list.needsRefresh()) {
                list.refresh();
            }
            assertNotNull(list.getId() + " failed", list.members());
            assertTrue(list.getId() + " failed", list.members().length > 0);
            assertNotNull(list.getId() + " failed", list.getActionPath());
            assertSame(list.getId() + " failed", ChangelistStatus.SUBMITTED,
                    list.getStatus());
            assertNotNull(list.getId() + " failed", list.getChangelist());
            assertNull(list.getId() + " failed", list.getParent());
        }
    }

    /**
     * Test pending changelists
     */
    public void testPendingChangelists() {
        IP4Connection connection = createConnection();

        IP4Changelist[] pending = connection.getPendingChangelists(false);
        assertNotNull(pending);
        assertTrue(pending.length > 0);
        for (IP4Changelist list : pending) {
            assertNotNull(list);
            assertTrue(list.getId() >= 0);
            assertNotNull(list.getDescription());
            if (list.isDefault()) {
                assertNull(list.getDate());
            } else {
                assertNotNull(list.getDate());
            }
            assertNotNull(list.getClient());
            assertNotNull(list.getClientName());
            assertNotNull(list.getUserName());
            assertNotNull(list.getShortDescription());
            assertSame(connection, list.getConnection());
            assertNotNull(list.members());
            assertSame(ChangelistStatus.PENDING, list.getStatus());
            if (list.isDefault()) {
                assertNull(list.getChangelist());
            } else {
                assertNotNull(list.getChangelist());
            }
            assertNull(list.getParent());
        }
    }

    /**
     * Test labels
     */
    public void testLabels() {
        IP4Connection connection = createConnection();
        IP4Label[] labels = connection.getLabels();
        assertNotNull(labels);
        for (IP4Label label : labels) {
            assertNotNull(label);
            assertNotNull(label.getLabel());
            assertNotNull(label.getName());
            assertNotNull(label.getDescription());
            assertNotNull(label.getUpdateTime());
            assertNotNull(label.getAccessTime());
        }
    }

    /**
     * Test branches
     */
    public void testBranches() {
        IP4Connection connection = createConnection();
        IP4Branch[] branches = connection.getBranches();
        assertNotNull(branches);
        for (IP4Branch branch : branches) {
            assertNotNull(branch);
            assertNotNull(branch.getName());
            assertNotNull(branch.getDescription());
            assertNotNull(branch.getUpdateTime());
            assertNotNull(branch.getAccessTime());
        }
    }

    /**
     * Test the connection integrate method
     */
    public void testIntegrate() {
        IP4Connection connection = createConnection();

        IP4Folder folder = new P4Folder(createConnection(), null,
                "//depot/p08.1/p4-eclipse/com.perforce.team.plugin");
        P4Collection collection = new P4Collection(new IP4Resource[] { folder });
        collection.revert();
        collection.forceSync(new NullProgressMonitor());

        String fromPath = "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml";
        String toPath = "//depot/dev/testtest/plugin.xml";
        P4FileIntegration options = new P4FileIntegration();
        options.setSource(fromPath);
        options.setTarget(toPath);
        IP4Resource[] resources = connection.integrate(options, 0, true, false,
                P4TeamUtils.createDefaultIntegration(connection));
        assertNotNull(resources);
        assertEquals(1, resources.length);
        assertTrue(resources[0] instanceof IP4File);
        IP4File p4File = (IP4File) resources[0];

        assertEquals(toPath, p4File.getRemotePath());
        assertSame(FileAction.BRANCH, p4File.getAction());
    }
}
