/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.model;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.P4JavaCallback;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.patch.P4PatchUiPlugin;
import com.perforce.team.ui.patch.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PatchDiffRunner {

    /**
     * PATCH_COMMAND_FILE_PREFIX
     */
    public static final String PATCH_COMMAND_FILE_PREFIX = "p4eclipsePatch"; //$NON-NLS-1$

    /**
     * PATCH_COMMAND_FILE_SUFFIX
     */
    public static final String PATCH_COMMAND_FILE_SUFFIX = ".txt"; //$NON-NLS-1$

    /**
     * P4_ENV_PREFIX
     */
    public static final String P4_ENV_PREFIX = "P4"; //$NON-NLS-1$

    private IP4Connection connection;
    private LinkedList<String> arguments;
    private File tmpFile = null;

    /**
     * @param connection
     * @throws IOException
     */
    public PatchDiffRunner(IP4Connection connection) throws IOException {
        this.connection = connection;
        this.arguments = setupArguments();
    }

    /**
     * Setup environment to p4 command
     * 
     * @param environment
     */
    protected void setupEnvironment(Map<String, String> environment) {
        if (environment != null) {
            String envKey = null;
            for (Object key : environment.keySet().toArray()) {
                envKey = key.toString();
                if (envKey.startsWith(P4_ENV_PREFIX)
                        && !envKey.equals(P4JavaCallback.SSO_CMD_ENV_KEY)
                        && !envKey.equals(Server.P4TICKETS_ENV_VAR)) {
                    environment.remove(key);
                }
            }
        }
    }

    /**
     * Setup core arguments to p4
     * 
     * @return non-null and non-empty list of arguments
     * @throws IOException
     */
    protected LinkedList<String> setupArguments() throws IOException {
        ConnectionParameters params = connection.getParameters();
        LinkedList<String> arguments = new LinkedList<String>();

        String p4Path = P4PatchUiPlugin.getDefault().getPreferenceStore()
                .getString(IPreferenceConstants.P4_PATH);
        if (p4Path.length() == 0) {
            p4Path = "p4"; //$NON-NLS-1$
        }

        arguments.add(p4Path);

        arguments.add("-p"); //$NON-NLS-1$
        arguments.add(params.getPort());
        arguments.add("-u"); //$NON-NLS-1$
        arguments.add(params.getUser());
        arguments.add("-c"); //$NON-NLS-1$
        arguments.add(params.getClient());

        String charset = params.getCharsetNoNone();
        if (charset != null) {
            arguments.add("-C"); //$NON-NLS-1$
            arguments.add(charset);
        }

//        String ticket = params.getAuthTicket();
        String passwd = params.getPassword();
        if (passwd != null) {
            arguments.add("-P"); //$NON-NLS-1$
            arguments.add(passwd);
        }

        this.tmpFile = generateTempFile(arguments);

        arguments.add("diff"); //$NON-NLS-1$
        arguments.add("-t"); //$NON-NLS-1$
        arguments.add("-du"); //$NON-NLS-1$

        return arguments;
    }

    /**
     * Generate temporary file path arguments file
     * 
     * @param arguments
     * @return tmp file
     * 
     * @throws IOException
     */
    protected File generateTempFile(List<String> arguments) throws IOException {
        File file = File.createTempFile(PATCH_COMMAND_FILE_PREFIX,
                PATCH_COMMAND_FILE_SUFFIX);
        if (file != null) {
            file.deleteOnExit();
            arguments.add("-x"); //$NON-NLS-1$
            arguments.add(file.getAbsolutePath());
        }
        return file;
    }

    /**
     * Write file path to temp file
     * 
     * @param filePath
     * @throws IOException
     */
    protected void writeFilePath(String filePath) throws IOException {
        Charset charset = ConnectionParameters.getJavaCharset(connection);
        PrintWriter writer = null;
        try {
        	writer = new PrintWriter(tmpFile, charset.name());
            writer.println(filePath);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Run a diff process
     * 
     * @param filePath
     * @return process
     * @throws IOException
     */
    public Process run(String filePath) throws IOException {
        Process process = null;
        if (filePath != null) {
            writeFilePath(filePath);
            ProcessBuilder builder = new ProcessBuilder(arguments);
            setupEnvironment(builder.environment());
            process = builder.start();
        }
        return process;
    }

    /**
     * Dispose of the patch diff runner
     */
    public void dispose() {
        if (tmpFile != null) {
            if(!tmpFile.delete()){
            	String msg = MessageFormat.format(Messages.PatchDiffRunner_DeleteFileError,tmpFile.getAbsolutePath());
            	PerforceProviderPlugin.logError(msg);
            }
        }
    }
}
