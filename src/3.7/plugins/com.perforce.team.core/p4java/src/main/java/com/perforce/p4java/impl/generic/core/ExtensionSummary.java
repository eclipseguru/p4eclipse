package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.impl.generic.core.file.IExtensionSummary;
import com.perforce.p4java.impl.mapbased.MapKeys;

import java.util.Map;

public class ExtensionSummary extends ServerResource implements IExtensionSummary {

	protected String extName; // actual extension name
	protected String extVersion;
	protected String nameSpace; // namespace
	protected String extRev;
	protected String extEnabled;
	protected String extUUID;

	// unique fields to summary
	private String extDeveloper;
	private String extDescriptionSnippet;
	private String extArchDir;
	private String extDataDir;
	private boolean extGlobalConf = false;
	private boolean extInstanceConf = false;

	public ExtensionSummary() {
	}

	public ExtensionSummary(Map<String, Object> map) {
		this(map, true);
	}

	protected ExtensionSummary(Map<String, Object> map, boolean summary) {

		if (map != null) {
			try {
				String revKey = (summary) ? MapKeys.EXTENSION_SUMMARY_REV_KEY : MapKeys.EXTENSION_REV_KEY;
				this.extRev = (String) map.get(revKey);

				String enabledKey = (summary) ? MapKeys.EXTENSION_SUMMARY_ENABLED_KEY : MapKeys.EXTENSION_ENABLED_KEY;
				this.extEnabled = (String) map.get(enabledKey);

				String UUIDKey = (summary) ? MapKeys.EXTENSION_SUMMARY_UUID_KEY : MapKeys.EXTENSION_UUID_KEY;
				this.extUUID = (String) map.get(UUIDKey);

				String versionKey = (summary) ? MapKeys.EXTENSION_SUMMARY_VERSION_KEY : MapKeys.EXTENSION_VERSION_KEY;
				this.extVersion = (String) map.get(versionKey);

				if (summary) {
					String name = (String) map.get(MapKeys.EXTENSION_SUMMARY_NAME_KEY);
					String[] names = name.split("::");
					this.nameSpace = names[0];
					this.extName = names[1];
				} else {
					this.extName = (String) map.get(MapKeys.EXTENSION_NAME_KEY);
					this.nameSpace = (String) map.get(MapKeys.EXTENSION_NAME_SPACE_KEY);
				}

				this.extDeveloper = (String) map.get(MapKeys.EXTENSION_SUMMARY_DEVELOPER_KEY);
				this.extDescriptionSnippet = (String) map.get(MapKeys.EXTENSION_SUMMARY_DESCRIPTION_SNIPPET_KEY);
				this.extArchDir = (String) map.get(MapKeys.EXTENSION_SUMMARY_ARCH_DIR_KEY);
				this.extDataDir = (String) map.get(MapKeys.EXTENSION_SUMMARY_DATA_DIR_KEY);

				if (map.get(MapKeys.EXTENSION_SUMMARY_GLOBAL_CONF_KEY) == "true") {
					extGlobalConf = true;
				}

				if (map.get(MapKeys.EXTENSION_SUMMARY_INSTANCE_CONF_KEY) == "true") {
					extInstanceConf = true;
				}

			} catch (Throwable thr) {
				Log.error("Unexpected exception in Extension constructor: " + thr.getLocalizedMessage());
				Log.exception(thr);
			}
		}
	}

	public String getExtName() {
		return extName;
	}

	public String getExtVersion() {
		return extVersion;
	}

	public String getExtUUID() {
		return extUUID;
	}

	public String getExtRev() {
		return extRev;
	}

	public String getExtEnabled() {
		return extEnabled;
	}

	public String getNameSpace() {
		return nameSpace;
	}

	public String getExtDeveloper() {
		return extDeveloper;
	}

	public String getExtDescriptionSnippet() {
		return extDescriptionSnippet;
	}

	public String getExtArchDir() {
		return extArchDir;
	}

	public String getExtDataDir() {
		return extDataDir;
	}

	public boolean getExtGlobalConf() {
		return extGlobalConf;
	}

	public boolean getExtInstanceConf() {
		return extInstanceConf;
	}

	public void setExtName(String extName) {
		this.extName = extName;
	}

	public void setExtVersion(String extVersion) {
		this.extVersion = extVersion;
	}

	public void setExtUUID(String extUUID) {
		this.extUUID = extUUID;
	}

	public void setExtRev(String extRev) {
		this.extRev = extRev;
	}

	public void setExtEnabled(String extEnabled) {
		this.extEnabled = extEnabled;
	}

	public void setNameSpace(String name) {
		this.nameSpace = name;
	}

	public void setExtDeveloper(String extDeveloper) {
		this.extDeveloper = extDeveloper;
	}

	public void setExtDescriptionSnippet(String extDescriptionSnippet) {
		this.extDescriptionSnippet = extDescriptionSnippet;
	}

	public void setExtArchDir(String extArchDir) {
		this.extArchDir = extArchDir;
	}

	public void setExtDataDir(String extDataDir) {
		this.extDataDir = extDataDir;
	}

	public void setExtGlobalConf(boolean extGlobalConf) {
		this.extGlobalConf = extGlobalConf;
	}

	public void setExtInstanceConf(boolean extInstanceConf) {
		this.extInstanceConf = extInstanceConf;
	}

}
