/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.c.timelapse.COutlinePage;
import com.perforce.team.ui.c.timelapse.CTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseInput;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.PageBookView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CTimeLapseTest extends BaseTimeLapseTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile(new Path("src/Client.cpp")),
                new ByteArrayInputStream("int main( int argc, char **argv ){}"
                        .getBytes()));
    }

    /**
     * Test opening the c time lapse editor
     */
    public void testOpen() {
        IFile file = project.getFile(new Path("src/Client.cpp"));
        assertTrue(file.exists());
        IP4Resource resource = createConnection().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IEditorPart part = null;
        try {
            IEditorInput input = new TimeLapseInput((IP4File) resource);
            part = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    CTimeLapseEditor.ID);
            assertNotNull(part);
            assertTrue(part instanceof CTimeLapseEditor);
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
     * Test outline page
     */
    public void testOutline() {
        Utils.closeIntro();
        IFile file = project.getFile(new Path("src/Client.cpp"));
        assertTrue(file.exists());
        IP4Resource resource = createConnection().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IEditorPart part = null;
        try {
            IEditorInput input = new TimeLapseInput((IP4File) resource);
            part = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    CTimeLapseEditor.ID, true);
            assertNotNull(part);
            assertTrue(part instanceof CTimeLapseEditor);
            while (((TimeLapseEditor) part).isLoading()) {
                Utils.sleep(.1);
            }
            IViewPart view = PerforceUIPlugin.getActivePage().showView(
                    "org.eclipse.ui.views.ContentOutline");
            assertNotNull(view);
            assertTrue(view instanceof PageBookView);
            Utils.sleep(.1);
            IPage page = ((PageBookView) view).getCurrentPage();
            assertNotNull(page);
            assertTrue(page.getClass().getName(), page instanceof COutlinePage);
            COutlinePage cOutline = (COutlinePage) page;
            assertNotNull(cOutline.getControl());
            assertNotNull(cOutline.getViewer());
            assertNotNull(cOutline.getSelection());
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
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

}
