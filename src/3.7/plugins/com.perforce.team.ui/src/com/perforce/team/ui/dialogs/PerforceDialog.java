package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.ui.PerforceUIPlugin;

/**
 * Base perforce dialog
 */
public class PerforceDialog extends Dialog {

    /**
     * Dialog title
     */
    protected String title;

    /**
     * PerforceDialog
     * 
     * @param parent
     * @param title
     */
    public PerforceDialog(Shell parent, String title) {
        super(parent);
        this.title = title;
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

    protected Text createTextField(Composite parent, int width, boolean fill) {
        return DialogUtils.createTextField(parent, width, fill);
    }

    protected Button createButton(Composite parent, String text, int flags) {
        return DialogUtils.createButton(parent, text, flags);
    }

    protected Text createTextField(Composite parent) {
        return DialogUtils.createTextField(parent);
    }

    protected Combo createCombo(Composite parent, String[] items,
            boolean readonly) {
        return DialogUtils.createCombo(parent, items, readonly);
    }

    protected Label createLabel(Composite parent, String text) {
        return DialogUtils.createLabel(parent, text);
    }

    protected Label createBlank(Composite parent) {
        return DialogUtils.createBlank(parent);
    }

    protected List createList(Composite parent, int vspan, int height) {
        return DialogUtils.createList(parent, vspan, height);
    }

    protected List createList(Composite parent, int vspan, int height,
            int width, boolean multi) {
        return DialogUtils.createList(parent, vspan, height, width, multi);
    }

    protected Button createRadio(Composite parent, String text) {
        return DialogUtils.createRadio(parent, text);
    }

    protected Button createCheck(Composite parent, String text) {
        return DialogUtils.createCheck(parent, text);
    }

    protected Group createGroup(Composite parent, String title, int numcols) {
        return DialogUtils.createGroup(parent, title, numcols);
    }

    protected Composite createComposite(Composite parent) {
        return DialogUtils.createComposite(parent);
    }

    protected Composite createComposite(Composite parent, int numcols) {
        return DialogUtils.createComposite(parent, numcols);
    }

    protected Composite createComposite(Composite parent, int numcols, int flags) {
        return DialogUtils.createComposite(parent, numcols, flags);
    }

    protected Composite createTitledArea(Composite parent, int flags) {
        return DialogUtils.createTitledArea(parent, flags);
    }

    protected Text createTextEditor(Composite parent) {
        return DialogUtils.createTextEditor(parent);
    }

    protected TextViewer createTextViewer(Composite parent) {
        return DialogUtils.createTextViewer(parent);
    }

    protected TextViewer createTextViewer(Composite parent, int style) {
        return DialogUtils.createTextViewer(parent, style);
    }

    protected SashForm createSash(Composite parent) {
        return DialogUtils.createSash(parent);
    }

    /**
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }

    protected void setModalResizeStyle() {
        setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    /**
     * Submit the dialog by calling the protected okPressed method
     * 
     * @see #okPressed()
     */
    public void submit() {
        okPressed();
    }
}
