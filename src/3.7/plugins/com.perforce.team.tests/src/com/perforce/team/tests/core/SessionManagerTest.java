/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SessionManagerTest extends P4TestCase {

    /**
     * Test of session manager constructor
     */
    public void testBasic() {
        assertNotNull(new SessionManager());
    }

    /**
     * Test of invalid params not throwing exceptions
     */
    public void testInvalid() {
        try {
            SessionManager.loadComboHistory(null, null);
            SessionManager.loadComboHistory(null, "");
            SessionManager.saveComboHistory(null, 1, null);
            SessionManager.saveComboHistory(null, -10, null);
            SessionManager.saveComboHistory(null, 10, "");
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
    }

}
