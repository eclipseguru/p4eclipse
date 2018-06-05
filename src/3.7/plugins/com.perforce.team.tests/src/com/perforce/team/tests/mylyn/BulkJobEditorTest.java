/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.job.BulkJobEditor;
import com.perforce.team.ui.mylyn.job.BulkJobInput;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobEditorTest extends ConnectionBasedTestCase {

    /**
     * Test opening bulk job editor test
     */
    public void testOpen() {
        IP4Connection connection = createConnection();
        BulkJobInput input = new BulkJobInput(connection);
        assertNotNull(input.getConnection());
        IEditorPart editor = null;
        try {
            editor = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    BulkJobEditor.ID);
            assertNotNull(editor);
        } catch (PartInitException e) {
            handle(e);
        } finally {
            if (editor != null) {
                PerforceUIPlugin.getActivePage().closeEditor(editor, false);
            }
        }
    }

}
