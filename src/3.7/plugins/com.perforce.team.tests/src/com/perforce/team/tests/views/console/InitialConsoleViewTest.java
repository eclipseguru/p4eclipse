/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.console;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.views.ConsoleDocument;
import com.perforce.team.ui.views.ConsoleView;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class InitialConsoleViewTest extends ConnectionBasedTestCase {

    /**
     * Test console view
     */
    public void testConsole() {
        ConsoleView view = ConsoleView.openInActivePerspective();
        assertNotNull(view);
        assertFalse(view.isDisposed());
        TextViewer viewer = view.getViewer();
        assertNotNull(viewer);

        Utils.sleep(.1);

        IDocument document = viewer.getDocument();
        assertNotNull(document);
        assertTrue(document instanceof ConsoleDocument);
        ((ConsoleDocument) document).clear();

        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(parameters);
        connection.refresh();
        connection.getPendingChangelists(false);

        Utils.sleep(.1);

        String content = document.get();
        assertNotNull(content);
        assertTrue(content.length() > 0);
        assertTrue("Content is: " + content,
                content.contains("Executing p4 info"));
        assertTrue("Content is: " + content,
                content.contains("Executing p4 opened"));

    }

    /**
     * Tests clearing the console
     */
    public void testClear() {
        ConsoleView view = ConsoleView.openInActivePerspective();
        assertNotNull(view);
        assertFalse(view.isDisposed());
        TextViewer viewer = view.getViewer();
        assertNotNull(viewer);

        Utils.sleep(.1);

        IDocument document = viewer.getDocument();
        assertNotNull(document);
        assertTrue(document instanceof ConsoleDocument);
        ((ConsoleDocument) document).clear();

        Utils.sleep(.1);

        IP4Connection connection = P4ConnectionManager.getManager()
                .getConnection(parameters);
        connection.refresh();

        Utils.sleep(.1);

        String content = document.get();
        assertNotNull(content);
        assertTrue(content.length() > 0);
        assertTrue("Content is: " + content,
                content.contains("Executing p4 info"));

        ((ConsoleDocument) document).clear();
        content = document.get();
        assertNotNull(content);
        assertTrue(content.length() == 0);
    }
}
