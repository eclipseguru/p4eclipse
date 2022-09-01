package com.perforce.p4java.server.delegator;

import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.core.ILicenseLimits;
import com.perforce.p4java.exception.P4JavaException;

public interface ILicenseDelegator {

    ILicenseLimits getLimits() throws P4JavaException;

    ILicense getLicense() throws P4JavaException;

    String updateLicense(final ILicense license) throws P4JavaException;
}
