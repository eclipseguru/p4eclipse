package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.option.server.GetDepotsOptions;

import java.util.List;

/**
 * The Interface IDepotsDelegator.
 */
public interface IDepotsDelegator {

	/**
	 * Gets the depots.
	 *
	 * @return the depots
	 * @throws ConnectionException the connection exception
	 * @throws RequestException    the request exception
	 * @throws AccessException     the access exception
	 */
	List<IDepot> getDepots() throws ConnectionException, RequestException, AccessException;

	/**
	 * Gets depots with type and namefilter
	 */
	List<IDepot> getDepots(GetDepotsOptions opts) throws P4JavaException;
}
