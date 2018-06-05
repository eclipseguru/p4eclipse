/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.preferences.decorators;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import com.perforce.team.ui.dialogs.PerforceDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class VariablesDialog extends PerforceDialog {

    private String[] variables = new String[0];
    private String[] descriptions = new String[0];
    private String[] selected = new String[0];
    private Composite displayArea;
    private Label varLabel;
    private CheckboxTableViewer varTable;

    /**
     * @param parent
     * @param variables
     * @param descriptions
     */
    public VariablesDialog(Shell parent, String[] variables,
            String[] descriptions) {
        super(parent, Messages.VariablesDialog_AddVariables);
        setModalResizeStyle();
        if (variables != null && descriptions != null
                && variables.length == descriptions.length) {
            this.variables = variables;
            this.descriptions = new String[descriptions.length];
            for (int i = 0; i < this.variables.length; i++) {
                this.descriptions[i] = this.variables[i] + " - " //$NON-NLS-1$
                        + descriptions[i];
            }
        }
    }

    /**
     * Get available variables
     * 
     * @return - array of available variables
     */
    public String[] getAvailableVariables() {
        return this.variables;
    }

    /**
     * Get the variable descriptions
     * 
     * @return - variable description array
     */
    public String[] getDescriptions() {
        return this.descriptions;
    }

    /**
     * Get the selected variables
     * 
     * @return - selected variable
     */
    public String[] getSelectedVariables() {
        return this.selected;
    }

    /**
     * Checkbox table viewer being displayed
     * 
     * @return - table viewer
     */
    public CheckboxTableViewer getViewer() {
        return this.varTable;
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
        varLabel = new Label(displayArea, SWT.LEFT);
        varLabel.setText(Messages.VariablesDialog_SelectVariablesToAdd);
        varTable = CheckboxTableViewer
                .newCheckList(displayArea, SWT.SINGLE | SWT.FULL_SELECTION
                        | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        varTable.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                List<String> selected = new ArrayList<String>();
                for (TableItem item : varTable.getTable().getItems()) {
                    if (item.getChecked()) {
                        selected.add(variables[varTable.getTable()
                                .indexOf(item)]);
                    }
                }
                VariablesDialog.this.selected = selected.toArray(new String[0]);
            }
        });
        varTable.getTable().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        varTable.setContentProvider(new ArrayContentProvider());
        varTable.setLabelProvider(new LabelProvider());
        varTable.setInput(this.descriptions);
        return c;
    }
}
