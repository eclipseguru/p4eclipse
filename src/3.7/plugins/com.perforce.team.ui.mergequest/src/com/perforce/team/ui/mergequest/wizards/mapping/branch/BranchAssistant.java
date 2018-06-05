/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.mapping.branch;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.editor.BaseContentAssistProcessor;
import com.perforce.team.ui.editor.BaseContentAssistant;
import com.perforce.team.ui.editor.IProposal;
import com.perforce.team.ui.mergequest.parts.SharedResources;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.graphics.Point;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchAssistant extends BaseContentAssistant {

    private Set<String> searches = new HashSet<String>();
    private Set<IProposal> branchProposals = new TreeSet<IProposal>();
    private SharedResources resources;
    private Runnable callback;
    private ITextViewer viewer;

    private ISchedulingRule searchRule = P4Runner.createRule();
    private IP4Connection connection = null;

    /**
     * Create a new branch content assistant
     * 
     * @param resources
     * @param connection
     */
    public BranchAssistant(SharedResources resources, IP4Connection connection) {
        this.resources = resources;
        this.connection = connection;
    }

    /**
     * Create a new branch content assistant
     * 
     * @param resources
     * @param connection
     * @param callback
     */
    public BranchAssistant(SharedResources resources, IP4Connection connection,
            Runnable callback) {
        this(resources, connection);
        this.callback = callback;
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistant#install(org.eclipse.jface.text.ITextViewer)
     */
    @Override
    public void install(ITextViewer textViewer) {
        this.viewer = textViewer;
        super.install(textViewer);
    }

    private String convertNameContains(String entered) {
        String converted = entered;
        if (converted.length() > 0) {
            converted = converted.replace("*", IP4Container.ELLIPSIS); //$NON-NLS-1$
            if (!converted.endsWith("...")) { //$NON-NLS-1$
                converted = converted + IP4Container.ELLIPSIS;
            }
        }
        return converted;
    }

    private boolean containsSearch(String prefix) {
        while (prefix.length() >= 1) {
            if (searches.contains(prefix)) {
                return true;
            } else {
                prefix = prefix.substring(0, prefix.length() - 1);
            }
        }
        return false;
    }

    private void searchForBranch(final String prefix, final String searchName) {
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(Messages.BranchAssistant_Searching,
                        prefix);
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final IP4Branch[] branches = connection.getBranches(null, -1,
                        searchName);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(viewer.getTextWidget())) {
                            loadProposals(branches);
                            showPossibleCompletions();
                        }
                    }
                });
            }

        }, searchRule);
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
                replaceLength, cursorOffset, proposal.getImage(), this.callback);
    }

    /**
     * Load the proposals for the specified branches
     * 
     * @param branches
     */
    public void loadProposals(IP4Branch[] branches) {
        if (branches != null) {
            for (IP4Branch branch : branches) {
                this.branchProposals.add(new BranchProposal(branch, resources));
                activatorSet.add(branch.getName().charAt(0));
            }
        }

        updateCharArray();
    }

    /**
     * @see com.perforce.team.ui.editor.BaseContentAssistant#init()
     */
    @Override
    public void init() {
        super.init();
        this.enablePrefixCompletion(false);
        this.setRepeatedInvocationMode(true);
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

                boolean useExisting = false;
                boolean empty = prefix.length() == 0;
                if (!empty && !containsSearch(prefix)) {
                    String search = convertNameContains(prefix);
                    searches.add(prefix);
                    proposals.add(new LoadingProposal(resources));
                    searchForBranch(prefix, search);
                } else {
                    useExisting = true;
                }

                if (useExisting) {
                    Point selection = viewer.getSelectedRange();
                    int selectOffset = selection.x;
                    int selectLength = selection.y;
                    createProposals(prefix, branchProposals, proposals, offset,
                            selectOffset, selectLength);
                }
                if (!empty && proposals.size() == 0) {
                    setShowEmptyList(true);
                    setEmptyMessage(MessageFormat.format(
                            Messages.BranchAssistant_NoBranchesFound, prefix));
                } else {
                    setShowEmptyList(false);
                }
                return proposals.toArray(new ICompletionProposal[proposals
                        .size()]);
            }
        };
    }
}
