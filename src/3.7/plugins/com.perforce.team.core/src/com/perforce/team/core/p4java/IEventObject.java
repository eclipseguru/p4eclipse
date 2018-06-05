/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * Interface for classes that fire events based on their model changing
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IEventObject {

    /**
     * Adds a listener to this object
     * 
     * @param listener
     */
    void addListener(IP4Listener listener);

    /**
     * Adds an array of listeners to this object
     * 
     * @param listeners
     */
    void addListeners(IP4Listener[] listeners);

    /**
     * Removes a listener from this object
     * 
     * @param listener
     */
    void removeListener(IP4Listener listener);

    /**
     * Notifies this object's listeners with the passed in event
     * 
     * @param event
     */
    void notifyListeners(P4Event event);

    /**
     * Clears the listeners
     */
    void clearListeners();

}
