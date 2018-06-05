/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.wizards.branch.BranchDescriptor;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class NewMappingWizard extends Wizard implements INewWizard {

    private IBranchGraph graph = null;
    private MappingWizardPage mappingPage = null;
    private Mapping created = null;
    private BranchDescriptor sourceDescriptor;
    private BranchDescriptor targetDescriptor;
    private Branch source = null;
    private Branch target = null;
    private Mapping mapping = null;

    /**
     * Create a new mapping wizard
     * 
     * @param graph
     */
    public NewMappingWizard(IBranchGraph graph) {
        this.graph = graph;
    }

    /**
     * Set initial source branch
     * 
     * @param source
     */
    public void setInitialSource(Branch source) {
        this.source = source;
    }

    /**
     * Set initial target branch
     * 
     * @param target
     */
    public void setInitialTarget(Branch target) {
        this.target = target;
    }

    /**
     * Set initial mapping
     * 
     * @param mapping
     */
    public void setInitialMapping(Mapping mapping) {
        this.mapping = mapping;
        if (this.mapping != null) {
            setInitialSource(this.mapping.getSource());
            setInitialTarget(this.mapping.getTarget());
        }
    }

    /**
     * Get created mapping
     * 
     * @return mapping
     */
    public Mapping getMapping() {
        return this.created;
    }

    /**
     * Get source branch descriptor
     * 
     * @return branch descriptor
     */
    public BranchDescriptor getSourceDescriptor() {
        return this.sourceDescriptor;
    }

    /**
     * Get target branch descriptor
     * 
     * @return branch descriptor
     */
    public BranchDescriptor getTargetDescriptor() {
        return this.targetDescriptor;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        this.created = this.mappingPage.createMapping();
        this.sourceDescriptor = this.mappingPage.getSourceDescriptor();
        this.targetDescriptor = this.mappingPage.getTargetDescriptor();
        if (this.source == null) {
            this.source = this.mappingPage.getSourceSelection();
        }
        if (this.target == null) {
            this.target = this.mappingPage.getTargetSelection();
        }
        return true;
    }

    /**
     * Get source branch selection
     * 
     * @return branch selection
     */
    public Branch getSourceSelection() {
        return this.source;
    }

    /**
     * Get target branch selection
     * 
     * @return branch selection
     */
    public Branch getTargetSelection() {
        return this.target;
    }

    /**
     * Create the mapping page for this wizard
     * 
     * @param graph
     * @return mapping wizard page
     */
    protected abstract MappingWizardPage createMappingPage(IBranchGraph graph);

    /**
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
     *      org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {

    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        this.mappingPage = createMappingPage(this.graph);
        this.mappingPage.setSource(source);
        this.mappingPage.setTarget(target);
        addPage(this.mappingPage);
    }

}
