/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.OpenDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UnshelveDialog extends OpenDialog {

    /**
     * FORCE_WRITABLE
     */
    public static final String FORCE_WRITABLE = "com.perforce.team.ui.shelve.FORCE_WRITABLE"; //$NON-NLS-1$

    /**
     * UNSHELVE_DEFAULT_DESCRIPTION
     */
    public static final String UNSHELVE_DEFAULT_DESCRIPTION = Messages.UnshelveDialog_UnshelveDefaultDescription;

    private boolean forceWritable;
    private boolean revert = false;

    private Button forceButton;
    private Button revertButton;

    /**
     * @param parent
     * @param resources
     * @param connection
     * @param selectedId
     * @param dialogTitle
     */
    public UnshelveDialog(Shell parent, IP4Resource[] resources,
            IP4Connection connection, int selectedId, String dialogTitle) {
        super(parent, resources, connection, selectedId, dialogTitle,
                Messages.UnshelveDialog_AddToPendingChangelist,
                UNSHELVE_DEFAULT_DESCRIPTION);
    }

    /**
     * Is revert options selected
     * 
     * @return - true to revert, false otherwise
     */
    public boolean isRevert() {
        return revert;
    }

    /**
     * Is force writable selected?
     * 
     * @return - true if selected, false otherwise
     */
    public boolean isForceWritable() {
        return this.forceWritable;
    }

    /**
     * @see com.perforce.team.ui.p4java.dialogs.OpenDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        Composite displayArea = new Composite(c, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        forceButton = new Button(displayArea, SWT.CHECK);
        forceButton
                .setText(Messages.UnshelveDialog_OverwriteFilesCurrentlyWriteable);
        forceWritable = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(FORCE_WRITABLE);
        forceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                forceWritable = forceButton.getSelection();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(FORCE_WRITABLE, forceWritable);
            }

        });
        forceButton.setSelection(forceWritable);

        revertButton = new Button(displayArea, SWT.CHECK);
        revertButton
                .setText(Messages.UnshelveDialog_OverwriteFilesCurrentlyWriteable);
        revertButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                revert = revertButton.getSelection();
            }

        });
        revertButton
                .setText(Messages.UnshelveDialog_RevertChangesBeforeUnshelving);

        return c;
    }

}
