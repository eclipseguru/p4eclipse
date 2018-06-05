/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.folder.diff.editor.input.BranchDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.DiffConfiguration;
import com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffMappingAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        if (this.getSelection() != null) {
            Object first = this.getSelection().getFirstElement();
            Mapping mapping = P4CoreUtils.convert(first, Mapping.class);
            if (mapping != null) {
                IBranchGraph graph = mapping.getGraph();
                if (graph != null) {
                    final IP4Connection connection = graph.getConnection();
                    if (connection != null) {
                        if (mapping instanceof BranchSpecMapping) {
                            runDiffAction((BranchSpecMapping) mapping,
                                    connection);
                        } else if (mapping instanceof DepotPathMapping) {
                            final DepotPathMapping depotMapping = (DepotPathMapping) mapping;
                            final String source = depotMapping.getSourcePath();
                            final String target = depotMapping.getTargetPath();
                            if (source != null && target != null) {
                                IP4Runnable runnable = new P4Runnable() {

                                    @Override
                                    public void run(IProgressMonitor monitor) {
                                        monitor.beginTask("", 3); //$NON-NLS-1$
                                        monitor.subTask(source);
                                        IP4Folder folder1 = connection
                                                .getFolder(getPath(source));
                                        monitor.worked(1);
                                        monitor.subTask(target);
                                        IP4Folder folder2 = connection
                                                .getFolder(getPath(target));
                                        monitor.worked(1);
                                        if (folder1 != null && folder2 != null) {
                                            runDiffAction(depotMapping,
                                                    folder1, folder2);
                                        }
                                        monitor.worked(1);
                                        monitor.done();
                                    }

                                    @Override
                                    public String getTitle() {
                                        return Messages.DiffMappingAction_LoadingFolders;
                                    }
                                };
                                runRunnable(runnable);
                            }
                        }
                    }
                }
            }
        }
    }

    private IDiffConfiguration createHeaderConfiguration(Mapping mapping) {
        return new DiffConfiguration(MessageFormat.format(
                Messages.DiffMappingAction_Comparing, mapping.getName()),
                P4UIUtils.getImageDescriptor(mapping));
    }

    private IDiffConfiguration createBranchConfiguration(Branch branch) {
        return branch != null ? new DiffConfiguration(branch.getName(),
                P4UIUtils.getImageDescriptor(branch)) {

            @Override
            public void setLabel(String label) {
                // Don't allow label changes since branch name is used
            }

        } : null;
    }

    private void runDiffAction(final BranchSpecMapping mapping,
            IP4Connection connection) {
        IP4Branch branch = mapping.generateBranch(connection);
        if (branch != null) {
            DiffBranchAction branchAction = new DiffBranchAction() {

                @Override
                protected IFolderDiffInput generateInput(IP4Branch branch) {
                    IDiffConfiguration leftConfig = createBranchConfiguration(mapping
                            .getSource());
                    IDiffConfiguration rightConfig = createBranchConfiguration(mapping
                            .getTarget());
                    IDiffConfiguration headerConfig = createHeaderConfiguration(mapping);
                    IFolderDiffInput input = new BranchDiffInput(
                            branch.getName(), branch.getConnection(),
                            headerConfig, leftConfig, rightConfig) {

                        @Override
                        public boolean exists() {
                            return false;
                        }

                        @Override
                        public IPersistableElement getPersistable() {
                            return null;
                        }
                    };
                    return input;

                }
            };
            branchAction
                    .selectionChanged(null, new StructuredSelection(branch));
            branchAction.run(null);
        }
    }

    private void runDiffAction(final DepotPathMapping depotMapping,
            final IP4Folder left, final IP4Folder right) {
        DiffFoldersAction action = new DiffFoldersAction() {

            @Override
            protected IFolderDiffInput generateInput(IP4Folder folder1,
                    IP4Folder folder2) {
                IDiffConfiguration leftConfig = createBranchConfiguration(depotMapping
                        .getSource());
                IDiffConfiguration rightConfig = createBranchConfiguration(depotMapping
                        .getTarget());
                IDiffConfiguration headerConfig = createHeaderConfiguration(depotMapping);
                FolderDiffInput input = new FolderDiffInput(
                        left.getConnection(), leftConfig, rightConfig,
                        headerConfig) {

                    @Override
                    public boolean exists() {
                        return false;
                    }

                    @Override
                    public IPersistableElement getPersistable() {
                        return null;
                    }

                };
                input.addPaths(left.getActionPath(), right.getActionPath());
                return input;
            }

        };
        action.selectionChanged(null, new StructuredSelection(new Object[] {
                left, right }));
        action.run(null);
    }

    private String getPath(String actionPath) {
        if (actionPath.endsWith(IP4Container.REMOTE_ELLIPSIS)) {
            return actionPath.substring(0, actionPath.length()
                    - IP4Container.REMOTE_ELLIPSIS.length());
        } else {
            return actionPath;
        }

    }
}
