/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditAdaptedActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile(new Path(
                "src/com/perforce/team/ui/UITeamProvider.java")));
    };

    class Adapted implements IAdaptable {

        IResource resource;

        Adapted(IResource resource) {
            this.resource = resource;
        }

        public Object getAdapter(Class adapter) {
            if (IResource.class.equals(adapter)) {
                return resource;
            }
            return null;
        }

    }

    /**
     * Test for job032094, selection containing objects that need to be adapted
     * to IResource objectse
     */
    public void testAdapted() {
        IFile javaFile = project.getFile(new Path(
                "src/com/perforce/team/ui/UITeamProvider.java"));
        assertTrue(javaFile.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(javaFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertFalse(p4File.isOpened());
        assertFalse(p4File.openedForEdit());

        Action wrap = Utils.getDisabledAction();
        EditAction action = new EditAction();
        action.setAsync(false);

        Adapted adaptedFile = new Adapted(javaFile);
        action.selectionChanged(wrap, new StructuredSelection(adaptedFile));
        assertTrue(wrap.isEnabled());
        action.run(wrap);

        assertTrue(p4File.isOpened());
        assertTrue(p4File.openedForEdit());

    }

    /**
     * 
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

}
