/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.branch.EditBranchWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchEditAction extends Action {

    private Branch branch;

    /**
     * Create a branch edit action
     * 
     * @param branch
     */
    public BranchEditAction(Branch branch) {
        this.branch = branch;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (branch == null) {
            return;
        }
        IBranchGraph graph = branch.getGraph();
        if (graph == null) {
            return;
        }
        EditBranchWizard wizard = new EditBranchWizard(graph, branch);
        WizardDialog dialog = new WizardDialog(P4UIUtils.getDialogShell(),
                wizard);
        if (WizardDialog.OK == dialog.open()) {
            BranchDescriptor changes = wizard.getDescriptor();
            branch.setName(changes.getName());
            branch.setType(changes.getType().getType());
        }
    }

}
