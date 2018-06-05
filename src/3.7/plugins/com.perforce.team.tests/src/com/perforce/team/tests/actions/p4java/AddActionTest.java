/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.OpenAction;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AddActionTest extends OpenActionTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile(".project"), project.getFile(".project")
                .getContents());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.p4eclipse.plugin";
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#createAction()
     */
    @Override
    protected OpenAction createAction() {
        return new AddAction();
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#createFiles()
     */
    @Override
    protected IFile[] createFiles() {
        byte[] content = new String("content").getBytes();
        ByteArrayInputStream stream = new ByteArrayInputStream(content);
        IFile file1 = project.getFile("plugin2.xml");
        assertFalse(file1.exists());
        try {
            file1.create(stream, true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse(true);
        }

        IFile file2 = project.getFile("test1/test.txt");
        assertFalse(file2.exists());
        stream = new ByteArrayInputStream(content);
        try {
            IFolder folder = project.getFolder("test1");
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            file2.create(stream, true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse(true);
        }

        IFile file3 = project.getFile("test2/test3/text.txt");
        assertFalse(file3.exists());
        stream = new ByteArrayInputStream(content);
        try {
            IFolder folder = project.getFolder("test2");
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            folder = folder.getFolder("test3");
            if (!folder.exists()) {
                folder.create(true, true, null);
            }
            file3.create(stream, true, null);
        } catch (CoreException e) {
            e.printStackTrace();
            assertFalse(true);
        }
        return new IFile[] { file1, file2, file3 };
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#getSelection()
     */
    @Override
    protected IStructuredSelection getSelection() {
        return new StructuredSelection(project);
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#validateState(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected void validateState(IP4File file) {
        assertTrue(file.isOpened());
        assertTrue(file.openedForAdd());
        assertSame(FileAction.ADD, file.getAction());
    }

}
