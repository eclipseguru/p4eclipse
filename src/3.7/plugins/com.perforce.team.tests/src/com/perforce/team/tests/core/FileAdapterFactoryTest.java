/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.P4FileAdapterFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileAdapterFactoryTest extends ProjectBasedTestCase {

    /**
     * Test the file adapter factory
     */
    public void testFactory() {
        P4FileAdapterFactory factory = new P4FileAdapterFactory();
        assertNotNull(factory.getAdapterList());
        assertTrue(factory.getAdapterList().length > 0);
        for (Class c : factory.getAdapterList()) {
            assertNotNull(c);
        }
        IFile file = project.getFile("plugin.xml");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        Object adapter = factory.getAdapter(p4File, IWorkbenchAdapter.class);
        assertTrue(adapter instanceof IWorkbenchAdapter);
        IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) adapter;
        assertNull(wbAdapter.getParent(p4File));
        assertNotNull(wbAdapter.getLabel(p4File));
        assertNull(wbAdapter.getImageDescriptor(p4File));
        assertNotNull(wbAdapter.getChildren(p4File));
        assertEquals(0, wbAdapter.getChildren(p4File).length);
    }

    /**
     * Test invalid use of file adapter factory
     */
    public void testInvalid() {
        P4FileAdapterFactory factory = new P4FileAdapterFactory();
        assertNull(factory.getAdapter(null, null));
        assertNull(factory.getAdapter(null, IWorkbenchAdapter.class));

        IFile file = project.getFile("plugin.xml");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        Object adapter = factory.getAdapter(p4File, IWorkbenchAdapter.class);
        assertTrue(adapter instanceof IWorkbenchAdapter);
        IWorkbenchAdapter wbAdapter = (IWorkbenchAdapter) adapter;
        assertNotNull(wbAdapter.getLabel(null));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
