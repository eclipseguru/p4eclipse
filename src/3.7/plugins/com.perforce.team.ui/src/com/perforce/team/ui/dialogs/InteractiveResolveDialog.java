package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class InteractiveResolveDialog extends Dialog {

    public InteractiveResolveDialog(Shell parent) {
        super(parent);
    }

    public Text createTextField(Composite parent) {
        Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = GridData.CENTER;
        data.grabExcessVerticalSpace = false;
        data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        data.horizontalSpan = 3;
        text.setLayoutData(data);
        text.setEditable(false);
        return text;
    }

    protected void createYourFileGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.InteractiveResolveDialog_YourFile);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = true;
        group.setLayout(layout);

        createTextField(group);
        Button acceptYours = new Button(group, SWT.PUSH);
        acceptYours.setText(Messages.InteractiveResolveDialog_Accept);
        Button diffYours = new Button(group, SWT.PUSH);
        diffYours.setText(Messages.InteractiveResolveDialog_Diff);
        Button editYours = new Button(group, SWT.PUSH);
        editYours.setText(Messages.InteractiveResolveDialog_Edit);
    }

    protected void createTheirFileGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.InteractiveResolveDialog_TheirFile);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
    }

    protected void createDiffGroup(Composite parent) {
        Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.InteractiveResolveDialog_Summary);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(dialogArea, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        createYourFileGroup(composite);
        createTheirFileGroup(composite);
        createDiffGroup(composite);

        return dialogArea;
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.InteractiveResolveDialog_ResolveFile);
    }
}
