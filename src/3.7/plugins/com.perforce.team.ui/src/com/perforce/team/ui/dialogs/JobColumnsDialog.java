package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;

/**
 * Job Columns Dialog
 */
public class JobColumnsDialog extends PerforceDialog {

    private JobsDialog jobsList;

    // Buttons
    private Button addButton;
    private Button removeButton;
    private Button upButton;
    private Button downButton;

    // List
    private List availableList;
    private List showList;

    /**
     * Creates a new job columns dialog
     * 
     * @param parent
     * @param jobsList
     */
    public JobColumnsDialog(Shell parent, JobsDialog jobsList) {
        super(parent, Messages.JobColumnsDialog_SetJobViewColumns);
        this.jobsList = jobsList;
    }

    /**
     * OK button pressed.
     */
    @Override
    protected void okPressed() {
        jobsList.saveDisplayColumns(showList.getItems());
        super.okPressed();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(dialogArea, 3, GridData.FILL_BOTH);

        Composite hiddenGroup = createTitledArea(composite, GridData.FILL_BOTH);
        createLabel(hiddenGroup, "These columns may be added:"); //$NON-NLS-1$
        availableList = createList(hiddenGroup, 1, 300, 200, false);

        Composite buttonGroup = createTitledArea(composite, GridData.FILL_BOTH);
        createLabel(buttonGroup, ""); //$NON-NLS-1$
        addButton = createButton(buttonGroup, Messages.JobColumnsDialog_Add,
                GridData.FILL_HORIZONTAL);
        removeButton = createButton(buttonGroup,
                Messages.JobColumnsDialog_Remove, GridData.FILL_HORIZONTAL);
        upButton = createButton(buttonGroup, Messages.JobColumnsDialog_MoveUp,
                GridData.FILL_HORIZONTAL);
        downButton = createButton(buttonGroup,
                Messages.JobColumnsDialog_MoveDown, GridData.FILL_HORIZONTAL);

        Composite shownGroup = createTitledArea(composite, GridData.FILL_BOTH);
        createLabel(shownGroup, "Show these columns:"); //$NON-NLS-1$
        showList = createList(shownGroup, 1, 300, 200, false);

        initLists();
        addButton.setEnabled(false);
        removeButton.setEnabled(false);
        upButton.setEnabled(false);
        downButton.setEnabled(false);

        addEventListeners();

        return composite;
    }

    private void showListButtons() {
        int idx = showList.getSelectionIndex();
        int count = showList.getItemCount();
        addButton.setEnabled(false);
        if (idx > 0) {
            removeButton.setEnabled(true);
        } else {
            removeButton.setEnabled(false);
        }
        if (idx > 1 && idx < (count - 1)) {
            upButton.setEnabled(true);
            downButton.setEnabled(true);
        } else if (idx > 1) {
            upButton.setEnabled(true);
            downButton.setEnabled(false);
        } else if (idx > 0 && idx < (count - 1)) {
            upButton.setEnabled(false);
            downButton.setEnabled(true);
        } else {
            upButton.setEnabled(false);
            downButton.setEnabled(false);
        }
    }

    private void availableListButtons() {
        removeButton.setEnabled(false);
        addButton.setEnabled(true);
        upButton.setEnabled(false);
        downButton.setEnabled(false);
    }

    private void addEventListeners() {
        showList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                availableList.deselectAll();
                showListButtons();
            }
        });

        availableList.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showList.deselectAll();
                availableListButtons();
            }
        });

        addButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelection();
            }
        });

        removeButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelection();
            }
        });

        upButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = showList.getSelectionIndex();
                String item = showList.getItem(idx);
                showList.remove(idx);
                showList.add(item, idx - 1);
                showList.setSelection(idx - 1);
                showListButtons();
            }
        });

        downButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = showList.getSelectionIndex();
                String item = showList.getItem(idx);
                showList.remove(idx);
                showList.add(item, idx + 1);
                showList.setSelection(idx + 1);
                showListButtons();
            }
        });
    }

    private void initLists() {
        Map<String, String> showLookup = new HashMap<String, String>();

        String[] showNames = jobsList.getDisplayColumns();
        for (int i = 0; i < showNames.length; i++) {
            String name = showNames[i];
            showList.add(name);
            showLookup.put(name, name);
        }

        String[] fields = jobsList.getFieldNames();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i];
            if (showLookup.get(name) == null) {
                availableList.add(name);
            }
        }
    }

    /**
     * Selects the specified column from the show list
     * 
     * @param column
     *            - column label to select
     */
    public void selectShowColumn(String column) {
        showList.select(showList.indexOf(column));
        showListButtons();
    }

    /**
     * Selects the specified column from the available list
     * 
     * @param column
     *            - column label to select
     */
    public void selectAvailableColumn(String column) {
        availableList.select(availableList.indexOf(column));
        availableListButtons();
    }

    /**
     * Adds the current selection from the available list
     */
    public void addSelection() {
        int idx = availableList.getSelectionIndex();
        addColumn(availableList.getItem(idx));
    }

    /**
     * Removes the current selection from the show list
     */
    public void removeSelection() {
        int idx = showList.getSelectionIndex();
        removeColumn(showList.getItem(idx));
    }

    /**
     * Adds a column to the show list and removes it from the available list
     * 
     * @param column
     *            - string column label to add
     */
    public void addColumn(String column) {
        if (addButton.isEnabled()) {
            availableList.remove(column);
            showList.add(column);
            showList.setSelection(showList.getItemCount() - 1);
            showList.setFocus();
            showListButtons();
        }
    }

    /**
     * Removes a column from the show list and adds it to the available list
     * 
     * @param column
     *            - string column label to remove
     */
    public void removeColumn(String column) {
        if (removeButton.isEnabled()) {
            showList.remove(column);
            availableList.add(column);
            availableList.setSelection(availableList.getItemCount() - 1);
            availableList.setFocus();
            availableListButtons();
        }
    }

    /**
     * Gets the list of currently available job column labels
     * 
     * @return - string array of column labels
     */
    public String[] getAvailableList() {
        return this.availableList.getItems();
    }

    /**
     * Gets the list of currently shown job column labels
     * 
     * @return - string array of column labels
     */
    public String[] getShowList() {
        return this.showList.getItems();
    }
}
