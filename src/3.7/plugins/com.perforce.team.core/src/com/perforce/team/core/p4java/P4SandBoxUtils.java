package com.perforce.team.core.p4java;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.server.IOptionsServer;

public class P4SandBoxUtils {

	public static List<IFileSpec> pull(IP4Connection connection) {
    	IOptionsServer server = (IOptionsServer) connection.getServer();
    	List<IFileSpec> specList = new ArrayList<IFileSpec>();
    	try {
			List<Map<String, Object>> result = server.execMapCmdList("pull", new String[]{"//streams/mirror"}, null);
			if (result != null) {
				for (Map<String, Object> map : result) {
			    	if(server instanceof Server)
					specList.add(((Server)server).handleFileReturn(map, connection.getClient()));
				}
			}

		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequestException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (P4JavaException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return specList;

	}
}
