/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;
import com.perforce.team.ui.mergequest.wizards.branch.NewBranchWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchCreateAction extends Action {

    private IBranchGraph graph;
    private BranchType type;
    private Point location;
    private Branch created;

    /**
     * Create branch action
     * 
     * @param graph
     * @param type
     * @param location
     */
    public BranchCreateAction(IBranchGraph graph, BranchType type,
            Point location) {
        this.graph = graph;
        this.type = type;
        this.location = location;
    }

    /**
     * Get last created branch
     * 
     * @return branch
     */
    public Branch getCreated() {
        return this.created;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        BranchDescriptor descriptor = new BranchDescriptor();
        descriptor.setType(type);
        NewBranchWizard wizard = new NewBranchWizard(this.graph, descriptor);
        WizardDialog dialog = new WizardDialog(P4UIUtils.getDialogShell(),
                wizard);
        if (WizardDialog.OK == dialog.open()) {
            Branch branch = this.graph.createBranch(null);
            branch.setType(wizard.getBranchType().getType());
            branch.setName(wizard.getBranchName());
            branch.setLocation(this.location.x, this.location.y);
            if (this.graph.add(branch)) {
                this.created = branch;
                for (IAction action : wizard.getPostFinishActions()) {
                    action.run();
                }
            }
        }
    }

}
