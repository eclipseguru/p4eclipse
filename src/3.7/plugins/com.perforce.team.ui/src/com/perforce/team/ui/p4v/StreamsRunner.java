/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4merge.IP4VConstants;

public class StreamsRunner extends P4VRunner {

    private IP4Connection connection = null;
	private String exec;
	private boolean useP4vc=false;

    /**
     * @param connection
     */
    public StreamsRunner(IP4Connection connection) {
        this.connection = connection;
    }

	protected void addCommandOpt(List<String> arguments) {
        List<String> args=new ArrayList<String>();
        if(useP4vc){
        	args.add("streamgraph");
        }else{
        	arguments.add(IP4VConstants.CMD);
            args.add("streamsGraph");/*"streams";*/ //$NON-NLS-1$
            args.add("flow");//$NON-NLS-1$
        }
        for(int i=0;i<args.size();i++){
            arguments.add(args.get(i));
        }
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
                            Messages.StreamsRunner_ErrorTitle,
                            Messages.StreamsRunner_ErrorMessage);
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
        return Messages.StreamsRunner_Title;
    }

    /**
     * @see com.perforce.team.ui.p4merge.ApplicationRunner#getTaskName()
     */
    @Override
    protected String getTaskName() {
        return Messages.StreamsRunner_Title;
    }

    @Override
    protected String getExecutable() {
    	if(exec==null){
	        String p4vPath = getPreferenceStore().getString(getPreference());
	        try {
	        	// try to find p4vc in the same folder
	        	File parent = new File(p4vPath).getParentFile();
	        	if(parent!=null && parent.exists()){
	        		File[] children = parent.listFiles(new FileFilter() {
	        			
	        			public boolean accept(File f) {
	        				if(f.getName().contains("p4vc"))
	        					return true;
	        				return false;
	        			}
	        		});
	        		if(children!=null){
	        			for(File c: children){
	        				if(c.canExecute()){
	        					exec=c.getAbsolutePath();
	        					useP4vc=true;
	        					break;
	        				}
	        			}
	        		}
	        	}
			} catch (Exception e) {
				PerforceProviderPlugin.logError(e);
			}
	        if(exec==null)
	        	exec=p4vPath;
    	}
    	return exec;
    }

    /**
     * @see com.perforce.team.ui.p4v.P4VRunner#getCommand()
     */
    @Override
    protected List<String> getCommand() {
    	// see addCommandOpt(), this is bypassed.
        return Collections.emptyList();
    }

}
