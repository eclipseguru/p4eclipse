/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import java.util.Comparator;

import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.diff.DiffSorter;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateTaskSorter extends DiffSorter {

    private ILabelProvider provider;

    /**
     * Create new sorter
     * 
     * @param provider
     */
    public IntegrateTaskSorter(ILabelProvider provider) {
        this.provider = provider;
    }

    /**
     * @see com.perforce.team.ui.diff.DiffSorter#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
        if (element instanceof UserTaskGroup) {
            if (((UserTaskGroup) element).isCurrentUser()) {
                return -1;
            } else {
                return 0;
            }
        } else if (element instanceof JobTaskGroup) {
            if (((JobTaskGroup) element).getJob() == null) {
                return -1;
            } else {
                return 0;
            }
        }
        return super.category(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof DateTaskGroup && e2 instanceof DateTaskGroup) {
            return -1 * ((DateTaskGroup) e1).compareTo(((DateTaskGroup) e2));
        }
        int cat1 = category(e1);
        int cat2 = category(e2);

        if (cat1 != cat2) {
            return cat1 - cat2;
        }

        if (e1 instanceof IP4SubmittedChangelist
                && e2 instanceof IP4SubmittedChangelist) {
            return ((IP4SubmittedChangelist) e2).getId()
                    - ((IP4SubmittedChangelist) e1).getId();
        } else if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
            return ((IP4Job) e1).getId().compareToIgnoreCase(
                    ((IP4Job) e2).getId());
        }

        String name1 = provider.getText(e1);
        String name2 = provider.getText(e2);
        if (name1 == null) {
            name1 = "";//$NON-NLS-1$
        }
        if (name2 == null) {
            name2 = "";//$NON-NLS-1$
        }
        @SuppressWarnings("unchecked")
        Comparator<String> comparator = getComparator();
        return comparator.compare(name1, name2);
    }

}
