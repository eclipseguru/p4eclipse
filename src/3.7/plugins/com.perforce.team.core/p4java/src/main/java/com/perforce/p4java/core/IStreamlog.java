package com.perforce.p4java.core;

import java.util.Date;
import java.util.List;

/**
 * Defines the methods and operations available on Perforce
 * streamlogs returned from the server.<p>
 * <p>
 * Streamlogs are typically returned from the server's streamlog
 * methods (e.g. getStreamlog) and normally contain only
 * the fields returned by the Perforce "p4 streamlog" command.
 */

public interface IStreamlog {

	/**
	 * Return the stream path.
	 *
	 * @return Stream path string.
	 */
	String getStream();

	/**
	 * Return Perforce changelist's number.
	 *
	 * @return changelist number.
	 */
	Integer getChange();

	/**
	 * Return action associated with the change typically add/edit/delete
	 *
	 * @return change action string add/delete/edit.
	 */
	String getAction();

	/**
	 * Get the date the changelist was created or last updated.
	 *
	 * @return the date the changelist was created or last updated, or null
	 * if unknown.
	 */
	Date getDate();

	/**
	 * Get the user associated with this changelist.
	 *
	 * @return the username of the user associated with this changelist,
	 * or null if no such name exists or can be determined.
	 */
	String getUser();

	/**
	 * Get the name of the Perforce client workspace associated with this changelist.
	 *
	 * @return the name of the client  associated with this changelist, or null if not known.
	 */
	String getClient();

	/**
	 * Return Perforce changelist's number of changelist associated with the change.
	 *
	 * @return changelist number.
	 */
	Integer getAssociatedChange();

	/**
	 * Return the description associated with this changelist.
	 *
	 * @return textual changelist description, or null if no such description.
	 */
	String getDescription();

	/**
	 * Return Integration history associated with changelist.
	 * @return List of hash maps describing integrations.
	 */
	List<IStreamIntegrationLog> getStreamIntegList();
}
