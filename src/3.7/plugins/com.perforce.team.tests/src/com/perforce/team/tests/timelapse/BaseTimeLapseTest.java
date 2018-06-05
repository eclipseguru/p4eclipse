/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.timelapse;

import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.text.timelapse.ITextAnnotateModel;
import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseTimeLapseTest extends ProjectBasedTestCase {

    /**
     * Validate {@link TextTimeLapseEditor}
     * 
     * @param editor
     */
    public void validateEditor(TextTimeLapseEditor editor) {
        ITextAnnotateModel model = editor.getAnnotateModel();
        assertNotNull(model);
        assertTrue(model.getRevisionCount() > 0);
        assertNotNull(model.getLatest());
        IP4Revision revision = model.getEarliest();
        assertNotNull(revision);
        while (revision != null) {
            int id = model.getRevisionId(revision);
            assertNotNull(model.getAuthor(id));
            assertNotNull(model.getDate(id));
            assertNotNull(model.getLineRanges(revision));
            assertNotNull(model.getLines(revision));
            assertEquals(revision, model.getRevisionById(id));
            revision = model.getNext(revision);
        }
    }

}
