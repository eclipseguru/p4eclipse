package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4TeamUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.SwitchStreamDialog;


public abstract class AbstractStreamAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        return isStreamConnection();
    }
    
    private boolean isStreamConnection() {
		if (containsOnlineConnection()) {
            IP4Resource[] resources = getResourceSelection().members();
            if (resources.length == 1) {
                if (resources[0] != null) {
                    IP4Resource resource = resources[0];
                    IP4Connection conn = resource.getConnection();
                    if(conn!=null && conn.getClient()!=null){
                    	return !StringUtils.isEmpty(conn.getClient().getStream());
                    }
                }
            }
        }
    	return false;
	}

	@Override
    protected void runAction() {
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1) {
            doRun(members[0]);
        }
    }

    abstract protected void doRun(final IP4Resource resource);

    protected boolean switchStreams(final IP4Resource resource, final boolean force) {
    	if(isResourceInClientStream(resource))
    		return true;
    	
    	// p4 client -f -s -S targetStream clientName
    	IP4Connection conn = resource.getConnection();

		final String targetStream=getStreamFromResource(resource);
		
		/**
		 * <ul>
		 * <li>find existing client:
		 * <ul>
		 * <li>if exist, prompt switch client</li>
		 * <li>if not, prompt new client</li>
		 * </ul>
		 * </li>
		 * <li>switch client</li>
		 * <li>refresh resource</li>
		 * </ul>
		 * 
		 */
		
		String message=Messages.WorkinStreamAction_AboutToSwitchWorkspace;
		int code=SwitchStreamDialog.open(MessageDialog.QUESTION, getShell(), Messages.WorkinStreamAction_SwitchStream, message, new String[]{IDialogConstants.YES_LABEL,IDialogConstants.NO_LABEL}, new int[]{IDialogConstants.YES_ID, IDialogConstants.NO_ID}, 0);
		if(code==IDialogConstants.YES_ID){
			IClient c = conn.getClient();
			c.setStream(targetStream);
			conn.updateClient(c);
			conn.refreshClient();
			P4TeamUtils.processClientChange(conn, getShell(),force, MessageFormat.format(Messages.AbstractStreamAction_StreamSwitchedDesc, targetStream));
			conn.markForRefresh();
			P4ConnectionManager.getManager().notifyListeners(new P4Event(EventType.REFRESHED, conn));
			return true;
		}
		
		return false;
		
   }

	protected String getStreamFromResource(IP4Resource resource) {
		String ts = null;
		if(resource instanceof IP4Stream){
			IP4Stream p4stream = (IP4Stream) resource;
			ts = p4stream.getStreamSummary().getStream();
		}else{
			ts=P4TeamUtils.getStream(resource);
		}
		return ts;
	}


	protected boolean isSelectionInClientStream() {
        IP4Resource[] resources = getResourceSelection().members();
        if (resources.length == 1) {
        	if (resources[0] != null) {
        		IP4Resource resource = resources[0];
        		return isResourceInClientStream(resource);
        	}
        }
        return false;
    }
	
	protected static boolean isResourceInClientStream(IP4Resource resource) {
        if (resource.getConnection().isConnected()) {
            IP4Connection conn = resource.getConnection();
            if (conn != null) {
                IClient client = conn.getClient();
                if (client != null) {
                    String stream = client.getStream();
                    if(stream!=null){
                    	if(resource instanceof IP4Stream){
                    		String s = ((IP4Stream) resource).getStreamSummary().getStream();
                    		return stream.equals(s);
                    	}
                        // enable only when the stream contains the selection resources.
                        String resourceRemotePath = resource.getRemotePath();
                        if(resourceRemotePath!=null){ // remote resource
                            if(resourceRemotePath.startsWith(stream))
                                return true;
                        }else{ // local resource 
                            String resourceLocalPath = resource.getLocalPath();
                            if(resourceLocalPath!=null && resourceLocalPath.startsWith(client.getRoot())){
                                return true;
                            }
                        }
                    }
                }

            }
        }
        return false;
    }

    protected boolean isConnectedToSandBox() {
        if (containsOnlineConnection()) {
            IP4Resource[] resources = getResourceSelection().members();
            if (resources.length == 1) {
                if (resources[0] !=null ) {
                    IP4Resource resource = resources[0];
                    // enabled = resource.getHeadRevision() > 0;

                    IP4Connection conn = resource.getConnection();
                    if (conn != null) {
                    	return conn.isSandbox();
                    }
                }
            }
        }
        return false;
    }

    protected boolean isDiffStreamDiffClientSwitch(){
        if (containsOnlineConnection()) {
            IP4Resource[] resources = getResourceSelection().members();
            if (resources.length == 1) {
                if (resources[0] != null) {
        		    IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
        		    String opt=store.getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN);
        			if(IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_DIFF_CLIENT.equals(opt)){
        				return true;
        			}
                }
            }
        }
        return false;
    }

}
