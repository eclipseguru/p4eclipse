/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.views.console;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.views.ConsoleDocument;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ConsoleDocumentTest extends P4TestCase {

    /**
     * Test bad offset
     */
    public void testBadOffset() {
        ConsoleDocument document = new ConsoleDocument();
        assertEquals(0, document.getLineType(-1));
    }

}
