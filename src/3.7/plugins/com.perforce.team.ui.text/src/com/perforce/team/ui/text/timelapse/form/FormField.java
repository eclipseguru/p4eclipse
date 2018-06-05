/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.form;

import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.text.PerforceUiTextPlugin;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FormField implements IWorkbenchAdapter {

    /**
     * Field type
     */
    public enum Type {

        /**
         * DATE
         */
        DATE,

        /**
         * SELECT
         */
        SELECT,

        /**
         * TEXT
         */
        TEXT,

        /**
         * WORD
         */
        WORD
    }

    private Type type;
    private String name;
    private String value;
    private int offset;
    private int length;

    /**
     * Create a form field
     * 
     * @param name
     * @param value
     * @param offset
     * @param length
     * @param type
     */
    public FormField(String name, String value, int offset, int length,
            Type type) {
        this.name = name;
        this.value = value;
        this.offset = offset;
        this.length = length;
        this.type = type;
    }

    /**
     * Get type
     * 
     * @return - form data type
     */
    public Type getType() {
        return this.type;
    }

    /**
     * @return the name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return this.value;
    }

    /**
     * @return the offset
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * @return the length
     */
    public int getLength() {
        return this.length;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
     */
    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
     */
    public ImageDescriptor getImageDescriptor(Object object) {
        if (type != null) {
            switch (type) {
            case DATE:
                return PerforceUiTextPlugin
                        .getImageDescriptor(PerforceUiTextPlugin.IMG_DATE);
            case SELECT:
                return PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_JOB_FIELD_SELECT);
            case TEXT:
                return PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_JOB_FIELD_TEXT);
            case WORD:
                return PerforceUIPlugin
                        .getDescriptor(IPerforceUIConstants.IMG_JOB_FIELD_WORD);
            default:
                break;
            }
        }
        return null;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
     */
    public String getLabel(Object o) {
        return this.name;
    }

    /**
     * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
     */
    public Object getParent(Object o) {
        return null;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof FormField
                    && this.name.equals(((FormField) obj).getName());
        }
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

}
