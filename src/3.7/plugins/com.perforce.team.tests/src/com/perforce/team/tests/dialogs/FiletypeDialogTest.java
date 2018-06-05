/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.dialogs.FileTypeDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FiletypeDialogTest extends P4TestCase {

    /**
     * Test text+w type
     */
    public void testTextWritable() {
        String type = "text+w";
        FileTypeDialog dialog = new FileTypeDialog(Utils.getShell(), type);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertEquals(type, dialog.getFileType());
            String newType = dialog.getSelectedFileType();
            assertEquals(type, newType);
        } finally {
            dialog.close();
        }
    }

    /**
     * Test binary+xl type
     */
    public void testBinaryExecutableExclusive() {
        String type = "binary+xl";
        FileTypeDialog dialog = new FileTypeDialog(Utils.getShell(), type);
        try {
            dialog.setBlockOnOpen(false);
            dialog.open();
            assertEquals(type, dialog.getFileType());
            String newType = dialog.getSelectedFileType();
            assertEquals(type, newType);
        } finally {
            dialog.close();
        }
    }
}
