package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IStreamIntegrationLog;
import com.perforce.p4java.core.IStreamlog;
import com.perforce.p4java.impl.mapbased.MapKeys;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Simple default generic implementation class for the IStreamlog interface.
 */

public class Streamlog extends ServerResource implements IStreamlog {

	protected String stream = null;
	protected Integer change = IChangelist.UNKNOWN;
	protected String action = null;
	protected Date date = null;
	protected String user = null;
	protected String client = null;
	protected Integer associatedChange = null;
	protected String description = null;
	protected List<IStreamIntegrationLog> streamIntegList = new ArrayList<>();

	/**
	 * Default constructor -- sets all fields to false or null,
	 * and calls the default ServerResource constructor.
	 */
	public Streamlog() {
	}

	/**
	 * Construct a Streamlog implementation, by parsing a map returned by the server.
	 * Streamlog is a single change per Stream, sorted by order, to create array.
	 */
	public Streamlog(Map<String, Object> map, int order) {
		if (map == null) {
			return;
		}

		try {
			this.stream = (String) map.get(MapKeys.STREAM_LC_KEY);

			String key;

			key = MapKeys.CHANGE_LC_KEY + order;
			this.change = new Integer((String) map.get(key));

			key = MapKeys.STREAMLOG_ACTION_KEY + order;
			this.action = (String) map.get(key);

			key = MapKeys.TIME_LC_KEY + order;
			this.date = parseDate((String) map.get(key));

			key = MapKeys.USER_LC_KEY + order;
			this.user = (String) map.get(key);

			key = MapKeys.CLIENT_LC_KEY + order;
			this.client = (String) map.get(key);

			key = MapKeys.STREAMLOG_ASSOCIATEDCHANGE_KEY + order;
			this.associatedChange = new Integer((String) map.get(key));

			key = MapKeys.DESC_LC_KEY + order;
			this.description = (String) map.get(key);

			int i = 0;
			while (map.containsKey(MapKeys.HOW_KEY + order + "," + i)) {
				StreamIntegrationLog streamIntegLog = new StreamIntegrationLog(map, order + "," + i);
				this.streamIntegList.add(streamIntegLog);
				i++;
			}

		} catch (Throwable thr) {
			Log.exception(thr);
		}
	}

	@Override
	public String getStream() {
		return stream;
	}

	@Override
	public Integer getChange() {
		return change;
	}

	@Override
	public String getAction() {
		return action;
	}

	@Override
	public Date getDate() {
		return date;
	}

	@Override
	public String getUser() {
		return user;
	}

	@Override
	public String getClient() {
		return client;
	}

	@Override
	public Integer getAssociatedChange() {
		return associatedChange;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public List<IStreamIntegrationLog> getStreamIntegList() {
		return streamIntegList;
	}
}



