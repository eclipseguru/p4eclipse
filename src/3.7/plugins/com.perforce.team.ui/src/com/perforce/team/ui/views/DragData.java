package com.perforce.team.ui.views;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.p4java.IP4Connection;

/**
 * Drag data class
 */
public class DragData {

    private static IP4Connection connection = null;
    private static Object source = null;;

    /**
     * Set the connection
     * 
     * @param con
     */
    public static void setConnection(IP4Connection con) {
        connection = con;
    }

    /**
     * Get the connection
     * 
     * @return - connection
     */
    public static IP4Connection getConnection() {
        return connection;
    }

    /**
     * Set the source
     * 
     * @param src
     */
    public static void setSource(Object src) {
        source = src;
    }

    /**
     * Get the source
     * 
     * @return - source object
     */
    public static Object getSource() {
        return source;
    }

    /**
     * Clear the connection and source
     */
    public static void clear() {
        connection = null;
        source = null;
    }
}
