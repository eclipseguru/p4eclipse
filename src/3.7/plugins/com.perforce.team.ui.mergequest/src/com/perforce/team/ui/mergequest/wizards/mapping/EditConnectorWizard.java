/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping;

import com.perforce.team.core.mergequest.model.Mapping;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class EditConnectorWizard extends Wizard implements INewWizard {

    /**
     * Mapping wizard page
     */
    protected MappingWizardPage mappingPage = null;

    /**
     * Mapping
     */
    protected Mapping mapping = null;

    /**
     * Create a new mapping wizard
     * 
     * @param mapping
     */
    public EditConnectorWizard(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * Get edited mapping
     * 
     * @return mapping
     */
    public Mapping getMapping() {
        return this.mapping;
    }

    /**
     * Create the mapping page for this wizard
     * 
     * @param mapping
     * @return mapping wizard page
     */
    protected abstract MappingWizardPage createMappingPage(Mapping mapping);

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
        this.mappingPage = createMappingPage(this.mapping);
        this.mappingPage.setSource(this.mapping.getSource());
        this.mappingPage.setTarget(this.mapping.getTarget());
        this.mappingPage.setExistingMapping(this.mapping);
        addPage(this.mappingPage);
    }

}
