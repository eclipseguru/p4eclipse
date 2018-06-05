/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import com.perforce.team.ui.p4merge.ApplicationRunner;
import com.perforce.team.ui.p4merge.IP4VConstants;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4VRunner extends ApplicationRunner {

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getBuilder()
     */
    @Override
    protected ProcessBuilder getBuilder() {
        List<String> arguments = new ArrayList<String>();
        arguments.add(getExecutable());

        addConnection(arguments);
        
        addCommandOpt(arguments);
        
        if (shouldConvertExec()) {
            convertExec(arguments);
        }

        return new ProcessBuilder(arguments);
    }

    protected String getExecutable() {
    	return getPreferenceValue();
	}

	protected void addCommandOpt(List<String> arguments) {
        arguments.add(IP4VConstants.CMD);
        List<String> args=getCommand();
        for(int i=0;i<args.size();i++){
            arguments.add(args.get(i));
        }
	}

	protected void addConnection(List<String> arguments) {
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
	}

	/**
     * Get the p4v command
     * 
     * @return - p4v cmd
     */
    protected abstract List<String> getCommand();

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getPreference()
     */
    @Override
    protected String getPreference() {
        return IPreferenceConstants.P4V_PATH;
    }

}
