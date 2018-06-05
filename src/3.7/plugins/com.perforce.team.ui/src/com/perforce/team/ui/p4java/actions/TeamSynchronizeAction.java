/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.ui.synchronize.ResourceScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.synchronize.PerforceSynchronizeParticipant;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TeamSynchronizeAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IResource[] localResources = getSelectedResources();
        PerforceSynchronizeParticipant perforceParticipant = (PerforceSynchronizeParticipant) SubscriberParticipant
                .getMatchingParticipant(PerforceSynchronizeParticipant.ID,
                        localResources);
        if (perforceParticipant == null) {
            perforceParticipant = PerforceSynchronizeParticipant
                    .addParticipant(new ResourceScope(localResources));
        }
        perforceParticipant.refresh(localResources,
                Messages.TeamSynchronizeAction_SychronizingTitle,
                MessageFormat.format(
                        Messages.TeamSynchronizeAction_SynchronizingMessage,
                        perforceParticipant.getName()), PerforceUIPlugin
                        .getActivePage().getActivePart().getSite());
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return containsOnlineConnection();
    }

}
