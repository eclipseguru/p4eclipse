/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.mergequest.IP4BranchGraphConstants;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;
import com.perforce.team.ui.mergequest.wizards.mapping.IMappingArea;
import com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage;

import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchMappingWizardPage extends MappingWizardPage {

    private boolean inSession = false;

    /**
     * @param pageName
     * @param graph
     */
    public BranchMappingWizardPage(String pageName, IBranchGraph graph) {
        super(pageName, graph);
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    @Override
    public void setErrorMessage(String message, IErrorProvider provider) {
        if (inSession) {
            setPageComplete(false);
            setErrorMessage(message);
        } else {
            super.setErrorMessage(message, provider);
        }
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        setTitle(Messages.BranchMappingWizardPage_Title);
        setMessage(Messages.BranchMappingWizardPage_Description);
        setImageDescriptor(P4BranchGraphPlugin
                .getImageDescriptor(IP4BranchGraphConstants.WIZARD_BRANCH_SPEC_MAPPING));

        ((BranchArea) getMappingArea()).getAssistant().addCompletionListener(
                new ICompletionListener() {

                    public void selectionChanged(ICompletionProposal proposal,
                            boolean smartToggle) {

                    }

                    public void assistSessionStarted(ContentAssistEvent event) {
                        inSession = true;
                        setPageComplete(false);
                    }

                    public void assistSessionEnded(ContentAssistEvent event) {
                        inSession = false;
                        setPageComplete(getErrorMessage() == null);
                    }
                });
    }

    /**
     * @see com.perforce.team.ui.mergequest.wizards.mapping.MappingWizardPage#createMappingArea(com.perforce.team.core.mergequest.model.IBranchGraph,
     *      com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    protected IMappingArea createMappingArea(IBranchGraph graph,
            IP4Connection connection) {
        return new BranchArea(graph, connection);
    }

}
