/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.internal;

import com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.core.ITask.PriorityLevel;
import org.eclipse.mylyn.tasks.ui.LegendElement;
import org.eclipse.mylyn.tasks.ui.TasksUiImages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Perforce1666JobUiConfiguration extends P4DefaultJobUiConfiguration {

    /**
     * @see com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration#isConfigurationFor(org.eclipse.mylyn.tasks.core.TaskRepository)
     */
    @Override
    public boolean isConfigurationFor(TaskRepository repository) {
        return InternalPlugin.isPerforceServer(repository.getRepositoryUrl());
    }

    /**
     * @see com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration#getTaskKindOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public ImageDescriptor getTaskKindOverlay(ITask task) {
        String kind = task.getTaskKind();
        if (kind != null) {
            if ("SIR".equalsIgnoreCase(kind)) {
                return InternalPlugin
                        .getDescriptor("icons/overlay-enhancement.png");
            } else if ("BUG".equalsIgnoreCase(kind)) {
                return InternalPlugin
                        .getDescriptor("icons/overlay-critical.png");
            } else if ("problem".equals(kind)) {
                return InternalPlugin.getDescriptor("icons/overlay-major.png");
            } else if ("todo".equals(kind)) {
                return InternalPlugin
                        .getDescriptor("icons/overlay-trivial.png");
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration#getTaskPriorityOverlay(org.eclipse.mylyn.tasks.core.ITask)
     */
    @Override
    public ImageDescriptor getTaskPriorityOverlay(ITask task) {
        String severity = task.getAttribute("Severity");
        if (severity != null) {
            if ("A".equals(severity)) {
                return TasksUiImages
                        .getImageDescriptorForPriority(PriorityLevel.P1);
            } else if ("B".equals(severity)) {
                return TasksUiImages
                        .getImageDescriptorForPriority(PriorityLevel.P2);
            } else if ("C".equals(severity)) {
                return TasksUiImages
                        .getImageDescriptorForPriority(PriorityLevel.P4);
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.mylyn.P4DefaultJobUiConfiguration#getLegendElements()
     */
    @Override
    public Collection<LegendElement> getLegendElements() {
        List<LegendElement> elements = new ArrayList<LegendElement>();
        elements.add(LegendElement.createTask("Bug on Perforce:1666",
                InternalPlugin.getDescriptor("icons/overlay-critical.png")));
        elements.add(LegendElement.createTask("SIR on Perforce:1666",
                InternalPlugin.getDescriptor("icons/overlay-enhancement.png")));
        elements.add(LegendElement.createTask("Problem on Perforce:1666",
                InternalPlugin.getDescriptor("icons/overlay-major.png")));
        elements.add(LegendElement.createTask("Todo on Perforce:1666",
                InternalPlugin.getDescriptor("icons/overlay-trivial.png")));
        return elements;
    }
}
