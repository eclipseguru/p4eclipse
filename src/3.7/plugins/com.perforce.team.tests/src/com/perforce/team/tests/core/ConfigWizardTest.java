/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import org.eclipse.core.resources.IProject;

import com.perforce.team.tests.ConnectionBasedTestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConfigWizardTest extends ConnectionBasedTestCase {

    private IProject project;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

//    /**
//     * @see com.perforce.team.tests.P4TestCase#tearDown()
//     */
//    @Override
//    public void tearDown() throws Exception {
//        try {
//            project.refreshLocal(IResource.DEPTH_INFINITE, null);
//            project.accept(new IResourceVisitor() {
//
//                public boolean visit(IResource resource) throws CoreException {
//                    ResourceAttributes attrs = resource.getResourceAttributes();
//                    if (attrs != null) {
//                        attrs.setReadOnly(false);
//                        try {
//                            resource.setResourceAttributes(attrs);
//                        } catch (CoreException e) {
//                        }
//                    }
//                    return true;
//                }
//            });
//            project.delete(true, true, null);
//            assertFalse(project.exists());
//        } catch (CoreException e) {
//            handle(e);
//        } finally {
//            super.tearDown();
//        }
//    }

    /**
     * Tests the share project wizard
     */
    public void testShareWizard() {
//        project = ResourcesPlugin.getWorkspace().getRoot()
//                .getProject("ConfigWizardTest");
//        IFile file1 = null;
//        IFile file2 = null;
//        try {
//            IProjectDescription desc = ResourcesPlugin.getWorkspace()
//                    .newProjectDescription("ConfigWizardTest");
//            IP4Folder folder = createConnection().getFolder(
//                    "//depot/ConfigWizardTest");
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
//                assertFalse(true);
//            }
//            try {
//                file1.create(file1Url.openStream(), true, null);
//                assertTrue(file1.exists());
//            } catch (IOException e) {
//                assertFalse(true);
//            }
//            file2 = project.getFile("Test2.xml");
//            URL file2Url = PerforceTestsPlugin.getDefault().getBundle()
//                    .getEntry("/resources/Test.txt");
//            assertNotNull(file2Url);
//            try {
//                file2Url = FileLocator.toFileURL(file2Url);
//            } catch (IOException e) {
//                assertFalse(true);
//            }
//            try {
//                file2.create(file2Url.openStream(), true, null);
//                assertTrue(file2.exists());
//            } catch (IOException e) {
//                assertFalse(true);
//            }
//        } catch (CoreException e) {
//            assertFalse(true);
//        }
//        ConfigWizard wizard = new ConfigWizard();
//        wizard.init(PerforceTestsPlugin.getDefault().getWorkbench(), project);
//        WizardDialog dialog = new WizardDialog(Display.getCurrent()
//                .getActiveShell(), wizard);
//
//        boolean closed = false;
//        try {
//            dialog.setBlockOnOpen(false);
//            dialog.open();
//            dialog.showPage(wizard.getConnectionPage());
//            BasicConnectionWidget connDialog = wizard.getConnectionPage()
//                    .getConnectionWidget();
//            IP4Connection[] connections = P4Workspace.getWorkspace()
//                    .getConnections();
//            assertEquals(connections.length == 0, connDialog.isEnabled());
//            connDialog.validate();
//            connDialog.setPort(parameters.getPortNoNull());
//            connDialog.setUser(parameters.getUserNoNull());
//            connDialog.setClient(parameters.getClientNoNull());
//            connDialog.setCharset(parameters.getCharsetNoNull());
//            ConnectionParameters created = connDialog.getConnectionParameters();
//            assertNotNull(created);
//            assertEquals(parameters.getPortNoNull(), created.getPortNoNull());
//            assertEquals(parameters.getUserNoNull(), created.getUserNoNull());
//            assertEquals(parameters.getClientNoNull(),
//                    created.getClientNoNull());
//            assertEquals(CharSetApi.getName(0), created.getCharsetNoNull());
//            assertNull(connDialog.getErrorMessage());
//            wizard.performFinish();
//            dialog.close();
//            closed = true;
//
//            assertNotNull(PerforceTeamProvider.getPerforceProvider(project));
//            assertNotNull(PerforceTeamProvider.getPerforceProvider(file1));
//            assertNotNull(PerforceTeamProvider.getPerforceProvider(file2));
//            assertEquals(PerforceTeamProvider.getPerforceProvider(file1),
//                    PerforceTeamProvider.getPerforceProvider(file2));
//
//            assertNotNull(PerforceProviderPlugin
//                    .getPerforceProviderFor(project));
//            assertNotNull(PerforceProviderPlugin.getPerforceProviderFor(file1));
//            assertNotNull(PerforceProviderPlugin.getPerforceProviderFor(file2));
//            assertEquals(PerforceProviderPlugin.getPerforceProviderFor(file1),
//                    PerforceProviderPlugin.getPerforceProviderFor(file2));
//            String[] paths = PerforceProviderPlugin
//                    .getResourcePath(new IResource[] { project, file1, file2 });
//            assertNotNull(paths);
//            assertEquals(3, paths.length);
//            assertNotNull(paths[0]);
//            assertNotNull(paths[1]);
//            assertNotNull(paths[2]);
//        } finally {
//            if (!closed) {
//                dialog.close();
//            }
//        }
    }
}
