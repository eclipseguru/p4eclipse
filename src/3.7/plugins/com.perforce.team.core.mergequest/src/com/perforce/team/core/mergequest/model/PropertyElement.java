/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import org.eclipse.core.runtime.PlatformObject;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PropertyElement extends PlatformObject implements IPropertyElement {

    /**
     * Change support
     */
    protected PropertyChangeSupport changeSupport = new PropertyChangeSupport(
            this);

    /**
     * @see com.perforce.team.core.mergequest.model.IPropertyElement#addPropertyListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void addPropertyListener(String property,
            PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(property, listener);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IPropertyElement#addPropertyListener(java.beans.PropertyChangeListener)
     */
    public void addPropertyListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IPropertyElement#removePropertyListener(java.lang.String,
     *      java.beans.PropertyChangeListener)
     */
    public void removePropertyListener(String property,
            PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(property, listener);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.IPropertyElement#removePropertyListener(java.beans.PropertyChangeListener)
     */
    public void removePropertyListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }

}
