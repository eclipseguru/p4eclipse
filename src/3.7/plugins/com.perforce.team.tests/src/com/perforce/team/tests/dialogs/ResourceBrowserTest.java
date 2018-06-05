/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.resource.ResourceBrowserDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ResourceBrowserTest extends ConnectionBasedTestCase {

    /**
     * Test resource browser
     */
    public void testBrowser() {
        IP4Connection connection = createConnection();
        ResourceBrowserDialog dialog = new ResourceBrowserDialog(
                Utils.getShell(), connection.members());
        assertNull(dialog.getViewer());
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertNotNull(dialog.getViewer());
            dialog.getViewer().expandToLevel(2);
            assertNotNull(dialog.getErrorMessage());
            assertNull(dialog.getSelectedResource());
        } finally {
            dialog.close();
        }
    }

}
