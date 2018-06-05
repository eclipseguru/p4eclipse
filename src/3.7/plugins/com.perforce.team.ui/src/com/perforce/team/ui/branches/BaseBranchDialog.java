/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import java.util.StringTokenizer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.impl.generic.core.BranchSpec;
import com.perforce.p4java.impl.generic.core.BranchSpec.BranchViewMapping;
import com.perforce.team.ui.dialogs.P4FormDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class BaseBranchDialog extends P4FormDialog {

    private BranchWidget widget;

    /**
     * @param parent
     */
    public BaseBranchDialog(Shell parent) {
        super(parent);
        setModalResizeStyle();
    }

    /**
     * @param parent
     * @param title
     */
    public BaseBranchDialog(Shell parent, String title) {
        super(parent, title);
        setModalResizeStyle();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        Composite displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.minimumWidth = 600;
        displayArea.setLayoutData(daData);
        widget = new BranchWidget(displayArea);
        widget.addFormNameValidation(this);
        fillWidget(widget);
        return c;
    }

    private ViewMap<IBranchMapping> getBranchView() {
        ViewMap<IBranchMapping> view = new ViewMap<IBranchMapping>();
        StringTokenizer tokenizer = new StringTokenizer(widget.getView(),
                "\r\n"); //$NON-NLS-1$
        int count = 0;
        while (tokenizer.hasMoreElements()) {
            view.addEntry(new BranchViewMapping(count, tokenizer.nextToken()));
            count++;
        }
        return view;
    }

    /**
     * Get latest spec from widget
     * 
     * @return - spec
     */
    protected IBranchSpec getLatestSpec() {
        BranchSpec spec = new BranchSpec();
        spec.setName(widget.getBranchName());
        spec.setLocked(widget.isLocked());
        spec.setDescription(widget.getDescription());
        spec.setBranchView(getBranchView());
        spec.setOwnerName(widget.getOwner());
        return spec;
    }

    /**
     * Fill widget with content
     * 
     * @param widget
     */
    protected abstract void fillWidget(BranchWidget widget);

    /**
     * @see com.perforce.team.ui.dialogs.P4FormDialog#getModelLabel()
     */
    @Override
    protected String getModelLabel() {
        return "branch"; //$NON-NLS-1$
    }

}
