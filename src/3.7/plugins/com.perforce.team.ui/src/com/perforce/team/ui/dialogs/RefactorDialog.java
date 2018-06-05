/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.dialogs;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.changelists.PendingCombo;

import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class RefactorDialog extends P4StatusDialog {

    private IP4Connection connection;
    private IResource source;
    private IResource destination;

    private boolean useSelected = false;

    private PendingCombo pendingCombo;

    /**
     * @param parent
     * @param source
     * @param destination
     * @param connection
     */
    public RefactorDialog(Shell parent, IResource source,
            IResource destination, IP4Connection connection) {
        super(parent, Messages.RefactorDialog_SelectRefactorChangelist);
        setModalResizeStyle();
        this.source = source;
        this.destination = destination;
        this.connection = connection;
    }

    /**
     * Use the selected changelist as the new current one?
     * 
     * @return - true to use as current, false otherwise
     */
    public boolean useSelected() {
        return this.useSelected;
    }

    /**
     * Get selected pending id
     * 
     * @return - pending id
     */
    public int getPendingId() {
        return this.pendingCombo.getSelected();
    }

    /**
     * Get pending description
     * 
     * @return - description
     * 
     */
    public String getPendingComment() {
        return this.pendingCombo.getDescription();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(2, false);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.heightHint = 150;
        displayArea.setLayoutData(daData);

        GridData twoColData = new GridData(SWT.FILL, SWT.FILL, true, false);
        twoColData.horizontalSpan = 2;

        String comment = MessageFormat.format(
                Messages.RefactorDialog_RefactoringFilesFromTo, source
                        .getProjectRelativePath().toString(), destination
                        .getProjectRelativePath().toString());
        pendingCombo = new PendingCombo(
                Messages.RefactorDialog_OpenInChangelist, connection);
        pendingCombo.createControl(displayArea, 2, comment);

        final PerforceLabelProvider provider = new PerforceLabelProvider(true);
        displayArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                provider.dispose();
            }
        });

        Image sourceImage = provider.getImage(this.source);

        Label fromIcon = new Label(displayArea, SWT.LEFT);
        fromIcon.setImage(sourceImage);
        Label fromPath = new Label(displayArea, SWT.WRAP);
        fromPath.setText(MessageFormat.format(Messages.RefactorDialog_From,
                this.source.getProjectRelativePath().toOSString()));
        fromPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Image destinationImage = provider.getImage(this.destination);

        Label toIcon = new Label(displayArea, SWT.WRAP);
        toIcon.setImage(destinationImage);
        Label toPath = new Label(displayArea, SWT.LEFT);
        toPath.setText(MessageFormat.format(Messages.RefactorDialog_To,
                this.destination.getProjectRelativePath().toOSString()));
        toPath.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        final Button useSelectedButton = new Button(displayArea, SWT.CHECK);
        useSelectedButton
                .setText(Messages.RefactorDialog_UseSelectedChangelistUntilSubmitted);
        useSelectedButton.setLayoutData(twoColData);
        useSelectedButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                useSelected = useSelectedButton.getSelection();
            }

        });

        return c;
    }

}
