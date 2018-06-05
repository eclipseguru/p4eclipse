/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.mylyn;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.internal.tasks.core.TaskTask;
import org.eclipse.mylyn.internal.tasks.ui.TasksUiPlugin;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenEvent;
import org.eclipse.mylyn.internal.tasks.ui.util.TaskOpenListener;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.AbstractRepositoryConnector;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskData;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobTaskTest extends ConnectionBasedTestCase {

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        createJob();
    }

    /**
     * Test task creation for job data
     */
    public void testTask() {
        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(createConnection());
        assertNotNull(repository);
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        try {
            String id = "job000001";
            IP4Job job = createConnection().getJob(id);
            assertNotNull(job);
            TaskData data = connector.getTaskData(repository, id,
                    new NullProgressMonitor());
            assertNotNull(data);
            ITask task = new TaskTask(connector.getConnectorKind(),
                    repository.getRepositoryUrl(), id);
            assertEquals("", task.getSummary());
            connector.updateTaskFromTaskData(repository, task, data);
            assertNotNull(task.getSummary());
            assertEquals(job.getShortDescription(), task.getSummary());
        } catch (CoreException e) {
            handle(e);
        }
    }

    /**
     * Test connection lookup
     */
    public void testLookup() {
        IP4Connection connection = createConnection();
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(connection);
        assertNotNull(repository);
        try {
            TasksUi.getRepositoryManager().addRepository(repository);
            String id = "job000001";
            ITask task = new TaskTask(connector.getConnectorKind(),
                    repository.getRepositoryUrl(), id);
            IP4Connection found = P4MylynUiUtils.getConnection(task);
            assertNotNull(found);
            assertEquals(found, connection);
            found = P4MylynUiUtils.getConnection(IP4MylynConstants.KIND,
                    repository.getRepositoryUrl());
            assertNotNull(found);
            assertEquals(found, connection);
            assertEquals(repository, P4MylynUiUtils.getRepository(task));
        } finally {
            TasksUiPlugin.getRepositoryManager().removeRepository(repository);
        }
    }

    /**
     * Test opening job task editor
     */
    public void testOpen() {
        IP4Connection connection = createConnection();
        AbstractRepositoryConnector connector = P4MylynUiUtils
                .getPerforceConnector();
        assertNotNull(connector);
        TaskRepository repository = P4MylynUiUtils
                .createTaskRepository(connection);
        assertNotNull(repository);
        try {
            TasksUi.getRepositoryManager().addRepository(repository);
            String id = "job000001";
            final TaskOpenEvent[] opened = new TaskOpenEvent[] { null };
            final IEditorPart[] editors = new IEditorPart[] { null };
            PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getActivePage().addPartListener(new IPartListener() {

                        public void partOpened(IWorkbenchPart part) {
                            if (part instanceof IEditorPart) {
                                editors[0] = (IEditorPart) part;
                            }
                        }

                        public void partDeactivated(IWorkbenchPart part) {

                        }

                        public void partClosed(IWorkbenchPart part) {

                        }

                        public void partBroughtToTop(IWorkbenchPart part) {

                        }

                        public void partActivated(IWorkbenchPart part) {

                        }
                    });
            TasksUiInternal.openTask(repository, id, new TaskOpenListener() {

                @Override
                public void taskOpened(TaskOpenEvent event) {
                    opened[0] = event;
                }
            });
            for (int i = 0; i < 50; i++) {
                if (opened[0] == null) {
                    Utils.sleep(.1);
                } else {
                    break;
                }
            }
            assertNotNull(opened[0]);
            assertNotNull(editors[0]);
            assertTrue(editors[0] instanceof TaskEditor);
        } finally {
            TasksUiPlugin.getRepositoryManager().removeRepository(repository);
        }
    }
}
