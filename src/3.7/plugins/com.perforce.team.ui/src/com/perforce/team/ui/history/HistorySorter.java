/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.history;

import com.perforce.team.core.p4java.ILocalRevision;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.BaseTableSorter;

import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class HistorySorter extends BaseTableSorter {

    /**
     * @param table
     * @param sortCol
     */
    public HistorySorter(Table table, String sortCol) {
        super(table, sortCol);
    }

    /**
     * @param tree
     * @param sortCol
     */
    public HistorySorter(Tree tree, String sortCol) {
        super(tree, sortCol);
    }

    /**
     * @see com.perforce.team.ui.BaseTableSorter#getField(java.lang.Object,
     *      java.lang.String)
     */
    @Override
    protected Object getField(Object field, String column) {
        if (field instanceof IP4Revision) {
            IP4Revision revision = (IP4Revision) field;
            if (P4HistoryPage.ACTION_COLUMN.equals(column)) {
                return revision.getAction();
            } else if (P4HistoryPage.CHANGELIST_COLUMN.equals(column)) {
                return revision.getChangelist();
            } else if (P4HistoryPage.FILENAME_COLUMN.equals(column)) {
                return revision.getRemotePath();
            } else if (P4HistoryPage.REVISION_COLUMN.equals(column)) {
                return revision.getRevision();
            }
        }
        if (field instanceof IFileRevision) {
            IFileRevision revision = (IFileRevision) field;
            if (P4HistoryPage.DATE_COLUMN.equals(column)) {
                return revision.getTimestamp();
            } else if (P4HistoryPage.DESCRIPTION_COLUMN.equals(column)) {
                return revision.getComment();
            } else if (P4HistoryPage.USER_COLUMN.equals(column)) {
                return revision.getAuthor();
            } else if (P4HistoryPage.CHANGELIST_COLUMN.equals(column)) {
                return -1;
            } else if (P4HistoryPage.REVISION_COLUMN.equals(column)) {
                return -1;
            } else if (P4HistoryPage.ACTION_COLUMN.equals(column)) {
                return null;
            }
        }
        if (field instanceof ILocalRevision) {
            ILocalRevision revision = (ILocalRevision) field;
            if (P4HistoryPage.FILENAME_COLUMN.equals(column)) {
                return revision.getLocalPath();
            }
        }
        return super.getField(field, column);
    }

}
