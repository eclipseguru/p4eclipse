package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.core.IExtension;
import com.perforce.p4java.impl.mapbased.MapKeys;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Extension extends ExtensionSummary implements IExtension {
	/**
	 * 901 ExtName line 64 once
	 * 902 ExtDescription text 128 once
	 * 903 ExtVersion line 32 once
	 * 904 ExtUUID line 36 once
	 * 905 ExtRev word 20 once
	 * 913 ExtMaxScriptTime word 12 optional
	 * 914 ExtMaxScriptMem word 12 optional
	 * 916 ExtEnabled word 12 default
	 * 906 Name line 32 default
	 * 907 Owner word 36 default
	 * 908 Update date 20 always
	 * 909 Description text 128 required
	 * 912 ExtConfig text 256 required
	 * <p>
	 * global only
	 * 915 ExtAllowedGroups wlist 32 default
	 * 917 ExtP4USER word 12 default
	 * <p>
	 * instance only
	 * 934 ExtDebug word 12 default
	 */

	private String extDescription;
	private String extMaxScriptTime;
	private String extMaxScriptMem;
	private String owner;
	private String update;
	private String description;
	private Map<String, String> extConfig;
	private List<String> extAllowedGroups;
	private String extP4USER;
	private String extDebug;

	public Extension() {
	}

	public Extension(Map<String, Object> map) {
		super(map, false);
		if (map != null) {
			try {
				this.extDescription = (String) map.get(MapKeys.EXTENSION_EXT_DESCRIPTION_KEY);
				this.extMaxScriptTime = (String) map.get(MapKeys.EXTENSION_SCRIPT_TIME_KEY);
				this.extMaxScriptMem = (String) map.get(MapKeys.EXTENSION_SCRIPT_MEMORY_KEY);
				this.owner = (String) map.get(MapKeys.EXTENSION_OWNER_KEY);
				this.update = (String) map.get(MapKeys.EXTENSION_UPDATE_KEY);
				this.description = (String) map.get(MapKeys.EXTENSION_DESCRIPTION_KEY);

				Object extConfig = map.get(MapKeys.EXTENSION_CONFIG_KEY);
				if (extConfig != null) {
					HashMap<String, String> configMap = new HashMap<>();
					String configStr = (String) extConfig;
					StringTokenizer configLines = new StringTokenizer(configStr, MapKeys.LF);
					while (configLines.hasMoreTokens()) {
						String str = configLines.nextToken();
						String[] pairs = StringUtils.split(str, MapKeys.COLON_SPACE, 2);
						String key = pairs[0];
						String value = "";
						if (pairs.length > 1) {
							value = pairs[1];
						}
						configMap.put(key, value);
					}
					this.extConfig = configMap;
				}

				Object extAllowedGroups = map.get(MapKeys.EXTENSION_GROUPS_KEY);
				if (extAllowedGroups != null) {
					this.extAllowedGroups = (List<String>) extAllowedGroups;
				}

				this.extP4USER = (String) map.get(MapKeys.EXTENSION_P4USER_KEY);
				this.extDebug = (String) map.get(MapKeys.EXTENSION_DEBUG);

			} catch (Throwable thr) {
				Log.error("Unexpected exception in Extension constructor: " + thr.getLocalizedMessage());
				Log.exception(thr);
			}
		}

	}

	@Override
	public String getExtDescription() {
		return extDescription;
	}

	@Override
	public String getExtMaxScriptTime() {
		return extMaxScriptTime;
	}

	@Override
	public String getExtMaxScriptMem() {
		return extMaxScriptMem;
	}

	@Override
	public String getOwner() {
		return owner;
	}

	@Override
	public String getUpdate() {
		return update;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public Map<String, String> getExtConfig() {
		return extConfig;
	}

	@Override
	public List<String> getExtAllowedGroups() {
		return extAllowedGroups;
	}

	@Override
	public String getExtP4USER() {
		return extP4USER;
	}

	@Override
	public String getExtDebug() {
		return extDebug;
	}

	@Override
	public void setExtDescription(String extDescription) {
		this.extDescription = extDescription;
	}

	@Override
	public void setExtMaxScriptTime(String extMaxScriptTime) {
		this.extMaxScriptTime = extMaxScriptTime;
	}

	@Override
	public void setExtMaxScriptMem(String extMaxScriptMem) {
		this.extMaxScriptMem = extMaxScriptMem;
	}

	@Override
	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Override
	public void setUpdate(String update) {
		this.update = update;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public void setExtConfig(Map<String, String> extConfig) {
		this.extConfig = extConfig;
	}

	@Override
	public void setExtAllowedGroups(List<String> extAllowedGroups) {
		this.extAllowedGroups = extAllowedGroups;
	}

	@Override
	public void setExtP4USER(String extP4USER) {
		this.extP4USER = extP4USER;
	}

	@Override
	public void setExtDebug(String extDebug) {
		this.extDebug = extDebug;
	}
}
