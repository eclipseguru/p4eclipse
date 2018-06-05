package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Change file type dialog
 */
public class FileTypeDialog extends PerforceDialog {

    // Base file type choices
    private Button textType;
    private Button binaryType;
    private Button symlinkType;
    private Button resourceType;
    private Button appleType;
    private Button unicodeType;

    // File modifier choices
    private Button execModifier;
    private Button writeableModifier;
    private Button keywordModifier;
    private Button onlyModifier;
    private Button preserveModifier;
    private Button multipleModifier;

    // Storage choices
    private Button defaultStorage;
    private Button compressedStorage;
    private Button deltaStorage;
    private Button fullStorage;
    private Button singleStorage;

    // Filetype in perforce switch format
    private String fileType;

    /**
     * Constructor.
     * 
     * @param parent
     *            the parent window
     * @param fileType
     *            the existing type of the file
     */
    public FileTypeDialog(Shell parent, String fileType) {
        super(parent, Messages.FileTypeDialog_ChangeFileType);
        this.fileType = fileType;
    }

    /**
     * Get the type of the file
     * 
     * @return the type of the file
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * OK button pressed.
     */
    @Override
    protected void okPressed() {
        this.fileType = getSelectedFileType();
        super.okPressed();
    }

    /**
     * Create dialog box controls.
     * 
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(dialogArea, 1);

        createTypeGroup(composite);
        createModifiersGroup(composite);
        createStorageGroup(composite);

        if (fileType.indexOf('+') == -1) {
            setOldTypeSwitches(fileType);
        } else {
            setNewTypeSwitches(fileType);
        }

        return dialogArea;
    }

    /**
     * Create base file type controls.
     */
    private void createTypeGroup(Composite parent) {
        Group group = createGroup(parent, Messages.FileTypeDialog_BaseFileType,
                6);

        textType = createRadio(group, Messages.FileTypeDialog_Text);
        binaryType = createRadio(group, Messages.FileTypeDialog_Binary);
        symlinkType = createRadio(group, Messages.FileTypeDialog_Symlink);
        unicodeType = createRadio(group, Messages.FileTypeDialog_Unicode);
        resourceType = createRadio(group, Messages.FileTypeDialog_Resource);
        appleType = createRadio(group, Messages.FileTypeDialog_Apple);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(textType, IHelpContextIds.FILE_TYPE_TEXT);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(binaryType, IHelpContextIds.FILE_TYPE_BINARY);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(symlinkType, IHelpContextIds.FILE_TYPE_SYMLINK);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(unicodeType, IHelpContextIds.FILE_TYPE_UNICODE);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(resourceType, IHelpContextIds.FILE_TYPE_RESOURCE);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(appleType, IHelpContextIds.FILE_TYPE_APPLE);
    }

    /**
     * Create file type modifiers controls.
     */
    private void createModifiersGroup(Composite parent) {
        Group group = createGroup(parent,
                Messages.FileTypeDialog_FileTypeModifiers, 4);

        execModifier = createCheck(group, "+x"); //$NON-NLS-1$
        createMarginLabel(group, Messages.FileTypeDialog_ExecBitSetOnClient, 3);

        writeableModifier = createCheck(group, "+w"); //$NON-NLS-1$
        createMarginLabel(group,
                Messages.FileTypeDialog_AlwaysWritableOnClient, 3);

        keywordModifier = createCheck(group, "+k"); //$NON-NLS-1$
        keywordModifier.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!keywordModifier.getSelection()) {
                    onlyModifier.setSelection(false);
                }
            }
        });
        createMarginLabel(group, Messages.FileTypeDialog_KeywordExpansion, 1);

        onlyModifier = createCheck(group, "+o"); //$NON-NLS-1$
        ((GridData) onlyModifier.getLayoutData()).horizontalIndent = 10;
        onlyModifier.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (onlyModifier.getSelection()) {
                    keywordModifier.setSelection(true);
                }
            }
        });
        createMarginLabel(group, Messages.FileTypeDialog_OnlyIdAndHeader, 1);

        preserveModifier = createCheck(group, "+m"); //$NON-NLS-1$
        createMarginLabel(group, Messages.FileTypeDialog_PreserveModeTimes, 3);

        multipleModifier = createCheck(group, "+l"); //$NON-NLS-1$
        createMarginLabel(group, Messages.FileTypeDialog_DisallowMultipleOpens,
                3);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(execModifier, IHelpContextIds.FILE_TYPE_EXEC);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(writeableModifier, IHelpContextIds.FILE_TYPE_WRITEABLE);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(keywordModifier, IHelpContextIds.FILE_TYPE_KEYWORD);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(preserveModifier, IHelpContextIds.FILE_TYPE_PRESERVE);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(multipleModifier, IHelpContextIds.FILE_TYPE_MULTIPLE);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(onlyModifier, IHelpContextIds.FILE_TYPE_ONLY);
    }

    /**
     * Create storage type controls.
     */
    private void createStorageGroup(Composite parent) {
        Group group = createGroup(parent,
                Messages.FileTypeDialog_ServerStorageMethod, 2);

        defaultStorage = createRadio(group,
                Messages.FileTypeDialog_ServerStoresDefaultMethod);
        ((GridData) defaultStorage.getLayoutData()).horizontalSpan = 2;

        compressedStorage = createRadio(group, "+C"); //$NON-NLS-1$
        createMarginLabel(group,
                Messages.FileTypeDialog_ServerStoresCompressedRevs, 1);

        deltaStorage = createRadio(group, "+D"); //$NON-NLS-1$
        createMarginLabel(group, Messages.FileTypeDialog_ServerStoresRCSDeltas,
                1);

        fullStorage = createRadio(group, "+F"); //$NON-NLS-1$
        createMarginLabel(group, Messages.FileTypeDialog_ServerStoresFullRevs,
                1);

        singleStorage = createRadio(group, "+S"); //$NON-NLS-1$
        createMarginLabel(group,
                Messages.FileTypeDialog_ServerStoresOnlyHeadRev, 1);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(defaultStorage, IHelpContextIds.FILE_TYPE_DEFAULT);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(compressedStorage,
                        IHelpContextIds.FILE_TYPE_COMPRESSED);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(deltaStorage, IHelpContextIds.FILE_TYPE_DELTA);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(fullStorage, IHelpContextIds.FILE_TYPE_FULL);
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(singleStorage, IHelpContextIds.FILE_TYPE_SINGLE);
    }

    /**
     * Initialise controls from Perforce new file type format
     */
    private void setNewTypeSwitches(String type) {
        int idx = type.indexOf('+');
        String baseType = type.substring(0, idx);
        String modifiers = type.substring(idx + 1);

        if (baseType.equals("text")) { //$NON-NLS-1$
            textType.setSelection(true);
        } else if (baseType.equals("binary")) { //$NON-NLS-1$
            binaryType.setSelection(true);
        } else if (baseType.equals("symlink")) { //$NON-NLS-1$
            symlinkType.setSelection(true);
        } else if (baseType.equals("resource")) { //$NON-NLS-1$
            resourceType.setSelection(true);
        } else if (baseType.equals("apple")) { //$NON-NLS-1$
            appleType.setSelection(true);
        } else {
            unicodeType.setSelection(true);
        }

        if (modifiers.indexOf('C') != -1) {
            compressedStorage.setSelection(true);
        } else if (modifiers.indexOf('D') != -1) {
            deltaStorage.setSelection(true);
        } else if (modifiers.indexOf('F') != -1) {
            fullStorage.setSelection(true);
        } else if (modifiers.indexOf('S') != -1) {
            singleStorage.setSelection(true);
        } else {
            defaultStorage.setSelection(true);
        }

        if (modifiers.indexOf('x') != -1) {
            execModifier.setSelection(true);
        }
        if (modifiers.indexOf('w') != -1) {
            writeableModifier.setSelection(true);
        }
        if (modifiers.indexOf('k') != -1) {
            keywordModifier.setSelection(true);
        }
        if (modifiers.indexOf('o') != -1) {
            onlyModifier.setSelection(true);
        }
        if (modifiers.indexOf('m') != -1) {
            preserveModifier.setSelection(true);
        }
        if (modifiers.indexOf('l') != -1) {
            multipleModifier.setSelection(true);
        }
    }

    /**
     * Initialise controls from Perforce old file type format
     */
    private void setOldTypeSwitches(String type) {
        if (type.equals("text") || type.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
            textType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("binary")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("symlink")) { //$NON-NLS-1$
            symlinkType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("resource")) { //$NON-NLS-1$
            resourceType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("apple")) { //$NON-NLS-1$
            appleType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("unicode")) { //$NON-NLS-1$
            unicodeType.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("ctext")) { //$NON-NLS-1$
            textType.setSelection(true);
            compressedStorage.setSelection(true);
        } else if (type.equals("cxtext")) { //$NON-NLS-1$
            textType.setSelection(true);
            execModifier.setSelection(true);
            compressedStorage.setSelection(true);
        } else if (type.equals("ktext")) { //$NON-NLS-1$
            textType.setSelection(true);
            keywordModifier.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("kxtext")) { //$NON-NLS-1$
            textType.setSelection(true);
            keywordModifier.setSelection(true);
            execModifier.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("ltext")) { //$NON-NLS-1$
            textType.setSelection(true);
            fullStorage.setSelection(true);
        } else if (type.equals("tempobj")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            writeableModifier.setSelection(true);
            singleStorage.setSelection(true);
        } else if (type.equals("ubinary")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            fullStorage.setSelection(true);
        } else if (type.equals("uresource")) { //$NON-NLS-1$
            resourceType.setSelection(true);
            fullStorage.setSelection(true);
        } else if (type.equals("uxbinary")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            execModifier.setSelection(true);
            fullStorage.setSelection(true);
        } else if (type.equals("xbinary")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            execModifier.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("xltext")) { //$NON-NLS-1$
            textType.setSelection(true);
            execModifier.setSelection(true);
            fullStorage.setSelection(true);
        } else if (type.equals("xtempobj")) { //$NON-NLS-1$
            binaryType.setSelection(true);
            writeableModifier.setSelection(true);
            execModifier.setSelection(true);
            singleStorage.setSelection(true);
        } else if (type.equals("xtext")) { //$NON-NLS-1$
            textType.setSelection(true);
            execModifier.setSelection(true);
            defaultStorage.setSelection(true);
        } else if (type.equals("xunicode")) { //$NON-NLS-1$
            unicodeType.setSelection(true);
            execModifier.setSelection(true);
            defaultStorage.setSelection(true);
        }
    }

    /**
     * Gets the string representation of the file type options currently
     * selected from the dialog
     * 
     * @return - string representation of the current UI selection
     */
    public String getSelectedFileType() {
        String type = null;
        if (textType.getSelection()) {
            type = "text"; //$NON-NLS-1$
        } else if (binaryType.getSelection()) {
            type = "binary"; //$NON-NLS-1$
        } else if (symlinkType.getSelection()) {
            type = "symlink"; //$NON-NLS-1$
        } else if (resourceType.getSelection()) {
            type = "resource"; //$NON-NLS-1$
        } else if (appleType.getSelection()) {
            type = "apple"; //$NON-NLS-1$
        } else {
            type = "unicode"; //$NON-NLS-1$
        }

        String typeModifiers = ""; //$NON-NLS-1$

        if (compressedStorage.getSelection()) {
            typeModifiers += "C"; //$NON-NLS-1$
        } else if (deltaStorage.getSelection()) {
            typeModifiers += "D"; //$NON-NLS-1$
        } else if (fullStorage.getSelection()) {
            typeModifiers += "F"; //$NON-NLS-1$
        } else if (singleStorage.getSelection()) {
            typeModifiers += "S"; //$NON-NLS-1$
        }

        if (execModifier.getSelection()) {
            typeModifiers += "x"; //$NON-NLS-1$
        }
        if (writeableModifier.getSelection()) {
            typeModifiers += "w"; //$NON-NLS-1$
        }
        if (keywordModifier.getSelection()) {
            typeModifiers += "k"; //$NON-NLS-1$
        }
        if (onlyModifier.getSelection()) {
            typeModifiers += "o"; //$NON-NLS-1$
        }
        if (preserveModifier.getSelection()) {
            typeModifiers += "m"; //$NON-NLS-1$
        }
        if (multipleModifier.getSelection()) {
            typeModifiers += "l"; //$NON-NLS-1$
        }

        if (typeModifiers.length() > 0) {
            type += "+" + typeModifiers; //$NON-NLS-1$
        }
        return type;
    }

    /**
     * Create label with a slight bottom margin. This is so that label are
     * aligned with radio buttons and check boxes.
     */
    private void createMarginLabel(Composite parent, String title, int span) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gd = new GridData();
        gd.horizontalSpan = span;
        composite.setLayoutData(gd);

        RowLayout row = new RowLayout();
        row.marginLeft = 0;
        row.marginRight = 0;
        row.marginTop = 0;
        row.marginBottom = 2;
        composite.setLayout(row);

        new Label(composite, SWT.NONE).setText(title);
    }
}
