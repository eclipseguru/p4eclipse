package com.perforce.team.ui.mylyn;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.editors.TaskEditorInput;
import org.eclipse.ui.IEditorInput;

/**
 * P4TaskEditorInput for adapting IHistoryPageSource.
 * 
 * @author ali
 * 
 */
public class P4TaskEditorInput extends TaskEditorInput implements IEditorInput {

    public P4TaskEditorInput(TaskRepository taskRepository, ITask task) {
        super(taskRepository, task);
    }

}
