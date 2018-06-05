/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.ui.BaseTableSorter;

import org.eclipse.swt.widgets.Table;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BranchSorter extends BaseTableSorter {

    /**
     * @param table
     * @param sortCol
     */
    public BranchSorter(Table table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * @see com.perforce.team.ui.BaseTableSorter#getField(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    protected Object getField(Object field, String column) {
        if (field instanceof IP4Branch) {
            IP4Branch branch = (IP4Branch) field;
            if (BranchesViewer.BRANCH_COLUMN.equals(column)) {
                return branch.getName();
            } else if (BranchesViewer.DESCRIPTION_COLUMN.equals(column)) {
                return branch.getDescription();
            } else if (BranchesViewer.ACCESS_COLUMN.equals(column)) {
                return branch.getAccessTime();
            } else if (BranchesViewer.UPDATE_COLUMN.equals(column)) {
                return branch.getUpdateTime();
            } else if (BranchesViewer.OWNER_COLUMN.equals(column)) {
                return branch.getOwner();
            }
        }
        return super.getField(field, column);
    }

}
