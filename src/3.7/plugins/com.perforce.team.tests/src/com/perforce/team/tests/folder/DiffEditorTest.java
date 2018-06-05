/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.folder;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.diff.editor.FolderDiffEditor;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;
import com.perforce.team.ui.folder.diff.editor.input.DiffConfiguration;
import com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.FileEntry;
import com.perforce.team.ui.folder.diff.model.IFolderDiffListener;

import java.io.ByteArrayInputStream;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffEditorTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        addFile(client, project.getFile("diff.txt"), new ByteArrayInputStream(
                "content1".getBytes()));
        addFile(client, project.getFile("diff.txt"), new ByteArrayInputStream(
                "content2".getBytes()));
    }

    /**
     * Test editor input
     */
    public void testInput() {
        IP4Connection connection = createConnection();
        FolderDiffInput input = new FolderDiffInput(connection);
        input.addPaths(getPath() + IP4Connection.REMOTE_ELLIPSIS, getPath()
                + IP4Connection.REMOTE_ELLIPSIS);
        assertNotNull(input.getLeftConfiguration());
        assertNotNull(input.getRightConfiguration());
        assertNotNull(input.getHeaderConfiguration());
        assertEquals(connection, input.getConnection());
        assertEquals(connection, input.getAdapter(IP4Connection.class));
        assertEquals(connection, input.getAdapter(IP4Connection.class));

        FileDiffContainer container = input.generateDiffs("#2", "#1",
                new NullProgressMonitor());
        assertNotNull(container);
        assertEquals(1, container.getContentCount());
        assertEquals(0, container.getUniqueCount());
        assertEquals(0, container.getLeftUniqueCount());
        assertEquals(0, container.getRightUniqueCount());
        FileEntry[] entries = container.getEntries();
        assertNotNull(entries);
        assertEquals(2, entries.length);
        assertEquals(entries[0], entries[1].getPair());
        assertEquals(entries[1], entries[0].getPair());
    }

    /**
     * Test editor opening
     */
    public void testEditor() {
        IP4Connection connection = createConnection();
        IDiffConfiguration left = new DiffConfiguration(getPath()
                + IP4Connection.REMOTE_ELLIPSIS);
        left.getOptions().setRevision("2");
        left.getOptions().setRevisionFilter(true);
        left.getOptions().setHeadFilter(false);
        IDiffConfiguration right = new DiffConfiguration(left.getLabel(left));
        right.getOptions().setRevision("1");
        right.getOptions().setRevisionFilter(true);
        left.getOptions().setHeadFilter(false);
        FolderDiffInput input = new FolderDiffInput(connection, left, right,
                null);
        input.addPaths(getPath() + IP4Connection.REMOTE_ELLIPSIS, getPath()
                + IP4Connection.REMOTE_ELLIPSIS);

        try {
            IEditorPart part = IDE.openEditor(PerforceUIPlugin.getActivePage(),
                    input, FolderDiffEditor.ID);
            assertNotNull(part);
            assertTrue(part instanceof FolderDiffEditor);
            FolderDiffEditor editor = (FolderDiffEditor) part;
            IFormPage page = editor.getActivePageInstance();
            assertNotNull(page);
            assertTrue(page instanceof FolderDiffPage);
            FolderDiffPage diffPage = (FolderDiffPage) page;
            assertNotNull(diffPage.getType());
            assertNull(diffPage.getContainer());
            final boolean[] done = new boolean[] { false };
            diffPage.addListener(new IFolderDiffListener() {

                public void diffsGenerated(FileDiffContainer container) {
                    done[0] = true;
                }
            });
            diffPage.loadDifferences();
            while (!done[0]) {
                Utils.sleep(.1);
            }
            Utils.sleep(.1);
            assertNotNull(diffPage.getContainer());
        } catch (PartInitException e) {
            handle(e);
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/folder/diff";
    }

}
