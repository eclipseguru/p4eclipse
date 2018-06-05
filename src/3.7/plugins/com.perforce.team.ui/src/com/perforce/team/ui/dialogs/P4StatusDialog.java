/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.IErrorDisplay;
import com.perforce.team.ui.IErrorProvider;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4StatusDialog extends StatusDialog implements
        IErrorDisplay {

    /**
     * Ok status button since {@link StatusDialog} ok button is private
     */
    protected Button okStatusButton;

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#updateButtonsEnableState(org.eclipse.core.runtime.IStatus)
     */
    @Override
    protected void updateButtonsEnableState(IStatus status) {
        if (okStatusButton != null && !okStatusButton.isDisposed()) {
            okStatusButton.setEnabled(!status.matches(IStatus.ERROR));
        } else {
            super.updateButtonsEnableState(status);
        }
    }

    /**
     * @param parent
     */
    public P4StatusDialog(Shell parent) {
        super(parent);
    }

    /**
     * @param parent
     * @param title
     */
    public P4StatusDialog(Shell parent, String title) {
        this(parent);
        setTitle(title);
    }

    /**
     * Get a non-null but possibly empty array containing only the objects from
     * the specified array if they are an {@link IP4File} or
     * {@link IP4ShelveFile}.
     * 
     * @param resources
     * @return - array of files
     */
    protected IP4Resource[] getFiles(IP4Resource[] resources) {
        List<IP4Resource> files = new ArrayList<IP4Resource>();
        if (resources != null) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4File
                        || resource instanceof IP4ShelveFile) {
                    files.add(resource);
                }
            }
        }
        return files.toArray(new IP4Resource[files.size()]);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getDialogBoundsSettings()
     */
    @Override
    protected IDialogSettings getDialogBoundsSettings() {
        IDialogSettings settings = PerforceUIPlugin.getPlugin()
                .getDialogSettings();
        if (settings != null) {
            settings = getCustomDialogSettings(settings);
            if (settings != null) {
                return settings;
            }
        }
        return super.getDialogBoundsSettings();
    }

    /**
     * Get the custom dialog settings by getting the setting name from
     * {@link #getSectionName()} and then checking if the section exists and if
     * it doesn't it will create it.
     * 
     * @param parent
     * @return - settings
     */
    protected IDialogSettings getCustomDialogSettings(IDialogSettings parent) {
        String name = getSectionName();
        if (name != null) {
            IDialogSettings settings = parent.getSection(name);
            if (settings == null) {
                settings = parent.addNewSection(name);
            }
            return settings;
        }
        return null;
    }

    /**
     * Get the unique section name for the dialog. Should be overriden by
     * subclasses.
     * 
     * @return - unique settings section name
     */
    protected String getSectionName() {
        return null;
    }

    /**
     * Set shell size to be modal and resizable
     */
    protected void setModalResizeStyle() {
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#getErrorMessage()
     */
    public String getErrorMessage() {
        IStatus status = getStatus();
        if (status != null && status.getSeverity() == IStatus.ERROR) {
            return status.getMessage();
        }
        return null;
    }

    /**
     * Set the message to display in the status area
     * 
     * @param severity
     * @param message
     */
    public void setMessage(int severity, String message) {
        IStatus status = null;
        if (message != null) {
            status = new Status(severity, PerforceUIPlugin.ID, Status.OK,
                    message, null);
        } else {
            status = Status.OK_STATUS;
        }
        updateStatus(status);
    }

    /**
     * @see com.perforce.team.ui.IErrorDisplay#setErrorMessage(java.lang.String,
     *      com.perforce.team.ui.IErrorProvider)
     */
    public void setErrorMessage(String message, IErrorProvider provider) {
        setMessage(IStatus.ERROR, message);
    }

    /**
     * Set the error message
     * 
     * @param message
     * @see #setErrorMessage(String, IErrorProvider)
     */
    public void setErrorMessage(String message) {
        setErrorMessage(message, null);
    }

    /**
     * Set the info message to display in the status area
     * 
     * @param message
     */
    public void setInfoMessage(String message) {
        setMessage(IStatus.INFO, message);
    }

    public void addFormNameValidation(final Text text, final String formTypeName) {
        final ControlDecoration textDecorator = createDecorator(text);
        textDecorator.setMarginWidth(2);

        text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                if (text.getEnabled() == true) {
                    String name = text.getText();
                    String error = P4UIUtils.validateName(name, formTypeName);

                    if (error != null) {
                        textDecorator.setDescriptionText(MessageFormat.format(
                                error, formTypeName));
                        textDecorator.show();
                        setErrorMessage(MessageFormat.format(
                                "Illegal {0} name.", formTypeName));
                    } else {
                        textDecorator.hide();
                        setErrorMessage(null);
                    }

                } else {
                    textDecorator.hide();
                    setErrorMessage(null);
                }
            }
        });

    }

    public static ControlDecoration createDecorator(Text text) {
        ControlDecoration controlDecoration = new ControlDecoration(text,
                SWT.LEFT | SWT.TOP);
        FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault()
                .getFieldDecoration(FieldDecorationRegistry.DEC_ERROR);
        controlDecoration.setImage(fieldDecoration.getImage());
        controlDecoration.hide();
        return controlDecoration;
    }
}
