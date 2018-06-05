/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.OpenAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditActionTest extends OpenActionTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile(".project"), project.getFile(".project")
                .getContents());
        super.addFile(project.getFile("plugin.xml"));
        super.addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
        super.addFile(project.getFile(new Path("icons/changelist/job.gif")));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#createAction()
     */
    @Override
    protected OpenAction createAction() {
        return new EditAction();
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#createFiles()
     */
    @Override
    protected IFile[] createFiles() {
        IFile file1 = project.getFile("plugin.xml");
        IFile file2 = project.getFile("META-INF/MANIFEST.MF");
        IFile file3 = project.getFile("icons/changelist/job.gif");
        return new IFile[] { file1, file2, file3 };
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#getSelection()
     */
    @Override
    protected IStructuredSelection getSelection() {
        return null;
    }

    /**
     * @see com.perforce.team.tests.actions.p4java.OpenActionTest#validateState(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected void validateState(IP4File file) {
        assertTrue(file.getName() + " is not opened", file.isOpened());
        assertTrue(file.getName() + " is not opened for edit",
                file.openedForEdit());
        assertSame(FileAction.EDIT, file.getAction());
    }
}
