/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.DescriptionTemplate;
import com.perforce.team.ui.dialogs.P4StatusDialog;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.util.List;

import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DescriptionTemplatesPreferencePage extends PreferencePage
        implements IWorkbenchPreferencePage {

    /**
     * ID - preference page id
     */
    public static final String ID = "com.perforce.team.ui.dialogs.DescriptionTemplatesPreferencePage"; //$NON-NLS-1$

    private static class EditDescriptionDialog extends P4StatusDialog {

        private TextViewer viewer;
        private String content = null;

        /**
         * @param parent
         * @param title
         * @param content
         */
        public EditDescriptionDialog(Shell parent, String title, String content) {
            super(parent, title);
            this.content = content;
            setModalResizeStyle();
        }

        /**
         * @param parent
         * @param title
         */
        public EditDescriptionDialog(Shell parent, String title) {
            this(parent, title, null);
        }

        /**
         * @see org.eclipse.jface.dialogs.Dialog#okPressed()
         */
        @Override
        protected void okPressed() {
            content = viewer.getDocument().get();
            super.okPressed();
        }

        /**
         * Get content
         * 
         * @return - content
         */
        String getContent() {
            return this.content;
        }

        /**
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected Control createDialogArea(Composite parent) {
            Composite c = (Composite) super.createDialogArea(parent);
            Label templateLabel = new Label(c, SWT.LEFT);
            templateLabel
                    .setText(Messages.DescriptionTemplatesPreferencePage_ChangelistDescriptionTemplate);
            viewer = new TextViewer(c, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL
                    | SWT.H_SCROLL);
            GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
            vData.heightHint = Math.max(100, convertHeightInCharsToPixels(15));
            vData.widthHint = Math.max(300, convertWidthInCharsToPixels(80));
            viewer.getTextWidget().setLayoutData(vData);
            viewer.setDocument(new Document());
            if (content != null) {
                viewer.getDocument().set(content);
            }
            viewer.addTextListener(new ITextListener() {

                public void textChanged(TextEvent event) {
                    validate();
                }
            });

            validate();
            return c;
        }

        private void validate() {
            if (viewer.getDocument().get().trim().length() == 0) {
                setErrorMessage(
                        Messages.DescriptionTemplatesPreferencePage_ChangelistDescriptionCantBeEmpty,
                        null);
            } else {
                setErrorMessage(null);
            }
        }

        /**
         * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
         */
        @Override
        protected void createButtonsForButtonBar(Composite parent) {
            super.createButtonsForButtonBar(parent);
            viewer.getTextWidget().setFocus();
        }

    }

    private TableViewer descTable;
    private TextViewer previewText;
    private List<DescriptionTemplate> templates;

    private IntegerFieldEditor storeDescriptions;

    private void createDescriptionArea(Composite parent) {
        Composite descArea = new Composite(parent, SWT.NONE);
        descArea.setLayout(new GridLayout(2, false));
        descArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData twoColData = new GridData(SWT.FILL, SWT.FILL, true, false);
        twoColData.horizontalSpan = 2;

        PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
        final Image newImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        final Image editImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_EDIT).createImage();
        final Image removeImage = plugin.getImageDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();

        descArea.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                newImage.dispose();
                editImage.dispose();
                removeImage.dispose();
            }
        });

        Label descLabel = new Label(descArea, SWT.LEFT);
        descLabel
                .setText(Messages.DescriptionTemplatesPreferencePage_CreateEditRemoveChangelistDescriptionTemplates);
        descLabel.setLayoutData(twoColData);

        descTable = new TableViewer(descArea, SWT.SINGLE | SWT.BORDER
                | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        descTable.setContentProvider(new ArrayContentProvider());
        descTable.setLabelProvider(new WorkbenchLabelProvider());
        descTable.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        templates = DescriptionTemplate.getTemplates();
        descTable.setInput(templates);

        ToolBar buttonBar = new ToolBar(descArea, SWT.FLAT | SWT.VERTICAL);
        buttonBar.setLayoutData(new GridData(SWT.END, SWT.NONE, false, false));
        ToolItem newItem = new ToolItem(buttonBar, SWT.PUSH);
        newItem.setToolTipText(Messages.DescriptionTemplatesPreferencePage_AddNewChangelistDescriptionTemplate);
        newItem.setImage(newImage);
        newItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                EditDescriptionDialog dialog = new EditDescriptionDialog(
                        getShell(),
                        Messages.DescriptionTemplatesPreferencePage_AddChangelistDescriptionTemplate);
                if (EditDescriptionDialog.OK == dialog.open()) {
                    templates.add(new DescriptionTemplate(dialog.content));
                    descTable.refresh();
                    updatePreview();
                }
            }

        });

        ToolItem editItem = new ToolItem(buttonBar, SWT.PUSH);
        editItem.setToolTipText(Messages.DescriptionTemplatesPreferencePage_EditSelectedChangelistDescriptionTemplate);
        editItem.setImage(editImage);
        editItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                edit(descTable.getSelection());
            }

        });

        ToolItem removeItem = new ToolItem(buttonBar, SWT.PUSH);
        removeItem
                .setToolTipText(Messages.DescriptionTemplatesPreferencePage_RemoveSelectedChangelistDescriptionTemplate);
        removeItem.setImage(removeImage);
        removeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                Object selected = ((IStructuredSelection) descTable
                        .getSelection()).getFirstElement();
                if (selected instanceof DescriptionTemplate) {
                    templates.remove(selected);
                    descTable.refresh();
                    updatePreview();
                }
            }

        });

        Label previewLabel = new Label(descArea, SWT.LEFT);
        previewLabel
                .setText(Messages.DescriptionTemplatesPreferencePage_Preview);
        previewLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        previewLabel.setLayoutData(twoColData);

        previewText = new TextViewer(descArea, SWT.BORDER | SWT.READ_ONLY
                | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        previewText.setDocument(new Document());
        GridData ptData = new GridData(SWT.FILL, SWT.FILL, true, true);
        ptData.horizontalSpan = 2;
        previewText.getTextWidget().setLayoutData(ptData);

        descTable.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                Object selected = ((IStructuredSelection) descTable
                        .getSelection()).getFirstElement();
                if (selected instanceof DescriptionTemplate) {
                    previewText.getDocument().set(
                            ((DescriptionTemplate) selected).getContent());
                } else {
                    previewText.getDocument().set(""); //$NON-NLS-1$
                }
            }
        });
        descTable.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                edit(descTable.getSelection());
            }
        });
    }

    private void edit(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object selected = ((IStructuredSelection) selection)
                    .getFirstElement();
            if (selected instanceof DescriptionTemplate) {
                DescriptionTemplate tpl = (DescriptionTemplate) selected;
                EditDescriptionDialog dialog = new EditDescriptionDialog(
                        getShell(),
                        Messages.DescriptionTemplatesPreferencePage_EditChangelistDescriptionTemplate,
                        tpl.getContent());
                if (EditDescriptionDialog.OK == dialog.open()) {
                    tpl.setContent(dialog.getContent());
                    descTable.refresh();
                    updatePreview();
                }
            }
        }
    }

    private void createOptionsArea(Composite parent) {
        Composite optionsArea = new Composite(parent, SWT.NONE);
        GridLayout oaLayout = new GridLayout(1, true);
        oaLayout.marginHeight = 0;
        oaLayout.marginWidth = 0;
        optionsArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        storeDescriptions = new IntegerFieldEditor(
                IPreferenceConstants.CHANGELISTS_SAVED,
                Messages.DescriptionTemplatesPreferencePage_MaxNumberOfChangelistsInHistory,
                optionsArea);
        storeDescriptions.setPreferenceStore(PerforceUIPlugin.getPlugin()
                .getPreferenceStore());
        storeDescriptions.load();
    }

    private void updatePreview() {
        Object selected = ((IStructuredSelection) descTable.getSelection())
                .getFirstElement();
        if (selected instanceof DescriptionTemplate) {
            previewText.getDocument().set(
                    ((DescriptionTemplate) selected).getContent());
        } else {
            previewText.getDocument().set(""); //$NON-NLS-1$
        }
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
     */
    @Override
    protected void performDefaults() {
        storeDescriptions.loadDefault();
        templates.clear();
        descTable.refresh();
        super.performDefaults();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#performOk()
     */
    @Override
    public boolean performOk() {
        storeDescriptions.store();
        DescriptionTemplate.saveTemplates(templates);
        return super.performOk();
    }

    /**
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createOptionsArea(displayArea);
        createDescriptionArea(displayArea);
        return displayArea;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {

    }

}
