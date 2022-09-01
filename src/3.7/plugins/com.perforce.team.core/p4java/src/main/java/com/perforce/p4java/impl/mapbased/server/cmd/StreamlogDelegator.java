package com.perforce.p4java.impl.mapbased.server.cmd;

import com.perforce.p4java.Log;
import com.perforce.p4java.core.IStreamlog;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Streamlog;
import com.perforce.p4java.impl.mapbased.MapKeys;
import com.perforce.p4java.option.server.StreamlogOptions;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.delegator.IStreamlogDelegator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.perforce.p4java.impl.mapbased.server.Parameters.processParameters;
import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.handleFileErrorStr;
import static com.perforce.p4java.server.CmdSpec.STREAMLOG;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class StreamlogDelegator extends BaseDelegator implements IStreamlogDelegator {

	/**
	 * Basic constructor, taking a server object.
	 *
	 * @param server - an instance of the currently effective server implementaion
	 */
	public StreamlogDelegator(IOptionsServer server) {
		super(server);
	}

	@Override
	public Map<String, List<IStreamlog>> getStreamlog(List<String> streamPaths, StreamlogOptions opts) throws P4JavaException {

		String[] args = {};
		if (nonNull(streamPaths)) {
			args = streamPaths.toArray(new String[streamPaths.size()]);
		}

		Map<String, List<IStreamlog>> streamlogMap = new HashMap<>();
		String streamName;

		List<Map<String, Object>> resultMaps = execMapCmdList(
				STREAMLOG,
				processParameters(opts, null, args, server),
				null);

		if (isNull(resultMaps)) {
			return streamlogMap;
		}

		for (Map<String, Object> StreamlogMap : resultMaps) {
			String errStr = handleFileErrorStr(StreamlogMap);

			if (isNotBlank(errStr)) {
				Log.error(errStr);
				return streamlogMap;
			}

			List<IStreamlog> streamlogList = new ArrayList<>();

			int i = 0;
			while (StreamlogMap.containsKey(MapKeys.TIME_LC_KEY + i)) {
				streamlogList.add(new Streamlog(StreamlogMap, i));
				i++;
			}

			streamName = streamlogList.get(0).getStream();
			streamlogMap.put(streamName, streamlogList);
		}

		return streamlogMap;
	}
}


