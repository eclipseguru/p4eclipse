/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.patch;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.DeleteAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.patch.model.ErrorCollector;
import com.perforce.team.ui.patch.model.IErrorCollector;
import com.perforce.team.ui.patch.model.P4Patch;
import com.perforce.team.ui.patch.model.WorkspaceStream;

import java.io.ByteArrayInputStream;
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
public class PatchTest extends BasePatchTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("edit.txt"), new ByteArrayInputStream(
                "test\ntest2\n".getBytes()));
        addFile(project.getFile("delete.txt"), new ByteArrayInputStream(
                "del\nete\n".getBytes()));

        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
    }

    /**
     * Test patch with edited file
     */
    public void testEdit() {
        IP4Connection connection = createConnection();
        IFile edit = project.getFile("edit.txt");
        assertTrue(edit.exists());
        EditAction action = new EditAction();
        action.setAsync(false);
        action.selectionChanged(null, new StructuredSelection(edit));
        action.run(null);

        IP4Resource p4Resource = connection.getResource(edit);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;
        assertTrue(p4File.openedForEdit());

        String newContent = "testa\n";
        try {
            edit.setContents(new ByteArrayInputStream(newContent.getBytes()),
                    IResource.FORCE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }

        IFile patchFile = project.getFile("patch.txt");
        assertFalse(patchFile.exists());
        WorkspaceStream stream = new WorkspaceStream();
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
            patchContent = Utils.getContent(patchFile);
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
            String patchedContent = Utils.getContent(edit);
            assertEquals(newContent, patchedContent);
            p4File.refresh();
            assertTrue(p4File.openedForEdit());
        } catch (Exception e) {
            handle(e);
        }
    }

    /**
     * Test patch with added file
     */
    public void testAdd() {
        IP4Connection connection = createConnection();
        IFile add = project.getFile("add.txt");
        assertFalse(add.exists());

        String newContent = "testa\n";
        try {
            add.create(new ByteArrayInputStream(newContent.getBytes()),
                    IResource.FORCE, new NullProgressMonitor());
        } catch (CoreException e) {
            handle(e);
        }

        AddAction action = new AddAction();
        action.setAsync(false);
        action.selectionChanged(null, new StructuredSelection(add));
        action.run(null);

        IP4Resource p4Resource = connection.getResource(add);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;
        assertTrue(p4File.openedForAdd());

        IFile patchFile = project.getFile("patch.txt");
        assertFalse(patchFile.exists());
        WorkspaceStream stream = new WorkspaceStream();
        stream.setFile(patchFile);
        P4Patch patch = new P4Patch(stream, new Object[] { add });
        patch.setAsync(false);
        IErrorCollector collector = new ErrorCollector();
        patch.generate(collector);

        assertEquals(0, collector.getErrorCount());

        assertTrue(patchFile.exists());

        String patchContent = null;
        try {
            patchContent = Utils.getContent(patchFile);
        } catch (Exception e) {
            handle(e);
        }
        assertNotNull(patchContent);
        assertTrue(patchContent.length() > 0);

        RevertAction revert = new RevertAction();
        revert.setAsync(false);
        revert.selectionChanged(null, new StructuredSelection(add));
        revert.runAction(false);

        p4File.refresh();
        assertFalse(p4File.openedForAdd());
        try {
            add.delete(true, new NullProgressMonitor());
        } catch (CoreException e1) {
            handle(e1);
        }
        assertFalse(add.exists());

        try {
            final WorkspacePatcher patcher = new WorkspacePatcher(add);
            patcher.parse(patchFile);
            patcher.refresh();
            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

                @Override
                protected void execute(IProgressMonitor monitor)
                        throws InvocationTargetException {
                    try {
                        patcher.applyAll(monitor, new Patcher.IFileValidator() {

                            public boolean validateResources(IFile[] resoures) {
                                return true;
                            }
                        });
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
            op.run(new NullProgressMonitor());
            assertTrue(add.exists());
            String patchedContent = Utils.getContent(add);
            assertEquals(newContent, patchedContent);
        } catch (Exception e) {
            handle(e);
        }
    }

    /**
     * Test patch with deleted file
     */
    public void testDelete() {
        IP4Connection connection = createConnection();
        IFile delete = project.getFile("delete.txt");
        assertTrue(delete.exists());
        DeleteAction action = new DeleteAction();
        action.setAsync(false);
        action.selectionChanged(null, new StructuredSelection(delete));
        action.run(null);
        assertFalse(delete.exists());

        IP4Resource p4Resource = connection.getResource(delete);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);

        IP4File p4File = (IP4File) p4Resource;
        assertTrue(p4File.openedForDelete());

        IFile patchFile = project.getFile("patch.txt");
        assertFalse(patchFile.exists());
        WorkspaceStream stream = new WorkspaceStream();
        stream.setFile(patchFile);
        P4Patch patch = new P4Patch(stream, new Object[] { delete });
        patch.setAsync(false);
        IErrorCollector collector = new ErrorCollector();
        patch.generate(collector);

        assertEquals(0, collector.getErrorCount());

        assertTrue(patchFile.exists());

        String patchContent = null;
        try {
            patchContent = Utils.getContent(patchFile);
        } catch (Exception e) {
            handle(e);
        }
        assertNotNull(patchContent);
        assertTrue(patchContent.length() > 0);

        RevertAction revert = new RevertAction();
        revert.setAsync(false);
        revert.selectionChanged(null, new StructuredSelection(delete));
        revert.runAction(false);

        p4File.refresh();
        assertFalse(p4File.openedForDelete());
        assertTrue(delete.exists());

        try {
            final WorkspacePatcher patcher = new WorkspacePatcher(delete);
            patcher.parse(patchFile);
            patcher.refresh();
            WorkspaceModifyOperation op = new WorkspaceModifyOperation() {

                @Override
                protected void execute(IProgressMonitor monitor)
                        throws InvocationTargetException {
                    try {
                        patcher.applyAll(monitor, new Patcher.IFileValidator() {

                            public boolean validateResources(IFile[] resoures) {
                                return true;
                            }
                        });
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    }
                }
            };
            op.run(new NullProgressMonitor());
            assertFalse(delete.exists());
            p4File.refresh();
            assertTrue(p4File.openedForDelete());
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
