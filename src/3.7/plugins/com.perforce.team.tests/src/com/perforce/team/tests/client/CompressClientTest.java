package com.perforce.team.tests.client;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CompressClientTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        IClientOptions options = client.getOptions();
        assertNotNull(options);
        options.setCompress(true);
        client.update();
        client = createConnection().getClient();
        assertNotNull(client.getOptions());
        assertTrue(client.getOptions().isCompress());
    }

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#configureProperties()
     */
    @Override
    protected void configureProperties() {
        super.configureProperties();
        // Socket pooling not working with compress clients
        P4Workspace.getWorkspace().getAdvancedProperties()
                .setProperty("socketPoolSize", "0");
    }

    /**
     * Test sync with compress client
     * 
     * @throws Exception
     */
    public void testSync() throws Exception {
        IFile file = project.getFile("testCompressSubmit"
                + System.currentTimeMillis() + ".txt");
        String content = "Content for this\nfile";

        IP4Connection connection = createConnection();
        Utils.fillFileWithString(file, content);
        Utils.addSubmit(file, connection);

        IP4File p4File = connection.getFile(file.getLocation().makeAbsolute()
                .toOSString());

        assertNotNull(p4File);
        assertEquals(1, p4File.getHeadRevision());
        assertEquals(1, p4File.getHaveRevision());

        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(p4File));
        sync.runAction("#none");

        assertEquals(0, p4File.getHaveRevision());

        sync.runAction("#head");

        assertEquals(1, p4File.getHaveRevision());

        String fetched = Utils.getContent(file);
        assertEquals(content, fetched);

    }

    /**
     * Test submit with compress client
     * 
     * @throws Exception
     */
    public void testSubmit() throws Exception {
        IFile file = project.getFile("testCompressSubmit"
                + System.currentTimeMillis() + ".txt");
        String content = "Content for this\nfile";

        IP4Connection connection = createConnection();
        Utils.fillFileWithString(file, content);
        Utils.addSubmit(file, connection);

        IP4File p4File = connection.getFile(file.getLocation().makeAbsolute()
                .toOSString());
        assertNotNull(p4File);
        assertEquals(1, p4File.getHeadRevision());
        assertEquals(1, p4File.getHaveRevision());

        String fetched = Utils.getContent(p4File.getHeadContents());
        assertEquals(content, fetched);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/test_compress";
    }

}
