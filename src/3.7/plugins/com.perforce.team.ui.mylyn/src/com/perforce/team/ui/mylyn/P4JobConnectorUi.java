/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.mylyn.tasks.core.IRepositoryQuery;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.ITaskMapping;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskRepositoryPage;
import org.eclipse.mylyn.tasks.ui.wizards.ITaskSearchPage;
import org.eclipse.mylyn.tasks.ui.wizards.NewTaskWizard;
import org.eclipse.mylyn.tasks.ui.wizards.RepositoryQueryWizard;
import org.eclipse.ui.IEditorInput;

import com.perforce.team.core.mylyn.IP4MylynConstants;
import com.perforce.team.ui.mylyn.connection.TaskRepositorySettingsListener;
import com.perforce.team.ui.mylyn.search.P4JobQueryPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4JobConnectorUi extends AbstractRepositoryConnectorUi {

    private P4JobUiConfigurationManager manager;

    /**
     * Create a perforce job ui connector
     */
    public P4JobConnectorUi() {
        TasksUi.getRepositoryManager().addListener(
                new TaskRepositorySettingsListener());
        manager = new P4JobUiConfigurationManager();
        new JobTaskSynchronizer();
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getReplyText(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.ITaskComment, boolean)
     */
    @Override
    public String getReplyText(TaskRepository taskRepository, ITask task,
            ITaskComment taskComment, boolean includeTask) {
        if (taskComment != null) {
            return manager.getConfiguration(taskRepository).getReplyText(
                    taskRepository, task, taskComment, includeTask);
        } else {
            return super.getReplyText(taskRepository, task, taskComment,
                    includeTask);
        }
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getLegendElements()
     */
    @Override
    public List<LegendElement> getLegendElements() {
        return manager.getLegendElements();
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#findHyperlinks(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String, int, int)
     */
    @Override
    public IHyperlink[] findHyperlinks(TaskRepository repository, String text,
            int index, int textOffset) {
        return manager.getConfiguration(repository).findHyperlinks(repository,
                text, index, textOffset);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getConnectorKind()
     */
    @Override
    public String getConnectorKind() {
        return IP4MylynConstants.KIND;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskKindLabel(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public String getTaskKindLabel(ITask task) {
        TaskRepository repository = P4MylynUiUtils.getRepository(task);
        String label = this.manager.getConfiguration(repository)
                .getTaskKindLabel(task);
        if (label == null) {
            label = super.getTaskKindLabel(task);
        }
        return label;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskKindOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public ImageDescriptor getTaskKindOverlay(ITask task) {
        TaskRepository repository = P4MylynUiUtils.getRepository(task);
        return this.manager.getConfiguration(repository).getTaskKindOverlay(
                task);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskPriorityOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public ImageDescriptor getTaskPriorityOverlay(ITask task) {
        TaskRepository repository = P4MylynUiUtils.getRepository(task);
        ImageDescriptor descriptor = this.manager.getConfiguration(repository)
                .getTaskPriorityOverlay(task);
        if (descriptor == null) {
            descriptor = super.getTaskPriorityOverlay(task);
        }
        return descriptor;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#hasSearchPage()
     */
    @Override
    public boolean hasSearchPage() {
        return true;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getNewTaskWizard(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITaskMapping)
     */
    @Override
    public IWizard getNewTaskWizard(TaskRepository taskRepository,
            ITaskMapping selection) {
        return new NewTaskWizard(taskRepository, selection);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getQueryWizard(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.IRepositoryQuery)
     */
    @Override
    public IWizard getQueryWizard(TaskRepository taskRepository,
            IRepositoryQuery queryToEdit) {
        RepositoryQueryWizard wizard = new RepositoryQueryWizard(taskRepository);
        wizard.addPage(new P4JobQueryPage(taskRepository, queryToEdit));
        return wizard;
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getSettingsPage(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public ITaskRepositoryPage getSettingsPage(TaskRepository taskRepository) {
        return new P4JobSettingsPage(taskRepository);
    }

    /**
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getSearchPage(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    @Override
    public ITaskSearchPage getSearchPage(TaskRepository repository,
            IStructuredSelection selection) {
        return new P4JobQueryPage(repository);
    }

    /**
     * Get changelist description for task
     * 
     * @param task
     * @return - changelist description
     */
    public String getChangelistDescription(ITask task) {
        TaskRepository repository = P4MylynUiUtils.getRepository(task);
        return this.manager.getConfiguration(repository)
                .getContextChangelistDescription(repository, task);
    }

    /**
     * Get pending changelist locator
     * 
     * @param task
     * @return pending changelist locator
     */
    public IPendingChangelistLocator getChangelistLocator(ITask task) {
        TaskRepository repository = P4MylynUiUtils.getRepository(task);
        Object config = this.manager.getConfiguration(repository);
        if (config instanceof IP4JobUiConfiguration2) {
            return ((IP4JobUiConfiguration2) config)
                    .getPendingChangelistLocator();
        } else {
            return null;
        }
    }

    @Override
    public IEditorInput getTaskEditorInput(TaskRepository repository, ITask task) {
        return new P4TaskEditorInput(repository, task);
    }

}
