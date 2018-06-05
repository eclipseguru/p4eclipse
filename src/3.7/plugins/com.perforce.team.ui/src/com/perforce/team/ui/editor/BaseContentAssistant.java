/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.editor;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.keys.IBindingService;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class BaseContentAssistant extends ContentAssistant {

    /**
     * Activators array
     */
    protected char[] activators = null;
    private Trigger keyTrigger = null;

    /**
     * Activators set
     */
    protected Set<Character> activatorSet = Collections
            .synchronizedSet(new HashSet<Character>());

    /**
     * Completion proposal comparator
     */
    protected Comparator<ICompletionProposal> comparator = new Comparator<ICompletionProposal>() {

        public int compare(ICompletionProposal o1, ICompletionProposal o2) {
            return o1.getDisplayString().compareTo(o2.getDisplayString());
        }

    };

    /**
     * Get content assist trigger
     * 
     * @return trigger
     */
    protected Trigger getContentAssistTrigger() {
        IBindingService service = (IBindingService) PlatformUI.getWorkbench()
                .getService(IBindingService.class);
        Trigger trigger = null;
        TriggerSequence[] sequences = service
                .getActiveBindingsFor(ContentAssistCommandAdapter.CONTENT_PROPOSAL_COMMAND);
        if (sequences.length > 0) {
            Trigger[] triggers = sequences[0].getTriggers();
            if (triggers.length > 0) {
                trigger = triggers[0];
            }
        }
        return trigger;
    }

    /**
     * Update activator character array
     */
    protected void updateCharArray() {
        StringBuilder activateString = new StringBuilder();
        synchronized (activatorSet) {
            for (Character c : activatorSet) {
                activateString.append(c);
            }
        }
        this.activators = activateString.toString().toCharArray();
    }

    /**
     * Create completion proposal
     * 
     * @param proposal
     * @param offset
     * @param prefix
     * @return completion proposal
     */
    protected ICompletionProposal createProposal(IProposal proposal,
            int offset, int prefix) {
        return createProposal(proposal, offset - prefix, prefix, proposal
                .getValue().length());
    }

    /**
     * Create completion proposal
     * 
     * @param proposal
     * @param replaceOffset
     * @param replaceLength
     * @param cursorOffset
     * @return completion proposal
     */
    protected ICompletionProposal createProposal(IProposal proposal,
            int replaceOffset, int replaceLength, int cursorOffset) {
        return new CompletionProposal(proposal.getValue(), replaceOffset,
                replaceLength, cursorOffset, proposal.getImage(),
                proposal.getDisplay(), null, null);
    }

    /**
     * Create completion proposals
     * 
     * @param prefix
     * @param proposals
     * @param realProposals
     * @param offset
     * @param selectOffset
     * @param selectLength
     */
    protected void createProposals(String prefix, Set<IProposal> proposals,
            Set<ICompletionProposal> realProposals, int offset,
            int selectOffset, int selectLength) {
        int prefixLength = prefix.length();
        if (prefixLength > 0) {
            for (IProposal raw : proposals) {
                String display = raw.getDisplay();
                if (prefixLength < display.length()) {
                    String match = display.substring(0, prefixLength);
                    if (match.equalsIgnoreCase(prefix)) {
                        ICompletionProposal prop = createProposal(raw, offset,
                                prefixLength);
                        realProposals.add(prop);
                    }
                } else if (prefixLength == display.length()) {
                    // If prefix and raw are same length only add entries that
                    // don't 100% already match the prefix
                    if (!display.equals(prefix) && display.equalsIgnoreCase(prefix)) {
                        ICompletionProposal prop = createProposal(raw, offset,
                                prefixLength);
                        realProposals.add(prop);
                    }
                }
            }
        } else {
            // Replace the selected text if no prefix and the cursor offset is
            // at the beginning of a selection, fix for job036194
            int length = selectOffset == offset && selectLength > 0
                    ? selectLength
                    : 0;
            for (IProposal raw : proposals) {
                ICompletionProposal prop = createProposal(raw, offset, length,
                        raw.getValue().length());
                realProposals.add(prop);
            }
        }
    }

    /**
     * Load and set the content assist trigger
     */
    protected void loadTrigger() {
        this.keyTrigger = getContentAssistTrigger();
    }

    /**
     * Auto activate proposals?
     * 
     * @return true to auto activate, false to not
     */
    protected boolean autoActivate() {
        return false;
    }

    /**
     * Load the content assist processor
     */
    protected void loadProcessor() {
        this.setContentAssistProcessor(createProcessor(),
                IDocument.DEFAULT_CONTENT_TYPE);
    }

    /**
     * Create a content assist processor
     * 
     * @return content assist processor
     */
    protected abstract IContentAssistProcessor createProcessor();

    /**
     * Initialize the assistant.
     * 
     */
    public void init() {
        loadProcessor();
        loadTrigger();
        this.enableAutoActivation(autoActivate());
        this.enablePrefixCompletion(true);
        this.enableAutoInsert(true);
    }

    /**
     * @see org.eclipse.jface.text.contentassist.ContentAssistant#install(org.eclipse.jface.text.ITextViewer)
     */
    @Override
    public void install(ITextViewer textViewer) {
        if (this.keyTrigger != null) {
            textViewer.getTextWidget().addVerifyKeyListener(
                    new VerifyKeyListener() {

                        public void verifyKey(VerifyEvent event) {
                            KeyStroke stroke = KeyStroke.getInstance(
                                    event.stateMask, event.character != 0
                                            ? event.character
                                            : event.keyCode);
                            if (keyTrigger.equals(stroke)) {
                                event.doit = false;
                                showPossibleCompletions();
                            }
                        }
                    });
        }
        super.install(textViewer);
    }

}
