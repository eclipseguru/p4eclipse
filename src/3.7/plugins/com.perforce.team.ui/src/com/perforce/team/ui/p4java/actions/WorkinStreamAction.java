package com.perforce.team.ui.p4java.actions;

import java.io.File;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.client.IClientSummary.ClientLineEnd;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.client.IClientSummary.IClientSubmitOptions;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.client.ClientOptions;
import com.perforce.p4java.impl.generic.client.ClientSubmitOptions;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.mapbased.client.Client;
import com.perforce.p4java.option.server.GetClientsOptions;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4TeamUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.SwitchStreamDialog;
import com.perforce.team.ui.server.EditClientDialog;
import com.perforce.team.ui.server.NewClientDialog;
import com.perforce.team.ui.streams.StreamUtil;


public class WorkinStreamAction extends AbstractStreamAction {
	
    public void workinStream(IP4Stream target){
        if (target != null) {
        	doRun(target);
        }
    }

	protected IClient createNewClient(IP4Connection connection,
			String stream, List<IClientSummary> clients) {
		IServerInfo info = connection.getServerInfo();
		if(info==null){
			connection.refresh();
			info=connection.getServerInfo();
		}
		if(info==null)
			return null;
		
		// try to generate client name like: ali_ubuntn_hh_3232
		String user = info.getUserName();
		String host = info.getClientHost();
		String streamSeg=StreamUtil.parseStream(stream)[1];
		StringBuilder sb = new StringBuilder();
		sb.append(user);
		sb.append("_"); //$NON-NLS-1$
		sb.append(host);
		sb.append("_"); //$NON-NLS-1$
		sb.append(streamSeg);
		sb.append("_"); //$NON-NLS-1$
		String clientName=sb.toString();
		int count=0;
		Random random=new Random();
		while(true && count<10){
			count++;
			int serial = 1000+random.nextInt(9000); // a random number 1000<r<9999
			String name=clientName+serial;
			boolean existed=false;
			for(IClientSummary c: clients){
				if(name.equals(c.getName())){
					existed=true;
					break;
				}
			}
			if(!existed){
				clientName=name;
				break;
			}
		}		
		
		String clientRoot=getPrefStore().getString(IPerforceUIConstants.PREF_CLIENT_ROOT_PARENT_DEFAULT)+File.separator+clientName;
		IClientOptions options=new ClientOptions();
		IClientSubmitOptions submitOptions= new ClientSubmitOptions();
		ClientView clientView=new ClientView();
		
		IClient candidate = new Client(clientName, new Date(), new Date(),
				Messages.WorkinStreamAction_CreatedBy + user, host, user, clientRoot,
				ClientLineEnd.LOCAL, options, submitOptions, null,
				connection.getServer(), clientView, stream);
		try {
			NewClientDialog dialog = new NewClientDialog(getShell(),
					connection, candidate);
			if (EditClientDialog.OK == dialog.open()) {
				return dialog.getEditedSpec();
			}else
				return null;
		} catch (P4JavaException e) {
            MessageDialog.openError(getShell(),
                    Messages.EditClientAction_ClientNotFoundTitle,
                    Messages.EditClientAction_ClientNotFoundMessage);
            return null;
		}
	}

	@Override
	protected void doRun(IP4Resource target) {
        if (target != null) {

        	if(isResourceInClientStream(target))
        		return;
        	
        	// p4 client -f -s -S targetStream clientName
        	final String stream = getStreamFromResource(target);
        	if(stream==null||stream.isEmpty()||stream.equals(StreamUtil.DEFAULT_PARENT))
        		return;
    		
        	final IP4Connection connection = target.getConnection();
        	
        	final String jobTitle=MessageFormat.format(Messages.WorkinStreamAction_SwitchToStream, stream);
        	
        	final Shell shell=getShell();
        	
	        P4Runner.schedule(new P4Runnable() {

	            @Override
	            public String getTitle() {
	                return jobTitle;
	            }

	            @Override
	            public void run(IProgressMonitor monitor) {
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
	            	GetClientsOptions opts = new GetClientsOptions();
	            	opts.setUserName(connection.getUser());
	            	opts.setStream(stream);
	            	opts.setMaxResults(100);
	            	final List<IClientSummary> clients = connection.getClients(opts);
	            	
	            	final IClientSummary[] clientToSwitch=new IClientSummary[1];
	            	
	            	final String switchStreamHow=getPrefStore().getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN);
	            	final boolean noWarn = getPrefStore().getBoolean(IPerforceUIConstants.PREF_CLIENT_SWITCH_NO_WARN);
	            	
	            	
	                PerforceUIPlugin.syncExec(new Runnable() {
	                    public void run() {
	                    	final String NO_WARN_TEXT=Messages.WorkinStreamAction_DontWarnWheSwitchStream;
	                    	if(IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT.equals(switchStreamHow)){
	                    		String message=Messages.WorkinStreamAction_AboutToSwitchWorkspace;
	                    		int code=SwitchStreamDialog.open(MessageDialog.QUESTION, getShell(), Messages.WorkinStreamAction_SwitchStream, message, new String[]{IDialogConstants.YES_LABEL,IDialogConstants.NO_LABEL}, new int[]{IDialogConstants.YES_ID, IDialogConstants.NO_ID}, 0);
	                    		if(code==IDialogConstants.YES_ID){
	                    			IClient c = connection.getClient();
	                    			c.setStream(stream);
	                    			clientToSwitch[0] = c;
	                    			connection.updateClient(c);
	                    		}
	                    	}else{ // diff stream with diff client
	                    		if(clients.isEmpty()){
	                    			if(noWarn){
	                    				clientToSwitch[0] = createNewClient(connection, stream, clients);
	                    			}else{
	                    				String message=MessageFormat.format(Messages.WorkinStreamAction_MustSwitchWorkspaceToWorkWithStream,stream);
	    	                    		int code=SwitchStreamDialog.open(MessageDialog.QUESTION, getShell(), Messages.WorkinStreamAction_SwitchStream, message, NO_WARN_TEXT, new String[]{Messages.WorkinStreamAction_NewWorkspace,IDialogConstants.CANCEL_LABEL}, new int[]{IDialogConstants.YES_ID, IDialogConstants.NO_ID}, 0);
	    	                    		if(code==IDialogConstants.YES_ID){
	    	                    			clientToSwitch[0] = createNewClient(connection, stream, clients);
	    	                    		}
	                    			}
	                    		}else{ // maybe we should allow user to choose which one?
	                    			IClientSummary c = clients.get(0);
	                    			if(noWarn){
    	                    			clientToSwitch[0] = c;	                    				
	                    			}else{
		                    			String message=MessageFormat.format(Messages.WorkinStreamAction_MustSwitchWorkspaceByCreateNew,stream, c.getName());
	    	                    		int code=SwitchStreamDialog.open(MessageDialog.QUESTION, getShell(), Messages.WorkinStreamAction_SwitchStream, message, NO_WARN_TEXT, new String[]{Messages.WorkinStreamAction_SwitchWorkspace,Messages.WorkinStreamAction_NewWorkspace,IDialogConstants.CANCEL_LABEL}, new int[]{IDialogConstants.OK_ID, IDialogConstants.YES_ID, IDialogConstants.NO_ID}, 0);
	    	                    		if(code==IDialogConstants.OK_ID){ // switch workspace
	    	                    			clientToSwitch[0] = c;
	    	                    		}else if(code==IDialogConstants.YES_ID){ // new workspace
	    	                    			clientToSwitch[0] = createNewClient(connection, stream, clients);
	    	                    		}
	                    			}
	                    		}
	                    	}
	                    }
	                });

	                if(clientToSwitch[0]==null)
	                	return;

	                /*
	                 * Note: The way P4ConnectionManager manages connection is keyed by ConnectionParameters, which
	                 * could change when Client is changed. So here we call getConnection() to add new connections
	                 * to the manager, and for unchanged connection, this has not effect.
	                 */
	                ConnectionParameters newParam = new ConnectionParameters(connection.getParameters().toString());
	                newParam.setClient(clientToSwitch[0].getName());
	                IP4Connection newConn = P4ConnectionManager.getManager().getConnection(newParam);
	                
	                newConn.refreshClient();
	                P4TeamUtils.processClientChange(newConn, shell,true, MessageFormat.format(com.perforce.team.ui.p4java.actions.Messages.AbstractStreamAction_StreamSwitchedDesc, stream));
                    newConn.markForRefresh();
                    P4ConnectionManager.getManager().notifyListeners(
                            new P4Event(EventType.REFRESHED, newConn));
	            }
	        });
        }
	}

	protected IPreferenceStore getPrefStore() {
    	return PerforceUIPlugin.getPlugin().getPreferenceStore();
	}

}
