/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;
import org.eclipse.mylyn.tasks.core.data.TaskCommentMapper;
import org.eclipse.mylyn.tasks.core.data.TaskData;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DefaultJobConfiguration implements IP4JobConfiguration {

    /**
     * COMMENT_FIELD
     */
    public static final String COMMENT_FIELD = "Comments"; //$NON-NLS-1$

    /**
     * PRIORITY_FIELD
     */
    public static final String PRIORITY_FIELD = "Priority"; //$NON-NLS-1$

    /**
     * CLOSED_STATUS_VALUE
     */
    public static final String CLOSED_STATUS_VALUE = "closed"; //$NON-NLS-1$

    /**
     * JOB_PATTERN
     */
    public static final Pattern JOB_PATTERN = Pattern.compile("job\\d+"); //$NON-NLS-1$

    /**
     * COMMENT_REGEX1
     */
    public static final String COMMENT_REGEX = "^(\\d\\d/\\d\\d/\\d\\d\\d\\d\\s*\\d\\d:\\d\\d:\\d\\d)\\s*\\-?\\s*(\\w+)\\s*:(.*)$"; //$NON-NLS-1$

    /**
     * COMMENT_PATTERN1
     */
    public static final Pattern COMMENT_PATTERN = Pattern
            .compile(COMMENT_REGEX);

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#getTaskIdsFromComment(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      java.lang.String)
     */
    public String[] getTaskIdsFromComment(TaskRepository repository,
            String comment) {
        if (comment != null) {
            List<String> jobs = new ArrayList<String>();
            Matcher matcher = JOB_PATTERN.matcher(comment);
            while (matcher.find()) {
                jobs.add(matcher.group());
            }
            if (!jobs.isEmpty()) {
                return jobs.toArray(new String[jobs.size()]);
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isCommentField(com.perforce.p4java.core.IJobSpec.IJobSpecField)
     */
    public boolean isCommentField(IJobSpecField field) {
        if (field == null) {
            return false;
        }
        return COMMENT_FIELD.equals(field.getName());
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isModifiedDateField(com.perforce.p4java.core.IJobSpec.IJobSpecField,
     *      com.perforce.p4java.core.IJobSpec)
     */
    public boolean isModifiedDateField(IJobSpecField field, IJobSpec spec) {
        if (field == null || spec == null) {
            return false;
        }
        // Modified date field will be date data type, always set, and have a
        // "now" preset
        return IP4Job.DATE_DATA_TYPE.equals(field.getDataType())
                && IP4Job.ALWAYS_FIELD_TYPE.equals(field.getFieldType())
                && IP4Job.NOW_PRESET
                        .equals(spec.getFieldPreset(field.getName()));
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isPriorityField(com.perforce.p4java.core.IJobSpec.IJobSpecField)
     */
    public boolean isPriorityField(IJobSpecField field) {
        if (field == null) {
            return false;
        }
        return PRIORITY_FIELD.equals(field.getName());
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#setCompletionDate(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
     */
    public void setCompletionDate(TaskAttribute root) {
        if (root != null
                && root.getAttribute(TaskAttribute.DATE_COMPLETION) == null) {
            TaskAttribute status = root.getAttribute(TaskAttribute.STATUS);
            if (status != null) {
                TaskAttribute completion = root
                        .createAttribute(TaskAttribute.DATE_COMPLETION);
                if (CLOSED_STATUS_VALUE.equals(status.getValue())) {
                    TaskAttribute mod = root
                            .getAttribute(TaskAttribute.DATE_MODIFICATION);
                    if (mod != null) {
                        completion.setValue(mod.getValue());
                    }
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#buildCommentField(org.eclipse.mylyn.tasks.core.data.TaskAttribute,
     *      java.lang.String, org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    public void buildCommentField(TaskAttribute root, String value,
            TaskRepository repository) {
        StringTokenizer tokenizer = new StringTokenizer(value, "\r\n", false); //$NON-NLS-1$
        TaskCommentMapper mapper = null;
        StringBuilder commentText = new StringBuilder();

        List<TaskCommentMapper> comments = new ArrayList<TaskCommentMapper>();
        while (tokenizer.hasMoreTokens()) {
            String line = tokenizer.nextToken();
            Matcher matcher = COMMENT_PATTERN.matcher(line);
            if (matcher.matches()) {
                if (mapper != null) {
                    mapper.setText(commentText.toString());
                }
                commentText = new StringBuilder();
                mapper = new TaskCommentMapper();
                comments.add(mapper);
                mapper.setCreationDate(P4MylynUtils.parseCommentDate(matcher
                        .group(1)));

                String person = matcher.group(2);
                if (person != null && person.length() > 0) {
                    mapper.setAuthor(repository.createPerson(person));
                }

                String startText = matcher.group(3);
                if (startText != null && startText.length() > 0) {
                    commentText.append(startText);
                    commentText.append('\n');
                }
            } else if (commentText != null) {
                commentText.append(line);
                commentText.append('\n');
            }
        }
        if (mapper != null && commentText != null) {
            mapper.setText(commentText.toString());
        }
        int count = comments.size();
        for (TaskCommentMapper comment : comments) {
            comment.setNumber(count);
            TaskAttribute attribute = root
                    .createAttribute(TaskAttribute.PREFIX_COMMENT + count);
            comment.applyTo(attribute);
            count--;
        }

        TaskAttribute newComment = root
                .createMappedAttribute(TaskAttribute.COMMENT_NEW);
        newComment.getMetaData().setKind(TaskAttribute.KIND_DEFAULT);
        newComment.getMetaData().setType(TaskAttribute.TYPE_LONG_RICH_TEXT);
        newComment.setValue(""); //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#generateCommentFieldValue(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.String, java.lang.String)
     */
    public String generateCommentFieldValue(IP4Connection connection,
            String newCommentValue, String currentCommentValue) {
        StringBuilder builder = new StringBuilder();
        String user = connection.getParameters().getUser();
        if (newCommentValue != null && newCommentValue.length() > 0) {
            builder.append(P4MylynUtils.formatCommentDate(new Date()));
            builder.append(' ');
            builder.append(user);
            builder.append(':');
            builder.append('\n');
            builder.append(newCommentValue);
            builder.append('\n');
        }
        if (currentCommentValue != null && currentCommentValue.length() > 0) {
            builder.append(currentCommentValue);
        }
        return builder.toString();
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    public boolean isConfigurationFor(TaskRepository repository) {
        return true;
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isCreatedDateField(com.perforce.p4java.core.IJobSpec.IJobSpecField,
     *      com.perforce.p4java.core.IJobSpec)
     */
    public boolean isCreatedDateField(IJobSpecField field, IJobSpec spec) {
        if (field == null || spec == null) {
            return false;
        }
        // Created date field will be date data type, once set, and have a
        // "now" preset
        return IP4Job.DATE_DATA_TYPE.equals(field.getDataType())
                && IP4Job.ONCE_FIELD_TYPE.equals(field.getFieldType())
                && IP4Job.NOW_PRESET
                        .equals(spec.getFieldPreset(field.getName()));
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#isKindField(com.perforce.p4java.core.IJobSpec.IJobSpecField)
     */
    public boolean isKindField(IJobSpecField field) {
        return false;
    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#updateTaskFromTaskData(org.eclipse.mylyn.tasks.core.TaskRepository,
     *      org.eclipse.mylyn.tasks.core.ITask,
     *      org.eclipse.mylyn.tasks.core.data.TaskData)
     */
    public void updateTaskFromTaskData(TaskRepository taskRepository,
            ITask task, TaskData taskData) {

    }

    /**
     * @see com.perforce.team.core.mylyn.IP4JobConfiguration#getAttributeMapper(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    public TaskAttributeMapper getAttributeMapper(TaskRepository taskRepository) {
        return new P4JobAttributeMapper(taskRepository);
    }

}
