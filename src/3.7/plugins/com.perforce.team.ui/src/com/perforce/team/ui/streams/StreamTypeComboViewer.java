package com.perforce.team.ui.streams;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamSummary.Type;
import com.perforce.p4java.server.IServerInfo;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;


public class StreamTypeComboViewer extends ComboViewer {
    public static String STREAM_DESC_DEV="used for long term projects and major new features";
    public static String STREAM_DESC_MAINLINE="serves as the base or trunk of a stream system";
    public static String STREAM_DESC_RELEASE="used for fix bugs, testing and release distribution";
    public static String STREAM_DESC_VIRTUAL="used to narrow the scope and submit directly to the parent";
    public static String STREAM_DESC_TASK="creates lightweight branch, used for bug fixes and new features";
    
    private static Map<IStreamSummary.Type, String> STREAM_TYPE_MAP=new HashMap<IStreamSummary.Type, String>();
    static{
        STREAM_TYPE_MAP.put(Type.DEVELOPMENT, STREAM_DESC_DEV);
        STREAM_TYPE_MAP.put(Type.MAINLINE, STREAM_DESC_MAINLINE);
        STREAM_TYPE_MAP.put(Type.RELEASE, STREAM_DESC_RELEASE);
        STREAM_TYPE_MAP.put(Type.VIRTUAL, STREAM_DESC_VIRTUAL);
        STREAM_TYPE_MAP.put(Type.TASK, STREAM_DESC_TASK);
    }

    int[] serverVersion=new int[]{2012,1}; // default server support virtual stream
    Type parentType=Type.MAINLINE; // default parent type
    Type fromType=Type.UNKNOWN; // from which type to convert
    boolean differentParentDepot=false; // parent stream and depot are different

	public StreamTypeComboViewer(Composite parent) {
        super(parent);

        setContentProvider(new ArrayContentProvider());
        setLabelProvider(new LabelProvider(){
            public String getText(Object element) {
                if(element instanceof IStreamSummary.Type){
                    IStreamSummary.Type type=(Type) element;
                    return type.name().toLowerCase()+" - "+STREAM_TYPE_MAP.get(type);//$NON-NLS-1$
                }
                return element == null ? "" : element.toString();//$NON-NLS-1$
            }
        });
        addFilter(new ViewerFilter() {
            
            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                // only show virtual for no less than 2012.1
                if((serverVersion[0]<2012 || (serverVersion[0]==2012 && serverVersion[1]<1)) && element==Type.VIRTUAL){
                    return false;
                }
                // only show task for no less than 2013.1
                if((serverVersion[0]<2013 || (serverVersion[0]==2013 && serverVersion[1]<1)) && element==Type.TASK){
                    return false;
                }
                
                if(element==Type.UNKNOWN)
                	return false;
                
                // Following are logics about reparenting and type converting.
                if(fromType==Type.TASK){ 
                	if(parentType==null || parentType==Type.UNKNOWN){// parentless task stream can only convert to mainline stream
                		if(element!=Type.MAINLINE && element!=Type.TASK)
                			return false;
                	}else{// parented task stream can only convert to development and release
                		if(differentParentDepot && element!=Type.TASK)
                			return false;
                			
	                	if(element!=Type.RELEASE && element!=Type.DEVELOPMENT && element!=Type.TASK)
	                		return false;
                	}
                }
                
                if(fromType!=Type.TASK && fromType!=Type.UNKNOWN){ // other streams can not convert to task stream
                	if(element==Type.TASK)
                		return false;
                }
                return true;
            }
        });
    }
    
    public void setConnection(IP4Connection connection){
        if(StreamUtil.connectionOK(connection)){
            IServerInfo info = connection.getServerInfo();
            if(info!=null){
            	this.serverVersion=P4CoreUtils.getVersion(info.getServerVersion());
            }
            refresh();
        }
    }
    
    public void setParentStreamType(Type parentType){
        this.parentType=parentType;
        refresh();
    }

    public void setFromStreamType(Type from){
        this.fromType=from;
        refresh();
    }
    
    public void setDifferentParentDepot(boolean differentParentDepot) {
		this.differentParentDepot = differentParentDepot;
        refresh();
	}

}
