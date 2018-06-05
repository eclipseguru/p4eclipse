/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ResolveAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.p4java.actions.SyncAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AutoResolveTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        IFile about = project.getFile("about.ini");
        IFile plugin = project.getFile("plugin.xml");
        for (int i = 0; i < 3; i++) {
            addFile(client, about);
            addFile(client, plugin);
        }
    }

    /**
     * Test for job029008 Tests the auto resolve action
     */
    public void testAutoResolve() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        StringBuffer buffer = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    file.getContents()));
            while (reader.ready()) {
                buffer.append((char) reader.read());
            }
            reader.close();
        } catch (Exception e1) {
            assertFalse(true);
        }

        SyncRevisionAction syncAction = new SyncRevisionAction();
        syncAction.setAsync(false);
        syncAction.selectionChanged(null, new StructuredSelection(project));
        syncAction.runAction("#2");
        StringBuffer secondRevision = new StringBuffer();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    file.getContents()));
            while (reader.ready()) {
                secondRevision.append((char) reader.read());
            }
            reader.close();
        } catch (Exception e1) {
            assertFalse(true);
        }

        assertFalse(buffer.equals(secondRevision));

        EditAction editAction = new EditAction();
        editAction.setAsync(false);
        StructuredSelection selection = new StructuredSelection(file);
        editAction.selectionChanged(null, selection);
        editAction.run(null);

        SyncAction syncHeadAction = new SyncAction();
        syncHeadAction.setAsync(false);
        syncHeadAction.selectionChanged(null, selection);
        syncHeadAction.run(null);

        try {
            file.refreshLocal(0, null);
        } catch (CoreException e1) {
            assertFalse(true);
        }

        ResolveAction resolveAction = new ResolveAction();
        resolveAction.setAsync(false);
        resolveAction.selectionChanged(null, new StructuredSelection(file));
        resolveAction.resolve(new ResolveFilesAutoOptions().setAcceptTheirs(true));

        try {
            IEditorPart editor = IDE.openEditor(
                    PerforceUIPlugin.getActivePage(), file,
                    "org.eclipse.ui.DefaultTextEditor");
            assertNotNull(editor);
            IDocumentProvider provider = ((TextEditor) editor)
                    .getDocumentProvider();
            assertNotNull(provider);
            IDocument document = provider.getDocument(editor.getEditorInput());
            assertNotNull(document);
            assertEquals(buffer.toString(), document.get());
        } catch (PartInitException e) {
            assertFalse(true);
        }

        RevertAction revertAction = new RevertAction();
        revertAction.setAsync(false);
        revertAction.selectionChanged(null, new StructuredSelection(file));
        revertAction.runAction(false);
    }

    /**
     * Test for job032011
     */
    public void testForceResolve() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());

        SyncRevisionAction syncAction = new SyncRevisionAction();
        syncAction.setAsync(false);
        syncAction.selectionChanged(null, new StructuredSelection(file));
        syncAction.runAction("#2");

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertEquals(2, p4File.getHaveRevision());

        EditAction editAction = new EditAction();
        editAction.setAsync(false);
        StructuredSelection selection = new StructuredSelection(file);
        editAction.selectionChanged(null, selection);
        editAction.run(null);

        try {
            Utils.fillFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            assertFalse("Error filling file", true);
        }

        assertFalse(p4File.isSynced());

        SyncAction syncHeadAction = new SyncAction();
        syncHeadAction.setAsync(false);
        syncHeadAction.selectionChanged(null, selection);
        syncHeadAction.run(null);

        assertTrue(p4File.isUnresolved());

        ResolveAction resolveAction = new ResolveAction();
        resolveAction.setAsync(false);
        resolveAction.selectionChanged(null, new StructuredSelection(file));
        resolveAction.resolve(new ResolveFilesAutoOptions().setForceResolve(true));

        assertFalse(p4File.isUnresolved());

    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
