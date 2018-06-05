/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.Date;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DateTaskGroup extends TaskGroup implements
        Comparable<DateTaskGroup> {

    private Date date;

    /**
     * Create date task group
     * 
     * @param name
     * @param date
     */
    public DateTaskGroup(String name, Date date) {
        super(name);
        this.date = date;
    }

    /**
     * Get date
     * 
     * @return - date
     */
    public Date getDate() {
        return this.date;
    }

    /**
     * @see org.eclipse.ui.model.WorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    @Override
    public ImageDescriptor getImageDescriptor(Object object) {
        return PerforceUIPlugin.getDescriptor(IPerforceUIConstants.IMG_DATES);
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(DateTaskGroup o) {
        if (this.date.getTime() < o.date.getTime()) {
            return -1;
        } else if (this.date.getTime() > o.date.getTime()) {
            return 1;
        } else {
            return 0;
        }
    }

}
