/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.dialogs.PopulateDialog;

public class PopulateDialogTest extends ProjectBasedTestCase {
    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile(".project"), project.getFile(".project")
                .getContents());
        super.addFile(project.getFile("plugin.xml"));
        super.addFile(project.getFile(new Path("images/empty.gif")));
        super.addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        super.addFile(project.getFile("about.ini"));
        super.addFile(project.getFile(new Path("bin/output.txt")));
    }
    
    /**
     * Test integrate dialog
     */
    public void testDialog() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4ConnectionManager.getManager().getResource(
                file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        String TARGET_PATH="//depot/p08.1/plugin.xml";
        PopulateDialog dialog = new PopulateDialog(Utils.getShell(), p4File.getConnection(), "//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml", TARGET_PATH);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertEquals(1,dialog.getSourcePaths().size());
            assertEquals(TARGET_PATH,dialog.getTargetPath());
            assertEquals("Branching",dialog.getDescription());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test preview
     */
    public void testPreview() {
    	IP4Connection connection = createConnection();
    	String SOURCE_PATH="//depot/p08.1/p4-eclipse/com.perforce.team.plugin/plugin.xml";
        String TARGET_PATH="//depot/p08.1/plugin.xml";
        PopulateDialog dialog = new PopulateDialog(Utils.getShell(), connection, SOURCE_PATH, TARGET_PATH);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertEquals(1,dialog.getSourcePaths().size());
            assertEquals(TARGET_PATH,dialog.getTargetPath());
            assertEquals("Branching",dialog.getDescription());
        } finally {
            dialog.close();
        }
        
		try {
			IP4Resource[] populated = connection.populate(SOURCE_PATH,
					TARGET_PATH, true, "description");
			assertNotNull(populated);
			assertEquals(1, populated.length);
			IP4Resource newResource = populated[0];
			assertNotNull(newResource);
			assertTrue(newResource instanceof IP4File);
			assertTrue(TARGET_PATH.equals(newResource.getActionPath()));
		} finally {
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
