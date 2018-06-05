/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest;

import org.eclipse.jface.fieldassist.SimpleContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TextContentAssist {

    private TextContentAdapter contentAdapter;
    private SimpleContentProposalProvider provider;
    private ContentAssistCommandAdapter commandAdapter;

    /**
     * Add text content assist with specified items to specified text field
     * 
     * @param text
     * @param items
     */
    public TextContentAssist(Text text, String[] items) {
        this.contentAdapter = new TextContentAdapter();

        this.provider = new SimpleContentProposalProvider(items);
        this.provider.setFiltering(true);

        this.commandAdapter = new ContentAssistCommandAdapter(text,
                contentAdapter, provider, null, null);

        this.commandAdapter.setAutoActivationCharacters(new char[0]);
        this.commandAdapter
                .setProposalAcceptanceStyle(ContentAssistCommandAdapter.PROPOSAL_REPLACE);
    }

    /**
     * Set content proposal provider items
     * 
     * @param items
     */
    public void setItems(String[] items) {
        this.provider.setProposals(items);
    }

}
