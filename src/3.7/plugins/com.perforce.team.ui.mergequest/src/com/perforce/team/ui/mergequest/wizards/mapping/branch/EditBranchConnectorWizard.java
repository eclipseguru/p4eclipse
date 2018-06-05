/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.wizards.mapping.EditConnectorWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditBranchConnectorWizard extends EditConnectorWizard {

    /**
     * @param mapping
     */
    public EditBranchConnectorWizard(Mapping mapping) {
        super(mapping);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.EditConnectorWizard#createMappingPage(com.perforce.team.core.mergequest.model.Mapping)
     */
    @Override
    protected MappingWizardPage createMappingPage(Mapping mapping) {
        return new BranchMappingWizardPage(
                "branchMappingPage", mapping.getGraph()); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        BranchSpecMapping created = (BranchSpecMapping) this.mappingPage
                .createMapping();
        BranchSpecMapping current = (BranchSpecMapping) this.mapping;
        current.setDirection(created.getDirection());
        current.setName(created.getName());
        return true;
    }

}
