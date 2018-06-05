/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourceAttributes;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PermissionsTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test {@link P4JavaSysFileCommandsHelper} permission setting
     */
    public void testPermissions() {
        P4JavaSysFileCommandsHelper helper = new P4JavaSysFileCommandsHelper();
        final IFile file = project.getFile("plugin.xml");
        String fileName = file.getLocation().makeAbsolute().toOSString();
        assertTrue(file.exists());
        ResourceAttributes attrs = file.getResourceAttributes();
        assertNotNull(attrs);
        assertFalse(attrs.isExecutable());
        assertFalse(helper.canExecute(fileName));
        assertTrue(attrs.isReadOnly());
        assertFalse(helper.isSymlink(fileName));
        helper.setWritable(fileName, true);
        attrs = file.getResourceAttributes();
        assertNotNull(attrs);
        assertFalse(attrs.isReadOnly());
        helper.setExecutable(fileName, true, true);
        attrs = file.getResourceAttributes();
        assertTrue(attrs.isExecutable());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
