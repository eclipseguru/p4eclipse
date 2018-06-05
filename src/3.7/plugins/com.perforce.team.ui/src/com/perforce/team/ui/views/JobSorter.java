package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.BaseTableSorter;

import org.eclipse.swt.widgets.Table;

/**
 * Sorter for Jobs view.
 */
public class JobSorter extends BaseTableSorter {

    /**
     * 
     * @param jobsTable
     * @param sortCol
     */
    public JobSorter(Table jobsTable, String sortCol) {
        super(jobsTable, sortCol);
    }

    /**
     * @see com.perforce.team.ui.BaseTableSorter#getField(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    protected Object getField(Object field, String column) {
        if (field instanceof IP4Job) {
            return ((IP4Job) field).getField(column);
        }
        return super.getField(field, column);
    }

}
