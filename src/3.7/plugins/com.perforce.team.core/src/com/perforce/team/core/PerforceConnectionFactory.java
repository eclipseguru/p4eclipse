/*
 * Copyright (c) 2007 Perforce Software.  All rights reserved.
 *
 */

package com.perforce.team.core;

import java.io.File;

/**
 * This class is responsible for creation of connection
 * 
 * @author Sehyo Chang
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public final class PerforceConnectionFactory {

    /**
     * DIR_WILDCARD_SUFFIX
     */
    protected static final String DIR_WILDCARD_SUFFIX = File.separatorChar
            + "*"; //$NON-NLS-1$

    /**
     * P4_LOCATION
     */
    protected static final String P4_LOCATION = "<p4location>"; //$NON-NLS-1$

    private PerforceConnectionFactory() {

    }

    /**
     * Formats filenames containing characters "@#&*"
     * 
     * @param paths
     *            array of path
     * @return array of args with paths containing special chars formatted
     */
    public static String[] formatFilenames(String[] paths) {
        String[] newpaths = new String[paths.length];
        for (int i = 0; i < paths.length; i++) {
            newpaths[i] = formatFilename(paths[i]);
        }
        return newpaths;
    }

    /**
     * Formats filenames containing characters "@#&*"
     * 
     * @param path
     * @return array of args with paths containing special chars formatted
     */
    public static String formatFilename(String path) {
        boolean wildSuff = false;
        if (path.endsWith(DIR_WILDCARD_SUFFIX)) {
            wildSuff = true;
            path = path.substring(0,
                    path.length() - DIR_WILDCARD_SUFFIX.length());
        }
        /*
         * Only convert a path that: i) Contain a file separator char ii) Are
         * not in depot syntax iii) Do not end in "/*" iv) Contain one of the
         * special chars
         */
        if (path.indexOf(File.separatorChar) > -1
                && !path.startsWith("//") && !path.endsWith(DIR_WILDCARD_SUFFIX) && hasSpecialChars(path)) { //$NON-NLS-1$
            StringBuffer buff = new StringBuffer(path);
            for (int j = 0;; j++) {
                if (j == buff.length()) {
                    break;
                }
                char ch = buff.charAt(j);
                String replace = null;
                if (ch == '@') {
                    replace = "%40"; //$NON-NLS-1$
                } else if (ch == '#') {
                    replace = "%23"; //$NON-NLS-1$
                } else if (ch == '%') {
                    replace = "%25"; //$NON-NLS-1$
                } else if (ch == '*') {
                    replace = "%2A"; //$NON-NLS-1$
                }
                if (replace != null) {
                    buff.replace(j, j + 1, replace);
                    j += 2;
                }
            }
            path = buff.toString();
        }
        if (wildSuff) {
            path = path + DIR_WILDCARD_SUFFIX;
        }
        return path;
    }

    /**
     * Does the string have special chars
     * 
     * @param path
     * @return - true if has special chars
     */
    public static boolean hasSpecialChars(String path) {
        if (path == null) {
            return false;
        }
        if (path.indexOf('@') > -1) {
            return true;
        } else if (path.indexOf('#') > -1) {
            return true;
        } else if (path.indexOf('%') > -1) {
            return true;
        } else if (path.indexOf('*') > -1) {
            return true;
        }
        return false;
    }

}
