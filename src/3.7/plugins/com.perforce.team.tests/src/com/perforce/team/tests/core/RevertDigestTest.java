package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * This test has to be run by itself since p4java caches line ending information
 * very early and this can't affect other test cases.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class RevertDigestTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        // Uncomment to run, commented out since this will impact other test
        // cases
        // System.setProperty("line.separator", "\r\n");
        super.setUp();
    }

    /**
     * Test digesting a file past the default p4java buffer size with a partial
     * newline at the end of the buffer to ensure look ahead works correctly.
     */
    public void testDigestNewlines() {
        IP4Connection connection = createConnection();
        IFile file = project.getFile("newlines" + System.currentTimeMillis()
                + ".txt");
        try {
            Utils.fillFile(file, "/resources/newlines.txt");
        } catch (Exception e) {
            handle("Filling failed", e);
        }
        try {
            Utils.addSubmit(file, connection);
        } catch (Exception e) {
            handle("Add/Submit failed", e);
        }

        IP4File p4File = connection.getFile(file.getLocation().makeAbsolute()
                .toOSString());
        assertNotNull(p4File);

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(p4File));
        edit.run(null);

        assertTrue(p4File.openedForEdit());

        P4Collection collection = new P4Collection(new IP4Resource[] { p4File });
        P4Collection revert = collection.previewUnchangedRevert();
        assertNotNull(revert);
        assertNotNull(revert.members());
        assertEquals(1, revert.members().length);
        assertEquals(p4File, revert.members()[0]);
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/revert_digest";
    }

}
