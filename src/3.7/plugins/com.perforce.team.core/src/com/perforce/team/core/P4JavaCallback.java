/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;

import com.perforce.p4java.exception.MessageSeverityCode;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4CommandListener;
import com.perforce.team.core.p4java.IP4ProgressListener;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * An implementation of few callback interfaces.
 * 
 * @author Alex Li (ali@perforce.com)
 * 
 */
public class P4JavaCallback implements IP4JavaCallback {
    /**
     * The environment variable name for defining the SSO script / app path.
     */
    public static final String SSO_CMD_ENV_KEY = "P4LOGINSSO"; //$NON-NLS-1$

	private ListenerList commandListeners = new ListenerList();
	private ListenerList progressListeners = new ListenerList();
	
	private String currentCommand=null;
	
    /**
     * Adds a command listener
     * 
     * @param listener
     */
    public void addCommandListener(IP4CommandListener listener) {
        if (listener != null) {
            this.commandListeners.add(listener);
        }
    }
    
    /**
     * Removes a command listener
     * 
     * @param listener
     */
    public void removeCommandListener(IP4CommandListener listener) {
        if (listener != null) {
            this.commandListeners.remove(listener);
        }
    }
    
    /**
     * Adds a report listener
     * 
     * @param listener
     */
    public void addProgressListener(IP4ProgressListener listener) {
        if (listener != null) {
            this.progressListeners.add(listener);
        }
    }
    
    /**
     * Removes a command listener
     * 
     * @param listener
     */
    public void removeProgressListener(IP4ProgressListener listener) {
        if (listener != null) {
            this.progressListeners.remove(listener);
        }
    }    

	@Override
	public boolean startResults(int key) throws P4JavaException {
		Object[] listeners = progressListeners.getListeners();
		boolean moveon=true;
		for (int i = 0; i < listeners.length; i++) {
			moveon=((IP4ProgressListener) listeners[i]).startResults(key);
			if(!moveon)
				break;
		}
		return moveon;
	}

	@Override
	public boolean endResults(int key) throws P4JavaException {
		Object[] listeners = progressListeners.getListeners();
		boolean moveon=true;
		for (int i = 0; i < listeners.length; i++) {
			moveon=((IP4ProgressListener) listeners[i]).endResults(key);
			if(!moveon)
				break;
		}
		return moveon;
	}

	@Override
	public boolean handleResult(Map<String, Object> resultMap, int key)
			throws P4JavaException {
		Object[] listeners = progressListeners.getListeners();
		boolean moveon=true;
		for (int i = 0; i < listeners.length; i++) {
			moveon=((IP4ProgressListener) listeners[i]).handleResult(resultMap, key);
			if(!moveon)
				break;
		}
		return moveon;
	}

	public void issuingServerCommand(int key, String commandString) {
		if (commandString == null) {
			return;
		}
		currentCommand=commandString;
		
		Object[] listeners = commandListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IP4CommandListener) listeners[i]).command(key, currentCommand);
		}
	}

	public void completedServerCommand(int key, long millisecsTaken) {
    	currentCommand=null;
  }

	public void receivedServerErrorLine(int key, String errorLine) {
		if (errorLine == null) {
			return;
		}
		Object[] listeners = commandListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IP4CommandListener) listeners[i]).error(key, errorLine);
		}
	}

	public void receivedServerInfoLine(int key, String infoLine) {
		if (infoLine == null) {
			return;
		}
		Object[] listeners = commandListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IP4CommandListener) listeners[i]).info(key, infoLine);
		}
	}

	public void receivedServerMessage(int key, int genericCode,
			int severityCode, String message) {
		if (message == null) {
			return;
		}
		if (MessageSeverityCode.E_WARN == severityCode) {
			Object[] listeners = commandListeners.getListeners();
			for (int i = 0; i < listeners.length; i++) {
				((IP4CommandListener) listeners[i]).info(key, message);
			}
		}
	}
	
    private void stripTrailingNewline(StringBuffer buffer) {
        // Strip trailing newline
        int length = buffer.length();
        if (length > 0) {
            if (buffer.charAt(length - 1) == '\n') {
                buffer.deleteCharAt(length - 1);
            }
        }
        length = buffer.length();
        if (length > 0) {
            if (buffer.charAt(length - 1) == '\r') {
                buffer.deleteCharAt(length - 1);
            }
        }
    }

    /**
     * @see com.perforce.p4java.server.callback.ISSOCallback#getSSOCredentials(java.lang.StringBuffer,
     *      java.lang.String, java.lang.String)
     */
    public Status getSSOCredentials(StringBuffer credBuffer, String ssoKey,
            String userName) {
    	final String USER="%user%";
    	final String SERVER_ADDRESS="%serverAddress%";
    	
        Status status = Status.UNSET;
        if (credBuffer == null) {
            throw new NullPointerException(
                    "null credBuffer passed to SSO callback");
        }

        String execCmd = System.getenv(SSO_CMD_ENV_KEY);
        if (execCmd == null) {
            execCmd = P4Workspace.getWorkspace().getAdvancedProperties()
                    .getProperty(SSO_CMD_ENV_KEY);
        }

        if (execCmd != null && execCmd.length() > 0) {
            boolean hasUser=execCmd.contains(USER);
            if(hasUser){
                if (userName != null) {
                    execCmd=execCmd.replaceAll(USER, userName);
                }
            }

            boolean hasServer=execCmd.contains(SERVER_ADDRESS);
            if(hasServer){
            	if (ssoKey != null) {
                    execCmd=execCmd.replaceAll(SERVER_ADDRESS, ssoKey);
                }
            }

            List<String> cmdList = new ArrayList<String>();
            String[] args = execCmd.split(" ");
            for(int i=0;i<args.length;i++){
            	cmdList.add(args[i]);
            }
            
            // following are just for backward compatibility
            if (userName != null) {
                cmdList.add(userName);
            }
            if (ssoKey != null) {
                cmdList.add(ssoKey);
            }

            ProcessBuilder processBuilder = new ProcessBuilder(cmdList);
            Process proc = null;
            InputStream inStream = null;
            int exitCode = 0;

            try {
                proc = processBuilder.start();
                inStream = proc.getInputStream();
                int bytesRead = 0;
                byte[] bytes = new byte[1024];
                while ((bytesRead = inStream.read(bytes)) >= 0) {
                    credBuffer.append(new String(bytes, 0, bytesRead, Charset.defaultCharset()));
                }
                stripTrailingNewline(credBuffer);
                exitCode = proc.waitFor();
                if (exitCode == 0) {
                    status = Status.PASS;
                } else {
                    status = Status.FAIL;
                }
            } catch (IOException exc) {
                PerforceProviderPlugin.logError(
                        "Error running P4LOGINSSO executable", exc);
                status = Status.PASS;
            } catch (InterruptedException exc) {
                PerforceProviderPlugin.logError(
                        "Error running P4LOGINSSO executable", exc);
                status = Status.PASS;
            } finally {
                if (inStream != null) {
                    try {
                        inStream.close();
                    } catch (IOException exc) {
                        // Ignore
                    }
                }
            }
        } else {
            PerforceProviderPlugin
                    .logError("SSO login not attempted because P4LOGINSSO is not a set environment variable");
        }
        return status;
    }

}
