/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.internal;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.editor.BaseContentAssistProvider;
import com.perforce.team.ui.editor.IProposal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Perforce1666ContentAssistProvider extends
        BaseContentAssistProvider {

    private IProposal[] proposals;

    /**
     * Perforce 1666 Content Assist Provider
     */
    public Perforce1666ContentAssistProvider() {
        List<IProposal> proposalList = new ArrayList<IProposal>();
        proposalList.add(new Perforce1666Proposal("No functional change."));
        proposalList.add(new Perforce1666Proposal("Porting change."));
        proposalList.add(new Perforce1666Proposal("Integration only change."));
        proposalList.add(new Perforce1666Proposal("Performance change."));
        proposalList.add(new Perforce1666Proposal(
                "Change to unreleased behavior."));
        proposalList.add(new Perforce1666Proposal(
                "Change to undocumented behavior."));

        proposals = proposalList.toArray(new IProposal[proposalList.size()]);
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistProvider#getProposals(java.lang.Object,
     *      java.lang.Object)
     */
    @Override
    public IProposal[] getProposals(Object modelContext, Object uiContext) {
        if (modelContext instanceof IP4Resource
                && InternalPlugin.isPerforceServer(((IP4Resource) modelContext)
                        .getConnection().getParameters().getPort())) {
            return proposals;
        }
        return EMPTY;
    }
}
