package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.IStreamlog;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.option.server.StreamlogOptions;

import java.util.List;
import java.util.Map;


public interface IStreamlogDelegator {
	/**
	 * List the revision history of the specified stream specs, from the most
	 * recent revision to the first.  If the stream was opened for edit
	 * and submitted, the change list information is displayed.
	 * Otherwise only the maximum change num at the time of edit is displayed.
	 * @param streamPaths Stream paths to get history.
	 * @param opts Command options.
	 * @see com.perforce.p4java.option.server.StreamlogOptions#StreamlogOptions(java.lang.String...)
	 * @return List of Streamlog.
	 * @see IStreamlog
	 * @throws P4JavaException
	 * @since 2021.2
	 */
	Map<String, List<IStreamlog>> getStreamlog(List<String> streamPaths, StreamlogOptions opts) throws P4JavaException;
}


