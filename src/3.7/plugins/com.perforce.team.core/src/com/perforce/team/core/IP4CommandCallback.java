/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core;

import java.util.List;
import java.util.Map;

/**
 * Very basic callback interface used for asynchronous methods
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4CommandCallback {

    /**
     * Callback with data
     * 
     * @param data
     */
    void callback(List<Map<String, Object>> data);

    /**
     * Error callback with data array
     * 
     * @param data
     */
    void callbackError(List<Map<String, Object>> data);

}
