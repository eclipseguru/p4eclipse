/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;

import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Label extends IP4Resource {

    /**
     * Get underlying p4j label object
     * 
     * @return - p4j label
     */
    ILabel getLabel();

    /**
     * Get label owner
     * 
     * @return - label owner
     */
    String getOwner();

    /**
     * Get label description
     * 
     * @return - label description
     */
    String getDescription();

    /**
     * Get label access time
     * 
     * @return - label access time
     */
    Date getAccessTime();

    /**
     * Get label update time
     * 
     * @return - label update time
     */
    Date getUpdateTime();

    /**
     * Get label revision
     * 
     * @return - label revision
     */
    String getRevision();

    /**
     * Get label view
     * 
     * @return - list of label view mappings
     */
    ViewMap<ILabelMapping> getView();

    /**
     * Is the label locked?
     * 
     * @return - true if locked, false otherwise
     */
    boolean isLocked();

}
