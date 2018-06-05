/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.mylyn.P4MylynUiUtils;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;
import com.perforce.team.ui.p4java.actions.EditJobAction;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.text.MessageFormat;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditJobTaskAction extends P4Action {

    private boolean useTaskEditor() {
        return PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.USE_TASK_EDITOR);
    }

    private TaskRepository create(IP4Connection connection, IP4Job job) {
        TaskRepository repository = null;
        String port = connection.getParameters().getPort();
        String message = MessageFormat.format(
                Messages.EditJobTaskAction_CreateTaskRepository, port);
        if (P4ConnectionManager.getManager().openConfirm(
                Messages.EditJobTaskAction_NoTaskRepository, message)) {
            repository = P4MylynUiUtils.createTaskRepository(connection);
            repository.setRepositoryLabel(port);
            TasksUi.getRepositoryManager().addRepository(repository);
        }
        return repository;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        if (useTaskEditor()) {
            IP4Resource resource = getSingleResourceSelection();
            if (resource instanceof IP4Job) {
                IP4Job job = (IP4Job) resource;
                IP4Connection connection = resource.getConnection();
                String id = job.getId();
                TaskRepository repository = P4MylynUiUtils.findRepository(
                        connection, IP4MylynConstants.KIND);
                if (repository == null) {
                    repository = create(connection, job);
                }
                if (repository != null) {
                    TasksUiUtil.openTask(repository, id);
                }
            }
        } else {
            EditJobAction defaultEdit = new EditJobAction();
            defaultEdit.selectionChanged(null, this.getSelection());
            defaultEdit.setCollection(this.collection);
            defaultEdit.run(null);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        if (containsOnlineConnection()) {
            return getSingleResourceSelection() instanceof IP4Job;
        }
        return false;
    }

}
