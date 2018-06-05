/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.P4BranchGraphCorePlugin;
import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditBranchWizard extends Wizard implements INewWizard {

    private IBranchGraph graph;
    private BranchDescriptor createdBranch = new BranchDescriptor();
    private Branch initialBranch = null;

    private BranchWizardPage branchPage;

    /**
     * Create new branch wizard
     * 
     * @param graph
     * @param branch
     * 
     */
    public EditBranchWizard(IBranchGraph graph, Branch branch) {
        this.graph = graph;
        this.initialBranch = branch;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        BranchDescriptor initial = new BranchDescriptor();
        initial.setName(this.initialBranch.getName());
        initial.setType(P4BranchGraphCorePlugin.getDefault()
                .getBranchRegistry().getType(this.initialBranch.getType()));
        this.branchPage = new BranchWizardPage("branchPage", this.graph, //$NON-NLS-1$
                initial) {

            @Override
            protected BranchNameArea createNameArea(Composite parent,
                    SharedResources resources, IBranchGraph graph,
                    IErrorProvider provider) {
                BranchNameArea area = super.createNameArea(parent, resources,
                        graph, provider);
                area.setSyncTypes(false);
                area.setEditable(true);
                area.selectAll();
                return area;
            }

        };
        addPage(this.branchPage);
        this.branchPage.setTitle(Messages.EditBranchWizard_EditBranchTitle);
        this.branchPage
                .setMessage(Messages.EditBranchWizard_EditBranchDescription);
        this.branchPage.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.WIZARD_BRANCH));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        this.createdBranch = this.branchPage.getCreatedBranch();
        return true;
    }

    /**
     * Get descriptor of changes made
     * 
     * @return branch descriptor
     */
    public BranchDescriptor getDescriptor() {
        return this.createdBranch;
    }

    /**
     * Get post finish actions
     * 
     * @return non-null but possibly empty array
     */
    public IAction[] getPostFinishActions() {
        return new IAction[0];
    }

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

}