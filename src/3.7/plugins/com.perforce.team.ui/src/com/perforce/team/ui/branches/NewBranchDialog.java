/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class NewBranchDialog extends BaseBranchDialog {

    private IP4Connection connection;
    private IP4Branch template;
    private IP4Branch createdBranch;

    /**
     * @param parent
     * @param connection
     * @param template
     */
    public NewBranchDialog(Shell parent, IP4Connection connection,
            IP4Branch template) {
        super(parent);
        if (connection != null) {
            ConnectionParameters params = connection.getParameters();
            setTitle(NLS.bind(Messages.NewBranchDialog_Title, params.getPort(),
                    params.getUser()));
        }
        this.connection = connection;
        this.template = template;
    }

    /**
     * @see com.perforce.team.ui.branches.BaseBranchDialog#fillWidget(com.perforce.team.ui.branches.BranchWidget)
     */
    @Override
    protected void fillWidget(BranchWidget widget) {
        widget.setEditable(true);
        widget.setDatesVisible(false);
        widget.update(this.template);
        widget.setBranchName(""); //$NON-NLS-1$
    }

    /**
     * Get created branch
     * 
     * @return - created branch or null if branch wasn't created
     */
    public IP4Branch getCreatedBranch() {
        return this.createdBranch;
    }

    private boolean branchExists(IP4Connection connection, String name) {
        boolean exists = false;
        IP4Branch branch = connection.getBranch(name);
        if (branch != null) {
            exists = branch.getUpdateTime() != null
                    && branch.getAccessTime() != null;
        }
        return exists;
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4FormDialog#save()
     */
    @Override
    public boolean save() {
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                IBranchSpec latest = getLatestSpec();
                if (!branchExists(connection, latest.getName())) {
                    this.createdBranch = connection.createBranch(latest);
                } else {
                    P4ConnectionManager
                            .getManager()
                            .openInformation(
                                    P4UIUtils.getDialogShell(),
                                    NLS.bind(
                                            Messages.NewBranchDialog_BranchExistsTitle,
                                            latest.getName()),
                                    NLS.bind(
                                            Messages.NewBranchDialog_BranchExistsMessage,
                                            latest.getName()));
                    return false;
                }
            } catch (P4JavaException e) {
                retry = P4ConnectionManager.getManager().displayException(
                        connection, e, true, true);
                if (!retry) {
                    return false;
                }
            }
        }
        return true;
    }
}
