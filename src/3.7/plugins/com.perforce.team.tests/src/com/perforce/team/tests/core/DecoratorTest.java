/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.decorators.DecoratorDefinition;
import org.eclipse.ui.internal.decorators.DecoratorManager;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceNavigator;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4TeamUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.decorator.P4Decoration;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.AddIgnoreAction;
import com.perforce.team.ui.p4java.actions.DeleteAction;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.p4java.actions.LockAction;
import com.perforce.team.ui.p4java.actions.SyncAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.DepotView;
import com.perforce.team.ui.views.PendingView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DecoratorTest extends ProjectBasedTestCase {

    private static final String PATH = "//depot/p07.2/p4-eclipse/com.perforce.team.plugin";

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        IClient client = createConnection().getClient();
        for (int i = 0; i < 5; i++) {
            addFile(client, project.getFile("file" + i + ".txt"));
            addFile(client, project.getFile(new Path("META-INF/MANIFEST.MF")));
            addFile(client, project.getFile("plugin.xml"));
        }
        addDepotFile(client, "//depot/r05.2/p4-eclipse/installer/site.xml");
        addFile(client, project.getFile("p4eclipse.properties"));
        deleteFile(client, project.getFile("p4eclipse.properties"));

        Utils.clearDecoratorPrefs();
        DecoratorManager manager = WorkbenchPlugin.getDefault()
                .getDecoratorManager();
        manager.clearCaches();
        for (DecoratorDefinition definitions : manager
                .getAllDecoratorDefinitions()) {
            if (definitions.getId().equals(
                    "com.perforce.team.ui.decorator.PerforceDecorator")) {
                definitions.setEnabled(true);
            }
        }
        manager.clearCaches();
        manager.updateForEnablementChange();
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        Utils.getUIStore().setToDefault(
                IPreferenceConstants.FILE_DECORATION_TEXT);

        DecoratorManager manager = WorkbenchPlugin.getDefault()
                .getDecoratorManager();
        manager.clearCaches();
        for (DecoratorDefinition definitions : manager
                .getAllDecoratorDefinitions()) {
            if (definitions.getId().equals(
                    "com.perforce.team.ui.decorator.PerforceDecorator")) {
                definitions.setEnabled(false);
            }
        }
        manager.clearCaches();
        manager.updateForEnablementChange();
    }

    /**
     * Test the perforce decorations
     */
    public void testDefaultNavigatorDecorations() {
        ResourceNavigator navigator = null;
        try {
            navigator = (ResourceNavigator) PerforceUIPlugin.getActivePage()
                    .showView("org.eclipse.ui.views.ResourceNavigator");
        } catch (PartInitException e) {
            assertFalse(true);
        }
        assertNotNull(navigator);

        TreeViewer viewer = navigator.getViewer();
        assertNotNull(viewer);

        TreeItem[] items = viewer.getTree().getItems();

        Utils.sleep(.1);

        IP4Connection connection = createConnection();

        String projectLabel = "[" + parameters.getPort() + ", "
                + parameters.getClient() + ", " + parameters.getUser() + "]";
        assertTrue(items[0].getText().contains(projectLabel));

        viewer.expandToLevel(2);
        items = viewer.getTree().getItems()[0].getItems();
        assertTrue(items.length > 5);

        Utils.sleep(.1);

        Utils.waitForFamily(DecoratorManager.FAMILY_DECORATE);

        boolean fileProcessed = false;
        for (TreeItem item : items) {
            if (item.getData() instanceof IFile) {
                IP4File p4File = (IP4File) connection.getResource((IFile) item
                        .getData());
                Utils.waitForFamily(DecoratorManager.FAMILY_DECORATE); // wait for decorating end
                if (p4File.isRemote()) {
                    assertTrue(item.getText(),
                            item.getText().matches("^.*#\\d/\\d.* <\\w+>$"));
                    fileProcessed = true;
                }
            }
        }
        assertTrue(fileProcessed);
    }

    public void testDepotViewConnectionDecorations() {
        Utils.getUIStore().setValue(
                IPreferenceConstants.CONNECTION_DECORATION_TEXT,"{name} [{[offline]}{[stream_name]}{,[stream_root]}{,[sandbox]}]");
        
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project);
        
        DepotView view = null;
        try {
            view = (DepotView) PerforceUIPlugin.getActivePage()
                    .showView(DepotView.VIEW_ID);
        } catch (PartInitException e) {
            assertFalse(true);
        }
        assertNotNull(view);

        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);

        Utils.sleep(.1);
        
        Utils.waitForFamily(DecoratorManager.FAMILY_DECORATE);
        
        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);
        view.getViewer().expandAll();

        for (TreeItem item : tree.getItems()) {
        	Object data = item.getData();
        	assertTrue(data instanceof IP4Connection);
        	if(data==connection){
        		assertTrue(!item.getText().contains(IPerforceUIConstants.DEC_OFFLINE));
        		break;
        	}
        }

        connection.setOffline(true);
    	viewer.refresh(connection, true);
        Utils.waitForFamily(DecoratorManager.FAMILY_DECORATE);

        for (TreeItem item : tree.getItems()) {
        	Object data = item.getData();
        	assertTrue(data instanceof IP4Connection);
        	if(data==connection){
        		assertTrue(item.getText().contains(IPerforceUIConstants.DEC_OFFLINE));
        		break;
        	}
        }

    }
    
    /**
     * Test the decorations in the pending view
     */
    public void testPendingViewDecorations() {
        EditAction edit = new EditAction();
        edit.setAsync(false);
        StructuredSelection selection = new StructuredSelection(project);
        Action wrapAction = new Action() {
        };
        wrapAction.setEnabled(false);
        edit.selectionChanged(wrapAction, selection);
        assertTrue(wrapAction.isEnabled());
        edit.run(null);

        PendingView view = PendingView.showView();
        ((PerforceContentProvider) view.getViewer().getContentProvider())
                .setLoadAsync(false);
        view.getPerforceViewControl().changeConnection(new StructuredSelection(project));
        view.showOtherChanges(false);

        while (view.isLoading()) {
            Utils.sleep(.1);
        }

        Tree tree = view.getViewer().getTree();
        assertNotNull(tree);
//        view.getViewer().expandToLevel(2);
        view.getViewer().expandAll();
        boolean filesProcessed = false;
        for (TreeItem item : tree.getItems()) {
            if (item.getText().startsWith("Default")) {
                TreeItem[] files = item.getItems();
                for (TreeItem file : files) {
                    if (file.getData() instanceof IP4File) {
                        IP4File p4File = (IP4File) file.getData();
                        IFile[] ifiles = p4File.getLocalFiles();
                        assertNotNull(ifiles);
                        for (IFile ifile : ifiles) {
                            if (ifile != null
                                    && ifile.getProject().equals(project)) {
                                assertTrue(file.getText().contains(
                                        p4File.getRemotePath()));
                                assertTrue(file.getText().contains("<edit>"));
                                assertTrue(file.getText().matches(
                                        "^.*<\\w+><\\w+>$"));
                                filesProcessed = true;
                            }
                        }
                    }
                }
                break;
            }
        }
        assertTrue(filesProcessed);
    }

    /**
     * Test the open for edit image decoration for a file not in the eclispe
     * workspace
     */
    public void testEditUnmanaged() {
        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(project);
        IP4File file = null;

        try {
            String path = "//depot/r05.2/p4-eclipse/installer/site.xml";
            try {
                List<IFileSpec> specs = new ArrayList<IFileSpec>();
                specs.add(new FileSpec(path));
                connection.getClient().editFiles(specs, false, false, -1, null);
            } catch (Exception e) {
                assertFalse("Exception thrown editing file", true);
            }

            IP4PendingChangelist defaultList = connection
                    .getPendingChangelist(0);
            assertNotNull(defaultList);
            defaultList.refresh();
            IP4File[] files = defaultList.getPendingFiles();
            assertNotNull(files);

            for (IP4File open : files) {
                if (path.equals(open.getRemotePath())) {
                    file = open;
                    break;
                }
            }

            assertNotNull(file);
            assertTrue(file.isOpened());
            assertTrue(file.openedByOwner());
            assertFalse(file.isReadOnly());

            ImageDescriptor desc = Utils
                    .getUIDescriptor(IPerforceUIConstants.IMG_UNMANAGED_FILE_ECLIPSE);
            Image base = desc.createImage();
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                            IPerforceUIConstants.ICON_BOTTOM_RIGHT);
            OverlayIcon icon = new OverlayIcon(
                    base,
                    new ImageDescriptor[] { Utils
                            .getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT) },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
            Image expected = icon.createImage();
            PerforceDecorator decorator = new PerforceDecorator(true);
            Image decorated = decorator.decorateImage(base, file);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } finally {
            if (file != null) {
                file.revert();
                file.refresh();
                assertFalse(file.isOpened());
            }
        }
    }

    /**
     * Tests the open for edit image decoration
     */
    public void testEditImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        OverlayIcon icon = new OverlayIcon(base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the open for edit text decoration
     */
    public void testEditText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml #" + have + "/" + head + " <\\w+><edit>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the open for add image decoration
     */
    public void testAddImage() {
        IFile file = project.getFile("plugin2.xml");
        assertFalse(file.exists());
        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(file));
        add.run(null);
        P4Workspace.getWorkspace().getResource(file);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        OverlayIcon icon = new OverlayIcon(base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_ADD) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the open for add text decoration
     */
    public void testAddText() {
        IFile file = project.getFile("plugin2.xml");
        assertFalse(file.exists());
        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(file));
        add.run(null);
        P4Workspace.getWorkspace().getResource(file);
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin2\\.xml  <\\w+><add>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the open for delete image decoration
     */
    public void testDeleteImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(file));
        delete.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_DELETE) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the open for delete text decoration
     */
    public void testDeleteText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        DeleteAction delete = new DeleteAction();
        delete.setAsync(false);
        delete.selectionChanged(null, new StructuredSelection(file));
        delete.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml #" + have + "/" + head
                + " <\\w+><delete>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Test branch text
     */
    public void testBranchText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        IntegrateAction integrate = new IntegrateAction();
        integrate.setAsync(false);
        P4FileIntegration fileIntegrate = new P4FileIntegration();
        fileIntegrate.setSource(file.getLocation().makeAbsolute().toOSString());
        IFile toFile = project.getFile("plugin123123.xml");
        assertFalse(toFile.exists());
        fileIntegrate.setTarget(toFile.getLocation().makeAbsolute()
                .toOSString());
        integrate.integrate(connection, fileIntegrate, 0, P4TeamUtils.createDefaultIntegration(connection));
        IP4Resource resource = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(toFile.getName(), toFile);
        String regex = "^plugin123123\\.xml  <\\w+><branch>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the open for branch image decoration
     */
    public void testBranchImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        IntegrateAction integrate = new IntegrateAction();
        integrate.setAsync(false);
        P4FileIntegration fileIntegrate = new P4FileIntegration();
        fileIntegrate.setSource(file.getLocation().makeAbsolute().toOSString());
        IFile toFile = project.getFile("plugin123123.xml");
        assertFalse(toFile.exists());
        fileIntegrate.setTarget(toFile.getLocation().makeAbsolute()
                .toOSString());
        integrate.integrate(connection, fileIntegrate, 0, P4TeamUtils.createDefaultIntegration(connection));
        IP4Resource resource = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(toFile.getName());
        Image base = desc.createImage();
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_BRANCH) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, toFile);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Test integrate text
     */
    public void testIntegrateText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        IntegrateAction integrate = new IntegrateAction();
        integrate.setAsync(false);
        P4FileIntegration fileIntegrate = new P4FileIntegration();
        fileIntegrate.setSource(file.getLocation().makeAbsolute().toOSString());
        IFile toFile = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(toFile.exists());
        fileIntegrate.setTarget(toFile.getLocation().makeAbsolute()
                .toOSString());
        P4IntegrationOptions options = getBaselessOptions(connection, true);
        options.setForce(true);
        integrate.integrate(connection, fileIntegrate, 0, options);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(toFile.getName(), toFile);
        String regex = "^MANIFEST\\.MF #" + have + "/" + head
                + " <\\w+><integrate>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the open for integrate image decoration
     */
    public void testIntegrateImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        IntegrateAction integrate = new IntegrateAction();
        integrate.setAsync(false);
        P4FileIntegration fileIntegrate = new P4FileIntegration();
        fileIntegrate.setSource(file.getLocation().makeAbsolute().toOSString());
//        P4IntegrationOptions options = new P4IntegrationOptions();
//        options.setBaselessMerge(true);
        P4IntegrationOptions options = getBaselessOptions(connection, true);
        options.setForce(true);
        IFile toFile = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(toFile.exists());
        fileIntegrate.setTarget(toFile.getLocation().makeAbsolute()
                .toOSString());
        integrate.integrate(connection, fileIntegrate, 0, options);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(toFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(toFile.getName());
        Image base = desc.createImage();
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_INTEGRATE) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, toFile);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the synced image decoration
     */
    public void testSyncedImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                        IPerforceUIConstants.ICON_TOP_RIGHT);
        OverlayIcon icon = new OverlayIcon(base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC) },
                new int[] { IPerforceUIConstants.ICON_TOP_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the synced text decoration
     */
    public void testSyncedText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml #" + have + "/" + head + " <\\w+>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the not synced image decoration
     */
    public void testNotSyncImage() {
        IFile file = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(file.exists());
        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#1");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_SYNC_ICON,
                        IPerforceUIConstants.ICON_TOP_RIGHT);
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_NOTSYNC) },
                new int[] { IPerforceUIConstants.ICON_TOP_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the not synced text decoration
     */
    public void testNotSyncedText() {
        IFile file = project.getFile(new Path("META-INF/MANIFEST.MF"));
        assertTrue(file.exists());
        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#1");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^MANIFEST\\.MF #" + have + "/" + head + " <\\w+>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));

    }

    /**
     * Tests the locked image decoration
     */
    public void testLockedImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        LockAction lock = new LockAction();
        lock.setAsync(false);
        lock.selectionChanged(null, new StructuredSelection(file));
        lock.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                        IPerforceUIConstants.ICON_TOP_RIGHT);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_LOCK_ICON,
                        IPerforceUIConstants.ICON_TOP_LEFT);
        OverlayIcon icon = new OverlayIcon(base, new ImageDescriptor[] {
                Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT),
                Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC),
                Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_LOCK) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT,
                        IPerforceUIConstants.ICON_TOP_RIGHT,
                        IPerforceUIConstants.ICON_TOP_LEFT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the locked text decoration
     */
    public void testLockedText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml #" + have + "/" + head + " <\\w+><edit>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the unresolved image decoration
     */
    public void testUnresolvedImage() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#1");
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        SyncAction latest = new SyncAction();
        latest.setAsync(false);
        latest.selectionChanged(null, new StructuredSelection(file));
        latest.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);

        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                        IPerforceUIConstants.ICON_TOP_RIGHT);
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_LEFT);
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] {
                        Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT),
                        Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC),
                        Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_UNRESOLVED) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT,
                        IPerforceUIConstants.ICON_TOP_RIGHT,
                        IPerforceUIConstants.ICON_BOTTOM_LEFT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        Image decorated = decorator.decorateImage(base, file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests the unresolved text decoration
     */
    public void testUnresolvedText() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#1");
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);
        SyncAction latest = new SyncAction();
        latest.setAsync(false);
        latest.selectionChanged(null, new StructuredSelection(file));
        latest.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml #" + have + "/" + head + " <\\w+><edit>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests online project image decoration
     */
    public void testOnlineProjectImage() {
        P4Workspace.getWorkspace().getResource(project);
        PerforceDecorator decorator = new PerforceDecorator(true);
        WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
        Image base = provider.getImage(project);
        assertNotNull(base);
        OverlayIcon icon = new OverlayIcon(
                base,
                new ImageDescriptor[] { TeamImages
                        .getImageDescriptor(ISharedImages.IMG_CHECKEDIN_OVR), },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT, });
        Image expected = icon.createImage();
        Image decorated = decorator.decorateImage(base, project);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Tests online project text decoration
     */
    public void testOnlineProjectText() {
        P4Workspace.getWorkspace().getResource(project);
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(project.getName(), project);
        String projectLabel = project.getName() + " [" + parameters.getPort()
                + ", " + parameters.getClient() + ", " + parameters.getUser()
                + "]";
        assertEquals(projectLabel, decorated);
    }

    /**
     * Tests offline project image decoration
     */
    public void testOfflineProjectImage() {
        P4Workspace.getWorkspace().getResource(project);
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        connection.setOffline(true);
        try {
            PerforceDecorator decorator = new PerforceDecorator(true);
            WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
            Image base = provider.getImage(project);
            assertNotNull(base);
            OverlayIcon icon = new OverlayIcon(
                    base,
                    new ImageDescriptor[] { Utils
                            .getUIDescriptor(IPerforceUIConstants.IMG_DEC_OFFLINE), },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT, });
            Image expected = icon.createImage();
            Image decorated = decorator.decorateImage(base, project);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } finally {
            connection.setOffline(false);
        }
    }

    /**
     * Tests offline project text decoration
     */
    public void testOfflineProjectText() {
        P4Workspace.getWorkspace().getResource(project);
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        connection.setOffline(true);
        try {
            PerforceDecorator decorator = new PerforceDecorator(true);
            String decorated = decorator.decorateText(project.getName(),
                    project);
            String projectLabel = project.getName() + " ["+IPerforceUIConstants.DEC_OFFLINE+", "
                    + parameters.getPort() + ", " + parameters.getClient()
                    + ", " + parameters.getUser() + "]";
            assertEquals(projectLabel, decorated);
        } finally {
            connection.setOffline(false);
        }
    }

    /**
     * Tests offline connection text decoration
     */
    public void testOfflineConnectionTest() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        connection.setOffline(true);
        try {
            PerforceDecorator decorator = new PerforceDecorator(true);
            String decorated = decorator.decorateText(connection.toString(),
                    connection);
            String projectLabel = connection.toString() + " ["+IPerforceUIConstants.DEC_OFFLINE+"]";
            assertEquals(projectLabel, decorated);
        } finally {
            connection.setOffline(false);
        }
    }

    /**
     * Basic test of overlay icon class
     */
    public void testOverlay() {
        WorkbenchLabelProvider provider = new WorkbenchLabelProvider();
        Image base = provider.getImage(project);
        OverlayIcon icon1 = new OverlayIcon(base, new ImageDescriptor[0],
                new int[0]);
        OverlayIcon icon2 = new OverlayIcon(
                base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_OFFLINE) },
                new int[] { 0 });
        OverlayIcon icon3 = new OverlayIcon(base, new ImageDescriptor[0],
                new int[0]);
        assertFalse(icon1.equals(icon2));
        assertEquals(icon1, icon3);
        assertFalse(icon1.equals(new Object()));
    }

    /**
     * Tests the ignored text decoration
     */
    public void testIgnoredTextDecoration() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        AddIgnoreAction ignore = new AddIgnoreAction();
        ignore.setAsync(false);
        ignore.selectionChanged(null, new StructuredSelection(file));
        ignore.run(null);
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_IGNORED_TEXT, true);
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^plugin\\.xml <ignored>$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the text decoration of a file where the head revision is a delete
     */
    public void testHeadRevisionDeletedText() {
        IFile file = project.getFile(new Path("p4eclipse.properties"));
        assertFalse(file.exists());
        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#1");
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^p4eclipse\\.properties #" + have + "/" + head
                + " <\\w+><-head rev deleted->$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Tests the text decoration of a file where the have revision is a delete
     */
    public void testHaveRevisionDeletedText() {
        IFile file = project.getFile(new Path("p4eclipse.properties"));
        assertFalse(file.exists());
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        p4File.refresh();
        int have = p4File.getHaveRevision();
        int head = p4File.getHeadRevision();
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText(file.getName(), file);
        String regex = "^p4eclipse\\.properties #" + have + "/" + head
                + " <\\w+><-deleted->$";
        assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                decorated.matches(regex));
    }

    /**
     * Test the text decoration of a linked resource
     */
    public void testLinkedDecorationText() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        try {
            IFile linked = project.getFile("plugin_linked.xml");
            assertFalse(linked.exists());
            linked.createLink(file.getLocation().makeAbsolute(), 0, null);
            assertTrue(linked.isLinked());
            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(linked));
            edit.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    linked);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            int have = p4File.getHaveRevision();
            int head = p4File.getHeadRevision();
            PerforceDecorator decorator = new PerforceDecorator(true);
            String decorated = decorator.decorateText(linked.getName(), linked);
            String regex = "^plugin_linked\\.xml #" + have + "/" + head
                    + " <\\w+><edit>$";
            assertTrue(decorated + " didn't match regex \"" + regex + "\"",
                    decorated.matches(regex));
        } catch (CoreException e) {
            String message = e.getMessage();
            if (e.getStatus() != null) {
                message += " " + e.getStatus().getMessage();
            }
            if (e.getCause() != null) {
                message += " " + e.getCause().getMessage();
                StringWriter writer = new StringWriter();
                e.getCause().printStackTrace(new PrintWriter(writer));
                message += " " + writer.toString();
            }
            assertFalse("Core exception thrown: " + message + " : " + e, true);
        }
    }

    /**
     * Test the icon decoration of a linked resource
     */
    public void testLinkedDecorationImage() {
        Utils.getUIStore().setValue(IPerforceUIConstants.PREF_REFACTOR_SUPPORT,
                true);
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        try {
            IFile linked = project.getFile("plugin_linked.xml");
            assertFalse(linked.exists());
            linked.createLink(file.getLocation().makeAbsolute(),
                    IResource.REPLACE, null);
            assertTrue(linked.isLinked());
            EditAction edit = new EditAction();
            edit.setAsync(false);
            edit.selectionChanged(null, new StructuredSelection(linked));
            edit.run(null);
            LockAction lock = new LockAction();
            lock.setAsync(false);
            lock.selectionChanged(null, new StructuredSelection(linked));
            lock.run(null);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    linked);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            ImageDescriptor desc = PlatformUI.getWorkbench()
                    .getEditorRegistry().getImageDescriptor(linked.getName());
            Image base = desc.createImage();
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_OPEN_ICON,
                            IPerforceUIConstants.ICON_BOTTOM_RIGHT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_SYNC2_ICON,
                            IPerforceUIConstants.ICON_TOP_RIGHT);
            Utils.getUIPlugin()
                    .getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_FILE_LOCK_ICON,
                            IPerforceUIConstants.ICON_TOP_LEFT);
            OverlayIcon icon = new OverlayIcon(base, new ImageDescriptor[] {
                    Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_EDIT),
                    Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_SYNC),
                    Utils.getUIDescriptor(IPerforceUIConstants.IMG_DEC_LOCK) },
                    new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT,
                            IPerforceUIConstants.ICON_TOP_RIGHT,
                            IPerforceUIConstants.ICON_TOP_LEFT });
            Image expected = icon.createImage();
            PerforceDecorator decorator = new PerforceDecorator(true);
            Image decorated = decorator.decorateImage(base, file);
            for (int x = 0; x < expected.getBounds().width; x++) {
                for (int y = 0; y < expected.getBounds().height; y++) {
                    assertEquals(expected.getImageData().getPixel(x, y),
                            decorated.getImageData().getPixel(x, y));
                }
            }
        } catch (CoreException e) {
            String message = e.getMessage();
            if (e.getStatus() != null) {
                message += " " + e.getStatus().getMessage();
            }
            if (e.getCause() != null) {
                message += " " + e.getCause().getMessage();
                StringWriter writer = new StringWriter();
                e.getCause().printStackTrace(new PrintWriter(writer));
                message += " " + writer.toString();
            }
            assertFalse("Core exception thrown: " + message + " : " + e, true);
        }
    }

    /**
     * Test {@link P4Decoration} class
     */
    public void testDecoration() {
        assertNotNull(new P4Decoration());
        Map<String, String> variables = new HashMap<String, String>();
        assertNotNull(P4Decoration.decorateFile(variables));
        assertNotNull(P4Decoration.decorateProject(variables));
    }

    /**
     * Test always with name
     */
    public void testAlwaysWithName() {
        Map<String, String> variables = new HashMap<String, String>();
        String name = "testName.txt";
        variables.put(P4Decoration.NAME_VARIABLE, name);
        StringBuilder fileName = P4Decoration.decorate("", variables);
        assertNotNull(fileName);
        assertTrue(fileName.toString().contains(name));
    }

    /**
     * Test unadded image decoration
     */
    public void testUnaddedImage() {
        IFile file = project.getFile("not_in_depot.log");
        assertNotNull(P4ConnectionManager.getManager().getResource(file));
        assertFalse(file.exists());
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_LOCAL_ONLY_ICON,
                        IPerforceUIConstants.ICON_BOTTOM_RIGHT);
        ImageDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
                .getImageDescriptor(file.getName());
        Image base = desc.createImage();
        OverlayIcon icon = new OverlayIcon(base,
                new ImageDescriptor[] { Utils
                        .getUIDescriptor(IPerforceUIConstants.IMG_DEC_LOCAL) },
                new int[] { IPerforceUIConstants.ICON_BOTTOM_RIGHT });
        Image expected = icon.createImage();
        PerforceDecorator decorator = new PerforceDecorator(true);
        IP4Resource p4file=P4ConnectionManager.getManager().getResource(file); // make sure no ayncGetResource() return right resource.
        Image decorated = decorator.decorateImage(base, p4file);
        for (int x = 0; x < expected.getBounds().width; x++) {
            for (int y = 0; y < expected.getBounds().height; y++) {
                assertEquals(expected.getImageData().getPixel(x, y), decorated
                        .getImageData().getPixel(x, y));
            }
        }
    }

    /**
     * Test unadded text decoration
     */
    public void testUnaddedText() {
        String unadded = "<TEST_UNADDED>";
        IFile file = project.getFile("not_in_depot.log");
        assertNotNull(P4ConnectionManager.getManager().getResource(file));
        assertFalse(file.exists());
        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(IPreferenceConstants.UNADDED_CHANGE_DECORATION,
                        unadded);

        Utils.getUIPlugin()
                .getPreferenceStore()
                .setValue(
                        IPreferenceConstants.FILE_DECORATION_TEXT,
                        IPreferenceConstants.NAME_VARIABLE + " "
                                + IPreferenceConstants.UNADDED_CHANGE_VARIABLE);
        IP4Resource p4file=P4ConnectionManager.getManager().getResource(file); // make sure no ayncGetResource() return right resource.
        PerforceDecorator decorator = new PerforceDecorator(true);
        String decorated = decorator.decorateText("", p4file);
        assertNotNull(decorated);
        assertTrue(decorated.contains(unadded));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return PATH;
    }
}
