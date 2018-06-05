/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Table;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.BaseTableSorter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistSorter extends BaseTableSorter {

    /**
     * CHANGELIST
     */
    public static final String CHANGELIST = Messages.ChangelistSorter_Changelist;

    /**
     * DATE
     */
    public static final String DATE = Messages.ChangelistSorter_Date;

    /**
     * USER
     */
    public static final String USER = Messages.ChangelistSorter_User;

    /**
     * DESCRIPTION
     */
    public static final String DESCRIPTION = Messages.ChangelistSorter_Description;

    /**
     * WORKSPACE
     */
    public static final String WORKSPACE = Messages.ChangelistSorter_Workspace;

    /**
     * @param table
     * @param sortCol
     */
    public ChangelistSorter(Table table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * @param table
     */
    public ChangelistSorter(Table table) {
        super(table, CHANGELIST);
    }

    /**
     * @param table
     */
    public ChangelistSorter(TreeViewer table) {
        super(table, CHANGELIST);
    }

    /**
     * @param table
     */
    public ChangelistSorter(TableViewer table) {
        super(table, CHANGELIST);
    }

    /**
     * 
     * @param table
     * @param sortCol
     */
    public ChangelistSorter(TableViewer table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * 
     * @param tree
     * @param sortCol
     */
    public ChangelistSorter(TreeViewer tree, String sortCol) {
        super(tree, sortCol);
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
            } else if (WORKSPACE.equals(column)) {
                return list.getClientName();
            } else{
            	return super.getField(field, column);
            }
        }
        return null;
    }

}
