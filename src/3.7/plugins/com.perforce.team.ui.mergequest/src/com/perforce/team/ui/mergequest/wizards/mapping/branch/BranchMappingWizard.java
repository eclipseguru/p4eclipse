/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;
import com.perforce.team.ui.mergequest.wizards.mapping.NewMappingWizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchMappingWizard extends NewMappingWizard {

    /**
     * @param graph
     */
    public BranchMappingWizard(IBranchGraph graph) {
        super(graph);
        setNeedsProgressMonitor(true);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.NewMappingWizard#createMappingPage(com.perforce.team.core.mergequest.model.IBranchGraph)
     */
    @Override
    protected MappingWizardPage createMappingPage(IBranchGraph graph) {
        return new BranchMappingWizardPage("branchMappingPage", graph); //$NON-NLS-1$
    }

}
