/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.ruby.timelapse.RubyOutlinePage;
import com.perforce.team.ui.ruby.timelapse.RubyTimeLapseEditor;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseInput;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
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
public class RubyTimeLapseTest extends BaseTimeLapseTest {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("test.rb"), new ByteArrayInputStream(
                "puts 'test'".getBytes()));
    }

    /**
     * Test opening the java time lapse editor
     */
    public void testOpen() {
        IFile rubyFile = project.getFile("test.rb");
        assertTrue(rubyFile.exists());
        IP4Resource resource = createConnection().getResource(rubyFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IEditorPart part = null;
        try {
            IEditorInput input = new TimeLapseInput((IP4File) resource);
            part = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    RubyTimeLapseEditor.ID);
            assertNotNull(part);
            assertTrue(part instanceof RubyTimeLapseEditor);
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
     * Test java outline page
     */
    public void testOutline() {
        Utils.closeIntro();
        IFile rubyFile = project.getFile("test.rb");
        assertTrue(rubyFile.exists());
        IP4Resource resource = createConnection().getResource(rubyFile);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IEditorPart part = null;
        try {
            IEditorInput input = new TimeLapseInput((IP4File) resource);
            part = IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                    RubyTimeLapseEditor.ID, true);
            assertNotNull(part);
            assertTrue(part instanceof RubyTimeLapseEditor);
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
            assertTrue(page.getClass().getName(),
                    page instanceof RubyOutlinePage);
            RubyOutlinePage rubyOutline = (RubyOutlinePage) page;
            assertNotNull(rubyOutline.getControl());
            assertNotNull(rubyOutline.getViewer());
            assertNotNull(rubyOutline.getSelection());
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
        return "//depot/dev/ksawicki";
    }

}