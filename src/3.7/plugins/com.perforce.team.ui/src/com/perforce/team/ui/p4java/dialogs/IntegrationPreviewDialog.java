/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.dialogs.PerforceDialog;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrationPreviewDialog extends PerforceDialog {

    private Composite displayArea;
    private TextViewer viewer;
    private StringBuffer text;

    public IntegrationPreviewDialog(Shell parent, IP4Resource[] resources) {
    	this(parent, resources, Messages.IntegrationPreviewDialog_IntegrationPreview);
    }
    /**
     * @param parent
     * @param resources
     */
    public IntegrationPreviewDialog(Shell parent, IP4Resource[] resources, String title) {
        super(parent, title);
        setModalResizeStyle();
        text = new StringBuffer();
        for (IP4Resource preview : resources) {
            if (preview instanceof IP4File) {
                IP4File file = (IP4File) preview;
                if (file.getStatus() == FileSpecOpStatus.VALID) {
                    IFileSpec spec = file.getIntegrationSpec();
                    if (spec != null) {
                        text.append(spec.getDepotPath());
                        text.append("#"); //$NON-NLS-1$
                        text.append(spec.getWorkRev());
                        FileAction action1 = spec.getAction();
                        FileAction action2 = spec.getOtherAction();
                        text.append(" - "); //$NON-NLS-1$
                        if (action1 != null) {
                            text.append(action1.toString().toLowerCase());
                        }
                        if (action2 != null) {
                            if (action1 != null) {
                                text.append("/"); //$NON-NLS-1$
                            }
                            text.append(action2.toString().toLowerCase());
                        }
                        text.append(Messages.IntegrationPreviewDialog_From);
                        text.append(spec.getFromFile());
                        text.append("#"); //$NON-NLS-1$
                        int start = spec.getStartFromRev();
                        if (start == IFileSpec.NO_FILE_REVISION) {
                            start = 1;
                        }
                        text.append(start);
                        text.append(",#"); //$NON-NLS-1$
                        text.append(spec.getEndFromRev());
                        text.append("\n"); //$NON-NLS-1$
                    }
                } else {
                    String message = file.getStatusMessage();
                    if (message != null) {
                        text.append(message);
                        text.append("\n"); //$NON-NLS-1$
                    }
                }
            }
        }

    }

    /**
     * Get the text viewer
     * 
     * @return - text viewer
     */
    public TextViewer getViewer() {
        return this.viewer;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer = new TextViewer(displayArea, SWT.READ_ONLY | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = 250;
        vData.widthHint = 500;
        viewer.getTextWidget().setLayoutData(vData);
        IDocument document = new Document(text.toString());
        viewer.setDocument(document);
        return c;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID,
                Messages.IntegrationPreviewDialog_Close, true);
    }

}
