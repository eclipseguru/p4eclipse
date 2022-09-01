package com.perforce.p4java.core;

import java.util.List;
import java.util.Map;

public interface ILicenseLimits extends IServerResource {
    boolean isLicensed();

    long getUserCount();

    long getUserLimit();

    long getClientCount();

    long getClientLimit();

    long getFileCount();

    long getFileLimit();

    long getRepoCount();

    long getRepoLimit();

    long getLicenseExpires();

    long getLicenseTimeRemaining();

    String getLicenseInvalid();

    List<Map<String, Map<String, String>>> getCapability();
}
