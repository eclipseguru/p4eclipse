/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Resource;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.ConfigWizard;
import com.perforce.team.ui.connection.BasicConnectionWidget;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.RevertAction;
import com.perforce.team.ui.p4java.actions.SubmitAction;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmitTest extends ConnectionBasedTestCase {

    private IProject project;
    private IFile file1 = null;
    private IFile file2 = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * @throws Exception
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        try {

            if (project != null) {
                RevertAction revertAction = new RevertAction();
                revertAction.setAsync(false);
                revertAction.selectionChanged(null, new StructuredSelection(
                        project));
                revertAction.runAction(false);

                try {
                    file1.delete(true, null);
                    file2.delete(true, null);
                    project.delete(true, true, null);
                } catch (CoreException e) {
                    handle(e);
                }
            }
        } finally {
            super.tearDown();
        }
    }

    /**
     * Test submit action
     */
    public void testAction() {
        SubmitAction submit = new SubmitAction();
        assertNull(submit.getDescription());
        assertFalse(submit.isReopen());
        assertFalse(submit.isEnabledEx());
    }

    /**
     * Tests the share project wizard
     */
    public void testShareWizard() {
//        IP4Connection connection = createConnection();
//        project = ResourcesPlugin.getWorkspace().getRoot()
//                .getProject("SubmitTest");
//        try {
//            IProjectDescription desc = ResourcesPlugin.getWorkspace()
//                    .newProjectDescription("SubmitTest");
//            IP4Folder folder = connection.getFolder("//depot/SubmitTest");
//            assertNotNull(folder);
//            folder.updateLocation();
//            assertNotNull(folder.getLocalPath());
//            Path path = new Path(folder.getLocalPath());
//            desc.setLocation(path);
//            project.create(desc, null);
//            project.open(null);
//            file1 = project.getFile("Test.txt");
//            URL file1Url = PerforceTestsPlugin.getDefault().getBundle()
//                    .getEntry("/resources/Test.txt");
//            assertNotNull(file1Url);
//            try {
//                file1Url = FileLocator.toFileURL(file1Url);
//            } catch (IOException e) {
//                handle(e);
//            }
//            try {
//                assertFalse(file1.exists());
//                file1.create(file1Url.openStream(), true, null);
//                assertTrue(file1.exists());
//            } catch (IOException e) {
//                handle(e);
//            }
//            file2 = project.getFile("Test2.xml");
//            URL file2Url = PerforceTestsPlugin.getDefault().getBundle()
//                    .getEntry("/resources/Test2.xml");
//            assertNotNull(file2Url);
//            try {
//                file2Url = FileLocator.toFileURL(file2Url);
//            } catch (IOException e) {
//                handle(e);
//            }
//            try {
//                assertFalse(file2.exists());
//                file2.create(file2Url.openStream(), true, null);
//                assertTrue(file2.exists());
//            } catch (IOException e) {
//                handle(e);
//            }
//        } catch (CoreException e) {
//            handle(e);
//        }
//        ConfigWizard wizard = new ConfigWizard();
//        wizard.init(PerforceTestsPlugin.getDefault().getWorkbench(), project);
//        WizardDialog dialog = new WizardDialog(Display.getCurrent()
//                .getActiveShell(), wizard);
//        dialog.setBlockOnOpen(false);
//        try {
//            dialog.open();
//            dialog.showPage(wizard.getConnectionPage());
//            wizard.getConnectionPage().setExistingServer(false);
//            BasicConnectionWidget connDialog = wizard.getConnectionPage()
//                    .getConnectionWidget();
//            connDialog.setCharset(parameters.getCharsetNoNull());
//            connDialog.setClient(parameters.getClientNoNull());
//            connDialog.setPort(parameters.getPortNoNull());
//            connDialog.setUser(parameters.getUserNoNull());
//            wizard.performFinish();
//        } finally {
//            dialog.close();
//        }
//
//        assertNotNull(PerforceTeamProvider.getPerforceProvider(file1));
//        assertNotNull(PerforceTeamProvider.getPerforceProvider(file2));
//
//        StructuredSelection selection = new StructuredSelection(new Object[] {
//                file1, file2, });
//
//        AddAction action = new AddAction();
//        action.setAsync(false);
//        Action wrapAction = new Action() {
//        };
//        wrapAction.setEnabled(false);
//        action.selectionChanged(wrapAction, selection);
//        assertTrue(wrapAction.isEnabled());
//        action.run(null);
//
//        String file1Path = file1.getLocation().makeAbsolute().toString();
//        String file2Path = file2.getLocation().makeAbsolute().toString();
//        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
//        assertNotNull(defaultList);
//        IP4Resource resource1 = connection.getResource(file1);
//        assertNotNull(resource1);
//        IP4Resource resource2 = connection.getResource(file2);
//        assertNotNull(resource2);
//
//        assertTrue(resource1 instanceof IP4File);
//        assertTrue(((IP4File) resource1).openedForAdd());
//        assertTrue(resource2 instanceof IP4File);
//        assertTrue(((IP4File) resource2).openedForAdd());
//
//        assertNotNull(defaultList.getFiles());
//        assertTrue(defaultList.getFiles().length >= 2);
//
//        String desc = "unit test submit";
//
//        ChangeSpecDialog dlg = new ChangeSpecDialog(defaultList,
//                new IP4Resource[] { resource1, resource2 }, Utils.getShell(),
//                true);
//        dlg.setBlockOnOpen(false);
//        dlg.open();
//        dlg.setDescription(desc);
//        dlg.submit();
//        dlg.close();
//        IP4File[] selectedFiles = dlg.getCheckedFiles();
//        assertEquals(2, selectedFiles.length);
//        if (P4CoreUtils.isWindows()) {
//            assertTrue(P4Resource.normalizeLocalPath(file1Path)
//                    .equalsIgnoreCase(
//                            P4Resource.normalizeLocalPath(selectedFiles[0]
//                                    .getLocalPath())));
//            assertTrue(P4Resource.normalizeLocalPath(file2Path)
//                    .equalsIgnoreCase(
//                            P4Resource.normalizeLocalPath(selectedFiles[1]
//                                    .getLocalPath())));
//        } else {
//            assertEquals(file1Path, selectedFiles[0].getLocalPath());
//            assertEquals(file2Path, selectedFiles[1].getLocalPath());
//        }
//        assertEquals(desc, dlg.getDescription());
    }
}
