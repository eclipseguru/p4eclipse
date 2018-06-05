/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import java.util.Collection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public interface IFilterNodeModel extends INodeModel {

    /**
     * Get a filter key for a specified filter label
     * 
     * @param filterLabel
     * @return - filter key
     */
    String getFilterKey(String filterLabel);

    /**
     * Get all filter labels in this model
     * 
     * @return - non-null collection of string filter labels
     */
    Collection<String> getFilterLabels();

}
