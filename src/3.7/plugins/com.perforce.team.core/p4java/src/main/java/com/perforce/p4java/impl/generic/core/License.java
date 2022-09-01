package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.impl.mapbased.MapKeys;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class License extends ServerResource implements ILicense {

    private String license;
    private String licenseExpires;
    private String supportExpires;
    private String customer;
    private String application;
    private String ipaddress;
    private String platform;
    private long clients;
    private long users;
    private List<String> capabilities;

    public License() {
    }

    public License(Map<String, Object> map) {
        if (map != null) {
            try {
                this.license = (String) map.get(MapKeys.LICENSE_KEY);
                this.licenseExpires = (String) map.get(MapKeys.LICENSE_EXPIRES_KEY);
                this.supportExpires = (String) map.get(MapKeys.LICENSE_SUPPORT_KEY);
                this.customer = (String) map.get(MapKeys.LICENSE_CUSTOMER_KEY);
                this.application = (String) map.get(MapKeys.LICENSE_APPLICATION_KEY);
                this.ipaddress = (String) map.get(MapKeys.LICENSE_IPADDRESS_KEY);
                this.platform = (String) map.get(MapKeys.LICENSE_PLATFORM_KEY);
                this.clients = License.readValues(map, MapKeys.LICENSE_CLIENTS_KEY);
                this.users = License.readValues(map, MapKeys.LICENSE_USERS_KEY);

                int i = 0;
                this.capabilities = new ArrayList<>();
                while (map.containsKey(MapKeys.LICENSE_CAPABILITIES_KEY + i)) {
                    capabilities.add((String) map.get(MapKeys.LICENSE_CAPABILITIES_KEY + i));
                    i++;
                }

            } catch (Throwable thr) {
                Log.error("Unexpected exception in License constructor: " + thr.getLocalizedMessage());
                Log.exception(thr);
            }
        }
    }

    public String getLicense() {
        return license;
    }

    public String getLicenseExpires() {
        return licenseExpires;
    }

    public String getSupportExpires() {
        return supportExpires;
    }

    public String getCustomer() {
        return customer;
    }

    public String getApplication() {
        return application;
    }

    public String getIpaddress() {
        return ipaddress;
    }

    public String getPlatform() {
        return platform;
    }

    public long getClients() {
        return clients;
    }

    public long getUsers() {
        return users;
    }

    public List<String> getCapabilities() {
        return capabilities;
    }

    public static long readValues(Map<String, Object> map, String key) {
        if (!map.containsKey(key)) {
            return 0;
        }
        String value = (String) map.get(key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            if (StringUtils.isEmpty(value)) {
                return 0;
            }
            if ("unlimited".equals(value)) {
                return Long.MAX_VALUE;
            }
            if (value.startsWith(">")) {
                return -1;
            }
            if (value.startsWith("-")) {
                return 0;
            }
            return 0;
        }
    }
}
