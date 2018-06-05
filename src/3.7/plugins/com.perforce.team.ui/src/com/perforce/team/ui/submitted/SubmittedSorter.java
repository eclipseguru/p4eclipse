/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.submitted;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.BaseTableSorter;

import org.eclipse.swt.widgets.Table;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class SubmittedSorter extends BaseTableSorter {

    /**
     * CHANGELIST
     */
    public static final String CHANGELIST = Messages.SubmittedSorter_Changelist;

    /**
     * DATE
     */
    public static final String DATE = Messages.SubmittedSorter_Date;

    /**
     * USER
     */
    public static final String USER = Messages.SubmittedSorter_User;

    /**
     * DESCRIPTION
     */
    public static final String DESCRIPTION = Messages.SubmittedSorter_Description;

    /**
     * @param table
     * @param sortCol
     */
    public SubmittedSorter(Table table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * @param table
     */
    public SubmittedSorter(Table table) {
        super(table, CHANGELIST);
    }

    /**
     * @see com.perforce.team.ui.BaseTableSorter#getField(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    protected Object getField(Object field, String column) {
        if (field instanceof IP4Changelist) {
            IP4Changelist list = (IP4Changelist) field;
            if (CHANGELIST.equals(column)) {
                return list.getId();
            } else if (DATE.equals(column)) {
                return list.getDate();
            } else if (USER.equals(column)) {
                return list.getUserName();
            } else if (DESCRIPTION.equals(column)) {
                return list.getDescription();
            }
        }
        return super.getField(field, column);
    }

}
