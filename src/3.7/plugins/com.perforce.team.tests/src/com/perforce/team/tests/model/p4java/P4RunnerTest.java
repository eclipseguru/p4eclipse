/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4RunnerTest extends ConnectionBasedTestCase {

    private IP4Resource[] members;
    private boolean finished = false;

    /**
     * Test null scheduling
     */
    public void testNull() {
        assertNull(P4Runner.schedule(null));
    }

    /**
     * Tests the p4 runner and p4 runnable objects
     */
    public void testP4Runnable() {
        final IP4Connection connection = createConnection();
        P4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                connection.refresh();
                members = connection.members();
                finished = true;
            }

        };
        P4Runner.schedule(runnable);
        int i = 0;
        while (!finished && i < 120) {
            Utils.sleep(.1);
            i++;
        }
        assertTrue(finished);
        assertNotNull(members);
        assertTrue(members.length > 0);
    }

    /**
     * Tests the p4 runner object and the p4 runnable interface
     */
    public void testIP4Runnable() {
        final IP4Connection connection = createConnection();
        IP4Runnable runnable = new IP4Runnable() {

            public void run() {
                connection.refresh();
                members = connection.members();
                finished = true;
            }

            public String getTitle() {
                return "test runnable";
            }

            public void run(IProgressMonitor monitor) {
                run();
            }

        };
        P4Runner.schedule(runnable);
        int i = 0;
        while (!finished && i < 120) {
            Utils.sleep(.1);
            i++;
        }
        assertTrue(finished);
        assertNotNull(members);
        assertTrue(members.length > 0);
    }
}
