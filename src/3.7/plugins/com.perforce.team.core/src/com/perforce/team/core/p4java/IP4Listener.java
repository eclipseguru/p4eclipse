/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4Listener {

    /**
     * P4 resource change event
     * 
     * @param event
     */
    void resoureChanged(P4Event event);

    String getName();
}
