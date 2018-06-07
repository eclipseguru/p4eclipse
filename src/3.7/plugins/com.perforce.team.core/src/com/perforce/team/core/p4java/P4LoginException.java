package com.perforce.team.core.p4java;

/**
 * An exception for when Perforce tickets expire.
 */
public class P4LoginException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public P4LoginException() {
		super();
	}

	public P4LoginException(String statusMessage) {
		super(statusMessage);
	}
}