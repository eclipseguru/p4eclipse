/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.IntegrateAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ResolveTest extends ProjectBasedTestCase {

    /**
     * 
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("plugin.xml"),
                new ByteArrayInputStream("a=3\ntest2\nb=2test3".getBytes()));
        addFile(client, project.getFile("plugin.xml"),
                new ByteArrayInputStream("a=3\ntest3\nb=2test3".getBytes()));
        addFile(client, project.getFile("plugin.properties"),
                new ByteArrayInputStream("b=3\na=1\nb=2\nc=3".getBytes()));
        addFile(client, project.getFile("plugin.properties"),
                new ByteArrayInputStream("b=3\na=1\nb=9999\nc=3".getBytes()));
    }

    /**
     * Test resolving a file that has been integrated over another with accept
     * yours
     */
    public void testResolveIntegrationOverAcceptYours() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

//            P4IntegrationOptions options = new P4IntegrationOptions();
//            options.setBaselessMerge(true);
            P4IntegrationOptions options=getBaselessOptions(p4File, true);
            integrate.integrate(source, target, null, null, 0, options);

            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertTrue(toP4File.isUnresolved());

            new P4Collection(new IP4Resource[] { toP4File }).getUnresolved();
            assertNotNull(toP4File.getIntegrationSpec());
            assertEquals(p4File.getRemotePath(), toP4File.getIntegrationSpec()
                    .getFromFile());

            P4Collection resolveCollection = new P4Collection(
                    new IP4Resource[] { toP4File });
            resolveCollection.setType(IP4Resource.Type.LOCAL);
            resolveCollection.resolve(new ResolveFilesAutoOptions().setAcceptYours(true));
            assertFalse(toP4File.isUnresolved());
            assertNotNull(toP4File.getResolvePath());

            try {
                assertEquals(Utils.getContent(toP4File.getRemoteContents()),
                        Utils.getContent(to));
            } catch (Exception e) {
                assertFalse("Exception thrown comparing file contents", true);
            }

        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
            }
        }
    }

	/**
     * Test resolving a file that has been integrated over another with accept
     * theirs
     */
    public void testResolveIntegrationOverAcceptTheirs() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

//            P4IntegrationOptions options = new P4IntegrationOptions();
//            options.setBaselessMerge(true);
            P4IntegrationOptions options = getBaselessOptions(p4File, true);
            integrate.integrate(source, target, null, null, 0, options);

            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertTrue(toP4File.isUnresolved());

            new P4Collection(new IP4Resource[] { toP4File }).getUnresolved();
            assertNotNull(toP4File.getIntegrationSpec());
            assertEquals(p4File.getRemotePath(), toP4File.getIntegrationSpec()
                    .getFromFile());

            P4Collection resolveCollection = new P4Collection(
                    new IP4Resource[] { toP4File });
            resolveCollection.setType(IP4Resource.Type.LOCAL);
            resolveCollection.resolve(new ResolveFilesAutoOptions().setAcceptTheirs(true));
            assertFalse(toP4File.isUnresolved());
            assertNotNull(toP4File.getResolvePath());

            try {
                assertEquals(Utils.getContent(from), Utils.getContent(to));
            } catch (Exception e) {
                assertFalse("Exception thrown comparing file contents", true);
            }

        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
            }
        }
    }

    /**
     * Test resolving a file that has been integrated over another with accept
     * merged conflicts
     */
    public void testResolveIntegrationOverAcceptConflicts() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

//            P4IntegrationOptions options = new P4IntegrationOptions();
//            options.setBaselessMerge(true);
            P4IntegrationOptions options = getBaselessOptions(p4File, true);
            integrate.integrate(source, target, null, null, 0, options);

            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertTrue(toP4File.isUnresolved());

            new P4Collection(new IP4Resource[] { toP4File }).getUnresolved();
            assertNotNull(toP4File.getIntegrationSpec());
            assertEquals(p4File.getRemotePath(), toP4File.getIntegrationSpec()
                    .getFromFile());

            P4Collection resolveCollection = new P4Collection(
                    new IP4Resource[] { toP4File });
            resolveCollection.setType(IP4Resource.Type.LOCAL);
            resolveCollection.resolve(new ResolveFilesAutoOptions().setForceResolve(true));
            assertFalse(toP4File.isUnresolved());
            assertNotNull(toP4File.getResolvePath());

            try {
                String toContent = Utils.getContent(to);
                String fromContent = Utils.getContent(from);
                assertFalse(fromContent.equals(toContent));
                assertTrue(toContent.contains("YOURS"));
                assertTrue(toContent.contains("THEIRS"));
                assertTrue(toContent.contains("ORIGINAL"));
            } catch (Exception e) {
                assertFalse("Exception thrown comparing file contents", true);
            }

        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
            }
        }
    }

    /**
     * Test resolving a file that has been integrated over another with accept
     * merged conflicts
     */
    public void testResolveIntegrationOverAcceptMerged() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

//            P4IntegrationOptions options = new P4IntegrationOptions();
//            options.setBaselessMerge(true);
            P4IntegrationOptions options = getBaselessOptions(p4File, true);
            integrate.integrate(source, target, null, null, 0, options);

            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertTrue(toP4File.isUnresolved());

            new P4Collection(new IP4Resource[] { toP4File }).getUnresolved();
            assertNotNull(toP4File.getIntegrationSpec());
            assertEquals(p4File.getRemotePath(), toP4File.getIntegrationSpec()
                    .getFromFile());

            P4Collection resolveCollection = new P4Collection(
                    new IP4Resource[] { toP4File });
            resolveCollection.setType(IP4Resource.Type.LOCAL);
            resolveCollection.resolve(new ResolveFilesAutoOptions());
            assertTrue(toP4File.isUnresolved());
            assertNotNull(toP4File.getResolvePath());

        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
            }
        }
    }

    /**
     * Test resolving a file that has been integrated over another with safe
     * resolve
     */
    public void testResolveIntegrationOverAcceptSafe() {
        IFile from = project.getFile("plugin.xml");
        assertTrue(from.exists());
        IFile to = project.getFile("plugin.properties");
        assertTrue(to.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(from);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(to);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertFalse(toP4File.isOpened());

        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(from));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = toP4File.getActionPath();

//            P4IntegrationOptions options = new P4IntegrationOptions();
//            options.setBaselessMerge(true);
            P4IntegrationOptions options = getBaselessOptions(p4File, true);
            integrate.integrate(source, target, null, null, 0, options);

            assertTrue(toP4File.isOpened());
            assertTrue(toP4File.openedForEdit());
            assertTrue(toP4File.isUnresolved());

            new P4Collection(new IP4Resource[] { toP4File }).getUnresolved();
            assertNotNull(toP4File.getIntegrationSpec());
            assertEquals(p4File.getRemotePath(), toP4File.getIntegrationSpec()
                    .getFromFile());

            P4Collection resolveCollection = new P4Collection(
                    new IP4Resource[] { toP4File });
            resolveCollection.setType(IP4Resource.Type.LOCAL);
            resolveCollection.resolve(new ResolveFilesAutoOptions().setSafeMerge(true));
            assertTrue(toP4File.isUnresolved());
            assertNotNull(toP4File.getResolvePath());

        } finally {
            if (toP4File != null) {
                toP4File.revert();
                toP4File.refresh();
                assertFalse(toP4File.isOpened());
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
