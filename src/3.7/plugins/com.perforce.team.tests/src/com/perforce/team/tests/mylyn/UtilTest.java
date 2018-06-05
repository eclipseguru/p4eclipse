/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.mylyn.PerforceCoreMylynPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import java.util.Date;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.mylyn.tasks.core.TaskRepository;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UtilTest extends ConnectionBasedTestCase {

    /**
     * Test date utilities
     */
    public void testDates() {
        assertNull(P4MylynUtils.parseCommentDate(null));
        assertNull(P4MylynUtils.parseDate(null));

        assertNull(P4MylynUtils.parseCommentDate("invalid"));
        assertNull(P4MylynUtils.parseDate("invalid"));

        assertNotNull(P4MylynUtils.parseCommentDate("03/03/1234 12:44:33"));
        assertNotNull(P4MylynUtils.parseDate("1234/01/01 22:44:22"));

        assertNull(P4MylynUtils.formatCommentDate(null));
        assertNull(P4MylynUtils.formatToP4Date(null));
        assertNull(P4MylynUtils.formatToMylynDate(null));

        assertNotNull(P4MylynUtils.formatCommentDate(new Date()));
        assertNotNull(P4MylynUtils.formatToP4Date(new Date()));
        assertNotNull(P4MylynUtils.formatToMylynDate(new Date()));
    }

    /**
     * Test error utilities
     */
    public void testErrors() {
        IStatus status = P4MylynUtils.getErrorStatus("error");
        assertNotNull(status);
        assertNotNull(status.getMessage());
        assertNull(status.getException());
        assertNotNull(status.getPlugin());

        status = P4MylynUtils.getErrorStatus(null);
        assertNotNull(status);
        assertNotNull(status.getMessage());
        assertNull(status.getException());
        assertNotNull(status.getPlugin());

        status = P4MylynUtils.getErrorStatus(null, new Throwable("abcd"));
        assertNotNull(status);
        assertNotNull(status.getMessage());
        assertNotNull(status.getException());
        assertNotNull(status.getPlugin());

        assertNotNull(P4MylynUtils.getConnectionErrorStatus());
        assertNotNull(P4MylynUtils.getConnectionOfflineStatus());
    }

    /**
     * Test repository-connection settings
     */
    public void testRepositorySettings() {
        assertNull(P4MylynUtils.getConnection(null));

        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(createConnection());
        assertNotNull(repository);
        IP4Connection connection = P4MylynUtils.getConnection(repository);
        assertNotNull(connection);
    }

    /**
     * Test query value escaping
     */
    public void testQueryValue() {
        assertNull(P4MylynUtils.escapeJobQueryValue(null));

        String value = "job=<*testest";
        String escaped = P4MylynUtils.escapeJobQueryValue(value);
        assertNotNull(escaped);
        assertFalse(value.equals(escaped));
        assertEquals(value.length() + 3, escaped.length());
    }

    /**
     * Test task helpers
     */
    public void testTask() {
        assertNull(P4MylynUtils.getTask(null));
    }

    /**
     * Basic plugin test
     */
    public void testPlugin() {
        assertNotNull(PerforceCoreMylynPlugin.getDefault());
        assertNotNull(PerforceUiMylynPlugin.getDefault());
    }

    /**
     * Test ui plugin utilities
     */
    public void testUiHelpers() {
        assertNotNull(P4MylynUiUtils.getTaskList());
        assertNotNull(P4MylynUiUtils.getNonPerforceRepositories());
        assertNotNull(P4MylynUiUtils.getPerforceConnector());
    }

}
