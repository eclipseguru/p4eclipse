/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.extensions;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ExtensionPointRegistry {

    /**
     * Get integer
     * 
     * @param value
     * @param defaultValue
     * @return parsed value or default value
     */
    protected int getInteger(String value, int defaultValue) {
        int parsed = defaultValue;
        if (value != null && value.length() > 0) {
            try {
                parsed = Integer.parseInt(value);
            } catch (NumberFormatException nfe) {
                parsed = defaultValue;
            }
        }
        return parsed;
    }

    /**
     * Get integer
     * 
     * @param value
     * @return parsed value or -1 if parsing fails
     */
    protected int getInteger(String value) {
        return getInteger(value, -1);
    }

    /**
     * Get boolean value of specified string
     * 
     * @param value
     * @return parsed value
     */
    protected boolean getBoolean(String value) {
        boolean parsed = false;
        if (value != null) {
            parsed = Boolean.valueOf(value);
        }
        return parsed;
    }

}
