/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import java.util.ArrayList;
import java.util.List;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RevisionGraphRunner extends P4VRunner {

    private String path = null;
    private IP4Connection connection = null;

    /**
     * @param connection
     * @param path
     */
    public RevisionGraphRunner(IP4Connection connection, String path) {
        this.connection = connection;
        this.path = path;
    }

    /**
     * @see com.perforce.team.ui.p4v.P4VRunner#getCommand()
     */
    @Override
    protected List<String> getCommand() {
        List<String> args=new ArrayList<String>();
        args.add("tree " + this.path); //$NON-NLS-1$
        return args;
    }

    /**
     * @see com.perforce.team.ui.p4v.P4VRunner#getConnection()
     */
    @Override
    protected IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#applicationFinished(int)
     */
    @Override
    protected void applicationFinished(int exitCode) {
        if (exitCode != 0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager.getManager().openInformation(
                            P4UIUtils.getShell(),
                            Messages.RevisionGraphRunner_ErrorTitle,
                            Messages.RevisionGraphRunner_ErrorMessage);
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#loadFiles()
     */
    @Override
    protected boolean loadFiles() {
        return true;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getApplicationName()
     */
    @Override
    protected String getApplicationName() {
        return Messages.RevisionGraphRunner_Title;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getTaskName()
     */
    @Override
    protected String getTaskName() {
        return this.path;
    }

}
