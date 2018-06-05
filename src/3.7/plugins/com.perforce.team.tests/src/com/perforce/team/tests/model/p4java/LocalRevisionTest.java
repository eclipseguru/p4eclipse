/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.LocalRevision;
import com.perforce.team.tests.ProjectBasedTestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class LocalRevisionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Local revision test
     */
    public void testLocal() {
        IFile file = project.getFile("plugin.xml");
        LocalRevision revision = new LocalRevision(file, null);
        assertNull(revision.getAuthor());
        assertNull(revision.getComment());
        assertNull(revision.getContentIdentifier());
        assertTrue(revision.exists());
        assertNotNull(revision.getURI());
        assertNotNull(revision.getLocalPath());
        assertEquals(file, revision.getFile());
        assertNotNull(revision.getName());
        try {
            assertNotNull(revision.getStorage(new NullProgressMonitor()));
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(revision.isCurrent());
        assertFalse(revision.isPropertyMissing());
        assertTrue(revision.getTimestamp() >= -1);
        try {
            assertNull(revision.withAllProperties(new NullProgressMonitor()));
        } catch (CoreException e) {
            handle(e);
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
