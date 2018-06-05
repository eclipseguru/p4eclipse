/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import org.eclipse.swt.widgets.Table;

import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.BaseTableSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelSorter extends BaseTableSorter {

    /**
     * @param table
     * @param sortCol
     */
    public LabelSorter(Table table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * @see com.perforce.team.ui.BaseTableSorter#getField(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    protected Object getField(Object field, String column) {
        if (field instanceof IP4Label) {
            IP4Label label = (IP4Label) field;
            if (LabelsViewer.LABEL_COLUMN.equals(column)) {
                return label.getName();
            } else if (LabelsViewer.DESCRIPTION_COLUMN.equals(column)) {
                return label.getDescription();
            } else if (LabelsViewer.ACCESS_COLUMN.equals(column)) {
                return label.getAccessTime();
            } else if (LabelsViewer.OWNER_COLUMN.equals(column)) {
                return label.getOwner();
            }
        }
        return super.getField(field, column);
    }

}
