/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.team.internal.ui.synchronize.GlobalRefreshResourceSelectionPage;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard;

import com.perforce.team.core.PerforceTeamProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceSynchronizeWizard extends ParticipantSynchronizeWizard {

    private GlobalRefreshResourceSelectionPage scopeSelectionPage;

    /**
     * @see org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard#createParticipant()
     */
    @Override
    protected void createParticipant() {
        PerforceSynchronizeParticipant participant = new PerforceSynchronizeParticipant(
                scopeSelectionPage.getSynchronizeScope());
        participant.run(null);
        TeamUI.getSynchronizeManager().addSynchronizeParticipants(
                new ISynchronizeParticipant[] { participant });
    }

    /**
     * @see org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard#createScopeSelectionPage()
     */
    @Override
    protected WizardPage createScopeSelectionPage() {
        scopeSelectionPage = new GlobalRefreshResourceSelectionPage(
                getRootResources());
        return scopeSelectionPage;
    }

    /**
     * @see org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard#getImportWizard()
     */
    @Override
    protected IWizard getImportWizard() {
        return null;
    }

    /**
     * @see org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard#getPageTitle()
     */
    @Override
    protected String getPageTitle() {
        return Messages.PerforceSynchronizeWizard_PageTitle;
    }

    /**
     * @see org.eclipse.team.ui.synchronize.ParticipantSynchronizeWizard#getRootResources()
     */
    @Override
    protected IResource[] getRootResources() {
        List<IProject> perforceProjects = new ArrayList<IProject>();
        for (IProject project : ResourcesPlugin.getWorkspace().getRoot()
                .getProjects())
            if (PerforceTeamProvider.getPerforceProvider(project) != null)
                perforceProjects.add(project);
        return perforceProjects.toArray(new IResource[perforceProjects.size()]);
    }
}
