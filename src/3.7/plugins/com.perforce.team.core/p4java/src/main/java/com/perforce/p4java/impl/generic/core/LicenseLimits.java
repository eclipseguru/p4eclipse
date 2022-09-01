package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.core.ILicenseLimits;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LicenseLimits extends ServerResource implements ILicenseLimits {

    /* SAMPLE licensed
     * p4 -Ztag license -u
     * ... isLicensed yes
     * ... userCount 16
     * ... userLimit 100
     * ... clientCount -
     * ... clientLimit unlimited
     * ... fileCount -
     * ... fileLimit unlimited
     * ... repoCount 0
     * ... repoLimit 10
     * ... licenseExpires 1652343538
     * ... licenseTimeRemaining 31534414
     * ... capability0 Hive
     * ... capabilityAttrKey0,0 ipaddress
     * ... capabilityAttrValue0,0 10.5.40.115
     * ... capability1 Hive
     * ... capabilityAttrKey1,0 ipaddress
     * ... capabilityAttrValue1,0 10.153.9.1
     */

    /* SAMPLE expired
     * ... isLicensed yes
     * ... userCount 16
     * ... userLimit 50
     * ... clientCount -
     * ... clientLimit unlimited
     * ... fileCount -
     * ... fileLimit unlimited
     * ... repoCount 0
     * ... repoLimit 10
     * ... licenseExpires 1620032964
     * ... licenseTimeRemaining -813978
     * ... licenseInvalid License expired.
     */

    /* SAMPLE no license
     * ... isLicensed no
     * ... userCount 2
     * ... userLimit unlimited
     * ... userSoftLimit 5
     * ... clientCount 20
     * ... clientLimit unlimited
     * ... clientSoftLimit 20
     * ... fileCount 327
     * ... fileLimit unlimited
     * ... fileSoftLimit 1000
     * ... repoCount 0
     * ... repoLimit 10
     * ... repoSoftLimit 10
     */

    /* SAMPLE no license over 1000 files
     * ... isLicensed no
     * ... userCount >5
     * ... userLimit 5
     * ... clientCount 2
     * ... clientLimit 20
     * ... fileCount >1000
     * ... fileLimit 1000
     * ... repoCount 0
     * ... repoLimit 10
     */

    private boolean licensed;
    private long userCount;
    private long userLimit;
    private long clientCount;
    private long clientLimit;
    private long fileCount;
    private long fileLimit;
    private long repoCount;
    private long repoLimit;
    private long licenseExpires;
    private long licenseTimeRemaining;
    private String licenseInvalid;

    private List<Map<String, Map<String, String>>> capability;

    public LicenseLimits() {
    }

    public LicenseLimits(Map<String, Object> map) {

        this.licensed = map.get("isLicensed").equals("yes");
        this.userCount = License.readValues(map, "userCount");
        this.userLimit = License.readValues(map, "userLimit");
        this.clientCount = License.readValues(map, "clientCount");
        this.clientLimit = License.readValues(map, "clientLimit");
        this.fileCount = License.readValues(map, "fileCount");
        this.fileLimit = License.readValues(map, "fileLimit");
        this.repoCount = License.readValues(map, "repoCount");
        this.repoLimit = License.readValues(map, "repoLimit");
        this.licenseExpires = License.readValues(map, "licenseExpires");
        this.licenseTimeRemaining = License.readValues(map, "licenseTimeRemaining");
        this.licenseInvalid = (String) map.get("licenseInvalid");

        int i = 0;
        this.capability = new ArrayList<>();
        while (map.containsKey("capability" + i)) {
            int j = 0;
            Map<String, String> attrs = new HashMap<>();
            while (map.containsKey("capabilityAttrKey" + i + "," + j)) {
                String attrsKey = (String) map.get("capabilityAttrKey" + i + "," + j);
                String attrsValue = (String) map.get("capabilityAttrValue" + i + "," + j);
                attrs.put(attrsKey, attrsValue);
                j++;
            }
            String key = (String) map.get("capability" + i);
            Map<String, Map<String, String>> capability = new HashMap<>();
            capability.put(key, attrs);
            this.capability.add(capability);
            i++;
        }
    }

    public boolean isLicensed() {
        return licensed;
    }

    public long getUserCount() {
        return userCount;
    }

    public long getUserLimit() {
        return userLimit;
    }

    public long getClientCount() {
        return clientCount;
    }

    public long getClientLimit() {
        return clientLimit;
    }

    public long getFileCount() {
        return fileCount;
    }

    public long getFileLimit() {
        return fileLimit;
    }

    public long getRepoCount() {
        return repoCount;
    }

    public long getRepoLimit() {
        return repoLimit;
    }

    public long getLicenseExpires() {
        return licenseExpires;
    }

    public long getLicenseTimeRemaining() {
        return licenseTimeRemaining;
    }

    public String getLicenseInvalid() {
        return licenseInvalid;
    }

    public List<Map<String, Map<String, String>>> getCapability() {
        return capability;
    }

}
