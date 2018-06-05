package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.history.P4HistoryPage;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

/**
 * View that show revision history for a file
 * 
 * @deprecated - use {@link P4HistoryPage} instead
 */
@Deprecated
public class HistoryView extends ViewPart {

    private Composite displayArea;

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Link newViewLink = new Link(displayArea, SWT.NONE);
        newViewLink
                .setText(Messages.HistoryView_P4EclipseSupportsEclipseHistoryView);
        newViewLink.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                PerforceUIPlugin.getActivePage().hideView(HistoryView.this);
                try {
                    PerforceUIPlugin.getActivePage().showView(
                            "org.eclipse.team.ui.GenericHistoryView"); //$NON-NLS-1$
                } catch (PartInitException e1) {
                    PerforceProviderPlugin.logError(e1);
                }
            }

        });
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        displayArea.setFocus();
    }

}
