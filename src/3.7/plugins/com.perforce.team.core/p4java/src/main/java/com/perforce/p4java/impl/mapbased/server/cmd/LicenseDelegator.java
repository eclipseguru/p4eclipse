package com.perforce.p4java.impl.mapbased.server.cmd;

import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.core.ILicenseLimits;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.InputMapper;
import com.perforce.p4java.impl.generic.core.License;
import com.perforce.p4java.impl.generic.core.LicenseLimits;
import com.perforce.p4java.server.IOptionsServer;
import com.perforce.p4java.server.delegator.ILicenseDelegator;
import org.apache.commons.lang3.Validate;

import java.util.List;
import java.util.Map;

import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.handleErrorStr;
import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.isInfoMessage;
import static com.perforce.p4java.impl.mapbased.server.cmd.ResultMapParser.parseCommandResultMapAsString;
import static com.perforce.p4java.server.CmdSpec.LICENSE;
import static java.util.Objects.nonNull;

public class LicenseDelegator extends BaseDelegator implements ILicenseDelegator {

    /**
     * Basic constructor, taking a server object.
     *
     * @param server - an instance of the currently effective server implementation
     */
    public LicenseDelegator(IOptionsServer server) {
        super(server);
    }

    /* (non-Javadoc)
     * @see com.perforce.p4java.server.delegator.ILicenseDelegator#getLicense()
     */
    @Override
    public ILicenseLimits getLimits() throws P4JavaException {

        List<Map<String, Object>> resultMaps = execMapCmdList(
                LICENSE,
                new String[]{"-u"},
                null);

        if (nonNull(resultMaps)) {
            for (Map<String, Object> map : resultMaps) {
                handleErrorStr(map);
                if (!isInfoMessage(map)) {
                    return new LicenseLimits(map);
                }
            }
        }

        return null;
    }

    @Override
    public ILicense getLicense() throws P4JavaException {

        List<Map<String, Object>> resultMaps = execMapCmdList(
                LICENSE,
                new String[]{"-o"},
                null);

        if (nonNull(resultMaps)) {
            for (Map<String, Object> map : resultMaps) {
                handleErrorStr(map);
                if (!isInfoMessage(map)) {
                    return new License(map);
                }
            }
        }

        return null;
    }

    @Override
    public String updateLicense(final ILicense license) throws P4JavaException {

        Validate.notNull(license);

        List<Map<String, Object>> resultMaps = execMapCmdList(
                LICENSE,
                new String[]{"-i"},
                InputMapper.map(license));

        return parseCommandResultMapAsString(resultMaps);
    }
}
