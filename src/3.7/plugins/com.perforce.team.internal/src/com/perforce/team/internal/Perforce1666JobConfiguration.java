/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.internal;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.mylyn.P4DefaultJobConfiguration;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Perforce1666JobConfiguration extends P4DefaultJobConfiguration {

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean isConfigurationFor(TaskRepository repository) {
        return InternalPlugin.isPerforceServer(repository.getRepositoryUrl());
    }

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#isCreatedDateField(com.perforce.p4java.core.IJobSpec.IJobSpecField,
     *      com.perforce.p4java.core.IJobSpec)
     */
    @Override
    public boolean isCreatedDateField(IJobSpecField field, IJobSpec spec) {
        if (field != null) {
            return "ReportedDate".equals(field.getName());
        } else {
            return false;
        }
    }

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#isKindField(com.perforce.p4java.core.IJobSpec.IJobSpecField)
     */
    @Override
    public boolean isKindField(IJobSpecField field) {
        return "Type".equals(field.getName());
    }

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#isModifiedDateField(com.perforce.p4java.core.IJobSpec.IJobSpecField,
     *      com.perforce.p4java.core.IJobSpec)
     */
    @Override
    public boolean isModifiedDateField(IJobSpecField field, IJobSpec spec) {
        if (field != null) {
            return "ModifiedDate".equals(field.getName());
        } else {
            return false;
        }
    }

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.data.TaskData)
     */
    @Override
    public void updateTaskFromTaskData(TaskRepository taskRepository,
            ITask task, TaskData taskData) {
        task.setAttribute("Severity", taskData.getRoot().getAttribute("108")
                .getValue());
        String severity = task.getAttribute("Severity");
        if (severity != null) {
            if ("A".equals(severity)) {
                task.setPriority(PriorityLevel.P1.toString());
            } else if ("B".equals(severity)) {
                task.setPriority(PriorityLevel.P2.toString());
            } else if ("C".equals(severity)) {
                task.setPriority(PriorityLevel.P4.toString());
            }
        }
        String kind = task.getTaskKind();
        if (kind != null) {
            double rank = 0;
            if ("SIR".equalsIgnoreCase(kind)) {
                rank = 2;
            } else if ("BUG".equalsIgnoreCase(kind)) {
                rank = 3;
            } else if ("problem".equals(kind)) {
                rank = 1;
            }
            task.setAttribute(TaskAttribute.RANK, Double.toString(rank));
        }
        TaskAttribute ownedBy = taskData.getRoot().getAttribute("109");
        if (ownedBy != null) {
            task.setOwner(ownedBy.getValue());
        }
    }

    /**
     * @see com.perforce.team.core.mylyn.P4DefaultJobConfiguration#setCompletionDate(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
     */
    @Override
    public void setCompletionDate(TaskAttribute root) {
        if (root != null
                && root.getAttribute(TaskAttribute.DATE_COMPLETION) == null) {
            TaskAttribute status = root.getAttribute(TaskAttribute.STATUS);
            if (status != null) {
                TaskAttribute completion = root
                        .createAttribute(TaskAttribute.DATE_COMPLETION);
                String value = status.getValue();
                if (!("open".equals(value) || "inprogress".equals(value) || "triaged"
                        .equals(value))) {
                    TaskAttribute mod = root
                            .getAttribute(TaskAttribute.DATE_MODIFICATION);
                    if (mod != null) {
                        completion.setValue(mod.getValue());
                    }
                }
            }
        }
    }

}
