/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.dialogs;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.ui.P4ConnectionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4FormDialog extends P4StatusDialog {

    /**
     * Dialog change type enumeration
     */
    public enum Type {

        /**
         * Create type
         */
        CREATE,

        /**
         * Edit type
         */
        EDIT
    }

    /**
     * Has this dialog been modified?
     */
    protected boolean modified = false;

    private Type type = Type.CREATE;

    /**
     * @param parent
     */
    public P4FormDialog(Shell parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param title
     */
    public P4FormDialog(Shell parent, String title) {
        super(parent);
    }

    /**
     * Set the type
     * 
     * @param type
     */
    public void setType(Type type) {
        if (type != null) {
            this.type = type;
        }
    }

    /**
     * Get the type
     * 
     * @return - type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Save the dialog
     * 
     * @return - true if save succeed, false otherwise
     */
    public abstract boolean save();

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (this.type == Type.CREATE) {
            okStatusButton = createButton(parent, IDialogConstants.OK_ID,
                    Messages.P4FormDialog_Create, true);
        } else if (this.type == Type.EDIT) {
            okStatusButton = createButton(parent, IDialogConstants.OK_ID,
                    Messages.P4FormDialog_Save, true);
        }
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Cancel the dialog
     * 
     * @return - true if cancelling should occur, false to deny cancel
     */
    public boolean cancel() {
        if (isModified()) {
            String title = ""; //$NON-NLS-1$
            String message = ""; //$NON-NLS-1$
            switch (this.type) {
            case CREATE:
                title = MessageFormat.format(
                        Messages.P4FormDialog_CancelCreationTitle,
                        getModelLabel());
                message = MessageFormat.format(
                        Messages.P4FormDialog_CancelCreationMessage,
                        getModelLabel());
                break;
            case EDIT:
                title = MessageFormat.format(
                        Messages.P4FormDialog_CancelChangesTitle,
                        getModelLabel());
                message = MessageFormat.format(
                        Messages.P4FormDialog_CancelChangesMessage,
                        getModelLabel());
                break;
            }

            return P4ConnectionManager.getManager().openQuestion(getShell(),
                    title, message);
        } else {
            return true;
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
     */
    @Override
    protected void cancelPressed() {
        if (cancel()) {
            super.cancelPressed();
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (save()) {
            super.okPressed();
        }
    }

    /**
     * @see org.eclipse.jface.window.Window#handleShellCloseEvent()
     */
    @Override
    protected void handleShellCloseEvent() {
        if (cancel()) {
            super.handleShellCloseEvent();
        }
    }

    /**
     * Get model name label
     * 
     * @return - non-null label for this model element
     */
    protected abstract String getModelLabel();

    /**
     * Has the dialog been modified?
     * 
     * @return - true if modified, false otherwise
     */
    protected boolean isModified() {
        return this.modified;
    }
}
