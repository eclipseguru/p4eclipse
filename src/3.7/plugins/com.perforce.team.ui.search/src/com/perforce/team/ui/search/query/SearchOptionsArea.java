/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.team.core.search.query.QueryOptions;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Spinner;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SearchOptionsArea {

    private Composite displayArea = null;
    private Button allRevisions = null;
    private Button searchBinaries = null;
    private Button caseInsensitive = null;
    private Button leadingContextButton = null;
    private Button trailingContextButton = null;
    private Spinner leadingContextSpinner = null;
    private Spinner trailingContextSpinner = null;

    /**
     * Create search options area
     */
    public SearchOptionsArea() {

    }

    /**
     * Fill specified non-null options object with currently selected options
     * 
     * @param options
     */
    public void fillOptions(QueryOptions options) {
        if (allRevisions != null) {
            options.setAllRevisions(allRevisions.getSelection());
        } else {
            options.setAllRevisions(false);
        }
        options.setCaseInsensitive(caseInsensitive.getSelection());
        options.setSearchBinaries(searchBinaries.getSelection());
        if (leadingContextButton.getSelection()) {
            options.setLeadingContext(leadingContextSpinner.getSelection());
        }
        if (trailingContextButton.getSelection()) {
            options.setTrailingContext(trailingContextSpinner.getSelection());
        }
    }

    /**
     * Create button with specified parent, text and style
     * 
     * @param parent
     * @param text
     * @param style
     * @return - button
     */
    protected Button createButton(Composite parent, String text, int style) {
        Button button = new Button(parent, style);
        if (text != null) {
            button.setText(text);
        }
        return button;
    }

    /**
     * Create search options controls
     * 
     * @param parent
     * @param showSearchAll
     */
    public void createControl(Composite parent, boolean showSearchAll) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(3, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        if (showSearchAll) {
            allRevisions = createButton(displayArea,
                    Messages.SearchOptionsArea_SearchAllRevs, SWT.CHECK);
        }

        caseInsensitive = createButton(displayArea,
                Messages.SearchOptionsArea_CaseInsensitive, SWT.CHECK);

        searchBinaries = createButton(displayArea,
                Messages.SearchOptionsArea_IncludeBinaryFiles, SWT.CHECK);

        createContextOptions(displayArea);
    }

    private void createContextOptions(Composite parent) {
        Composite leadingArea = new Composite(parent, SWT.NONE);
        leadingArea.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false,
                false));
        GridLayout laLayout = new GridLayout(2, false);
        laLayout.marginHeight = 0;
        laLayout.marginWidth = 0;
        leadingArea.setLayout(laLayout);

        leadingContextButton = createButton(leadingArea,
                Messages.SearchOptionsArea_LeadingContextLines, SWT.CHECK);
        leadingContextSpinner = new Spinner(leadingArea, SWT.NONE);
        leadingContextSpinner.setMinimum(1);
        leadingContextSpinner.setSelection(1);
        leadingContextSpinner.setEnabled(false);
        leadingContextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                leadingContextSpinner.setEnabled(leadingContextButton
                        .getSelection());
            }
        });

        Composite trailingArea = new Composite(parent, SWT.NONE);
        trailingArea.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false,
                false));
        GridLayout taLayout = new GridLayout(2, false);
        taLayout.marginHeight = 0;
        taLayout.marginWidth = 0;
        trailingArea.setLayout(taLayout);

        trailingContextButton = createButton(trailingArea,
                Messages.SearchOptionsArea_TrailingContextLines, SWT.CHECK);
        trailingContextSpinner = new Spinner(trailingArea, SWT.NONE);
        trailingContextSpinner.setMinimum(1);
        trailingContextSpinner.setSelection(1);
        trailingContextSpinner.setEnabled(false);
        trailingContextButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                trailingContextSpinner.setEnabled(trailingContextButton
                        .getSelection());
            }
        });
    }

    /**
     * Create search options controls
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        createControl(parent, true);
    }

    /**
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        allRevisions.setEnabled(enabled);
        searchBinaries.setEnabled(enabled);
        caseInsensitive.setEnabled(enabled);
        leadingContextButton.setEnabled(enabled);
        trailingContextButton.setEnabled(enabled);
        leadingContextSpinner.setEnabled(enabled);
        trailingContextSpinner.setEnabled(enabled);
    }
}
