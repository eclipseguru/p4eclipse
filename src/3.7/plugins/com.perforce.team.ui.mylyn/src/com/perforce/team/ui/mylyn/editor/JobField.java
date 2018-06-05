/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.p4java.core.IJobSpec.IJobSpecField;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JobField {

    private JobFieldGroup page;
    private IJobSpecField field;

    /**
     * Create a job editor field with the specified job spec field with job
     * editor page as parent of this field
     * 
     * @param page
     * @param field
     */
    public JobField(JobFieldGroup page, IJobSpecField field) {
        this.page = page;
        this.field = field;
    }

    /**
     * Get the parent page of this field
     * 
     * @return - page
     */
    public JobFieldGroup getParent() {
        return this.page;
    }

    /**
     * Get underlying job spec field
     * 
     * @return - job spec field
     */
    public IJobSpecField getField() {
        return this.field;
    }

    /**
     * Get index of field in parent group.
     * 
     * @return - index or -1 if no parent
     */
    public int getIndex() {
        int index = -1;
        if (this.page != null) {
            index = this.page.indexOf(this);
        }
        return index;
    }

    /**
     * Get index of parent group
     * 
     * @return - index or -1 if no parent
     */
    public int getParentIndex() {
        int index = -1;
        if (this.page != null) {
            index = this.page.getIndex();
        }
        return index;
    }

    /**
     * Set the parent page of this field. This method will remove this field
     * from the current parent but does not add it to the new parent.
     * 
     * @param page
     */
    public void setParent(JobFieldGroup page) {
        if (this.page != page) {
            if (this.page != null) {
                this.page.remove(this);
            }
            if (page != null) {
                this.page = page;
            }
        }
    }

}
