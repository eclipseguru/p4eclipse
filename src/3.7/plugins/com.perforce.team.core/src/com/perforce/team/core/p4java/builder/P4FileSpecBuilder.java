package com.perforce.team.core.p4java.builder;

import java.util.List;

import com.perforce.p4java.core.file.FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.p4java.P4Connection;

public class P4FileSpecBuilder {

	/**
	 * Given an array of file paths (which might include revision or label specs, etc.),
	 * return a corresponding list of file specs. Returns null if pathArray is null; skips
	 * any null element of the array. 
	 * 
	 * @param pathArray array of path strings
	 * @return possibly-null (or empty) list of filespecs
	 */
	
	public static List<IFileSpec> makeFileSpecList(String[] pathArray) {
		if(pathArray!=null){
			for(int i=0;i<pathArray.length;i++){
				if (pathArray[i] != null) {
					pathArray[i] = P4Connection.convertDiskLabel(pathArray[i]);
				}
			}
		}
		return FileSpecBuilder.makeFileSpecList(pathArray);
	}
	
	/**
	 * Create a list containing a single file spec created from the specified
	 * path.
	 * 
	 * @param path
	 * @return non-null but possibly empty list of filespecs
	 */
	public static List<IFileSpec> makeFileSpecList(String path) {
		return makeFileSpecList(new String[] { path });
	}
	
	/**
	 * Given a list of file specs, return a list of the valid file specs in that list.
	 * "Valid" here means a) non-null, and b) getOpStatus() returns VALID.
	 * 
	 * @param fileSpecs candidate file specs
	 * @return non-null but possibly-empty list of valid file specs
	 */
	
	public static List<IFileSpec> getValidFileSpecs(List<IFileSpec> fileSpecs) {
		return FileSpecBuilder.getValidFileSpecs(fileSpecs);
	}
	
	/**
	 * Given a list of file specs, return a list of the invalid file specs in that list.
	 * "Invalid" here means a) non-null, and b) getOpStatus() returns anything but VALID.
	 * 
	 * @param fileSpecs candidate file specs
	 * @return non-null but possibly-empty list of invalid file specs
	 */
	
	public static List<IFileSpec> getInvalidFileSpecs(List<IFileSpec> fileSpecs) {
		return FileSpecBuilder.getInvalidFileSpecs(fileSpecs);
	}

}
