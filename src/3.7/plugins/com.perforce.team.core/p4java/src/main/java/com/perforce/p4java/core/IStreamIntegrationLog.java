package com.perforce.p4java.core;


/**
 *  StreamIntegrationLog is typically returned from the server as part of the streamlog command.
 *  It carries integration/branching data for perforce Stream spec
 */
public interface IStreamIntegrationLog {

	/**
	 * Returns integration method: variations on merge/branch/copy/ignore/delete.
	 * For example "branch from".
	 * @return Integration type.
	 */
	String getHow();

	/**
	 * Returns String associated with the integration.
	 * @return Stream path.
	 */
	String getStream();

	/**
	 * Returns field.
	 */
	String getField();

	/**
	 * Returns the starting change of the from stream spec.
	 */
	String getStartFromChange();

	/**
	 * Returns the ending change of the to stream spec.
	 */
	String getEndFromChange();
}
