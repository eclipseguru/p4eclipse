/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.timelapse.TimeLapseAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseActionTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("about.ini"));
    }

    /**
     * Test action enablement
     */
    public void testEnablement() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());
        Action wrap = Utils.getDisabledAction();
        TimeLapseAction action = new TimeLapseAction();
        action.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(action.isEnabledEx());
        action.selectionChanged(wrap, new StructuredSelection(project));
        assertFalse(action.isEnabledEx());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
