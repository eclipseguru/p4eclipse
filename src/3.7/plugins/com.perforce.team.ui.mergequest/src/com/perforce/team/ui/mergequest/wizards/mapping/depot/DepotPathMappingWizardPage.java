/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.depot;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;

import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMappingWizardPage extends MappingWizardPage {

    /**
     * @param pageName
     * @param graph
     */
    public DepotPathMappingWizardPage(String pageName, IBranchGraph graph) {
        super(pageName, graph);
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        setTitle(Messages.DepotPathMappingWizardPage_Title);
        setMessage(Messages.DepotPathMappingWizardPage_Description);
        setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.WIZARD_DEPOT_PATH_MAPPING));
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage#createMappingArea(com.perforce.team.core.mergequest.model.IBranchGraph,
     *      com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    protected IMappingArea createMappingArea(IBranchGraph graph,
            IP4Connection connection) {
        return new DepotPathArea(graph, connection);
    }
}
