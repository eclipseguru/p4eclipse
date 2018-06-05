/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.core.IDepot.DepotType;
import com.perforce.p4java.server.IServer;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IP4ServerConstants;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4IntegrationOptions3;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.dialogs.SwitchClientConfirmDialog;
import com.perforce.team.ui.p4java.actions.SyncAction;

/**
 * Perforce UI-Team utility methods.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4TeamUtils {

    /**
     * Share a project with the connection parameters
     * 
     * @param project
     * @param params
     * @return - true if shared
     */
    public static boolean shareProject(IProject project,
            ConnectionParameters params) {
        boolean ret = false;
        if (project != null && params != null) {
            try {
                // Suspend decoration as on Eclipse 3.3 there is an issue with
                // ViewerCell throwing an NPE when setting the text, appears to
                // be fixed in 3.4+
                PerforceDecorator.suspendDecoration();
                ret = PerforceProviderPlugin.manageProject(project, params);
                if (ret) {
                    P4ConnectionManager.getManager().getConnection(project);
                }
            } finally {
                PerforceDecorator.unsuspendDecoration();
            }
            if (ret) {
                PerforceDecorator decorator = PerforceDecorator
                        .getActivePerforceDecorator();
                if (decorator != null) {
                    decorator.resourceStateChanged(new IResource[] { project });
                }
            }
        }
        return ret;
    }

	public static void syncWorkspace(IP4Connection connection, boolean force) {
		SyncAction action = new SyncAction();
		action.setAsync(true);
		action.setForce(true);
		connection.clearCache();
		action.setCollection(action.getCollection(connection.getMappedProjects()));
		action.runAction();		
	}

	public static P4IntegrationOptions createDefaultIntegration(IP4Connection connection) {
		P4IntegrationOptions options=null;
    	IServer server = connection.getServer();
    	String stream=connection.getClient().getStream();
    	if(stream!=null && !stream.trim().isEmpty()){ // stream client always using integ 3, server will take care of this.
    		options = new P4IntegrationOptions3();//IntegOptionWidget.INTEG3;
    	}else{
	    	int serverVer = server.getServerVersionNumber();
	    	String level=null;
			try {
				IServerInfo info = server.getServerInfo();
				level = info.getIntegEngine();
			} catch (Exception e) {
				PerforceProviderPlugin.logError(e.getLocalizedMessage());
				PerforceUIPlugin.syncExec(new Runnable() {
					
					public void run() {
						MessageDialog.openError(null, Messages.P4TeamUtils_Error, Messages.P4TeamUtils_CannotRetrieveEngineInfo);
					}
				});
			}
	    	if(level==null){
	    		if(serverVer<IP4ServerConstants.INTEG3_SERVERID_VERSION){ 
	    			options=new P4IntegrationOptions2(); // older server default to integEngine 2
	    		}else{
	    			options=new P4IntegrationOptions3(); // newer server default to integEngine 3
	    		}
	    	}else{
	    		if("2".equals(level.trim())){ //$NON-NLS-1$
	    			options=new P4IntegrationOptions2(); // older server default to integEngine 2
	    		}else if("3".equals(level.trim())){ //$NON-NLS-1$
	    			options=new P4IntegrationOptions3(); // newer server default to integEngine 3
	    		}else
	    			options=null;
	    	}
    	}
		return options;
	}

	public static String getStream(IP4Resource resource){
		if(resource==null)
			return null;
		
    	if(resource.getParent() instanceof P4Depot){
    		P4Depot depot=(P4Depot) resource.getParent();
    		if(depot.getType()==DepotType.STREAM){
    			return "//"+depot.getName()+"/"+resource.getName();//$NON-NLS-1$ //$NON-NLS-2$,$NON-NLS-2$
    		}
    	}else if(resource.getParent() instanceof P4Connection){
    		P4Connection conn=(P4Connection) resource.getParent();
    		if(!StringUtils.isEmpty(conn.getClient().getStream())){
    			String path = resource.getActionPath(Type.REMOTE);
    			if(path!=null){
    				String[] segments = path.substring(2).split("/"); //$NON-NLS-1$
    				if(segments.length>=2){
    					return "//"+segments[0]+"/"+segments[1]; //$NON-NLS-1$ //$NON-NLS-2$
    				}
    			}
    		}
			return null;
    	}
    	return getStream(resource.getParent());
	}

	public static boolean processClientChange(IP4Connection connection, Shell shell, boolean force, String promptMsg) {
    	boolean switched=false;
    	// Logic to check the prompt option
    	// if prompt
    	// then popup dialog
    	//     if sync
    	//     then do sync
    	//     else nosync
    	IPreferenceStore store =
        		PerforceUIPlugin.getPlugin().getPreferenceStore();
    	String switchOpt = store.getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_OPTION);
    	if(IPerforceUIConstants.NEVER.equals(switchOpt)){
    	}else if(IPerforceUIConstants.PROMPT.equals(switchOpt)){
    		final SwitchClientConfirmDialog switchDlg= new SwitchClientConfirmDialog(shell);
    		if(promptMsg!=null)
    			switchDlg.setDescription(promptMsg);
    		
    		final boolean[] sync=new boolean[]{false};
    		if(shell.getDisplay().getThread()==Thread.currentThread()){
    			if(SwitchClientConfirmDialog.OK==switchDlg.open()){
    				sync[0]=switchDlg.isAutoSync();
    			}
    		}else{
    			PerforceUIPlugin.syncExec(new Runnable() {
					
					public void run() {
		    			if(SwitchClientConfirmDialog.OK==switchDlg.open()){
		    				sync[0]=switchDlg.isAutoSync();
		    			}
					}
				});
    		}
    		if(sync[0]){
    			P4TeamUtils.syncWorkspace(connection, force);
    			switched=true;
    		}
    	}else if(IPerforceUIConstants.ALWAYS.equals(switchOpt)){
    		P4TeamUtils.syncWorkspace(connection,force);
    		switched=true;
    	}
    	if(switched){
    		P4Collection collection = new P4Collection();
    		collection.add(connection);
    		// no need collection.refresh(), since this was done in P4TeamUtils.syncWorkspace() already.
    		collection.refreshLocalResources(IResource.DEPTH_INFINITE);
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.REFRESHED,
                            connection));

    	}
    	return switched;
		
	}
}
