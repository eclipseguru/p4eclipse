/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.server.PerforceCharsets;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ApplicationRunner {

    /**
     * Current process
     */
    protected Process currentProcess = null;

    /**
     * Run everything in calling thread or spawn new threads?
     */
    protected boolean async = true;

    /**
     * @return the async
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * @param async
     *            the async to set
     */
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Get the UI plugin preference store
     * 
     * @return - ui plugin preference store
     */
    protected IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    /**
     * Get the preference value of the key returned from
     * {@link #getPreference()}
     * 
     * @return - preference value
     */
    protected String getPreferenceValue() {
        return getPreferenceStore().getString(getPreference());
    }

    /**
     * Get the preference key used by this application as an executable
     * 
     * @return = preference ky
     */
    protected abstract String getPreference();

    /**
     * Load any necessary files using p4 print before the arguments are ready to
     * be passed to p4 merge.
     * 
     * @return - true if loading the files succeeded, false otherwise
     */
    protected abstract boolean loadFiles();

    /**
     * Get the process builder used by thie application
     * 
     * @return - process builder
     */
    protected abstract ProcessBuilder getBuilder();

    /**
     * Callback for when application terminates
     * 
     * @param exitCode
     */
    protected abstract void applicationFinished(int exitCode);

    /**
     * Get the connection being used
     * 
     * @return - p4 connection
     */
    protected abstract IP4Connection getConnection();

    /**
     * Get the last or current process run
     * 
     * @return - process
     */
    public Process getProcess() {
        return this.currentProcess;
    }

    /**
     * Run the application
     */
    public void run() {
        // Check preference
        String path = getPreferenceValue();
        if ("".equals(path)) { //$NON-NLS-1$
            // Open dialog if preference not sent
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    if (P4ConnectionManager
                            .getManager()
                            .openQuestion(
                                    P4UIUtils.getShell(),
                                    MessageFormat
                                            .format(Messages.ApplicationRunner_ConfigureAppTitle,
                                                    getApplicationName()),
                                    MessageFormat
                                            .format(Messages.ApplicationRunner_ConfigureAppMessage,
                                                    getApplicationName()))) {
                        P4UIUtils
                                .openPreferencePage("com.perforce.team.ui.preferences.ExternalToolsPreferencePage"); //$NON-NLS-1$
                    }
                }
            });
        }
        // Check preference
        path = getPreferenceValue();
        if (!"".equals(path)) { //$NON-NLS-1$

            IP4Runnable runnable = new P4Runnable() {

                @Override
                public void run(final IProgressMonitor monitor) {
                    if (loadFiles()) {
                        if (monitor.isCanceled()) {
                            return;
                        }
                        // Run external tool
                        final ProcessBuilder builder = getBuilder();
                        final IOException[] exceptions = new IOException[] { null };
                        final boolean[] done = new boolean[] { false };

                        String task = getTaskName();
                        if (task != null) {
                            monitor.beginTask(task, 1);
                        }

                        // Wrapper thread for the outer runnable to monitor the
                        // inner thread
                        Thread thread = new Thread(new Runnable() {

                            public void run() {
                                try {
                                    currentProcess = builder.start();
                                    int rc = currentProcess.waitFor();
                                    // Only call finished callback if monitor is
                                    // not cancelled
                                    if (!monitor.isCanceled()) {
                                        applicationFinished(rc);
                                    }
                                } catch (IOException e) {
                                    exceptions[0] = e;
                                } catch (InterruptedException e) {
                                    PerforceProviderPlugin.logError(e);
                                } finally {
                                    done[0] = true;
                                }
                            }
                        });
                        thread.start();
                        // Monitor other thread so we can terminate the process
                        // if selected by the user
                        while (!done[0] && thread.isAlive()) {
                            if (monitor.isCanceled()) {
                                done[0] = true;
                                if (currentProcess != null) {
                                    currentProcess.destroy();
                                }
                            }
                            // Only poll every 2 seconds
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                // Ignore and keep polling wrapped thread
                            }
                        }
                        monitor.worked(1);
                        monitor.done();
                        if (exceptions[0] != null) {
                            UIJob job = new UIJob(MessageFormat.format(
                                    Messages.ApplicationRunner_DisplayAppError,
                                    getApplicationName())) {

                                @Override
                                public IStatus runInUIThread(
                                        IProgressMonitor monitor) {
                                    P4ConnectionManager
                                            .getManager()
                                            .openError(
                                                    P4UIUtils.getShell(),
                                                    MessageFormat
                                                            .format(Messages.ApplicationRunner_ErrorExecutingAppTitle,
                                                                    getApplicationName()),
                                                    MessageFormat
                                                            .format(Messages.ApplicationRunner_ErrorExecutingAppMessage,
                                                                    getApplicationName()));
                                    return Status.OK_STATUS;
                                }
                            };
                            job.schedule();
                        }
                    }
                }

                @Override
                public String getTitle() {
                    return MessageFormat.format(
                            Messages.ApplicationRunner_RunningApp,
                            getApplicationName());
                }
            };
            if (isAsync()) {
                P4Runner.schedule(runnable);
            } else {
                runnable.run(new NullProgressMonitor());
            }
        }
    }

    /**
     * Get the display name for the application being run
     * 
     * @return - display version of the application name
     */
    protected abstract String getApplicationName();

    /**
     * Get the task name
     * 
     * @return - task name or null to not set
     */
    protected String getTaskName() {
        return null;
    }

    /**
     * Should the argument list be passed to {@link #convertExec(List)} for
     * conversion?
     * 
     * @return - true to convert, false otherwise
     */
    protected boolean shouldConvertExec() {
        return P4CoreUtils.isWindows()
                && !getConnection().getParameters()
                .getCharset().equals("none")
                && PerforceCharsets.isSupported(getConnection().getParameters()
                        .getCharset());
    }

    /**
     * Convert argument list to proper windows exec to work-around Java bug
     * 4947220.
     * 
     * @param arguments
     */
    protected void convertExec(List<String> arguments) {
        // On Windows the process and arguments need to be put in a batch file
        // then exec just the batch file due to Java bug
        // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4947220
    	OutputStreamWriter writer=null;
        try {
            File tempBatch = File.createTempFile("p4eclipse_win_exec", ".bat"); //$NON-NLS-1$ //$NON-NLS-2$
            tempBatch.deleteOnExit();

            // Encode batch file in UTF-8
            writer = new OutputStreamWriter(
                    new FileOutputStream(tempBatch), "UTF-8"); //$NON-NLS-1$

            // Switch cmd code page or else unicode code characters are not read
            // properly from batch file
            writer.write("@echo off \r\n"); //$NON-NLS-1$
            writer.write("chcp 1252 \r\n"); //$NON-NLS-1$

            for (String arg : arguments) {
                // Quote arguments that contain a space
                if (arg.indexOf(' ') == -1) {
                    writer.write(arg);
                } else {
                    writer.write('"');
                    writer.write(arg);
                    writer.write('"');
                }

                // Try to avoid long-line length issue by
                writer.write('^');
                writer.write('\r');
                writer.write('\n');
                writer.write(' ');
            }

            writer.flush();

            // Clear arguments and replace with just the path of the batch file
            // that will be exec'ed.
            arguments.clear();
            arguments.add(tempBatch.getAbsolutePath());
        } catch (IOException e) {
            // If exception is thrown just log the exception and don't touch the
            // argument list.
            PerforceProviderPlugin.logError(e);
        } finally{
        	if(writer!=null){
				try {
					writer.close();
				} catch (IOException e) {
		            PerforceProviderPlugin.logError(e);
				}
        	}
        }
    }

}
