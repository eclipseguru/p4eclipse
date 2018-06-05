/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import com.perforce.p4java.CharsetDefs;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.patch.model.ErrorCollector;
import com.perforce.team.ui.patch.model.IErrorCollector;
import com.perforce.team.ui.patch.model.P4Patch;
import com.perforce.team.ui.patch.model.WorkspaceStream;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.internal.patch.Patcher;
import org.eclipse.compare.internal.patch.WorkspacePatcher;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UnicodeTest extends ProjectBasedTestCase {

    /**
     * Create unicode test
     */
    public UnicodeTest() {
        this.unicode = true;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("unicode.txt"), new ByteArrayInputStream(
                "t₤est\ngrößeren\n".getBytes(CharsetDefs.UTF8)));
    }

    /**
     * Test unicode patch
     */
    public void testPatch() {
        IP4Connection connection = createConnection();
        IFile edit = project.getFile("unicode.txt");
        assertTrue(edit.exists());
        EditAction action = new EditAction();
        action.setAsync(false);
        action.selectionChanged(null, new StructuredSelection(edit));
        action.run(null);

        IP4Resource p4Resource = connection.getResource(edit);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;
        assertTrue(p4File.getHeadType().contains("unicode"));
        assertTrue(p4File.openedForEdit());

        String newContent = "te₤sta\nquält\n";
        try {
            edit.setContents(
                    new ByteArrayInputStream(newContent
                            .getBytes(CharsetDefs.UTF8)), IResource.FORCE,
                    new NullProgressMonitor());
            edit.setCharset(CharsetDefs.UTF8_NAME, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }

        IFile patchFile = project.getFile("patch.txt");
        try {
            patchFile.create(new ByteArrayInputStream(new byte[0]), true,
                    new NullProgressMonitor());
            patchFile.setCharset(CharsetDefs.UTF8_NAME,
                    new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }
        assertTrue(patchFile.exists());
        WorkspaceStream stream = new WorkspaceStream();
        stream.setValidateFile(false);
        stream.setFile(patchFile);
        P4Patch patch = new P4Patch(stream, new Object[] { edit });
        patch.setAsync(false);
        IErrorCollector collector = new ErrorCollector();
        patch.generate(collector);

        log(Utils.getErrorMessages(collector.getErrors()));
        assertEquals(0, collector.getErrorCount());

        assertTrue(patchFile.exists());

        String patchContent = null;
        try {
            patchContent = Utils.getContent(new InputStreamReader(
                    new FileInputStream(patchFile.getLocation().toFile()),
                    CharsetDefs.UTF8));
        } catch (Exception e) {
            handle(e);
        }
        assertNotNull(patchContent);
        assertTrue(patchContent.length() > 0);

        RevertAction revert = new RevertAction();
        revert.setAsync(false);
        revert.selectionChanged(null, new StructuredSelection(edit));
        revert.runAction(false);

        p4File.refresh();
        assertFalse(p4File.openedForEdit());
        assertTrue(edit.exists());

        try {
            final WorkspacePatcher patcher = new WorkspacePatcher(edit);
            patcher.parse(patchFile);
            patcher.refresh();
            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

                @Override
                protected void execute(IProgressMonitor monitor)
                        throws InvocationTargetException {
                    try {
                        patcher.applyAll(monitor, new Patcher.IFileValidator() {

                            public boolean validateResources(IFile[] resoures) {
                                return ResourcesPlugin
                                        .getWorkspace()
                                        .validateEdit(resoures,
                                                P4UIUtils.getShell()).isOK();
                            }
                        });
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
            op.run(new NullProgressMonitor());
            String patchedContent = Utils.getContent(new InputStreamReader(
                    new FileInputStream(edit.getLocation().toFile()),
                    CharsetDefs.UTF8));
            assertEquals(newContent, patchedContent);
            p4File.refresh();
            assertTrue(p4File.openedForEdit());
        } catch (Exception e) {
            handle(e);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/unicodepatch";
    }

}
