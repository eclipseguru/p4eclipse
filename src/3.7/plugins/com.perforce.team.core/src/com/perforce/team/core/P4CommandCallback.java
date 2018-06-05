/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core;

import java.util.List;
import java.util.Map;

/**
 * Base implementation of {@link IP4CommandCallback}
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4CommandCallback implements IP4CommandCallback {

	@Override
	public void callback(List<Map<String, Object>> data) {
		// Does nothing by default, subclasses should override
	}

	@Override
	public void callbackError(List<Map<String, Object>> data) {
		// Does nothing by default, subclasses should override
		
	}

}
