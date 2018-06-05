package com.perforce.team.ui.PropertyTesters;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.preference.IPreferenceStore;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.streams.StreamUtil;
import com.perforce.team.ui.streams.StreamsView;
import com.perforce.team.ui.streams.StreamsViewControl;

/**
 * Property tester for stream related actions.
 * 
 * @author ali
 *
 */
public class StreamPropertyTester extends PropertyTester {

	public static String CAN_COPY_TO_STREAM="canCopyToStream"; //$NON-NLS-1$
	public static String CAN_MERGE_TO_STREAM="canMergeToStream"; //$NON-NLS-1$
	public static String CAN_CREATE_STREAM_FROM="canCreateStreamFrom"; //$NON-NLS-1$
	public static String CAN_WORKIN="canWorkinStream"; //$NON-NLS-1$
	public static String CAN_RELOAD="canReload"; //$NON-NLS-1$
	public static String CAN_UNLOAD="canUnload"; //$NON-NLS-1$

	public StreamPropertyTester() {
    }

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
    	boolean enable=false;
    	if(receiver instanceof IP4Stream){
    		IP4Stream p4stream = (IP4Stream) receiver;

    		IStreamSummary targetStream = p4stream.getStreamSummary();
    		String clientStream = p4stream.getConnection().getClient().getStream();
    		
    		IP4Connection conn = p4stream.getConnection();
    		if(CAN_COPY_TO_STREAM.equals(property) || CAN_MERGE_TO_STREAM.equals(property)){
    			if(CAN_COPY_TO_STREAM.equals(property) && targetStream.getType()==Type.TASK){
    				return false; // task stream can only be merged from parent.
    			}
    			String parent = targetStream.getParent();
				if (CAN_MERGE_TO_STREAM.equals(property)
						&& targetStream.getType() == Type.TASK
						&& (StreamUtil.DEFAULT_PARENT.equals(parent) || StringUtils
								.isEmpty(parent))) {
					return false; // parentless task stream can not be merged to from parent.
				}
    			// already in target stream?
        		if(targetStream.getStream().equals(clientStream)){
        			enable=true;
        		}else if(conn!=null && conn.isSandbox()){ // sandbox server
        			enable=true;
        		}else{ // change client spec?
        		    IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
        		    String opt=store.getString(IPerforceUIConstants.PREF_CLIENT_SWITCH_ON_STREAM_OPERATOIN);
        			if(IPerforceUIConstants.STREAM_SWITCH_STREAM_WITH_SAME_CLIENT.equals(opt)){
        				enable=true;
        			}
        		}
    		}else if(CAN_CREATE_STREAM_FROM.equals(property)){
    			if(Type.TASK!=targetStream.getType()){
    				enable=true;
    			}
    		}else if(CAN_RELOAD.equals(property)){
    			if(Type.TASK==targetStream.getType()){
	    			StreamsView view = StreamsView.findView();
	    			if(view!=null){
	    				StreamsViewControl control = view.getPerforceViewControl();
	    				if(control!=null){
	    					if(control.isShownUnloadedOnly()){
	    						enable=true;
	    					}
	    				}
	    			}
    			}
    		}else if(CAN_UNLOAD.equals(property)){
    			if(Type.TASK==targetStream.getType()){
    				enable=true;
    			}
    		}else if(CAN_WORKIN.equals(property)){
        		if(!targetStream.getStream().equals(clientStream))
        			enable=true;
    		}
    	}
    	return enable;
    }

}
