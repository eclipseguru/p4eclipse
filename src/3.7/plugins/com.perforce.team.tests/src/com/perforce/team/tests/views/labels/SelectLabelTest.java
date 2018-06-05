/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.labels;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.labels.SelectLabelDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SelectLabelTest extends ConnectionBasedTestCase {

    /**
     * Test {@link SelectLabelDialog}
     */
    public void testDialog() {
        IP4Connection connection = createConnection();
        SelectLabelDialog dialog = new SelectLabelDialog(Utils.getShell(),
                connection);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertNull(dialog.getSelected());
        } finally {
            dialog.close();
        }
    }

}
