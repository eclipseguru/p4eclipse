/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MergeRunner extends ApplicationRunner {

    private IP4Connection connection = null;

    private IP4File file;

    private String basePath = null;
    private String leftPath = null;
    private String rightPath = null;

    private boolean loadBase = false;
    private boolean loadLeft = false;
    private boolean loadRight = false;

    private String baseFile = null;
    private String leftFile = null;
    private String rightFile = null;
    private String mergeFile = null;

    private int startFromRev = -1;
    private int endFromRev = -1;

    private IP4Resource resolved;

    /**
     * Creates a new merge runner
     * 
     * @param connection
     * @param file
     * @param basePath
     * @param loadBase
     * @param leftPath
     * @param loadLeft
     * @param rightPath
     * @param loadRight
     * 
     */
    public MergeRunner(IP4Connection connection, IP4File file, String basePath,
            boolean loadBase, String leftPath, boolean loadLeft,
            String rightPath, boolean loadRight, int startFromRev,
            int endFromRev) {
        this.connection = connection;
        this.file = file;

        this.basePath = basePath;
        this.leftPath = leftPath;
        this.rightPath = rightPath;

        this.loadBase = loadBase;
        this.loadLeft = loadLeft;
        this.loadRight = loadRight;

        this.startFromRev = startFromRev;
        this.endFromRev = endFromRev;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getConnection()
     */
    @Override
    protected IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * Get the resolve resource. Will only be available after the file has been
     * replaced which will be at different times depending on the return value
     * of {@link #isAsync()}.
     * 
     * @return the resolved
     */
    public IP4Resource getResolved() {
        return resolved;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getBuilder()
     */
    @Override
    protected ProcessBuilder getBuilder() {
        List<String> arguments = new ArrayList<String>();
        arguments.add(getPreferenceValue());

        arguments.add(IP4MergeConstants.LEFT_LABEL);
        arguments.add(Messages.MergeRunner_Theirs);
        arguments.add(IP4MergeConstants.RIGHT_LABEL);
        arguments.add(Messages.MergeRunner_Yours);

        arguments.add(IP4MergeConstants.CHARSET);
        String charset = connection.getParameters().getCharset();
        if (charset == null) {
            charset = IP4MergeConstants.NONE;
        }
        arguments.add(charset);

        arguments.add(IP4MergeConstants.NAME_MERGE);
        arguments.add(Messages.MergeRunner_MergeFileUsedForResolve);
        arguments.add(IP4MergeConstants.NAME_LEFT);
        arguments.add(this.leftPath);
        arguments.add(IP4MergeConstants.NAME_RIGHT);
        arguments.add(this.rightPath);
        arguments.add(IP4MergeConstants.NAME_BASE);
        arguments.add(this.basePath);

        arguments.add(this.baseFile);
        arguments.add(this.leftFile);
        arguments.add(this.rightFile);
        arguments.add(this.mergeFile);

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
        if (loadBase) {
            File baseFile = connection.printToTempFile(basePath);
            if (baseFile != null) {
                this.baseFile = baseFile.getAbsolutePath();
            } else {
                success = false;
            }
        } else {
            this.baseFile = this.basePath;
        }
        if (loadLeft) {
            File leftFile = connection.printToTempFile(leftPath);
            if (leftFile != null) {
                this.leftFile = leftFile.getAbsolutePath();
            } else {
                success = false;
            }
        } else {
            this.leftFile = this.leftPath;
        }
        if (loadRight) {
            File rightFile = connection.printToTempFile(rightPath);
            if (rightFile != null) {
                this.rightFile = rightFile.getAbsolutePath();
            } else {
                success = false;
            }
        } else {
            this.rightFile = this.rightPath;
        }
        try {
            this.mergeFile = File.createTempFile("p4ws", ".tmp") //$NON-NLS-1$ //$NON-NLS-2$
                    .getAbsolutePath();
        } catch (IOException e) {
            success = false;
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
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getTaskName()
     */
    @Override
    protected String getTaskName() {
        return this.rightFile;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#applicationFinished(int)
     */
    @Override
    protected void applicationFinished(int exitCode) {
        if (exitCode == 0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    final String name = file.getName();
                    boolean replace = P4ConnectionManager
                            .getManager()
                            .openQuestion(
                                    P4UIUtils.getShell(),
                                    Messages.MergeRunner_ReplaceMergedTitle,
                                    MessageFormat
                                            .format(Messages.MergeRunner_ReplaceMergedMessage,
                                                    name));
                    if (replace) {
                        IP4Runnable runnable = new P4Runnable() {

                            @Override
                            public void run(IProgressMonitor monitor) {
                                WorkspaceModifyOperation replaceFile = new WorkspaceModifyOperation() {

                                    @Override
                                    protected void execute(
                                            IProgressMonitor monitor)
                                            throws CoreException,
                                            InvocationTargetException,
                                            InterruptedException {
                                        try {
                                            P4Collection single = P4ConnectionManager
                                                    .getManager()
                                                    .createP4Collection(
                                                            new IP4Resource[] { file });
                                            single.setType(IP4Resource.Type.LOCAL);
                                            IP4Resource[] resolved = single
                                                    .resolve(
                                                            new FileInputStream(
                                                                    mergeFile),
                                                            true, // do textual
                                                                  // resolve
                                                                  // since we
                                                                  // just did
                                                                  // textual
                                                                  // merge
                                                            startFromRev,
                                                            endFromRev);
                                            if (resolved != null
                                                    && resolved.length == 1) {
                                                MergeRunner.this.resolved = resolved[0];
                                            }
                                        } catch (Exception e) {
                                            PerforceProviderPlugin.logError(e);
                                        }
                                    }
                                };
                                try {
                                    replaceFile.run(new NullProgressMonitor());
                                } catch (InvocationTargetException e) {
                                } catch (InterruptedException e) {
                                }
                            }

                            @Override
                            public String getTitle() {
                                return MessageFormat.format(
                                        Messages.MergeRunner_Resolving, name);
                            }

                        };
                        if (isAsync()) {
                            P4Runner.schedule(runnable);
                        } else {
                            runnable.run(new NullProgressMonitor());
                        }
                    }
                }
            });
        }
    }
}
