/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4merge;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffRunner extends ApplicationRunner {

    private IP4Connection connection;

    private boolean loadLeft = false;
    private boolean loadRight = false;
    private String leftLabel;
    private String rightLabel;
    private String leftPath;
    private String rightPath;

    /**
     * 
     * @param connection
     * @param loadLeft
     * @param loadRight
     * @param leftLabel
     * @param leftPath
     * @param rightLabel
     * @param rightPath
     */
    public DiffRunner(IP4Connection connection, boolean loadLeft,
            boolean loadRight, String leftLabel, String leftPath,
            String rightLabel, String rightPath) {
        this.connection = connection;
        this.loadLeft = loadLeft;
        this.loadRight = loadRight;
        this.leftLabel = leftLabel;
        this.leftPath = leftPath;
        this.rightLabel = rightLabel;
        this.rightPath = rightPath;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getConnection()
     */
    @Override
    protected IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getBuilder()
     */
    @Override
    protected ProcessBuilder getBuilder() {
        List<String> arguments = new ArrayList<String>();
        arguments.add(getPreferenceValue());
        arguments.add(IP4MergeConstants.CHARSET);
        arguments.add(IP4MergeConstants.NONE);
        arguments.add(IP4MergeConstants.NAME_LEFT);
        arguments.add(this.leftLabel);
        arguments.add(IP4MergeConstants.NAME_RIGHT);
        arguments.add(this.rightLabel);
        arguments.add(this.leftPath);
        arguments.add(this.rightPath);
        return new ProcessBuilder(arguments);
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getPreference()
     */
    @Override
    protected String getPreference() {
        return IPreferenceConstants.P4MERGE_PATH;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#loadFiles()
     */
    @Override
    protected boolean loadFiles() {
        boolean success = true;
        if (loadLeft) {
            File leftFile = connection.printToTempFile(leftPath);
            if (leftFile != null) {
                leftPath = leftFile.getAbsolutePath();
            } else {
                success = false;
            }
        }
        if (loadRight) {
            File rightFile = connection.printToTempFile(rightPath);
            if (rightFile != null) {
                rightPath = rightFile.getAbsolutePath();
            } else {
                success = false;
            }
        }
        return success;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getApplicationName()
     */
    @Override
    protected String getApplicationName() {
        return "P4Merge"; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#applicationFinished(int)
     */
    @Override
    protected void applicationFinished(int exitCode) {

    }
}
