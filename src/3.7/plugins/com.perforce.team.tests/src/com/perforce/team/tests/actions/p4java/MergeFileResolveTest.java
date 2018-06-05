package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class MergeFileResolveTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#useRpc()
     */
    @Override
    protected boolean useRpc() {
        return true;
    }

    /**
     * Test resolving a file opened for integrate using a merge file
     */
    public void testIntegrate() {
        IFile fromFile = project.getFile("about.ini");
        assertTrue(fromFile.exists());

        IP4Resource fromResource = P4Workspace.getWorkspace().getResource(
                fromFile);
        assertNotNull(fromResource);
        assertTrue(fromResource instanceof IP4File);
        IP4File fromP4File = (IP4File) fromResource;
        assertTrue(fromP4File.isSynced());
        assertNotNull(fromP4File.getConnection());

        IFile toFile = project.getFile("plugin.xml.template");
        assertTrue(toFile.exists());
        assertTrue(toFile.isReadOnly());

        IP4Resource toResource = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(toResource);
        assertTrue(toResource instanceof IP4File);
        IP4File toP4File = (IP4File) toResource;
        assertTrue(toP4File.isSynced());

        P4FileIntegration integ = new P4FileIntegration();
        integ.setSource(fromFile.getLocation().makeAbsolute().toOSString());
        integ.setTarget(toFile.getLocation().makeAbsolute().toOSString());

        IServer server = fromP4File.getConnection().getServer();
        P4IntegrationOptions options = P4IntegrationOptions.createInstance(server);
        if(options instanceof P4IntegrationOptions2){
        	((P4IntegrationOptions2) options).setBaselessMerge(true);
        }

        IP4Resource[] integed = fromP4File.getConnection().integrate(integ, -1,
                false, false, options);
        assertNotNull(integed);
        assertEquals(1, integed.length);
        assertEquals(toP4File, integed[0]);

        assertTrue(toP4File.isUnresolved());

        String content = "Test\nintegration resolve\n   with some \t merge \n\t\t content\n\n\n";
        File mergeFile = null;
        FileInputStream stream = null;
        try {
            mergeFile = File.createTempFile("p4eclipse_tests", ".tmp");
            PrintWriter writer = new PrintWriter(mergeFile);
            writer.print(content);
            writer.flush();
            writer.close();
            stream = new FileInputStream(mergeFile);
        } catch (IOException e) {
            handle("IO Exception thrown:", e);
        }

        P4Collection collection = new P4Collection(
                new IP4Resource[] { toP4File });
        IP4Resource[] resolved = collection.resolve(stream, true, -1, -1);
        assertNotNull(resolved);
        assertEquals(1, resolved.length);
        assertEquals(toP4File, resolved[0]);
        assertFalse(toP4File.isUnresolved());
        assertTrue(toP4File.openedForEdit());
        assertEquals(FileAction.INTEGRATE, toP4File.getAction());
        assertTrue(toFile.isReadOnly());

        try {
            assertEquals(content, Utils.getContent(toFile));
        } catch (Exception e) {
            handle("Exception getting file content:", e);
        }

    }

    /**
     * Test resolving a file opened for edit using a merge file
     */
    public void testEdit() {
        IFile file = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertTrue(p4File.isSynced());
        assertEquals(3, p4File.getHeadRevision());
        assertFalse(p4File.isUnresolved());

        SyncRevisionAction syncRev = new SyncRevisionAction();
        syncRev.setAsync(false);
        syncRev.selectionChanged(null, new StructuredSelection(file));
        syncRev.runAction("#1");

        assertEquals(1, p4File.getHaveRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        assertTrue(p4File.openedForEdit());

        syncRev.runAction("#head");

        assertTrue(p4File.isUnresolved());

        String content = "Test\nresolve\n   with \t merge \n\t\t content\n\n\n";
        File mergeFile = null;
        FileInputStream stream = null;
        try {
            mergeFile = File.createTempFile("p4eclipse_tests", ".tmp");
            PrintWriter writer = new PrintWriter(mergeFile);
            writer.print(content);
            writer.flush();
            writer.close();
            stream = new FileInputStream(mergeFile);
        } catch (IOException e) {
            handle("IO Exception thrown:", e);
        }

        P4Collection collection = new P4Collection(new IP4Resource[] { p4File });
        IP4Resource[] resolved = collection.resolve(stream, true, -1, -1);
        assertNotNull(resolved);
        assertEquals(1, resolved.length);
        assertEquals(p4File, resolved[0]);
        assertFalse(p4File.isUnresolved());
        assertTrue(p4File.openedForEdit());

        try {
            assertEquals(content, Utils.getContent(file));
        } catch (Exception e) {
            handle("Exception getting file content:", e);
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
