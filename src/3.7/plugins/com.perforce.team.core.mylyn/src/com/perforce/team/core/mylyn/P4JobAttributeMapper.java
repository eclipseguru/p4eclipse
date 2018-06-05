/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mylyn;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.data.TaskAttribute;
import org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4JobAttributeMapper extends TaskAttributeMapper {

    /**
     * @param taskRepository
     */
    public P4JobAttributeMapper(TaskRepository taskRepository) {
        super(taskRepository);
    }

    /**
     * @see org.eclipse.mylyn.tasks.core.data.TaskAttributeMapper#getValueLabels(org.eclipse.mylyn.tasks.core.data.TaskAttribute)
     */
    @Override
    public List<String> getValueLabels(TaskAttribute taskAttribute) {
        List<String> labels = null;
        if (TaskAttribute.TYPE_DATETIME.equals(taskAttribute.getMetaData()
                .getType())) {
            List<String> values = taskAttribute.getValues();
            Map<String, String> options = getOptions(taskAttribute);
            labels = new ArrayList<String>(values.size());
            for (String value : values) {
                String option = options.get(value);
                if (option != null) {
                    value = option;
                }
                try {
                    long time = Long.parseLong(value);
                    value = P4MylynUtils.formatToMylynDate(new Date(time));
                } catch (NumberFormatException nfe) {
                    // Ignore and use default value label
                }
                labels.add(value);
            }
        }
        if (labels == null) {
            labels = super.getValueLabels(taskAttribute);
        }
        return labels;
    }

}
