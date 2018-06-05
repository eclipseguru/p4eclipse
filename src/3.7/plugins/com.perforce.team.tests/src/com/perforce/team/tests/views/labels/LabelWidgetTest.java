/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.labels;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.labels.LabelWidget;
import com.perforce.team.ui.labels.LabelsView;

import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelWidgetTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createLabel();
    }

    /**
     * Test widget that displays entire labels model
     */
    public void testWidget() {
        LabelsView view = LabelsView.showView();
        assertNotNull(view);
        view.showDetails(true);
        IP4Connection connection = createConnection();
        view.getPerforceViewControl().changeConnection(new StructuredSelection(connection));

        LabelWidget widget = view.getLabelDetails();
        assertNotNull(widget);
        IP4Label[] labels = connection.getLabels(1);
        assertNotNull(labels);
        assertEquals(1, labels.length);
        assertNotNull(labels[0]);
        assertTrue(labels[0].needsRefresh());
        labels[0].refresh();
        assertFalse(labels[0].needsRefresh());
        widget.update(labels[0]);

        checkField(labels[0].getName(), widget.getLabelName());
        checkField(labels[0].getOwner(), widget.getOwner());
        checkField(P4UIUtils.formatLabelDate(labels[0].getAccessTime()),
                widget.getAccess());
        checkField(P4UIUtils.formatLabelDate(labels[0].getUpdateTime()),
                widget.getUpdate());
        checkField(labels[0].getDescription(), widget.getDescription());
        checkField(labels[0].getRevision(), widget.getRevision());
        assertNotNull(widget.getView());
        assertEquals(labels[0].isLocked(), widget.isLocked());
    }

    private void checkField(String expected, String actual) {
        assertNotNull(actual);
        if (expected != null) {
            assertTrue(actual.length() > 0);
            assertEquals(expected, actual);
        } else {
            assertEquals(0, actual.length());
        }
    }
}
