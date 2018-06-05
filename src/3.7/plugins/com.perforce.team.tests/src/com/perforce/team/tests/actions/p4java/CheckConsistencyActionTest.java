/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4JavaSysFileCommandsHelper;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.CheckConsistencyAction;
import com.perforce.team.ui.p4java.actions.CheckConsistencyAction.Consistency;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourceAttributes;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CheckConsistencyActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile(".project"), project.getFile(".project")
                .getContents());
        super.addFile(project.getFile("plugin.xml"));
        super.addFile(project.getFile(new Path("images/empty.gif")));
        super.addFile(project.getFile(new Path("META-INF/MANIFEST.MF")));
    }

    /**
     * Test the action enablement
     */
    public void testEnablement() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        connection.setOffline(true);
        assertTrue(connection.isOffline());

        Action wrap = Utils.getDisabledAction();
        CheckConsistencyAction action = new CheckConsistencyAction();
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertFalse(wrap.isEnabled());

        connection.setOffline(false);
        assertFalse(connection.isOffline());

        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test the action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        CheckConsistencyAction action = new CheckConsistencyAction();
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertTrue(wrap.isEnabled());

        IFile newFile = project.getFile(new Path("images/about2.ini"));
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            newFile.create(fileUrl.openStream(), true, null);
            assertTrue(newFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        IFile diffFile = project.getFile("plugin.xml");
        assertTrue(diffFile.exists());
        ResourceAttributes attributes = diffFile.getResourceAttributes();
        attributes.setReadOnly(false);
        try {
            diffFile.setResourceAttributes(attributes);
            diffFile.setContents(fileUrl.openStream(), IResource.FORCE, null);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }

        IFile missingFile = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(missingFile.exists());

        File rawFile = missingFile.getLocation().toFile();
        new P4JavaSysFileCommandsHelper().setWritable(
                rawFile.getAbsolutePath(), true);
        assertTrue(rawFile.delete());
        assertFalse(rawFile.exists());
        // try {
        // missingFile.refreshLocal(IResource.DEPTH_ONE, null);
        // } catch (CoreException e) {
        // assertFalse("Core exception thrown", true);
        // }
        // assertFalse(missingFile.exists());

        // try {
        // project.refreshLocal(IResource.DEPTH_INFINITE, null);
        // } catch (CoreException e) {
        // assertFalse("Core exception thrown", true);
        // }

        Consistency consistency = action.calculateConsistency(P4Workspace
                .getWorkspace().getConnection(project),
                new IResource[] { project }, new NullProgressMonitor());

        assertNotNull(consistency.diffFiles);
        assertNotNull(consistency.newFiles);
        assertNotNull(consistency.missingFiles);

        assertEquals(1, consistency.newFiles.length);
        assertEquals(newFile, consistency.newFiles[0]);

        assertEquals(1, consistency.diffFiles.length);
        assertEquals(diffFile, consistency.diffFiles[0]);

        assertEquals(1, consistency.missingFiles.length);
        assertEquals(missingFile, consistency.missingFiles[0]);

    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.core";
    }

}
