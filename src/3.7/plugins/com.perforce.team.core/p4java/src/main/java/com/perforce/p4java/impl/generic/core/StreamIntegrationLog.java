package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.core.IStreamIntegrationLog;
import com.perforce.p4java.impl.mapbased.MapKeys;

import java.util.Map;

public class StreamIntegrationLog implements IStreamIntegrationLog {

	protected String how = null;
	protected String stream = null;
	protected String field = null;
	protected String startFromChange = null;
	protected String endFromChange = null;

	public StreamIntegrationLog() {
	}

	public StreamIntegrationLog(Map<String, Object> map, String suffix) {
		String key;

		key = MapKeys.HOW_KEY + suffix;
		this.how = (String) map.get(key);

		key = MapKeys.STREAM_LC_KEY + suffix;
		this.stream = (String) map.get(key);

		key = MapKeys.FIELD_KEY + suffix;
		this.field = (String) map.get(key);

		key = MapKeys.START_FROM_CHANGE_KEY + suffix;
		this.startFromChange = (String) map.get(key);

		key = MapKeys.END_FROM_CHANGE_KEY + suffix;
		this.endFromChange = (String) map.get(key);
	}

	@Override
	public String getHow() {
		return how;
	}

	@Override
	public String getStream() {
		return stream;
	}

	@Override
	public String getField() {
		return field;
	}

	@Override
	public String getStartFromChange() {
		return startFromChange;
	}

	@Override
	public String getEndFromChange() {
		return endFromChange;
	}
}
