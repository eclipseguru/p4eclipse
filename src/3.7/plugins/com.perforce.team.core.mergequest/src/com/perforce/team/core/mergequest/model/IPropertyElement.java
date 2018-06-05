/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IPropertyElement extends IAdaptable {

    /**
     * Add property listener
     * 
     * @param property
     * @param listener
     */
    void addPropertyListener(String property, PropertyChangeListener listener);

    /**
     * Add property listener
     * 
     * @param listener
     */
    void addPropertyListener(PropertyChangeListener listener);

    /**
     * Remove property listener
     * 
     * @param property
     * @param listener
     */
    void removePropertyListener(String property, PropertyChangeListener listener);

    /**
     * Remove property listener
     * 
     * @param listener
     */
    void removePropertyListener(PropertyChangeListener listener);

}
