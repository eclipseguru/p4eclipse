/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.perforce.p4java.CharsetDefs;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.connection.ConnectionWizard;
import com.perforce.team.ui.connection.ConnectionWizardDialog;
import com.perforce.team.ui.p4merge.ApplicationRunner;
import com.perforce.team.ui.p4merge.IP4VConstants;
import com.perforce.team.ui.preferences.IPreferenceConstants;

public class P4SandboxConfigRunner extends ApplicationRunner {

    private IP4Connection connection = null;

    /**
     * @param connection
     */
    public P4SandboxConfigRunner(IP4Connection connection) {
        this.connection = connection;
    }

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

        arguments.add(IP4VConstants.PORT);
        arguments.add(getConnection().getParameters().getPort());
        arguments.add(IP4VConstants.USER);
        arguments.add(getConnection().getParameters().getUser());
        arguments.add(IP4VConstants.CLIENT);
        arguments.add(getConnection().getParameters().getClient());

        arguments.add(IP4VConstants.CHARSET);
        String charset = getConnection().getParameters().getCharset();
        if (charset == null) {
            charset = IP4VConstants.NONE;
        }
        arguments.add(charset);

        if (shouldConvertExec()) {
            convertExec(arguments);
        }

        return new ProcessBuilder(arguments);
    }


    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getPreference()
     */
    @Override
    protected String getPreference() {
        return IPreferenceConstants.P4SANDBOXCONFIG_PATH;
    }

    @Override
    protected boolean loadFiles() {
        return true;
    }

    @Override
    protected void applicationFinished(int exitCode) {
        if (exitCode != 0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager.getManager().openInformation(
                            P4UIUtils.getShell(),
                            Messages.P4SandboxConfigRunner_ErrorTitle,
                            Messages.P4SandboxConfigRunner_ErrorMessage);
                }
            });
        } else {
            InputStream stdout = this.getProcess().getInputStream();
            BufferedReader reader = new BufferedReader (new InputStreamReader(stdout,CharsetDefs.DEFAULT));
            StringBuffer sb= new StringBuffer();
            try {
                String line;
                while ((line = reader.readLine ()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
            }
            
            String parts[] = sb.toString().trim().split(" ");
            Map<String,String> config = new HashMap<String,String>();
            for (int i = 0; i + 1 < parts.length; i += 2) {
                config.put(parts[i], parts[i+1]);
            }
            final String port = config.get("-p");
            final String user = config.get("-u");
            final String client = config.get("-c");
            final String charset = config.get("-C");
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
            ConnectionWizardDialog dialog = new ConnectionWizardDialog(
                    P4UIUtils.getShell(), new ConnectionWizard(port, user, client, charset));
            dialog.open();
                }
            });
        }
    }

    @Override
    protected String getApplicationName() {
        return Messages.P4SandboxConfigRunner_Title;
    }

}
