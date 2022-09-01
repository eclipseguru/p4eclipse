package com.perforce.p4java.server;

import java.util.Locale;

public enum CustomSpec {

	JOB,
	STREAM;

	/**
	 * Returns a string suitable for passing to the lower levels of an IServer
	 * object as a Perforce command name. Usually means it's just the lower case
	 * representation of the name, but this is not guaranteed to be true. Most
	 * useful with the execMapXXX series of methods on IServer. Note the use of
	 * the English locale; this is to try to ensure that we don't trip up on
	 * off default locales like Turkish (with its dotted-i issue -- see e.g.
	 * job037128).
	 *
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		return this.name().toLowerCase(Locale.ENGLISH);
	}

}
