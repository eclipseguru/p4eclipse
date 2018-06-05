/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FileShelfDialog extends P4StatusDialog {

    private FileShelfWidget widget;
    private PendingCombo combo;

    private IP4File file;
    private IP4ShelveFile[] shelves;
    private IP4ShelveFile selected;
    private boolean revert = false;
    private boolean forceWritable = false;
    private int changelist = -1;
    private String description = null;

    /**
     * @param parent
     * @param file
     * @param shelves
     */
    public FileShelfDialog(Shell parent, IP4File file, IP4ShelveFile[] shelves) {
        super(parent);
        setTitle(MessageFormat.format(Messages.FileShelfDialog_UnshelveFile,
                file.getName()));
        setModalResizeStyle();
        this.file = file;
        this.shelves = shelves;
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4StatusDialog#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return FileShelfWidget.SECTION_NAME;
    }

    /**
     * Revert file before unshelving?
     * 
     * @return - true to revert
     */
    public boolean revert() {
        return revert;
    }

    /**
     * Get changelist to unshelve file into
     * 
     * @return - pending changelist id
     */
    public int getChangelist() {
        return this.changelist;
    }

    /**
     * Get changelist description
     * 
     * @return - pending changelist description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Unshelve over writable files?
     * 
     * @return - true to unshelve over files already writable
     */
    public boolean overwrite() {
        return this.forceWritable;
    }

    /**
     * Get selected shelve file
     * 
     * @return - shelve file
     */
    public IP4ShelveFile getSelected() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.selected = widget.getSelectedFile();
        this.revert = widget.isRevert();
        this.changelist = combo.getSelected();
        this.description = combo.getDescription();
        super.okPressed();
    }

    private void validate() {
        if (widget.getViewer().getSelection().isEmpty()) {
            setErrorMessage(Messages.FileShelfDialog_MustSelectFileToUnshelve);
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

        combo = new PendingCombo(Messages.FileShelfDialog_AddToChangelist, file);
        combo.createControl(c, UnshelveDialog.UNSHELVE_DEFAULT_DESCRIPTION);

        widget = new FileShelfWidget(this.shelves, file, MessageFormat.format(
                Messages.FileShelfDialog_UnshelvingFile, file.getRemotePath()));
        widget.createControl(c);
        widget.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });
        widget.getViewer().addSelectionChangedListener(
                new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        validate();
                    }
                });

        final Button forceButton = new Button(c, SWT.CHECK);
        forceWritable = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(UnshelveDialog.FORCE_WRITABLE);
        forceButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                forceWritable = forceButton.getSelection();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(UnshelveDialog.FORCE_WRITABLE, forceWritable);
            }

        });
        forceButton.setSelection(forceWritable);
        forceButton
                .setText(Messages.FileShelfDialog_OverwriteFilesCurrentlyWriteable);

        validate();

        return c;
    }

}
