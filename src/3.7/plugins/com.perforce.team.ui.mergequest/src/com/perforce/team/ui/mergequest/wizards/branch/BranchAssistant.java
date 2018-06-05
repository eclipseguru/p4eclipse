/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.branch;

import com.perforce.team.core.mergequest.model.Branch;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.editor.BaseContentAssistProcessor;
import com.perforce.team.ui.editor.BaseContentAssistant;
import com.perforce.team.ui.editor.IProposal;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchAssistant extends BaseContentAssistant {

    private Set<IProposal> branchProposals = new TreeSet<IProposal>();
    private SharedResources resources;

    /**
     * Create a new branch content assistant
     * 
     * @param resources
     */
    public BranchAssistant(SharedResources resources) {
        this.resources = resources;
    }

    /**
     * Load the proposals for the specified branch graph
     * 
     * @param graph
     */
    public void loadProposals(IBranchGraph graph) {
        if (graph != null) {
            for (Branch branch : graph.getBranches()) {
                this.branchProposals.add(new BranchProposal(branch, resources));
                activatorSet.add(branch.getName().charAt(0));
            }
        }
        updateCharArray();
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistant#createProposal(com.perforce.team.ui.editor.IProposal,
     *      int, int, int)
     */
    @Override
    protected ICompletionProposal createProposal(IProposal proposal,
            int replaceOffset, int replaceLength, int cursorOffset) {
        return new BranchCompletionProposal(
                ((BranchProposal) proposal).getBranch(), replaceOffset,
                replaceLength, cursorOffset, proposal.getImage());
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistant#init()
     */
    @Override
    public void init() {
        super.init();
        this.enablePrefixCompletion(false);
        this.setShowEmptyList(true);
        this.enableColoredLabels(true);
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistant#createProcessor()
     */
    @Override
    protected IContentAssistProcessor createProcessor() {
        return new BaseContentAssistProcessor() {

            public char[] getCompletionProposalAutoActivationCharacters() {
                return activators;
            }

            public ICompletionProposal[] computeCompletionProposals(
                    ITextViewer viewer, int offset) {
                String prefix = getPrefix(viewer.getDocument(), offset);

                Set<ICompletionProposal> proposals = new TreeSet<ICompletionProposal>(
                        comparator);

                Point selection = viewer.getSelectedRange();
                int selectOffset = selection.x;
                int selectLength = selection.y;
                createProposals(prefix, branchProposals, proposals, offset,
                        selectOffset, selectLength);

                return proposals.toArray(new ICompletionProposal[proposals
                        .size()]);
            }
        };
    }

}
