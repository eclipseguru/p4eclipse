/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.labels.LabelFilesDialog;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelFilesAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                enabled = !getResourceSelection().isEmpty();
            }
        }
        return enabled;
    }

    private boolean labelExists(IP4Connection connection, String name) {
        boolean exists = false;
        IP4Label label = connection.getLabel(name);
        exists = label != null && label.getUpdateTime() != null
                && label.getAccessTime() != null;
        return exists;
    }

    private boolean confirmLabel(IP4Connection connection, String name) {
        boolean confirmed = labelExists(connection, name);
        if (!confirmed) {
            // Open confirm dialog
            confirmed = P4ConnectionManager.getManager().openConfirm(
                    Messages.LabelFilesAction_LabelDoesNotExistTitle,
                    MessageFormat.format(
                            Messages.LabelFilesAction_LabelDoesNotExistMessage,
                            name));
        }
        return confirmed;
    }

    /**
     * Label the current resource selection with the specified options
     * 
     * @param labelName
     * @param revision
     * @param delete
     */
    public void label(final String labelName, final String revision,
            final boolean delete) {
        if (labelName != null) {
            final P4Collection collection = getResourceSelection();
            if (!collection.isEmpty()) {
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        collection.tag(labelName, revision, delete, false);
                    }

                    @Override
                    public String getTitle() {
                        return MessageFormat.format(
                                Messages.LabelFilesAction_LabelingResources,
                                labelName);
                    }
                };
                runRunnable(runnable);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
            LabelFilesDialog dialog = new LabelFilesDialog(getShell(),
                    collection);
            if (LabelFilesDialog.OK == dialog.open()) {
                final IP4Connection connection = collection.members()[0]
                        .getConnection();
                final String label = dialog.getSelectedLabel();
                final String revision = dialog.getRevision();
                final boolean delete = dialog.deleteFromLabel();
                IP4Runnable runnable = new P4Runnable() {

                    @Override
                    public void run(IProgressMonitor monitor) {
                        if (confirmLabel(connection, label)) {
                            label(label, revision, delete);
                        }
                    }
                };
                runRunnable(runnable);
            }
        }
    }

}
