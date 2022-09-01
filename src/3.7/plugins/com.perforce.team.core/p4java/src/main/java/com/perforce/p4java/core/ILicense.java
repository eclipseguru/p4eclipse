package com.perforce.p4java.core;

import java.util.List;

public interface ILicense {

    String getLicense();

    String getLicenseExpires();

    String getSupportExpires();

    String getCustomer();

    String getApplication();

    String getIpaddress();

    String getPlatform();

    long getClients();

    long getUsers();

    List<String> getCapabilities();
}
