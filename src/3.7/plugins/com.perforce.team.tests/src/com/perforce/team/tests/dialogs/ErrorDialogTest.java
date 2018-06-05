/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.dialogs.PerforceErrorDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ErrorDialogTest extends P4TestCase {

    /**
     * Test error dialog
     */
    public void testErrorDialog() {
        FileSpec error = new FileSpec(FileSpecOpStatus.ERROR, "error 1");
        PerforceErrorDialog dialog = PerforceErrorDialog.showErrors(
                Utils.getShell(), new IFileSpec[] { error }, false);
        assertNotNull(dialog);
        try {
            assertNotNull(dialog.getDetailsText());
            assertEquals(error.getStatusMessage().trim(), dialog
                    .getDetailsText().trim());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test empty error dialog
     */
    public void testEmpty() {
        assertNull(PerforceErrorDialog
                .showErrors(Utils.getShell(), null, false));
        assertNull(PerforceErrorDialog.showErrors(Utils.getShell(),
                new IFileSpec[0], false));
    }

}
