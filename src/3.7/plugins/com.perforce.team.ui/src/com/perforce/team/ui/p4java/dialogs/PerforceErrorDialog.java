/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4LogUtils;
import com.perforce.team.ui.dialogs.PerforceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceErrorDialog extends PerforceDialog {

    /**
     * Get an opened error dialog for the specified error specs
     * 
     * @param parent
     * @param specs
     * @param block
     * @return - opened dialog
     */
    public static PerforceErrorDialog showErrors(Shell parent,
            IFileSpec[] specs, boolean block) {
        PerforceErrorDialog dialog = null;
        if (parent!=null && !parent.isDisposed() && specs != null && specs.length > 0) {
            dialog = new PerforceErrorDialog(parent, specs);
            dialog.setBlockOnOpen(block);
            dialog.open();
        }
        return dialog;
    }

    /**
     * Show the error specs in the specified array
     * 
     * @param parent
     * @param specs
     */
    public static void showErrors(Shell parent, IFileSpec[] specs) {
        showErrors(parent, specs, true);
    }

    private Composite displayArea;
    private Label errorsIcon;
    private Label errorsLabel;
    private Text detailsText;

    private IFileSpec[] specs;

    /**
     * @param parent
     * @param specs
     */
    public PerforceErrorDialog(Shell parent, IFileSpec[] specs) {
        super(parent, Messages.PerforceErrorDialog_PerforceError);
        this.specs = specs;
        setModalResizeStyle();
    }

    /**
     * Get details text
     * 
     * @return - error details
     */
    public String getDetailsText() {
        return this.detailsText.getText();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.heightHint = 200;
        displayArea.setLayoutData(daData);
        errorsIcon = new Label(displayArea, SWT.LEFT);
        errorsIcon.setImage(displayArea.getShell().getDisplay()
                .getSystemImage(SWT.ICON_ERROR));
        errorsLabel = new Label(displayArea, SWT.LEFT);
        errorsLabel.setText(Messages.PerforceErrorDialog_ErrorsLabel);
        detailsText = new Text(displayArea, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        GridData dtData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dtData.horizontalSpan = 2;
        detailsText.setLayoutData(dtData);

        detailsText.setText(P4LogUtils.getError(specs));

        return c;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
                true);
    }

}
