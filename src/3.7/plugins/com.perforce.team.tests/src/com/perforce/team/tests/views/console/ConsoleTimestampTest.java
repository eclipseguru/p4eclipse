/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.console;

import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.views.ConsoleDocument;
import com.perforce.team.ui.views.ConsoleView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConsoleTimestampTest extends ConnectionBasedTestCase {

    /**
     * Test timestamp
     */
    public void testTimestamp() {
        ConsoleView view = ConsoleView.openInActivePerspective();
        assertNotNull(view);
        assertFalse(view.isDisposed());
        Utils.getUIStore().setValue(
                IPerforceUIConstants.PREF_CONSOLE_TIMESTAMP, true);
        try {
            String contains = "test with time";
            assertNotNull(view.getViewer());
            IDocument document = view.getViewer().getDocument();
            assertNotNull(document);
            assertTrue(document instanceof ConsoleDocument);
            Utils.sleep(.1);
            ((ConsoleDocument) document).clear();
            Utils.sleep(.1);
            assertEquals("", document.get());
            view.executed(contains);
            Utils.sleep(.1);
            String content = document.get();
            int index = content.indexOf(contains);
            assertTrue("content is:" + content, index >= 0);
            try {
                int line = document.getLineOfOffset(index);
                assertTrue(line >= 0);
                int lineOffset = document.getLineOffset(line);
                assertTrue(lineOffset >= 0);
                int length = document.getLineLength(line);
                assertTrue(length > 0);
                String timeLine = document.get(lineOffset, length);
                assertTrue(timeLine.length() > 0);
                assertTrue(timeLine.contains(contains));
                Pattern pattern = Pattern.compile("\\d+:\\d\\d [AP]M");
                Matcher matcher = pattern.matcher(timeLine);
                assertTrue(matcher.find());
                String time = timeLine
                        .substring(matcher.start(), matcher.end());
                assertTrue(time.length() > 0);
            } catch (BadLocationException e) {
                assertFalse("bad location exception thrown", true);
            }

        } finally {
            Utils.getUIStore().setValue(
                    IPerforceUIConstants.PREF_CONSOLE_TIMESTAMP, false);
        }
    }
}
