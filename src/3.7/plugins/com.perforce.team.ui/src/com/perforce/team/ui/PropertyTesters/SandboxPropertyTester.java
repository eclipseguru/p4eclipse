package com.perforce.team.ui.PropertyTesters;

import org.eclipse.core.expressions.PropertyTester;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;

public class SandboxPropertyTester extends PropertyTester {

	public static String CAN_PULL_FROM_SERVER="canPullFromServer";
	
	public SandboxPropertyTester() {
		// TODO Auto-generated constructor stub
	}

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
    	if(receiver instanceof IP4Stream){
    		IP4Stream stream = (IP4Stream) receiver;
    		IP4Connection conn = stream.getConnection();
    		if(CAN_PULL_FROM_SERVER.equals(property)){
        		if(IP4Resource.MIRROR_STREAM.equals(stream.getStreamSummary().getStream())){
		    		if(conn!=null && conn.isSandbox())
		    			return true;
        		}
    		}
    	}
        return false;
    }
}
