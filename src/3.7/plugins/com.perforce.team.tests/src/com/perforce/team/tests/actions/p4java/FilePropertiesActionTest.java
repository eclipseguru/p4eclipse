/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.dialogs.FilePropertiesDialog;
import com.perforce.team.ui.p4java.actions.FilePropertiesAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FilePropertiesActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Test file properties page action enablement
     */
    public void testEnablement() {
        FilePropertiesAction action = new FilePropertiesAction();

        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        Action wrap = Utils.getDisabledAction();

        action.selectionChanged(wrap, new StructuredSelection(project));
        assertFalse(wrap.isEnabled());
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test the properties page dialog opening when action is run
     */
    public void testOpening() {
        FilePropertiesAction action = new FilePropertiesAction();
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        action.selectionChanged(null, new StructuredSelection(file));
        PreferenceDialog dialog = action.openFileProperyPage(false);
        assertNotNull(dialog);
        try {
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof FilePropertiesDialog);
            FilePropertiesDialog propDialog = (FilePropertiesDialog) page;
            IAdaptable element = propDialog.getElement();
            assertNotNull(element);
            if(element instanceof IFile){
            	element=P4ConnectionManager.getManager().getResource((IResource) element);
            }
            assertTrue(element instanceof IP4File);
            assertEquals(file.getLocation().toOSString(),
                    ((IP4File) element).getLocalPath());
            assertTrue(propDialog.isValid());
            assertNull(propDialog.getErrorMessage());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test the opened properties page dialog
     */
    public void testDialog() {
        FilePropertiesAction action = new FilePropertiesAction();
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        action.selectionChanged(null, new StructuredSelection(file));
        PreferenceDialog dialog = action.openFileProperyPage(false);
        assertNotNull(dialog);
        try {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            Object page = dialog.getSelectedPage();
            assertNotNull(page);
            assertTrue(page instanceof FilePropertiesDialog);
            FilePropertiesDialog propDialog = (FilePropertiesDialog) page;
            assertNotNull(propDialog.getTitle());
            assertNotNull(propDialog.getDepotLabel());
            assertEquals(resource.getRemotePath(), propDialog.getDepotLabel());
            assertNotNull(propDialog.getClientLabel());
            assertEquals(resource.getClientPath(), propDialog.getClientLabel());
            assertNotNull(propDialog.getTypeLabel());
            assertEquals(p4File.getHeadType(), propDialog.getTypeLabel());
            assertNotNull(propDialog.getHaveLabel());
            assertEquals(Integer.toString(p4File.getHaveRevision()),
                    propDialog.getHaveLabel());
            assertNotNull(propDialog.getHeadLabel());
            assertEquals(Integer.toString(p4File.getHeadRevision()),
                    propDialog.getHeadLabel());
            assertNotNull(propDialog.getHeadActionLabel());
            assertEquals(p4File.getHeadAction().toString().toLowerCase(),
                    propDialog.getHeadActionLabel());
            assertNotNull(propDialog.getHeadChangeLabel());
            assertEquals(Integer.toString(p4File.getHeadChange()),
                    propDialog.getHeadChangeLabel());
            assertNotNull(propDialog.getLastModifiedLabel());
        } finally {
            dialog.close();
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
