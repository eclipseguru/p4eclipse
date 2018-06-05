/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.resource.ResourceBrowserDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathDialog extends P4StatusDialog {

    private Text pathText;
    private String path = ""; //$NON-NLS-1$
    private IP4Connection connection;

    /**
     * @param parent
     * @param connection
     */
    public DepotPathDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.DepotPathDialog_EnterDepotPath);
        setModalResizeStyle();
        this.connection = connection;
    }

    /**
     * Get path
     * 
     * @return path
     */
    public String getPath() {
        return this.path;
    }

    private void validate() {
        this.path = this.pathText.getText().trim();
        if (this.path.length() == 0) {
            setErrorMessage(Messages.DepotPathDialog_EnterDepotPath2);
        } else if (!this.path.startsWith("//")) { //$NON-NLS-1$
            setErrorMessage(Messages.DepotPathDialog_PathMustStartWithDepot);
        } else {
            setErrorMessage(null);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(3, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label pathLabel = new Label(displayArea, SWT.NONE);
        pathLabel.setText(Messages.DepotPathDialog_DepotPath);

        pathText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        pathText.setText("//"); //$NON-NLS-1$
        pathText.selectAll();
        GridData ptData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        ptData.widthHint = 200;
        pathText.setLayoutData(ptData);
        pathText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                validate();
            }
        });

        Button browseButton = new Button(displayArea, SWT.PUSH);
        browseButton.setText(Messages.DepotPathDialog_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceBrowserDialog dialog = new ResourceBrowserDialog(
                        pathText.getShell(), connection.members());
                if (dialog.open() == ResourceBrowserDialog.OK) {
                    IP4Resource resource = dialog.getSelectedResource();
                    if (resource != null) {
                        String actionPath = resource
                                .getActionPath(IP4Resource.Type.REMOTE);
                        if (actionPath != null) {
                            pathText.setText(actionPath);
                            validate();
                        }
                    }
                }
            }

        });

        validate();

        return c;
    }
}
