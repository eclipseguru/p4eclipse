/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.depot;

import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.mergequest.wizards.mapping.EditConnectorWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditDepotPathConnectorWizard extends EditConnectorWizard {

    /**
     * @param mapping
     */
    public EditDepotPathConnectorWizard(Mapping mapping) {
        super(mapping);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.EditConnectorWizard#createMappingPage(com.perforce.team.core.mergequest.model.Mapping)
     */
    @Override
    protected MappingWizardPage createMappingPage(Mapping mapping) {
        return new DepotPathMappingWizardPage(
                "depotPathPage", mapping.getGraph()); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        DepotPathMapping created = (DepotPathMapping) this.mappingPage
                .createMapping();
        DepotPathMapping current = (DepotPathMapping) this.mapping;
        current.setDirection(created.getDirection());
        current.setName(created.getName());
        current.setSourcePath(created.getSourcePath());
        current.setTargetPath(created.getTargetPath());
        return true;
    }
}
