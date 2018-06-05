/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.text.timelapse.GenericTextTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseInput;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TextTimeLapseTest extends BaseTimeLapseTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"), new ByteArrayInputStream(
                "test text".getBytes()));
    }

    /**
     * Test opening the text time lapse editor
     */
    public void testOpen() {
        IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = createConnection().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IEditorPart part = null;
        try {
            IEditorInput input = new TimeLapseInput((IP4File) resource);
            part = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    GenericTextTimeLapseEditor.ID);
            assertNotNull(part);
            assertTrue(part instanceof GenericTextTimeLapseEditor);
            while (((TimeLapseEditor) part).isLoading()) {
                Utils.sleep(.1);
            }
            validateEditor((TextTimeLapseEditor) part);
        } catch (PartInitException e) {
            handle(e);
        } finally {
            if (part != null) {
                PerforceUIPlugin.getActivePage().closeEditor(part, false);
            }
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
