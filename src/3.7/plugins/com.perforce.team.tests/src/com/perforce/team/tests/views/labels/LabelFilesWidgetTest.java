/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.labels;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.labels.LabelFilesDialog;
import com.perforce.team.ui.labels.LabelFilesWidget;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelFilesWidgetTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"), new ByteArrayInputStream(
                "<plugin></plugin>".getBytes()));
    }

    /**
     * Test label files widget
     */
    public void testWidget() {
        IP4Connection connection = createConnection();
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = connection.getResource(file);
        assertNotNull(resource);
        P4Collection collection = new P4Collection(
                new IP4Resource[] { resource });
        LabelFilesDialog dialog = new LabelFilesDialog(Utils.getShell(),
                collection);
        assertNull(dialog.getRevision());
        assertNull(dialog.getSelectedLabel());
        assertFalse(dialog.deleteFromLabel());
        assertNull(dialog.getErrorMessage());
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            LabelFilesWidget widget = dialog.getLabelFilesWidget();
            widget.validate();
            assertNotNull(widget);
            assertNull(widget.getRevision());
            assertNull(widget.getSelectedLabel());
            assertNotNull(widget.getErrorMessage());
            assertFalse(widget.deleteFromLabel());
            assertNotNull(dialog.getErrorMessage());
            assertNull(dialog.getRevision());
            assertNull(dialog.getSelectedLabel());
            assertFalse(dialog.deleteFromLabel());
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
