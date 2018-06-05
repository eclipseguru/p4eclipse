/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class OpenResourceDialog extends P4StatusDialog {

    private String labelText = null;

    /**
     * @param parent
     * @param title
     * @param labelText
     */
    public OpenResourceDialog(Shell parent, String title, String labelText) {
        super(parent, title);
        this.labelText = labelText;
        setModalResizeStyle();
    }

    /**
     * Get modify listener to use. Sub-classes should override
     * 
     * @return - modify listener
     */
    protected ModifyListener getListener() {
        return null;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label resourceLabel = new Label(displayArea, SWT.NONE);
        resourceLabel.setText(labelText);

        Text resourceText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        resourceText
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ModifyListener listener = getListener();
        if (listener != null) {
            resourceText.addModifyListener(listener);
            resourceText.setText(""); //$NON-NLS-1$
        }
        return c;
    }

}
