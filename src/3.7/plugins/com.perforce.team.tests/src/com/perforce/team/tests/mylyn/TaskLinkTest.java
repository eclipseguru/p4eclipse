/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.P4TaskRepositoryLinkProvider;

import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TaskLinkTest extends ProjectBasedTestCase {

    /**
     * Test null params
     */
    public void testNull() {
        P4TaskRepositoryLinkProvider provider = new P4TaskRepositoryLinkProvider();
        assertNull(provider.getTaskRepository(null, null));
    }

    /**
     * Test null params
     */
    public void testProvider() {
        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(createConnection());
        assertNotNull(repository);
        try {
            TasksUi.getRepositoryManager().addRepository(repository);
            P4TaskRepositoryLinkProvider provider = new P4TaskRepositoryLinkProvider();
            assertNotNull(provider.getTaskRepository(project, null));
        } finally {
            TasksUiPlugin.getRepositoryManager().removeRepository(repository);
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
