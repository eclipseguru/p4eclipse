/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.patch.model.ErrorCollector;
import com.perforce.team.ui.patch.model.FileSystemStream;
import com.perforce.team.ui.patch.model.IErrorCollector;
import com.perforce.team.ui.patch.model.P4Patch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.eclipse.compare.patch.WorkspacePatcherUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FileTest extends BasePatchTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("file.txt"), new ByteArrayInputStream(
                "content\n".getBytes()));
    }

    /**
     * Test empty file stream
     */
    public void testEmpty() {
        FileSystemStream stream = new FileSystemStream();
        try {
            stream.initialize(new NullProgressMonitor());
            assertFalse(true);
        } catch (IOException e) {
            assertNotNull(e);
        }
    }

    /**
     * Test patching to clipboard
     */
    public void testPatch() {
        P4UIUtils.copyToClipboard(" ");
        assertEquals(" ", P4UIUtils.pasteFromClipboard());

        IFile edit = project.getFile("file.txt");
        EditAction action = new EditAction();
        action.setAsync(false);
        action.selectionChanged(null, new StructuredSelection(edit));
        action.run(null);

        try {
            edit.setContents(new ByteArrayInputStream("edited\n".getBytes()),
                    IResource.FORCE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }

        File file = null;
        try {
            file = File.createTempFile("patchtest", ".txt");
        } catch (IOException e) {
            handle(e);
        }
        assertNotNull(file);
        assertTrue(file.exists());
        FileSystemStream stream = new FileSystemStream();
        stream.setValidateFile(false);
        stream.setPath(file.getAbsolutePath());
        P4Patch patch = new P4Patch(stream, new Object[] { edit });
        patch.setAsync(false);
        IErrorCollector collector = new ErrorCollector();
        patch.generate(collector);

        log(Utils.getErrorMessages(collector.getErrors()));
        assertEquals(0, collector.getErrorCount());

        try {
            String patchContent = Utils.getContent(new FileInputStream(file));
            assertNotNull(patchContent);
            assertTrue(patchContent.length() > 0);
            assertTrue(patchContent.startsWith(WorkspacePatcherUI
                    .getWorkspacePatchHeader()));
            assertTrue(patchContent.contains(edit.getFullPath().toString()));
        } catch (Exception e) {
            handle(e);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/patch";
    }

}
