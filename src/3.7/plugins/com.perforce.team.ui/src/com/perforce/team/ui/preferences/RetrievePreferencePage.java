/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.PerforcePreferencesDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class RetrievePreferencePage extends PerforcePreferencesDialog {

    /**
     * Retrieve all elements radio button
     */
    protected Button allElementsRadio;

    /**
     * Retrieve maximum number of elements radio button
     */
    protected Button maxElementsRadio;

    /**
     * Text box to store maximum number of elements
     */
    protected Text maxElementsText;

    private boolean warningShown = false;

    /**
     * @param numColumns
     */
    public RetrievePreferencePage(int numColumns) {
        super(numColumns);
    }

    /**
     * Get the retrieve preference name
     * 
     * @return - name of retrieve preference
     */
    protected abstract String getRetrievePref();

    /**
     * Get the plural name of the elements retrieved
     * 
     * @return - plural name
     */
    protected abstract String getName();

    /**
     * @see com.perforce.team.ui.dialogs.PerforcePreferencesDialog#performOk()
     */
    @Override
    public boolean performOk() {
        int num;
        if (allElementsRadio.getSelection()) {
            num = -1;
        } else {
            num = Integer.parseInt(maxElementsText.getText().trim());
        }
        setPrefInt(getRetrievePref(), num);
        return true;
    }

    /**
     * Restore defaults
     */
    @Override
    protected void performDefaults() {
        initValues(getPrefDefInt(getRetrievePref()));
    }

    /**
     * Create the retrieve elements area
     * 
     * @param parent
     * @return - retrieve area
     */
    protected Composite createRetrieveArea(Composite parent) {
        Group group = DialogUtils.createGroup(parent, MessageFormat.format(
                Messages.RetrievePreferencePage_WhenRetrieving, getName()), 1);

        // Fix for job038212, use no focus button on windows since focus and
        // selection goes to the first radio button on Windows with Eclipse 3.5
        // due to code added that sets focus when a workbench preference dialog
        // is created with an initial selected page
        int buttonStyle = P4CoreUtils.isWindows() ? SWT.NO_FOCUS : SWT.NONE;

        allElementsRadio = DialogUtils.createRadio(group, buttonStyle,
                MessageFormat.format(
                        Messages.RetrievePreferencePage_RetrieveAllFromServer,
                        getName()));
        allElementsRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (!warningShown) {
                    P4ConnectionManager
                            .getManager()
                            .openWarning(
                                    allElementsRadio.getShell(),
                                    MessageFormat
                                            .format(Messages.RetrievePreferencePage_RetrieveAllWarningTitle,
                                                    getName()),
                                    MessageFormat
                                            .format(Messages.RetrievePreferencePage_RetrieveAllWarningMessage,
                                                    getName()));
                    warningShown = true;
                }
            }

        });

        Composite row = new Composite(group, 0);
        row.setLayoutData(new GridData());
        RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.marginLeft = 0;
        row.setLayout(layout);

        maxElementsRadio = new Button(row, SWT.RADIO | buttonStyle);
        maxElementsRadio.setText(Messages.RetrievePreferencePage_RetrieveMaxOf);
        maxElementsRadio.setLayoutData(new RowData());

        maxElementsText = new Text(row, SWT.SINGLE | SWT.BORDER);
        RowData data = new RowData();
        data.width = IDialogConstants.ENTRY_FIELD_WIDTH / 4;
        maxElementsText.setLayoutData(data);

        Label label = new Label(row, SWT.LEFT);
        label.setText(MessageFormat.format(
                Messages.RetrievePreferencePage_NumFromServer, getName()));

        allElementsRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                maxElementsRadio.setSelection(false);
                maxElementsText.setEnabled(false);
                setValid(true);
            }
        });
        maxElementsRadio.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                allElementsRadio.setSelection(false);
                maxElementsText.setEnabled(true);
            }
        });
        maxElementsText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                try {
                    int num = Integer
                            .parseInt(maxElementsText.getText().trim());
                    if (num > 0) {
                        setValid(true);
                        return;
                    }
                } catch (NumberFormatException e) {
                }
                setValid(false);
            }
        });

        initValues(getPrefInt(getRetrievePref()));

        return group;
    }

    private void initValues(int num) {
        if (num < 0) {
            maxElementsRadio.setSelection(false);
            allElementsRadio.setSelection(true);
            maxElementsText.setText("100"); //$NON-NLS-1$
            maxElementsText.setEnabled(false);
        } else {
            allElementsRadio.setSelection(false);
            maxElementsRadio.setSelection(true);
            maxElementsText.setText(Integer.toString(num));
            maxElementsText.setEnabled(true);
        }
    }

    /**
     * Select the all elements button and de-selects the max elements button and
     * disables max elements text field
     */
    public void selectAllElements() {
        if (allElementsRadio != null && !allElementsRadio.isDisposed()) {
            allElementsRadio.setSelection(true);
            maxElementsRadio.setSelection(false);
            maxElementsText.setEnabled(false);
        }
    }

    /**
     * Sets the max elements field with specified string
     * 
     * @param maxElements
     */
    public void setMaxElements(String maxElements) {
        if (maxElements != null && maxElementsText != null
                && !maxElementsText.isDisposed()) {
            maxElementsText.setText(maxElements);
        }
    }

    /**
     * Selects the max elements button and de-selects the all elements button
     * and enables the max elements text field
     */
    public void selectMaxElements() {
        if (maxElementsRadio != null && !maxElementsRadio.isDisposed()) {
            maxElementsRadio.setSelection(true);
            allElementsRadio.setSelection(false);
            maxElementsText.setEnabled(true);
        }
    }

}
