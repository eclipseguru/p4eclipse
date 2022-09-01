package com.perforce.p4java.core;

import com.perforce.p4java.impl.generic.core.file.IExtensionSummary;

import java.util.List;
import java.util.Map;

public interface IExtension extends IServerResource, IExtensionSummary {
	/**
	 * A Perforce Extension Specification.
	 *
	 * Fields:
	 *         902 ExtDescription text 128 once
	 *         913 ExtMaxScriptTime word 12 optional
	 *         914 ExtMaxScriptMem word 12 optional
	 *         907 Owner word 36 default
	 *         908 Update date 20 always
	 *         909 Description text 128 required
	 *         912 ExtConfig text 256 required
	 *
	 *         global only
	 *         915 ExtAllowedGroups wlist 32 default
	 *         917 ExtP4USER word 12 default
	 *
	 *         instance only
	 *         934 ExtDebug word 12 default
	 */


	/**
	 * Get the description of the Extension being configured.
	 *
	 * @return Extension description
	 */
	String getExtDescription();

	/**
	 * Get maximum seconds the Extension may be run.
	 *
	 * @return possibly-null (optional) Maximum seconds
	 */
	String getExtMaxScriptTime();

	/**
	 * Get maximum megabytes the Extension may use.
	 *
	 * @return possibly-null (optional) Maximum megabytes
	 */
	String getExtMaxScriptMem();

	/**
	 * Get the user who created this Extension config.
	 *
	 * @return Owner
	 */
	String getOwner();

	/**
	 * Get update time for the Extension config spec.
	 *
	 * @return Update time
	 */
	String getUpdate();

	/**
	 * Get the description of this Extension config.
	 *
	 * @return Extension config description
	 */
	String getDescription();

	/**
	 * Get the extension config.
	 *
	 * @return Extension config
	 */
	Map<String, String> getExtConfig();

	// global config only

	/**
	 * Get groups whose members may configure the Extension.
	 *
	 * @return Allowed groups
	 */
	List<String> getExtAllowedGroups();

	/**
	 * Get Perforce user account for the Extension to use.
	 *
	 * @return User
	 */
	String getExtP4USER();

	// instance config only

	/**
	 * Check if extension diagnostics are enabled.
	 *
	 * @return Extension debug
	 */
	String getExtDebug();

	/**
	 * Set the description of the Extension being configured.
	 */
	void setExtDescription(String extDescription);

	/**
	 * Set maximum seconds the Extension may be run.
	 */
	void setExtMaxScriptTime(String extMaxScriptTime);

	/**
	 * Set maximum megabytes the Extension may use.
	 */
	void setExtMaxScriptMem(String extMaxScriptMem);

	/**
	 * Set the user who created this Extension config.
	 **/
	void setOwner(String owner);

	/**
	 * Set update time for the Extension config spec.
	 */
	void setUpdate(String update);

	/**
	 * Set the description of this Extension config.
	 */
	void setDescription(String description);

	/**
	 * Set the extension config.
	 */
	void setExtConfig(Map<String, String> extConfig);

	/**
	 * Set groups whose members may configure the Extension.
	 **/
	void setExtAllowedGroups(List<String> extAllowedGroups);

	/**
	 * Set Perforce user account for the Extension to use.
	 */
	void setExtP4USER(String extP4USER);

	/**
	 * When the 'ExtDebug' field in the instance config is set to
	 * 'tracing' (off of the default of 'none'), every execution of
	 * the instance of the Extension will append execution traces to
	 * a log file named '.p4-debug-tracing.txt' in the Extension data
	 * directory.
	 */
	void setExtDebug(String extDebug);
}
