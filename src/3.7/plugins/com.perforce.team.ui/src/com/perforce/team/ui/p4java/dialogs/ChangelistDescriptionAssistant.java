/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.DescriptionTemplate;
import com.perforce.team.ui.editor.IProposal;
import com.perforce.team.ui.editor.Proposal;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistDescriptionAssistant extends ContentAssistant {

    private Set<IProposal> jobProposals = new TreeSet<IProposal>();
    private Set<IProposal> templateProposals = Collections
            .synchronizedSet(new TreeSet<IProposal>());
    private Set<IProposal> fileProposals = new TreeSet<IProposal>();
    private Set<Character> activatorSet = Collections
            .synchronizedSet(new HashSet<Character>());
    private Set<IProposal> contributedProposals = new TreeSet<IProposal>();
    private char[] activators = null;
    private Trigger keyTrigger = null;

    private IPropertyChangeListener prefListener = new IPropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent event) {
            if (IPreferenceConstants.DESCRIPTION_AUTO_ACTIVATE.equals(event
                    .getProperty())) {
                enableAutoActivation(autoActivate());
            }
        }
    };

    private Comparator<ICompletionProposal> comparator = new Comparator<ICompletionProposal>() {

        public int compare(ICompletionProposal o1, ICompletionProposal o2) {
            return o1.getDisplayString().compareTo(o2.getDisplayString());
        }

    };

    private Trigger getContentAssistTrigger() {
        IBindingService service = (IBindingService) PlatformUI.getWorkbench()
                .getService(IBindingService.class);
        Trigger trigger = null;
        TriggerSequence[] sequences = service
                .getActiveBindingsFor(IWorkbenchCommandConstants.EDIT_CONTENT_ASSIST);
        if (sequences.length > 0) {
            Trigger[] triggers = sequences[0].getTriggers();
            if (triggers.length > 0) {
                trigger = triggers[0];
            }
        }
        return trigger;
    }

    private void updateCharArray() {
        StringBuilder activateString = new StringBuilder();
        synchronized (activatorSet) {
            for (Character c : activatorSet) {
                activateString.append(c);
            }
        }
        this.activators = activateString.toString().toCharArray();
    }

    private void loadProposals(IP4File[] files, IP4Job[] jobs,
            DescriptionTemplate[] templates) {
        this.fileProposals.clear();
        this.templateProposals.clear();

        for (IP4File file : files) {
            String remote = file.getRemotePath();
            this.fileProposals.add(new Proposal(remote));
            activatorSet.add(remote.charAt(0));
            String name = file.getName();
            this.fileProposals.add(new Proposal(name));
            activatorSet.add(name.charAt(0));
            int lastDot = name.lastIndexOf('.');
            if (lastDot > 0) {
                name = name.substring(0, lastDot);
                this.fileProposals.add(new Proposal(name));
                activatorSet.add(name.charAt(0));
            }
        }

        refreshTemplates(templates, false);
        refreshJobs(jobs, false);

        updateCharArray();
    }

    private void loadProcessor() {
        IContentAssistProcessor processor = new IContentAssistProcessor() {

            public String getErrorMessage() {
                return null;
            }

            public IContextInformationValidator getContextInformationValidator() {
                return null;
            }

            public char[] getContextInformationAutoActivationCharacters() {
                return null;
            }

            public char[] getCompletionProposalAutoActivationCharacters() {
                return ChangelistDescriptionAssistant.this.activators;
            }

            public IContextInformation[] computeContextInformation(
                    ITextViewer viewer, int offset) {
                return null;
            }

            private String getPrefix(IDocument document, int offset) {
                StringBuilder builder = new StringBuilder();
                try {
                    offset--;
                    char c = document.getChar(offset);
                    while (!Character.isWhitespace(c)) {
                        builder.insert(0, c);
                        offset--;
                        c = document.getChar(offset);
                    }
                } catch (BadLocationException e) {
                }
                return builder.toString();
            }

            public ICompletionProposal[] computeCompletionProposals(
                    ITextViewer viewer, int offset) {
                String prefix = getPrefix(viewer.getDocument(), offset);

                Set<ICompletionProposal> proposals = new TreeSet<ICompletionProposal>(
                        comparator);

                Point selection = viewer.getSelectedRange();
                int selectOffset = selection.x;
                int selectLength = selection.y;
                createProposals(prefix, fileProposals, proposals, offset,
                        selectOffset, selectLength);
                createProposals(prefix, templateProposals, proposals, offset,
                        selectOffset, selectLength);
                createProposals(prefix, jobProposals, proposals, offset,
                        selectOffset, selectLength);
                createProposals(prefix, contributedProposals, proposals,
                        offset, selectOffset, selectLength);

                return proposals.toArray(new ICompletionProposal[proposals
                        .size()]);
            }
        };
        this.setContentAssistProcessor(processor,
                IDocument.DEFAULT_CONTENT_TYPE);
    }

    private ICompletionProposal createProposal(IProposal proposal, int offset,
            int prefix) {
        return createProposal(proposal, offset - prefix, prefix, proposal
                .getValue().length());
    }

    private ICompletionProposal createProposal(IProposal proposal,
            int replaceOffset, int replaceLength, int cursorOffset) {
        return new CompletionProposal(proposal.getValue(), replaceOffset,
                replaceLength, cursorOffset, proposal.getImage(),
                proposal.getDisplay(), null, null);
    }

    private void createProposals(String prefix, Set<IProposal> proposals,
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

    private void loadTrigger() {
        this.keyTrigger = getContentAssistTrigger();
    }

    private boolean autoActivate() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.DESCRIPTION_AUTO_ACTIVATE);
    }

    /**
     * Initialize the assistant.
     * 
     * @param files
     * @param jobs
     * @param templates
     */
    public void init(IP4File[] files, IP4Job[] jobs,
            DescriptionTemplate[] templates) {
        loadProposals(files, jobs, templates);
        loadProcessor();
        loadTrigger();
        this.enableAutoActivation(autoActivate());
        this.enablePrefixCompletion(true);
        this.enableAutoInsert(true);
    }

    private void refreshJobs(IP4Job[] jobs, boolean updateActivators) {
        this.jobProposals.clear();
        for (IP4Job job : jobs) {
            String name = job.getName();
            this.jobProposals.add(new Proposal(name));
            activatorSet.add(name.charAt(0));
        }
        if (updateActivators) {
            updateCharArray();
        }
    }

    /**
     * Update jobs showing assistance for
     * 
     * @param jobs
     */
    public void updateJobs(IP4Job[] jobs) {
        refreshJobs(jobs, true);
    }

    private void refreshTemplates(DescriptionTemplate[] templates,
            boolean updateActivators) {
        this.templateProposals.clear();
        for (DescriptionTemplate template : templates) {
            String content = template.getContent();
            if (content.length() > 0) {
                this.templateProposals.add(new Proposal(template.getContent()));
                this.activatorSet.add(content.charAt(0));
            }
        }
        if (updateActivators) {
            updateCharArray();
        }
    }

    /**
     * Update templates showing assistance for
     * 
     * @param templates
     */
    public void updateTemplates(DescriptionTemplate[] templates) {
        refreshTemplates(templates, true);
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
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .addPropertyChangeListener(prefListener);
        textViewer.getTextWidget().addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .removePropertyChangeListener(prefListener);
            }
        });
        super.install(textViewer);
    }

    /**
     * Get proposals to this assistant
     * 
     * @param proposals
     */
    public void addProposals(IProposal[] proposals) {
        if (proposals != null) {
            for (IProposal proposal : proposals) {
                this.contributedProposals.add(proposal);
                activatorSet.add(proposal.getValue().charAt(0));
            }
        }
    }
}
