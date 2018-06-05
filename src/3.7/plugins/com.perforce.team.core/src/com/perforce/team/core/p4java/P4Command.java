/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.IP4CommandCallback;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.P4JavaCallback;
import com.perforce.team.core.PerforceProviderPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4Command {
    private String command;
    private IP4Connection connection;

    /**
     * Creates a new p4 command
     * 
     * @param connection
     * @param command
     */
    public P4Command(IP4Connection connection, String command) {
        this.connection = connection;
        this.command = command;
    }

    /**
     * Runs the command and returns the output array
     * 
     * @param refresh
     *            - true to refresh the changes resources currently in the cache
     * @param commandCallback
     * 
     */
    public void run(final boolean refresh, final IP4CommandCallback commandCallback) {
        final IP4Connection p4Connection = this.connection;
        final String p4Command = this.command;
        if (p4Connection == null || p4Command == null) {
            return;
        }
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(Messages.P4Command_0, command);
            }

            @Override
            public void run(IProgressMonitor monitor) {
                if (p4Connection != null && !p4Connection.isOffline()
                        && p4Command != null) {
                    IServer server = p4Connection.getServer();
                    if (server != null) {
                        String[] args = command.split(" "); //$NON-NLS-1$
                        List<String> realArgs = new ArrayList<String>();
                        for (int i = 0; i < args.length; i++) {
                            String arg = args[i].trim();
                            if (arg.length() > 0) {
                                if (i > 0 || !"p4".equals(arg)) { //$NON-NLS-1$
                                    realArgs.add(args[i].trim());
                                }
                            }
                        }
                        try {
                            String commandName = ""; //$NON-NLS-1$
                            if (realArgs.size() > 0) {
                                commandName = realArgs.remove(0);
                            }

                            monitor.beginTask(commandName, 1000);

                            // Call issuing server command callback, this is
                            // required since execQuietMapCmdList is use and so it
                            // doesn't log anything
                            P4JavaCallback p4jCallback = P4Workspace
                                    .getWorkspace().getCallback();
                            if (p4jCallback != null) {
                                StringBuffer executing = new StringBuffer();
                                executing.append(commandName);
                                for (String arg : realArgs) {
                                    executing.append(" "); //$NON-NLS-1$
                                    executing.append(arg);
                                }
                                p4jCallback.issuingServerCommand(-1,
                                        executing.toString());
                            }

                            List<IFileSpec> fileSpecs=new ArrayList<IFileSpec>();
                        	int serverVer = server.getServerVersionNumber();
                        	if(serverVer>=IP4ServerConstants.PROGRESS_SERVERID_VERSION && ("sync".equalsIgnoreCase(commandName)||"submit".equalsIgnoreCase(commandName))){
                        		P4ProgressListener progListener=P4CoreUtils.createStreamCallback(connection, commandName, new SubProgressMonitor(monitor, 500));
                        		server.execStreamingMapCommand(commandName,
                        				realArgs.toArray(new String[0]), null, progListener, P4CoreUtils.getRandomInt());
                        		fileSpecs = progListener.getFileSpecs();
                        	}else{
                        		if(server instanceof IOptionsServer){
    								 List<Map<String, Object>> output = ((IOptionsServer) server).execQuietMapCmdList(commandName,
    										realArgs.toArray(new String[0]), null);
    								if (commandCallback != null) {
    									commandCallback.callback(output);
    								}
    								fileSpecs = P4CoreUtils.extractFileSpecs(p4Connection, output);
                        		}
								monitor.worked(500);                        	
                        	}
                            if (refresh) {
                            	monitor.setTaskName("Refresh resources...");
                                refreshResources(fileSpecs);
                                monitor.worked(500);
                            }
                        } catch (P4JavaException e) {
                            String message = e.getMessage();
                            if (message != null) {
                                if (commandCallback != null) {
                                	List<Map<String, Object>> output = new ArrayList<Map<String, Object>>();
                                	output.add(new HashMap<String, Object>());
                                    output.get(0).put("Error", message); //$NON-NLS-1$
                                    commandCallback.callbackError(output);
                                }
                            }
                            PerforceProviderPlugin.logError(e);
                        }
                        monitor.done();
                    }
                }
            }

        });
    }

    private void refreshResources(List<IFileSpec> specs) {
        P4Collection resources = new P4Collection();
        for (IFileSpec spec: specs) {
            if (FileSpecOpStatus.VALID == spec.getOpStatus()) {
                String path = spec.getDepotPathString();
                if (path == null) {
                    path = P4Resource.normalizeLocalPath(spec);
                }
                resources.add(connection.getResource(path));
            }
        }
        resources.refresh();
    }
}
