/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import com.perforce.team.ui.P4UIUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseUtils {

    private static final SimpleDateFormat FORMATTER = new SimpleDateFormat(
            "M/dd/yy"); //$NON-NLS-1$

    /**
     * Format a long to a string
     * 
     * @param time
     * @return - string
     */
    public static String format(long time) {
        return format(new Date(time));
    }

    /**
     * Format a date to a string
     * 
     * @param date
     * @return - string
     */
    public static String format(Date date) {
        if (date != null) {
            synchronized (FORMATTER) {
                return FORMATTER.format(date);
            }
        } else {
            return P4UIUtils.EMPTY;
        }
    }

}
