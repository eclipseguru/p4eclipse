package com.perforce.team.ui.views;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * Utils for views. 
 * 
 * <ul>
 *   <li>Providing view comparator mainly for depot view and pending view.</li>
 * </ul>
 * 	
 */
public class ViewUtil {
	private static PendingViewComparator pendindingComparator=new PendingViewComparator();
	private static DepotViewComparator depotComparator=new DepotViewComparator();
	
	private static ViewerComparator viewComparator=new ViewerComparator(){
    	@Override
    	public int compare(Viewer viewer, Object e1, Object e2) {
    		if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
    			IP4Job job1 = (IP4Job) e1;
    			IP4Job job2 = (IP4Job) e2;
    			if (job1.getId() != null && job2.getId() != null) {
    				return job1.getId().compareTo(job2.getId());
    			}
    		} else if (e1 instanceof IP4Job && e2 instanceof IP4File) {
    			return 1;
    		} else if (e1 instanceof IP4File && e2 instanceof IP4Job) {
    			return -1;
    		} else if (e1 instanceof IP4File && e2 instanceof IP4Folder) {
    			return 1;
    		} else if (e1 instanceof IP4Folder && e2 instanceof IP4File) {
    			return -1;
    		} else if (e1 instanceof IP4PendingChangelist
    				&& e2 instanceof IP4PendingChangelist) {
    			IP4PendingChangelist o1 = (IP4PendingChangelist) e1;
    			IP4PendingChangelist o2 = (IP4PendingChangelist) e2;
    			if (o1.isOnClient() && !o2.isOnClient()) {
    				return -1;
    			} else if (!o1.isOnClient() && o2.isOnClient()) {
    				return 1;
    			} else if ((o1.isReadOnly() && o2.isReadOnly())
    					|| (!o1.isReadOnly() && !o2.isReadOnly())) {
    				if (o1.isDefault() && !o2.isDefault()) {
    					return -1;
    				} else if (!o1.isDefault() && o2.isDefault()) {
    					return 1;
    				} else if (!o1.isDefault() && !o2.isDefault()) {
    					return o1.getId() - o2.getId();
    				}
    			} else if (o1.isReadOnly() && !o2.isReadOnly()) {
    				return 1;
    			} else if (o2.isReadOnly() && !o1.isReadOnly()) {
    				return -1;
    			} else if (o1.isDefault() && !o2.isDefault()) {
    				return -1;
    			} else if (o2.isDefault() && !o1.isDefault()) {
    				return 1;
    			}
    		} else if (e1 instanceof IP4File
    				&& e2 instanceof IP4ShelvedChangelist) {
    			return -1;
    		} else if (e1 instanceof IP4Job
    				&& e2 instanceof IP4ShelvedChangelist) {
    			return -1;
    		} else if (e1 instanceof IP4ShelvedChangelist
    				&& e2 instanceof IP4File) {
    			return 1;
    		} else if (e1 instanceof IP4ShelvedChangelist
    				&& e2 instanceof IP4Job) {
    			return 1;
    		} else if (e1 instanceof IP4File && e2 instanceof IP4File){
    			String p1 = ((IP4File) e1).getActionPath();
    			String p2 = ((IP4File) e2).getActionPath();
    			if(p1!=null && p2!=null)
    				return p1.compareTo(p2);
    		}else if (e1 instanceof IP4Connection && e2 instanceof IP4Connection) {
    			return e1.toString().compareTo(e2.toString());
    		}else if (e1 instanceof IP4Resource && e2 instanceof IP4Resource) {
    			String n1 = ((IP4Resource) e1).getName();
    			String n2 = ((IP4Resource) e2).getName();
    			if(n1!=null && n2!=null)
    				return n1.compareTo(n2);
    		}
    		return 0;
    	}
    };

	public static IP4PendingChangelist[]  getPendingChangelists(IP4Connection connection, boolean showOtherChanges){
    	IP4PendingChangelist[] list = connection.getPendingChangelists(showOtherChanges);
    	return list;
    }
	public static IP4Connection[] getConnections(boolean filterClientFiles, boolean showDeletedFiles){
        IP4Connection[] connections = P4ConnectionManager.getManager()
                .getConnections();
        for (IP4Connection connection : connections) {
            connection.setShowClientOnly(filterClientFiles);
            connection.setShowFoldersWIthOnlyDeletedFiles(showDeletedFiles);
        }
        return connections;
	}
	public static void sortPendingViewMembers(IP4Resource[] members) {
		if(members!=null)
			Arrays.sort(members,pendindingComparator);
	}
	public static void sortDepotViewMembers(IP4Connection[] members) {
		if(members!=null)
			Arrays.sort(members,depotComparator);
	}
	public static ViewerComparator getViewComparator() {
		return viewComparator;
	}

}

class DepotViewComparator implements Comparator<IP4Resource>, Serializable{

	private static final long serialVersionUID = -1573637558066587373L;

	public int compare(IP4Resource e1, IP4Resource e2) {
        if (e1 instanceof IP4Connection && e2 instanceof IP4Connection) {
            return e1.toString().compareTo(e2.toString());
        } else {
            return 0;
        }
	}
	
}

class PendingViewComparator implements Comparator<IP4Resource>, Serializable{
	
	private static final long serialVersionUID = 1L;

	public int compare(IP4Resource e1, IP4Resource e2) {
		if (e1 instanceof IP4Job && e2 instanceof IP4Job) {
			IP4Job job1 = (IP4Job) e1;
			IP4Job job2 = (IP4Job) e2;
			if (job1.getId() != null && job2.getId() != null) {
				return job1.getId().compareTo(job2.getId());
			}
		} else if (e1 instanceof IP4Job && e2 instanceof IP4File) {
			return 1;
		} else if (e1 instanceof IP4File && e2 instanceof IP4Job) {
			return -1;
		} else if (e1 instanceof IP4PendingChangelist
				&& e2 instanceof IP4PendingChangelist) {
			IP4PendingChangelist o1 = (IP4PendingChangelist) e1;
			IP4PendingChangelist o2 = (IP4PendingChangelist) e2;
			if (o1.isOnClient() && !o2.isOnClient()) {
				return -1;
			} else if (!o1.isOnClient() && o2.isOnClient()) {
				return 1;
			} else if ((o1.isReadOnly() && o2.isReadOnly())
					|| (!o1.isReadOnly() && !o2.isReadOnly())) {
				if (o1.isDefault() && !o2.isDefault()) {
					return -1;
				} else if (!o1.isDefault() && o2.isDefault()) {
					return 1;
				} else if (!o1.isDefault() && !o2.isDefault()) {
					return o1.getId() - o2.getId();
				}
			} else if (o1.isReadOnly() && !o2.isReadOnly()) {
				return 1;
			} else if (o2.isReadOnly() && !o1.isReadOnly()) {
				return -1;
			} else if (o1.isDefault() && !o2.isDefault()) {
				return -1;
			} else if (o2.isDefault() && !o1.isDefault()) {
				return 1;
			}
		} else if (e1 instanceof IP4File
				&& e2 instanceof IP4ShelvedChangelist) {
			return -1;
		} else if (e1 instanceof IP4Job
				&& e2 instanceof IP4ShelvedChangelist) {
			return -1;
		} else if (e1 instanceof IP4ShelvedChangelist
				&& e2 instanceof IP4File) {
			return 1;
		} else if (e1 instanceof IP4ShelvedChangelist
				&& e2 instanceof IP4Job) {
			return 1;
		} else if (e1 instanceof IP4File && e2 instanceof IP4File){
			String p1 = e1.getActionPath();
			String p2 = e2.getActionPath();
			if(p1!=null && p2!=null)
				return p1.compareTo(p2);
		}
		return 0;
	}
	
}
