/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.depot;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;
import com.perforce.team.ui.mergequest.wizards.mapping.NewMappingWizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMappingWizard extends NewMappingWizard {

    /**
     * @param graph
     */
    public DepotPathMappingWizard(IBranchGraph graph) {
        super(graph);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.NewMappingWizard#createMappingPage(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    @Override
    protected MappingWizardPage createMappingPage(IBranchGraph graph) {
        return new DepotPathMappingWizardPage("depotPathPage", graph); //$NON-NLS-1$
    }

}
