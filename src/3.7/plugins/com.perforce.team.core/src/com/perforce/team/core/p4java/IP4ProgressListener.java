/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.List;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.server.callback.IStreamingCallback;

/**
 * Extended callback interface.
 * 
 * @author Alex Li (ali@perforce.com)
 */
public interface IP4ProgressListener extends IStreamingCallback{
	/*
	 * The order of call is:
	 * 	boolean startResults(int key);
	 * 	boolean handleResult(Map<String, Object> resultMap, int key);
	 * 	boolean endResults(int key);
	 */
	List<IFileSpec> getFileSpecs();

	boolean isCancelled();
	
	void setConnection(IP4Connection conn);
}
