/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.TableLabelProviderAdapter;
import com.perforce.team.ui.changelists.ChangelistSorter;
import com.perforce.team.ui.views.SessionManager;

import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FileShelfWidget {

    /**
     * Section name to use when this widget is the main UI element in a dialog
     */
    public static final String SECTION_NAME = "FILE_SHELF_WIDGET"; //$NON-NLS-1$

    /**
     * COLUMN_SIZES
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.shelve.fileshelf.COLUMN_SIZES"; //$NON-NLS-1$

    private IP4File file;
    private IP4ShelveFile[] files;
    private TableViewer viewer;
    private Button revertButton;
    private String title = null;

    /**
     * Create a new file shelf widget
     * 
     * @param files
     * @param file
     * @param title
     */
    public FileShelfWidget(IP4ShelveFile[] files, IP4File file, String title) {
        this.files = files;
        this.file = file;
        this.title = title;
    }

    /**
     * Is revert selected?
     * 
     * @return - true if force, false otherwise
     */
    public boolean isRevert() {
        return this.revertButton != null
                ? this.revertButton.getSelection()
                : false;
    }

    /**
     * Create the widget controls with the specified parent
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        createControl(parent, true);
    }

    /**
     * Create the widget controls with the specified parent
     * 
     * @param parent
     * @param showRevert
     */
    public void createControl(Composite parent, boolean showRevert) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        daData.heightHint = 150;
        daData.widthHint = 515;
        displayArea.setLayoutData(daData);

        if (this.title != null) {
            Label titleLabel = new Label(displayArea, SWT.NONE);
            titleLabel.setText(this.title);
        }
        viewer = new TableViewer(displayArea, SWT.SINGLE | SWT.FULL_SELECTION
                | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        final Table table = viewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(ChangelistSorter.CHANGELIST);
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(ChangelistSorter.DATE);
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(ChangelistSorter.USER);
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(ChangelistSorter.WORKSPACE);
        column.setWidth(100);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(ChangelistSorter.DESCRIPTION);
        column.setWidth(100);

        ChangelistSorter sorter = new ChangelistSorter(viewer) {

            @Override
            protected Object getField(Object field, String column) {

                if (field instanceof IP4ShelveFile) {
                    IP4ShelveFile file = (IP4ShelveFile) field;
                    if (CHANGELIST.equals(column)) {
                        return file.getId();
                    } else if (DATE.equals(column)) {
                        return file.getDate();
                    } else if (USER.equals(column)) {
                        return file.getUser();
                    } else if (DESCRIPTION.equals(column)) {
                        return file.getDescription();
                    } else if (WORKSPACE.equals(column)) {
                        return file.getWorkspace();
                    } else {
                    	return super.getField(field, column);
                    }
                }
                return null;
            }
        };
        sorter.setAscending();
        sorter.addColumnListeners();
        sorter.setSortColumn(ChangelistSorter.CHANGELIST);

        TableLayout vLayout = new TableLayout();

        Map<String, Integer> columnSizes = SessionManager
                .loadColumnSizes(COLUMN_SIZES);
        for (TableColumn tableColumn : table.getColumns()) {
            int width = 100;
            if (columnSizes.containsKey(tableColumn.getText())) {
                int size = columnSizes.get(tableColumn.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            vLayout.addColumnData(new ColumnPixelData(width, true));
        }
        table.setLayout(vLayout);
        table.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                SessionManager.saveColumnPreferences(table, COLUMN_SIZES);
            }
        });

        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        viewer.setSorter(sorter);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(new TableLabelProviderAdapter() {

            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof IP4ShelveFile) {
                    IP4ShelveFile file = (IP4ShelveFile) element;
                    String text = null;
                    switch (columnIndex) {
                    case 0:
                        text = Integer.toString(file.getId());
                        if (file.getId() == FileShelfWidget.this.file
                                .getChangelistId()) {
                            text = "*" + text; //$NON-NLS-1$
                        }
                        break;
                    case 1:
                        text = P4UIUtils.formatLabelDate(file.getDate());
                        break;
                    case 2:
                        text = file.getUser();
                        break;
                    case 3:
                        text = file.getWorkspace();
                        break;
                    case 4:
                        text = file.getDescription();
                        if (text != null) {
                            text = P4CoreUtils.removeWhitespace(text);
                        }
                        break;
                    default:
                        break;
                    }
                    if (text != null) {
                        return text;
                    }
                }
                return ""; //$NON-NLS-1$
            }

        });
        viewer.setInput(this.files);

        if (showRevert) {
            revertButton = new Button(displayArea, SWT.CHECK);
            revertButton
                    .setText(Messages.FileShelfWidget_RevertChangesBeforeUnshelving);
            revertButton.setSelection(file.isOpened());
        }
    }

    /**
     * Get first shelve file in the viewer selection
     * 
     * @return - shelve file or null if selection is empty
     */
    public IP4ShelveFile getSelectedFile() {
        return (IP4ShelveFile) ((IStructuredSelection) viewer.getSelection())
                .getFirstElement();
    }

    /**
     * Get viewer
     * 
     * @return - table viewer
     */
    public TableViewer getViewer() {
        return this.viewer;
    }

}
