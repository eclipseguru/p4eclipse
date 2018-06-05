/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn;

import com.perforce.team.core.mylyn.IP4Configuration;
import com.perforce.team.ui.mylyn.changeset.P4ContextChangeSet;

import java.util.Collection;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.ITaskComment;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.LegendElement;

/**
 * Interface for specified job spec configuration of task UI elements.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4JobUiConfiguration extends IP4Configuration {

    /**
     * Find hyperlinks in the specified text for the specified task repository
     * 
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#findHyperlinks(TaskRepository,
     *      String, int, int)
     * @param repository
     * @param text
     * @param index
     * @param textOffset
     * @return - hyperlink array
     */
    IHyperlink[] findHyperlinks(TaskRepository repository, String text,
            int index, int textOffset);

    /**
     * Get the task priority overlay for the specified task
     * 
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskPriorityOverlay(ITask)
     * @param task
     * @return - image descriptor
     */
    ImageDescriptor getTaskPriorityOverlay(ITask task);

    /**
     * Get task kind overlay for the specified task
     * 
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskKindOverlay(ITask)
     * @param task
     * @return - image descriptor
     */
    ImageDescriptor getTaskKindOverlay(ITask task);

    /**
     * Get task kind label for the specified task
     * 
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getTaskKindLabel(ITask)
     * @param task
     * @return - task kind label
     */
    String getTaskKindLabel(ITask task);

    /**
     * 
     * Get the comment reply text for the specified comment and task repository
     * 
     * @see org.eclipse.mylyn.tasks.ui.AbstractRepositoryConnectorUi#getReplyText(TaskRepository,
     *      ITask, ITaskComment, boolean)
     * @param taskRepository
     * @param task
     * @param taskComment
     * @param includeTask
     * @return - reply text
     */
    String getReplyText(TaskRepository taskRepository, ITask task,
            ITaskComment taskComment, boolean includeTask);

    /**
     * Get contributed legend elements
     * 
     * @return - legend elements
     */
    Collection<LegendElement> getLegendElements();

    /**
     * Get the changelist description that will be used for
     * {@link P4ContextChangeSet} objects to find the associated pending
     * changelist for the task they are configured for.
     * 
     * @param taskRepository
     * @param task
     * 
     * @return - non-null changelist description
     */
    String getContextChangelistDescription(TaskRepository taskRepository,
            ITask task);

}
