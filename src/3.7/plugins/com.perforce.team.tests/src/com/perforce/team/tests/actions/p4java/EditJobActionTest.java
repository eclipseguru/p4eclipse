/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.EditJobAction;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditJobActionTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
    }

    /**
     *
     */
    public void testEnablement() {
        IP4Connection connection = createConnection();
        IP4Job[] jobs = connection.getJobs(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.length);
        IP4Job job = jobs[0];
        assertNotNull(job);
        Action wrap = Utils.getDisabledAction();
        EditJobAction edit = new EditJobAction();
        edit.selectionChanged(wrap, new StructuredSelection(job));
        assertTrue(wrap.isEnabled());
        connection.setOffline(true);
        edit.selectionChanged(wrap, new StructuredSelection(job));
        assertFalse(wrap.isEnabled());
        connection.setOffline(false);
        edit.selectionChanged(wrap, new StructuredSelection(connection));
        assertFalse(wrap.isEnabled());
    }

}
