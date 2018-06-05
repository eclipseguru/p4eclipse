/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.p4java.actions.AddIgnoreAction;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AddIgnoreActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile("about.ini"));
        super.addFile(project.getFile(new Path("bin/output.txt")));
    }

    /**
     * Test action enablement
     */
    public void testEnablement() {
        IFile existing = project.getFile("about.ini");
        Action wrap = Utils.getDisabledAction();
        AddIgnoreAction ignore = new AddIgnoreAction();
        ignore.selectionChanged(wrap, new StructuredSelection(existing));
        assertFalse(wrap.isEnabled());

        IFile newFile = project.getFile("abount2.ini");
        ignore.selectionChanged(wrap, new StructuredSelection(newFile));
        assertTrue(wrap.isEnabled());

        IFolder folder = project.getFolder("bin");
        assertTrue(folder.exists());
        ignore.selectionChanged(wrap, new StructuredSelection(folder));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test the add ignore action which adds things to a p4 ignore file
     */
    public void testAction() {

        IFile file = project.getFile("about2.ini");
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            file.create(fileUrl.openStream(), true, null);
            assertTrue(file.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        assertFalse(IgnoredFiles.isIgnored(file));

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertFalse(p4File.isRemote());
        assertEquals(0, p4File.getHeadRevision());
        assertEquals(0, p4File.getHaveRevision());

        Action wrap = Utils.getDisabledAction();
        AddIgnoreAction ignore = new AddIgnoreAction();
        ignore.setAsync(false);
        ignore.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());

        ignore.run(wrap);

        assertTrue(IgnoredFiles.isIgnored(file));

    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
