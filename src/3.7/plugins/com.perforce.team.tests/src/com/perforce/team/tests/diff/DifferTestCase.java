/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.diff;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Storage;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.diff.DiffRegistry;
import com.perforce.team.ui.diff.IFileDiffer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class DifferTestCase extends ProjectBasedTestCase {

    /**
     * Test differ
     */
    public void testDiffer() {
        IFile diffFile = getFile();
        assertTrue(diffFile.exists());
        IP4Connection connection = createConnection();
        IP4Resource resource = connection.getResource(diffFile);
        assertTrue(resource instanceof IP4File);
        final IP4File p4File = (IP4File) resource;
        p4File.edit();
        p4File.refresh();
        assertTrue(p4File.isOpened());
        try {
            Utils.fillFileWithString(diffFile, "");
        } catch (CoreException e) {
            handle(e);
        }
        IFileDiffer differ = DiffRegistry.getRegistry().getDiffer(p4File);
        if (differ == null) {
            differ = DiffRegistry.getRegistry().getDifferByExtension(
                    diffFile.getFileExtension());
        }
        assertNotNull(differ);
        assertEquals(getDifferClass(), differ.getClass());
        final File local = p4File.toFile();
        assertNotNull(local);
        assertTrue(local.exists());
        IStorage left = new P4Storage() {

            public InputStream getContents() throws CoreException {
                try {
                    return new FileInputStream(local);
                } catch (FileNotFoundException e) {
                    return new ByteArrayInputStream(getClass().getName().getBytes());
                }
            }
        };
        IStorage right = new P4Storage() {

            public InputStream getContents() throws CoreException {
                return p4File.getHeadContents();
            }
        };
        assertFalse(differ.diffGenerated(p4File));
        differ.generateDiff(p4File, p4File, left, right);
        Utils.sleep(.1);
        assertTrue(differ.diffGenerated(p4File));
        Object[] diffs = differ.getDiff(p4File);
        assertNotNull(diffs);
        assertTrue(diffs.length > 0);
        for (Object diff : diffs) {
            assertTrue("Class check: " + diff.getClass().getName(),
                    diff instanceof IDiffElement);
        }
    }

    /**
     * Get file
     * 
     * @return - file
     */
    protected abstract IFile getFile();

    /**
     * Get differ class
     * 
     * @return -
     */
    protected abstract Class<? extends IFileDiffer> getDifferClass();

}
