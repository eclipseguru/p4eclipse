/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.p4java.actions.IntegrateAction;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingIntegrateAction extends Action {

    private Mapping mapping;
    private boolean reverse = false;

    /**
     * Create a mapping integrate action
     * 
     * @param mapping
     */
    public MappingIntegrateAction(Mapping mapping) {
        this(mapping, false);
    }

    /**
     * Create a mapping integrate action
     * 
     * @param mapping
     * @param reverse
     */
    public MappingIntegrateAction(Mapping mapping, boolean reverse) {
        this.mapping = mapping;
        this.reverse = reverse;
    }

    /**
     * Set whether to reverse the integration
     * 
     * @param reverse
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    private void integrateBranch(final BranchSpecMapping mapping,
            final boolean reverse) {
        final IP4Connection connection = getConnection(mapping);
        if (connection != null) {
            UIJob job = new UIJob(MessageFormat.format(
                    Messages.MappingIntegrateAction_IntegratingMapping,
                    mapping.getName())) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    IP4Branch branch = mapping.generateBranch(connection);
                    IntegrateAction action = new IntegrateAction();
                    action.integrateBranch(branch, reverse);
                    return Status.OK_STATUS;
                }
            };
            job.setSystem(true);
            job.schedule();
        }
    }

    private IP4Connection getConnection(Mapping mapping) {
        IP4Connection connection = null;
        if (mapping != null) {
            IBranchGraph graph = mapping.getGraph();
            if (graph != null) {
                connection = graph.getConnection();
            }
        }
        return connection;
    }

    private void integratePath(final DepotPathMapping mapping,
            final boolean reverse) {
        final IP4Connection connection = getConnection(mapping);
        if (connection != null) {
            UIJob job = new UIJob(MessageFormat.format(
                    Messages.MappingIntegrateAction_IntegratingMapping,
                    mapping.getName())) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    IntegrateAction action = new IntegrateAction();
                    String source = reverse ? mapping.getTargetPath() : mapping
                            .getSourcePath();
                    String target = reverse ? mapping.getSourcePath() : mapping
                            .getTargetPath();
                    action.integratePaths(connection, source, target);
                    return Status.OK_STATUS;
                }
            };
            job.setSystem(true);
            job.schedule();
        }
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (mapping instanceof BranchSpecMapping) {
            integrateBranch((BranchSpecMapping) mapping, reverse);
        } else if (mapping instanceof DepotPathMapping) {
            integratePath((DepotPathMapping) mapping, reverse);
        }
    }

}
