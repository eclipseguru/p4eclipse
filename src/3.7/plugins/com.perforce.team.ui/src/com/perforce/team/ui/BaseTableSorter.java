/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BaseTableSorter extends ViewerSorter {

    /**
     * ASCENDING sort direction
     */
    public static final int ASCENDING = 1;

    /**
     * DESCENDING sort direction
     */
    public static final int DESCENDING = -1;

    /**
     * Current sort column
     */
    protected String sortCol;

    /**
     * Current sort direction
     */
    protected int direction;

    private StructuredViewer viewer = null;
    private Table table = null;
    private Tree tree = null;

    /**
     * 
     * @param table
     * @param sortCol
     */
    public BaseTableSorter(Table table, String sortCol) {
        this.sortCol = sortCol;
        direction = DESCENDING;
        this.table = table;
    }

    /**
     * 
     * @param tree
     * @param sortCol
     */
    public BaseTableSorter(Tree tree, String sortCol) {
        this.sortCol = sortCol;
        direction = DESCENDING;
        this.tree = tree;
    }

    /**
     * 
     * @param table
     * @param sortCol
     */
    public BaseTableSorter(TableViewer table, String sortCol) {
        this.sortCol = sortCol;
        direction = DESCENDING;
        this.viewer = table;
        this.table = table.getTable();
    }

    /**
     * 
     * @param tree
     * @param sortCol
     */
    public BaseTableSorter(TreeViewer tree, String sortCol) {
        this.sortCol = sortCol;
        direction = DESCENDING;
        this.viewer = tree;
        this.tree = tree.getTree();
    }

    /**
     * Add a header listener to all columns
     */
    public void addColumnListeners() {
        if (tree != null) {
            SelectionListener headerListener = new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    TreeColumn column = (TreeColumn) e.widget;
                    setSortColumn(column.getText());
                    if (viewer != null) {
                        viewer.refresh();
                    }
                }
            };
            for (TreeColumn column : tree.getColumns()) {
                column.addSelectionListener(headerListener);
            }
        } else if (table != null) {
            SelectionListener headerListener = new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    TableColumn column = (TableColumn) e.widget;
                    setSortColumn(column.getText());
                    if (viewer != null) {
                        viewer.refresh();
                    }
                }
            };
            for (TableColumn column : table.getColumns()) {
                column.addSelectionListener(headerListener);
            }
        }
    }

    /**
     * Set sort column
     * 
     * @param column
     * @param direction
     */
    public void setSortColumn(TableColumn column, int direction) {
        if (table != null) {
            table.setSortColumn(column);
            table.setSortDirection(direction);
        }
    }

    /**
     * Set sort column
     * 
     * @param column
     * @param direction
     */
    public void setSortColumn(TreeColumn column, int direction) {
        if (tree != null) {
            tree.setSortColumn(column);
            tree.setSortDirection(direction);
        }
    }

    /**
     * Get the field value for the given column
     * 
     * @param field
     * @param column
     * @return - value
     */
    protected Object getField(Object field, String column) {
        return field.toString();
    }

    /**
     * Init a viewer with a non-null selection listener that will handle
     * re-sorting when a column is selected.
     * 
     * @param sortColumn
     * @param listener
     * @param direction
     */
    public void init(TableColumn sortColumn, SelectionListener listener,
            int direction) {
        if (listener != null) {
            for (TableColumn column : this.table.getColumns()) {
                column.addSelectionListener(listener);
            }
        }
        this.table.setSortColumn(sortColumn);
        this.table.setSortDirection(direction);
    }

    /**
     * Init a viewer that will refresh when a column sort is selected
     * 
     * @param viewer
     * @param sortColumn
     * @param direction
     */
    public void init(final TableViewer viewer, TableColumn sortColumn,
            int direction) {
        SelectionListener headerListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // column selected - need to sort
                TableColumn column = (TableColumn) e.widget;
                setSortColumn(column.getText());
                viewer.refresh();
            }
        };
        init(sortColumn, headerListener, direction);
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        Object field1 = getField(e1, sortCol);
        Object field2 = getField(e2, sortCol);

        int result = 0;

        if (field1 instanceof Comparable && field2 instanceof Comparable) {
            result = ((Comparable) field1).compareTo(field2);
        } else {
            if (field1 != null) {
                field1 = field1.toString();
            } else {
                field1 = ""; //$NON-NLS-1$
            }

            if (field2 != null) {
                field2 = field2.toString();
            } else {
                field2 = ""; //$NON-NLS-1$
            }
            result = super.compare(viewer, field1, field2);
        }
        return result * direction;
    }

    /**
     * Set direction to be ascending
     */
    public void setAscending() {
        this.direction = ASCENDING;
    }

    /**
     * Set direction to be descending
     */
    public void setDescending() {
        this.direction = DESCENDING;
    }

    /**
     * Set sort column
     * 
     * @param col
     */
    public void setSortColumn(String col) {
        // If new sort column is same as old then reverse direction
        if (sortCol.equals(col)) {
            if (direction == DESCENDING) {
                direction = ASCENDING;
            } else {
                direction = DESCENDING;
            }
        } else {
            sortCol = col;
            direction = DESCENDING;
        }
        if (col != null) {
            int sortDirection = direction == ASCENDING ? SWT.UP : SWT.DOWN;
            if (this.table != null) {
                for (TableColumn column : this.table.getColumns()) {
                    if (col.equals(column.getText())) {
                        this.table.setSortColumn(column);
                        break;
                    }
                }
                this.table.setSortDirection(sortDirection);
            } else if (this.tree != null) {
                for (TreeColumn column : this.tree.getColumns()) {
                    if (col.equals(column.getText())) {
                        this.tree.setSortColumn(column);
                        break;
                    }
                }
                this.tree.setSortDirection(sortDirection);
            }
        }
    }

}
