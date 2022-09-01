package com.perforce.p4java.server.delegator;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.CustomSpec;

import java.util.Map;

public interface ISpecDelegator {

	/**
	 * Return the Perforce spec associated with this Perforce server.
	 * <p>
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @return possibly-null map representing the underlying Perforce
	 *         server's spec.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 * @since 2020.1
	 */
	Map<String, Object> getSpec(CustomSpec type) throws P4JavaException;

	/**
	 * Update a Perforce spec on the Perforce server.
	 *
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @param spec updated spec as a map.
	 * @return non-null result message string from the Perforce server; this may
	 * include form trigger output pre-pended and / or appended to the
	 * "normal" message.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 * @since 2020.1
	 */
	String updateSpec(CustomSpec type, Map<String, Object> spec) throws P4JavaException;

	/**
	 * Update a Perforce spec on the Perforce server.
	 *
	 * @param type spec type, ('job' or 'stream') to be updated.
	 * @param spec updated spec as a map.
	 * @return non-null result message string from the Perforce server; this may
	 * include form trigger output pre-pended and / or appended to the
	 * "normal" message.
	 * @throws P4JavaException if any error occurs in the processing of this method.
	 * @since 2020.1
	 */
	String updateSpecString(CustomSpec type, String spec) throws P4JavaException;

}
