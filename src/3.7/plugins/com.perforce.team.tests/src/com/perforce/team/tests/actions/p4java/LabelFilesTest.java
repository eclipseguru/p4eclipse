/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.LabelFilesAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelFilesTest extends ProjectBasedTestCase {

    /**
     * Test label action enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        IP4Connection connection = createConnection();
        connection.setOffline(true);
        LabelFilesAction label = new LabelFilesAction();
        label.selectionChanged(wrap, new StructuredSelection(connection));
        assertFalse(wrap.isEnabled());
        connection.setOffline(false);
        label.selectionChanged(wrap, new StructuredSelection(connection));
        assertTrue(wrap.isEnabled());
    }

    /**
     * Test label action
     */
    public void testAction() {
        IP4Connection connection = createConnection();
        IFile file = null;
        try {
            file = Utils.addSubmitRandom(getClass().getName(), project,
                    connection);
        } catch (Throwable e) {
            e.printStackTrace();
            assertFalse("Exception thrown", true);
        }
        assertTrue(file.exists());

        IP4Resource resource = connection.getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        LabelFilesAction label = new LabelFilesAction();
        label.setAsync(false);
        label.selectionChanged(null, new StructuredSelection(resource));
        String labelName = "testLabel" + System.currentTimeMillis();
        label.label(labelName, null, false);
        IP4Label[] labels = connection.getLabels(null, 1, "..." + labelName
                + "...");
        assertNotNull(labels);
        assertEquals(1, labels.length);
        assertNotNull(labels[0]);
        assertEquals(labelName, labels[0].getName());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_label";
    }

}
