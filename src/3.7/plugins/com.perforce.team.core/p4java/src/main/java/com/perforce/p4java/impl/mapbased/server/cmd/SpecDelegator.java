package com.perforce.p4java.impl.mapbased.server.cmd;

import com.perforce.p4java.common.function.Function;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.server.CustomSpec;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.delegator.ISpecDelegator;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.parseCommandResultMapIfIsInfoMessageAsString;
import static com.perforce.p4java.server.CmdSpec.SPEC;

public class SpecDelegator extends BaseDelegator implements ISpecDelegator {

	/**
	 * Instantiates a new job spec delegator.
	 *
	 * @param server the server
	 */
	public SpecDelegator(final IOptionsServer server) {
		super(server);
	}

	/* (non-Javadoc)
	 * @see com.perforce.p4java.server.delegator.ISpecDelegator#getSpec(CustomSpec)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getSpec(CustomSpec type) throws AccessException, RequestException, ConnectionException {
		List<Map<String, Object>> resultMaps = execMapCmdList(SPEC, new String[]{"-o", type.toString()}, null);
		return ResultListBuilder.buildNullableObjectFromNonInfoMessageCommandResultMaps(
				resultMaps,
				new Function<Map, Map>() {
					@Override
					public Map apply(Map map) {
						return map;
					}
				}
		);
	}

    /* (non-Javadoc)
     * @see com.perforce.p4java.server.delegator.ISpecDelegator#updateSpec(CustomSpec, Map)
     */
	@Override
	public String updateSpec(CustomSpec type, Map<String, Object> spec) throws P4JavaException {
		Validate.notNull(spec);

		List<Map<String, Object>> resultMaps = execMapCmdList(SPEC, new String[]{"-i", type.toString()}, spec);


		return parseCommandResultMapIfIsInfoMessageAsString(resultMaps);
	}

    /* (non-Javadoc)
     * @see com.perforce.p4java.server.delegator.ISpecDelegator#updateSpecString(CustomSpec, String)
     */
	@Override
	public String updateSpecString(CustomSpec type, String spec) throws P4JavaException {
		Validate.notNull(spec);

		List<Map<String, Object>> resultMaps = server.execInputStringMapCmdList(SPEC.toString(), new String[]{"-i", type.toString()}, spec);

		return parseCommandResultMapIfIsInfoMessageAsString(resultMaps);
	}

}
