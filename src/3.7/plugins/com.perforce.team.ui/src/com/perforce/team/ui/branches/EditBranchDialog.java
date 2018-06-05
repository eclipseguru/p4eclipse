/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class EditBranchDialog extends BaseBranchDialog {

    private IP4Branch branch;
    private IBranchSpec updatedBranch;
    private boolean canEdit = true;

    /**
     * @param parent
     * @param branch
     */
    public EditBranchDialog(Shell parent, IP4Branch branch) {
        super(parent);
        setType(Type.EDIT);
        this.branch = branch;
        if (branch != null) {
			setTitle(NLS.bind(Messages.EditBranchDialog_EditBranch,
					branch.getName()));
			String user = this.branch.getConnection().getParameters()
					.getUserNoNull();
			String owner = branch.getOwner();
			if (this.branch.isLocked()) {
				if (owner != null && !"".equals(owner) && !user.equals(owner)) { //$NON-NLS-1$
					this.canEdit = false;
				}
			}
        }
    }

    /**
     * @see com.perforce.team.ui.branches.BaseBranchDialog#fillWidget(com.perforce.team.ui.branches.BranchWidget)
     */
    @Override
    protected void fillWidget(BranchWidget widget) {
        widget.setEditable(this.canEdit);
        widget.setBranchNameEditable(false);
        widget.setDatesEditable(false);
        widget.update(this.branch);
    }

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#create()
     */
    @Override
    public void create() {
        super.create();
        if (!this.canEdit) {
            setErrorMessage(Messages.EditBranchDialog_BranchIsLocked);
        }
    }

    private void updateBranch() {
        this.updatedBranch = getLatestSpec();
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4FormDialog#save()
     */
    @Override
    public boolean save() {
        updateBranch();
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                branch.update(this.updatedBranch);
            } catch (P4JavaException e) {
                retry = P4ConnectionManager.getManager().displayException(
                        branch.getConnection(), e, true, true);
                if (!retry) {
                    return false;
                }
            }
        }
        return true;
    }

}
