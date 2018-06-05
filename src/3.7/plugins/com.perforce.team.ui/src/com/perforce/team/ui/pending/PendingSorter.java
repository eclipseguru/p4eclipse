/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.pending;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.ui.diff.DiffSorter;

import org.eclipse.jface.viewers.Viewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingSorter extends DiffSorter {

    /**
     * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer,
     *      java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, Object e1, Object e2) {
        if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
            IP4Job job1 = (IP4Job) e1;
            IP4Job job2 = (IP4Job) e2;
            if (job1.getId() != null && job2.getId() != null) {
                return job1.getId().compareTo(job2.getId());
            }
        } else if (e1 instanceof IP4Job && e2 instanceof IP4File) {
            return 1;
        } else if (e1 instanceof IP4File && e2 instanceof IP4Job) {
            return -1;
        } else if (e1 instanceof IP4PendingChangelist
                && e2 instanceof IP4PendingChangelist) {
            IP4PendingChangelist o1 = (IP4PendingChangelist) e1;
            IP4PendingChangelist o2 = (IP4PendingChangelist) e2;
            if (o1.isOnClient() && !o2.isOnClient()) {
                return -1;
            } else if (!o1.isOnClient() && o2.isOnClient()) {
                return 1;
            } else if (o1.isReadOnly() && o2.isReadOnly()) {
                if (o1.isDefault() && !o2.isDefault()) {
                    return -1;
                } else if (!o1.isDefault() && o2.isDefault()) {
                    return 1;
                } else if (!o1.isDefault() && !o2.isDefault()) {
                    return o1.getId() - o2.getId();
                }
            } else if (o1.isReadOnly() && !o2.isReadOnly()) {
                return 1;
            } else if (o2.isReadOnly() && !o1.isReadOnly()) {
                return -1;
            } else if (o1.isDefault() && !o2.isDefault()) {
                return -1;
            } else if (o2.isDefault() && !o1.isDefault()) {
                return 1;
            }
        } else if (e1 instanceof IP4File && e2 instanceof IP4ShelvedChangelist) {
            return -1;
        } else if (e1 instanceof IP4Job && e2 instanceof IP4ShelvedChangelist) {
            return -1;
        } else if (e1 instanceof IP4ShelvedChangelist && e2 instanceof IP4File) {
            return 1;
        } else if (e1 instanceof IP4ShelvedChangelist && e2 instanceof IP4Job) {
            return 1;
        }
        return super.compare(viewer, e1, e2);
    }

}
