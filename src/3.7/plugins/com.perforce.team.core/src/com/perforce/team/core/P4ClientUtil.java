package com.perforce.team.core;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.client.ClientView;

public class P4ClientUtil {

	public static boolean isViewMappingChanged(ClientView oldView,
			ClientView newView) {
    	boolean mappingChanged=false;
    	if(oldView!=newView){
    		IClientViewMapping[] oldMappings=null;
    		IClientViewMapping[] newMappings=null;
    		
    		IClientViewMapping[] EMPTY = new IClientViewMapping[0];
    		if(oldView!=null)
    			oldMappings = oldView.getEntryList().toArray(EMPTY);
    		if(newView!=null)
    			newMappings = newView.getEntryList().toArray(EMPTY);
    		
    		if(oldMappings==null)
    			oldMappings=EMPTY;
    		if(newMappings==null)
    			newMappings=EMPTY;
    		
    		if(oldMappings!=newMappings){
				if(oldMappings.length!=newMappings.length){
					mappingChanged=true;
				}else{
					for(int i=0;i<oldMappings.length;i++){
						IClientViewMapping om = oldMappings[i];
						IClientViewMapping nm = newMappings[i];
						if(om==nm)
							continue;
						if(om==null || nm==null){
							mappingChanged=true;
							break;
						}else{
							if(!om.getLeft().equals(nm.getLeft())
									|| !om.getRight().equals(nm.getRight())
									|| om.getType()!=nm.getType()){
								mappingChanged=true;
								break;
							}
						}
					}
				}
			}
    	}
		return mappingChanged;
	}

	public static boolean isRootDiff(String oldRoot, String newRoot) {
    	boolean rootChanged=false;
    	if(oldRoot!=null || newRoot!=null){
    		rootChanged=(oldRoot!=null)?!oldRoot.equals(newRoot):!newRoot.equals(oldRoot);
    	}
		return rootChanged;
	}

	public static boolean isAtChangeDiff(int oldAtChange, int newAtChange) {
		return oldAtChange!=newAtChange;
	}

	public static boolean isStreamDifferent(String oldStream, String newStream) {
    	if(StringUtils.isEmpty(oldStream) && StringUtils.isEmpty(newStream))
    		return false;
    	
    	boolean streamChanged=false;
    	if(oldStream!=null || newStream!=null){
    		streamChanged=(oldStream!=null)?
    				!oldStream.equals(newStream)
    				:!newStream.equals(oldStream);
    	}
		return streamChanged;
	}
	
    public static boolean shouldSyncClient(IClient oldClient, IClient newClient) {
    	String oldStream = oldClient.getStream();
    	String newStream = newClient.getStream();
    	int oldAtChange = oldClient.getStreamAtChange(); 
    	int newAtChange = newClient.getStreamAtChange();
    	String oldRoot = oldClient.getRoot();
    	String newRoot = newClient.getRoot();
    	ClientView oldView = oldClient.getClientView();
    	ClientView newView = newClient.getClientView();
    	
    	boolean streamChanged=P4ClientUtil.isStreamDifferent(oldStream,newStream);
    	boolean atChangeChanged=P4ClientUtil.isAtChangeDiff(oldAtChange,newAtChange);
    	boolean rootChanged=P4ClientUtil.isRootDiff(oldRoot,newRoot); 
    	boolean mappingChanged=P4ClientUtil.isViewMappingChanged(oldView,newView);
    	
		return rootChanged || streamChanged || atChangeChanged || mappingChanged;
	}

    /**
     * This does not check the access time and updated time
     * @param oldSpec
     * @param newSpec
     * @return
     */
	public static boolean isClientChanged(IClient oldSpec, IClient newSpec) {
		if(oldSpec==newSpec)
			return false;
		if(oldSpec==null || newSpec==null)
			return true;
					
		if(!P4LogUtils.testEquals(oldSpec.getName(), newSpec.getName())){
			return true;
		}
		if(!P4LogUtils.testEquals(oldSpec.getHostName(), newSpec.getHostName())){
			return true;
		}
		if(!P4LogUtils.testEquals(oldSpec.getDescription(), newSpec.getDescription())){
			return true;
		}
		if(!P4LogUtils.testEquals(oldSpec.getOwnerName(), newSpec.getOwnerName())){
			return true;
		}
//		if(!P4LogUtils.testEquals(oldSpec.getUpdated(), newSpec.getUpdated())){
//			return true;
//		}
//		if(!P4LogUtils.testEquals(oldSpec.getAccessed(), newSpec.getAccessed())){
//			return true;
//		}
		if(!P4LogUtils.reflectiveEquals(oldSpec.getSubmitOptions(), newSpec.getSubmitOptions())){
			return true;
		}
		if(!P4LogUtils.reflectiveEquals(oldSpec.getLineEnd(), newSpec.getLineEnd())){
			return true;
		}
		
		List<String> altRootsOld = oldSpec.getAlternateRoots();
		List<String> altRootsNew = newSpec.getAlternateRoots();
		if(!P4LogUtils.reflectiveEquals(altRootsOld, altRootsNew)){
			return true;
		}
		if(isStreamDifferent(oldSpec.getStream(), newSpec.getStream())){
			return true;
		}
		
		if(isRootDiff(oldSpec.getRoot(), newSpec.getRoot())){
			return true;
		}
		
		if(isAtChangeDiff(oldSpec.getStreamAtChange(), newSpec.getStreamAtChange())){
			return true;
		}

		if(StringUtils.isEmpty(oldSpec.getStream()) && StringUtils.isEmpty(newSpec.getStream())){
			if(isViewMappingChanged(oldSpec.getClientView(), newSpec.getClientView())){
				return true;
			}
		}
		
		if(!P4LogUtils.reflectiveEquals(oldSpec.getOptions(), newSpec.getOptions())){
			return true;
		}
		
		if(!P4LogUtils.testEquals(oldSpec.getServerId(),newSpec.getServerId())){
			return true;
		}
		
		return false;
	}

    /**
     * Return theirs version when resolve a conflicts
     * @param spec
     * @return
     */
    public static String computeTheirRev(IFileSpec spec) {
    	
		String toRev = "#"+Math.max(1, spec.getEndFromRev()); //$NON-NLS-1$

        int shelvedChange = spec.getShelvedChange();
        
        if(isResolveShelvedChange(spec)){
        	toRev="@=" + shelvedChange; //$NON-NLS-1$
        }

		return toRev;
	}

	public static boolean isResolveShelvedChange(IFileSpec spec) {
        String fromFile = spec.getFromFile();
        String baseFile = getBaseFile(spec);
        int shelvedChange = spec.getShelvedChange();

        if(!StringUtils.isEmpty(fromFile) && !StringUtils.isEmpty(baseFile)){
        	if(fromFile.equals(baseFile) && IChangelist.UNKNOWN!=shelvedChange){
        		return true;
        	}
        }
        return false;
	}
	
	public static int getBaseRev(IFileSpec integSpec){
		int baseRev = integSpec.getBaseRev();
		if(baseRev!=IFileSpec.NO_FILE_REVISION){
			return baseRev;
		}
		baseRev=integSpec.getStartFromRev();
		if(baseRev>0){
			baseRev--;
		}else{
			baseRev=0;
		}
		return baseRev;
	}
	
	public static String getBaseFile(IFileSpec integSpec){
        return (integSpec.getBaseFile()!=null?integSpec.getBaseFile():integSpec.getFromFile());
	}
	
}
