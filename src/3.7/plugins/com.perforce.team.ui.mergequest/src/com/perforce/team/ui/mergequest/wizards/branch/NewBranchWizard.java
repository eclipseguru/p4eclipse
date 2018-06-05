/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.registry.BranchType;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class NewBranchWizard extends Wizard implements INewWizard {

    private IBranchGraph graph;
    private BranchDescriptor createdBranch = new BranchDescriptor();
    private BranchDescriptor initialBranch = new BranchDescriptor();

    private BranchWizardPage branchPage;

    /**
     * Create new branch wizard
     * 
     * @param graph
     * 
     * @param descriptor
     */
    public NewBranchWizard(IBranchGraph graph, BranchDescriptor descriptor) {
        this.graph = graph;
        if (descriptor == null) {
            descriptor = new BranchDescriptor();
        }
        this.initialBranch = descriptor;
    }

    /**
     * Create new branch wizard
     * 
     * @param graph
     */
    public NewBranchWizard(IBranchGraph graph) {
        this(graph, null);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        this.branchPage = new BranchWizardPage("branchPage", this.graph, //$NON-NLS-1$
                this.initialBranch);
        addPage(this.branchPage);
        this.branchPage.setTitle(Messages.BranchWizardPage_NewBranch);
        this.branchPage.setMessage(Messages.BranchWizardPage_AddNewBranch);
        this.branchPage.setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.WIZARD_BRANCH));
    }

    /**
     * Set initial name of branch
     * 
     * @param name
     */
    public void setInitialName(String name) {
        this.initialBranch.setName(name);
    }

    /**
     * Set initial type of branch
     * 
     * @param type
     */
    public void setInitialType(BranchType type) {
        this.initialBranch.setType(type);
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
     * Get branch name
     * 
     * @return name
     */
    public String getBranchName() {
        return this.createdBranch.getName();
    }

    /**
     * Get branch type
     * 
     * @return type
     */
    public BranchType getBranchType() {
        return this.createdBranch.getType();
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
