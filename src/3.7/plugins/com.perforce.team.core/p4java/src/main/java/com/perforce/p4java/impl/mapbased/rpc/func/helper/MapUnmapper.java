/*
 * Copyright 2009 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.mapbased.rpc.func.helper;

import com.perforce.p4java.impl.generic.core.License;
import com.perforce.p4java.impl.mapbased.MapKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides unmapping services to the P4Java RPC implementation.
 * These are not what they probably sound like -- they're basically
 * a way to serialise an input map for something like a changelist
 * or job onto a single byte buffer to be sent to the server as a
 * single data argument with newlines, tabs, etc.<p>
 * <p>
 * The need for this will probably go away when we refactor the upper
 * levels of P4Java to optimise and rationalise the use of maps overall.
 */

public class MapUnmapper {

    /**
     * Unmap a change list. Absolutely no sanity or other checks are done on
     * the passed-in map...
     *
     * @param inMap
     * @param strBuf
     */

    public static void unmapChangelistMap(Map<String, Object> inMap,
                                          StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.CHANGE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.CHANGE_KEY) + MapKeys.DOUBLE_LF);

            Object client = inMap.get(MapKeys.CLIENT_KEY);
            if (client != null) {
                strBuf.append(MapKeys.CLIENT_KEY + MapKeys.COLON_SPACE + client + MapKeys.DOUBLE_LF);
            }
            Object user = inMap.get(MapKeys.USER_KEY);
            if (user != null) {
                strBuf.append(MapKeys.USER_KEY + MapKeys.COLON_SPACE + user + MapKeys.DOUBLE_LF);
            }
            Object type = inMap.get(MapKeys.TYPE_KEY);
            if (type != null) {
                strBuf.append(MapKeys.TYPE_KEY + MapKeys.COLON_SPACE + type + MapKeys.DOUBLE_LF);
            }
            Object status = inMap.get(MapKeys.STATUS_KEY);
            if (status != null) {
                strBuf.append(MapKeys.STATUS_KEY + MapKeys.COLON_SPACE + status + MapKeys.DOUBLE_LF);
            }
            Object date = inMap.get(MapKeys.DATE_KEY);
            if (date != null) {
                strBuf.append(MapKeys.DATE_KEY + MapKeys.COLON_SPACE + date + MapKeys.DOUBLE_LF);
            }
            Object stream = inMap.get(MapKeys.STREAM_KEY);
            if (stream != null) {
                strBuf.append(MapKeys.STREAM_KEY + MapKeys.COLON_SPACE + stream + MapKeys.DOUBLE_LF);
            }

            String descr = replaceNewlines((String) inMap.get(MapKeys.DESCRIPTION_KEY));
            strBuf.append(MapKeys.DESCRIPTION_KEY + MapKeys.COLON_LF + (descr == null ? MapKeys.EMPTY : descr) + MapKeys.LF);
            strBuf.append(MapKeys.FILES_KEY + MapKeys.COLON_LF);

            for (int i = 0; ; i++) {
                String fileStr = (String) inMap.get(MapKeys.FILES_KEY + i);

                if (fileStr != null) {
                    strBuf.append(MapKeys.TAB + inMap.get(MapKeys.FILES_KEY + i) + MapKeys.LF);
                } else {
                    break;
                }
            }

            strBuf.append(MapKeys.LF);

            for (int i = 0; ; i++) {
                String fileStr = (String) inMap.get(MapKeys.JOBS_KEY + i);

                if (fileStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.JOBS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + inMap.get(MapKeys.JOBS_KEY + i) + MapKeys.LF);
                } else {
                    break;
                }
            }

            strBuf.append(MapKeys.LF);
        }
    }

    /**
     * Unmap a job. Jobs basically have free format defined by the associated
     * jobspec (which we don't have access to here), so we have to try to the
     * best we can with what we've got -- which is to dump the map to the strbuf
     * while guessing at things like string formats, etc. This may prove error-prone
     * in the long run.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapJobMap(Map<String, Object> inMap,
                                   StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            for (Map.Entry<String, Object> entry : inMap.entrySet()) {
                strBuf.append(entry.getKey() + MapKeys.COLON_SPACE + replaceNewlines((String) entry.getValue()) + MapKeys.DOUBLE_LF);
            }
        }
    }

    /**
     * Unmap a spec. specs basically have free format so we have to try to the
     * best we can with what we've got -- which is to dump the map to the strbuf
     * while guessing at things like string formats, etc. This may prove error-prone
     * in the long run.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapSpecMap(Map<String, Object> inMap,
                                    StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            updateSpecSection(inMap, MapKeys.FIELDS_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.VALUES_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.PRESETS_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.WORDS_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.FORMATS_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.OPENABLE_KEY, strBuf);
            updateSpecSection(inMap, MapKeys.COMMENTS_KEY, strBuf);
        }
    }

    public static void unmapLicenseMap(Map<String, Object> inMap,
                                       StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.LICENSE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.LICENSE_KEY) + MapKeys.DOUBLE_LF);

            Object licenseExpires = inMap.get(MapKeys.LICENSE_EXPIRES_KEY);
            if (licenseExpires != null) {
                strBuf.append(MapKeys.LICENSE_EXPIRES_KEY + MapKeys.COLON_SPACE + licenseExpires + MapKeys.DOUBLE_LF);
            }

            Object supportExpires = inMap.get(MapKeys.LICENSE_SUPPORT_KEY);
            if (supportExpires != null) {
                strBuf.append(MapKeys.LICENSE_SUPPORT_KEY + MapKeys.COLON_SPACE + supportExpires + MapKeys.DOUBLE_LF);
            }

            Object customer = inMap.get(MapKeys.LICENSE_CUSTOMER_KEY);
            if (customer != null) {
                strBuf.append(MapKeys.LICENSE_CUSTOMER_KEY + MapKeys.COLON_SPACE + customer + MapKeys.DOUBLE_LF);
            }

            Object application = inMap.get(MapKeys.LICENSE_APPLICATION_KEY);
            if (application != null) {
                strBuf.append(MapKeys.LICENSE_APPLICATION_KEY + MapKeys.COLON_SPACE + application + MapKeys.DOUBLE_LF);
            }

            Object ipaddress = inMap.get(MapKeys.LICENSE_IPADDRESS_KEY);
            if (ipaddress != null) {
                strBuf.append(MapKeys.LICENSE_IPADDRESS_KEY + MapKeys.COLON_SPACE + ipaddress + MapKeys.DOUBLE_LF);
            }

            Object platform = inMap.get(MapKeys.LICENSE_PLATFORM_KEY);
            if (platform != null) {
                strBuf.append(MapKeys.LICENSE_PLATFORM_KEY + MapKeys.COLON_SPACE + platform + MapKeys.DOUBLE_LF);
            }

            long clients = License.readValues(inMap, MapKeys.LICENSE_CLIENTS_KEY);
            if (clients > 0) {
                strBuf.append(MapKeys.LICENSE_CLIENTS_KEY + MapKeys.COLON_SPACE + clients + MapKeys.DOUBLE_LF);
            }

            long users = License.readValues(inMap, MapKeys.LICENSE_USERS_KEY);
            if (users > 0) {
                strBuf.append(MapKeys.LICENSE_USERS_KEY + MapKeys.COLON_SPACE + users + MapKeys.DOUBLE_LF);
            }

            if (inMap.get(MapKeys.LICENSE_CAPABILITIES_KEY + 0) != null) {
                strBuf.append(MapKeys.LICENSE_CAPABILITIES_KEY + MapKeys.COLON_SPACE + MapKeys.LF);

                for (int i = 0; ; i++) {
                    String capability = (String) inMap.get(MapKeys.LICENSE_CAPABILITIES_KEY + i);

                    if (capability != null) {
                        strBuf.append(MapKeys.TAB + inMap.get(MapKeys.LICENSE_CAPABILITIES_KEY + i) + MapKeys.LF);
                    } else {
                        break;
                    }
                }
            }
        }
    }

    public static void unmapExtensionMap(Map<String, Object> inMap,
                                         StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {

            Object extName = inMap.get(MapKeys.EXTENSION_NAME_KEY);
            if (extName != null) {
                strBuf.append(MapKeys.EXTENSION_NAME_KEY + MapKeys.COLON_SPACE + extName + MapKeys.DOUBLE_LF);
            }

            Object extDescription = inMap.get(MapKeys.EXTENSION_EXT_DESCRIPTION_KEY);
            if (extDescription != null) {
                strBuf.append(MapKeys.EXTENSION_EXT_DESCRIPTION_KEY + MapKeys.COLON_SPACE + extDescription + MapKeys.DOUBLE_LF);
            }

            Object extVersion = inMap.get(MapKeys.EXTENSION_VERSION_KEY);
            if (extVersion != null) {
                strBuf.append(MapKeys.EXTENSION_VERSION_KEY + MapKeys.COLON_SPACE + extVersion + MapKeys.DOUBLE_LF);
            }

            Object extUUID = inMap.get(MapKeys.EXTENSION_UUID_KEY);
            if (extUUID != null) {
                strBuf.append(MapKeys.EXTENSION_UUID_KEY + MapKeys.COLON_SPACE + extUUID + MapKeys.DOUBLE_LF);
            }

            Object extRev = inMap.get(MapKeys.EXTENSION_REV_KEY);
            if (extRev != null) {
                strBuf.append(MapKeys.EXTENSION_REV_KEY + MapKeys.COLON_SPACE + extRev + MapKeys.DOUBLE_LF);
            }

            Object extMaxScriptTime = inMap.get(MapKeys.EXTENSION_SCRIPT_TIME_KEY);
            if (extMaxScriptTime != null) {
                strBuf.append(MapKeys.EXTENSION_SCRIPT_TIME_KEY + MapKeys.COLON_SPACE + extMaxScriptTime + MapKeys.DOUBLE_LF);
            }

            Object extMaxScriptMem = inMap.get(MapKeys.EXTENSION_SCRIPT_MEMORY_KEY);
            if (extMaxScriptMem != null) {
                strBuf.append(MapKeys.EXTENSION_SCRIPT_MEMORY_KEY + MapKeys.COLON_SPACE + extMaxScriptMem + MapKeys.DOUBLE_LF);
            }

            Object extEnabled = inMap.get(MapKeys.EXTENSION_ENABLED_KEY);
            if (extEnabled != null) {
                strBuf.append(MapKeys.EXTENSION_ENABLED_KEY + MapKeys.COLON_SPACE + extEnabled + MapKeys.DOUBLE_LF);
            }

            Object name = inMap.get(MapKeys.EXTENSION_NAME_SPACE_KEY);
            if (name != null) {
                strBuf.append(MapKeys.EXTENSION_NAME_SPACE_KEY + MapKeys.COLON_SPACE + name + MapKeys.DOUBLE_LF);
            }

            Object owner = inMap.get(MapKeys.EXTENSION_OWNER_KEY);
            if (owner != null) {
                strBuf.append(MapKeys.EXTENSION_OWNER_KEY + MapKeys.COLON_SPACE + owner + MapKeys.DOUBLE_LF);
            }

            Object update = inMap.get(MapKeys.EXTENSION_UPDATE_KEY);
            if (update != null) {
                strBuf.append(MapKeys.EXTENSION_UPDATE_KEY + MapKeys.COLON_SPACE + update + MapKeys.DOUBLE_LF);
            }

            Object description = inMap.get(MapKeys.EXTENSION_DESCRIPTION_KEY);
            if (description != null) {
                strBuf.append(MapKeys.EXTENSION_DESCRIPTION_KEY + MapKeys.COLON_SPACE + description + MapKeys.DOUBLE_LF);
            }

            Object extConfig = inMap.get(MapKeys.EXTENSION_CONFIG_KEY);
            if (extConfig != null) {
                Map<String, String> extConfigMap = (Map<String,String>)extConfig;
                /**
                 * ExtConfig:
                 * 	auth_token:	00000000-0000-0000-0000-000000000000
                 * 	p4search_url:	http://p4search.mydomain.com:4567/api/v1/obliterate
                 */
                strBuf.append(MapKeys.EXTENSION_CONFIG_KEY + MapKeys.COLON_LF);
                for (String key : extConfigMap.keySet()) {
                    strBuf.append(MapKeys.TAB + key + MapKeys.COLON_SPACE + extConfigMap.get(key) + MapKeys.LF);
                }

            }

            Object extAllowedGroups = inMap.get(MapKeys.EXTENSION_GROUPS_KEY);
            if (extAllowedGroups != null) {
                strBuf.append(MapKeys.EXTENSION_GROUPS_KEY + MapKeys.COLON_SPACE + extAllowedGroups + MapKeys.DOUBLE_LF);
            }

            Object extP4USER = inMap.get(MapKeys.EXTENSION_P4USER_KEY);
            if (extP4USER != null) {
                strBuf.append(MapKeys.EXTENSION_P4USER_KEY + MapKeys.COLON_SPACE + extP4USER + MapKeys.DOUBLE_LF);
            }

            Object extDebug = inMap.get(MapKeys.EXTENSION_DEBUG);
            if (extDebug != null) {
                strBuf.append(MapKeys.EXTENSION_DEBUG + MapKeys.COLON_SPACE + extDebug + MapKeys.DOUBLE_LF);
            }
        }

    }

	private static void updateSpecSection(Map<String, Object> inMap, String section, StringBuffer strBuf) {
        List<String> keyList = new ArrayList<>();
        for (String key : inMap.keySet()) {
            if (key.startsWith(section)) {
                keyList.add(key);
            }
        }
        if (keyList.size() > 0) {
            strBuf.append(section + MapKeys.COLON_LF);
            for (String key : keyList) {
                Object value = inMap.get(key);
                strBuf.append(MapKeys.TAB + value + MapKeys.LF);
            }
        }
    }

    /**
     * Unmap a client map. Similar in intent and execution to unmapJobMap.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapClientMap(Map<String, Object> inMap,
                                      StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.CLIENT_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.CLIENT_KEY) + MapKeys.DOUBLE_LF);

            Object owner = inMap.get(MapKeys.OWNER_KEY);
            if (owner != null) {
                strBuf.append(MapKeys.OWNER_KEY + MapKeys.COLON_SPACE + owner + MapKeys.DOUBLE_LF);
            }

            // Fix for job036074, a null Host should not be turned into the
            // "null" string but should be omitted from the map string.
            Object host = inMap.get(MapKeys.HOST_KEY);
            if (host != null) {
                strBuf.append(MapKeys.HOST_KEY + MapKeys.COLON_SPACE + host.toString() + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.UPDATE_KEY)) {
                strBuf.append(MapKeys.UPDATE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.UPDATE_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.ACCESS_KEY)) {
                strBuf.append(MapKeys.ACCESS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.ACCESS_KEY) + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.OPTIONS_KEY)) {
                strBuf.append(MapKeys.OPTIONS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OPTIONS_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.SUBMITOPTIONS_KEY)) {
                strBuf.append(MapKeys.SUBMITOPTIONS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.SUBMITOPTIONS_KEY) + MapKeys.DOUBLE_LF);
            }
            strBuf.append(MapKeys.ROOT_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.ROOT_KEY) + MapKeys.DOUBLE_LF);
            if (inMap.containsKey(MapKeys.LINEEND_KEY)) {
                strBuf.append(MapKeys.LINEEND_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.LINEEND_KEY) + MapKeys.DOUBLE_LF);
            }
            String descr = replaceNewlines((String) inMap.get(MapKeys.DESCRIPTION_KEY));
            strBuf.append(MapKeys.DESCRIPTION_KEY + MapKeys.COLON_LF + (descr == null ? MapKeys.EMPTY : descr) + MapKeys.LF);
            strBuf.append(MapKeys.VIEW_KEY + MapKeys.COLON_LF);

            for (int i = 0; ; i++) {
                String fileStr = (String) inMap.get(MapKeys.VIEW_KEY + i);

                if (fileStr != null) {
                    strBuf.append(MapKeys.TAB + inMap.get(MapKeys.VIEW_KEY + i) + MapKeys.LF);
                } else {
                    break;
                }
            }

            strBuf.append(MapKeys.LF);

            for (int i = 0; ; i++) {
                String fileStr = (String) inMap.get(MapKeys.ALTROOTS_KEY + i);

                if (fileStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.ALTROOTS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + inMap.get(MapKeys.ALTROOTS_KEY + i) + MapKeys.LF);
                } else {
                    break;
                }
            }

            strBuf.append(MapKeys.LF);

            if (inMap.containsKey(MapKeys.STREAM_KEY)) {
                strBuf.append(MapKeys.STREAM_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.STREAM_KEY) + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.SERVERID_KEY)) {
                strBuf.append(MapKeys.SERVERID_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.SERVERID_KEY) + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.STREAMATCHANGE_KEY)) {
                strBuf.append(MapKeys.STREAMATCHANGE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.STREAMATCHANGE_KEY) + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.TYPE_KEY)) {
                strBuf.append(MapKeys.TYPE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.TYPE_KEY) + MapKeys.DOUBLE_LF);
            }

            for (int i = 0; ; i++) {
                String fileStr = (String) inMap.get(MapKeys.CHANGE_VIEW_KEY + i);

                if (fileStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.CHANGE_VIEW_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + inMap.get(MapKeys.CHANGE_VIEW_KEY + i) + MapKeys.LF);
                } else {
                    break;
                }
            }

            strBuf.append(MapKeys.LF);

            if (inMap.containsKey(MapKeys.CLIENT_BACKUP_KEY)) {
                strBuf.append(MapKeys.CLIENT_BACKUP_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.CLIENT_BACKUP_KEY) + MapKeys.DOUBLE_LF);
            }
        }
    }

    /**
     * Unmap a Perforce user map.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapUserMap(Map<String, Object> inMap,
                                    StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.USER_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.USER_KEY) + MapKeys.DOUBLE_LF);
            if (inMap.containsKey(MapKeys.EMAIL_KEY)) {
                strBuf.append(MapKeys.EMAIL_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.EMAIL_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.FULLNAME_KEY)) {
                strBuf.append(MapKeys.FULLNAME_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.FULLNAME_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.JOBVIEW_KEY)) {
                strBuf.append(MapKeys.JOBVIEW_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.JOBVIEW_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.PASSWORD_KEY)) {
                strBuf.append(MapKeys.PASSWORD_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.PASSWORD_KEY) + MapKeys.DOUBLE_LF);
            }

            if (inMap.containsKey(MapKeys.TYPE_KEY)) {
                strBuf.append(MapKeys.TYPE_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.TYPE_KEY) + MapKeys.DOUBLE_LF);
            }

            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.REVIEWS_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.REVIEWS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a Perforce user group map.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapUserGroupMap(Map<String, Object> inMap,
                                         StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.GROUP_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.GROUP_KEY) + MapKeys.DOUBLE_LF);
            if (inMap.containsKey(MapKeys.MAXRESULTS_KEY)) {
                strBuf.append(MapKeys.MAXRESULTS_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.MAXRESULTS_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.MAXSCANROWS_KEY)) {
                strBuf.append(MapKeys.MAXSCANROWS_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.MAXSCANROWS_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.MAXLOCKTIME_KEY)) {
                strBuf.append(MapKeys.MAXLOCKTIME_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.MAXLOCKTIME_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.TIMEOUT_KEY)) {
                strBuf.append(MapKeys.TIMEOUT_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.TIMEOUT_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.PASSWORD_TIMEOUT_KEY)) {
                strBuf.append(MapKeys.PASSWORD_TIMEOUT_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.PASSWORD_TIMEOUT_KEY) + MapKeys.DOUBLE_LF);
            }
			if (inMap.containsKey(MapKeys.MAXOPENFILES_KEY)) {
				strBuf.append(MapKeys.MAXOPENFILES_KEY + MapKeys.COLON_SPACE
						+ inMap.get(MapKeys.MAXOPENFILES_KEY) + MapKeys.DOUBLE_LF);
			}
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.SUBGROUPS_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.SUBGROUPS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.OWNERS_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.OWNERS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.USERS_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.USERS_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a Label Perforce label.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapLabelMap(Map<String, Object> inMap,
                                     StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.LABEL_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.LABEL_KEY) + MapKeys.DOUBLE_LF);
            strBuf.append(MapKeys.OWNER_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OWNER_KEY) + MapKeys.DOUBLE_LF);
            if (inMap.containsKey(MapKeys.UPDATE_KEY)) {
                strBuf.append(MapKeys.UPDATE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.UPDATE_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.ACCESS_KEY)) {
                strBuf.append(MapKeys.ACCESS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.ACCESS_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.REVISION_KEY)) {
                strBuf.append(MapKeys.REVISION_KEY + MapKeys.COLON_SPACE
                        + inMap.get(MapKeys.REVISION_KEY) + MapKeys.DOUBLE_LF);
            }
            String descr = replaceNewlines((String) inMap.get(MapKeys.DESCRIPTION_KEY));
            strBuf.append(MapKeys.DESCRIPTION_KEY + MapKeys.COLON_LF + (descr == null ? MapKeys.EMPTY : descr) + MapKeys.LF);
            if (inMap.containsKey(MapKeys.OPTIONS_KEY)) {
                strBuf.append(MapKeys.OPTIONS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OPTIONS_KEY) + MapKeys.DOUBLE_LF);
            }

            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.VIEW_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.VIEW_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a BranchSpec spec.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapBranchMap(Map<String, Object> inMap,
                                      StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.BRANCH_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.BRANCH_KEY) + MapKeys.DOUBLE_LF);

            if (inMap.containsKey(MapKeys.OWNER_KEY)) {
                strBuf.append(MapKeys.OWNER_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OWNER_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.UPDATE_KEY)) {
                strBuf.append(MapKeys.UPDATE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.UPDATE_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.ACCESS_KEY)) {
                strBuf.append(MapKeys.ACCESS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.ACCESS_KEY) + MapKeys.DOUBLE_LF);
            }
            String descr = replaceNewlines((String) inMap.get(MapKeys.DESCRIPTION_KEY));
            strBuf.append(MapKeys.DESCRIPTION_KEY + MapKeys.COLON_LF + (descr == null ? MapKeys.EMPTY : descr) + MapKeys.LF);
            if (inMap.containsKey(MapKeys.OPTIONS_KEY)) {
                strBuf.append(MapKeys.OPTIONS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OPTIONS_KEY) + MapKeys.DOUBLE_LF);
            }

            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.VIEW_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.VIEW_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a depot map.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapDepotMap(Map<String, Object> inMap,
                                     StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.DEPOT_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.DEPOT_KEY) + MapKeys.DOUBLE_LF);
            if (inMap.containsKey(MapKeys.OWNER_KEY)) {
                strBuf.append(MapKeys.OWNER_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.OWNER_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.DATE_KEY)) {
                strBuf.append(MapKeys.DATE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.DATE_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.TYPE_KEY)) {
                strBuf.append(MapKeys.TYPE_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.TYPE_KEY) + MapKeys.DOUBLE_LF);
            }
            String descr = replaceNewlines((String) inMap.get(MapKeys.DESCRIPTION_KEY));
            strBuf.append(MapKeys.DESCRIPTION_KEY + MapKeys.COLON_LF + (descr == null ? MapKeys.EMPTY : descr) + MapKeys.LF);

            if (inMap.containsKey(MapKeys.ADDRESS_KEY)) {
                strBuf.append(MapKeys.ADDRESS_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.ADDRESS_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.SUFFIX_KEY)) {
                strBuf.append(MapKeys.SUFFIX_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.SUFFIX_KEY) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.STREAM_DEPTH)) {
                strBuf.append(MapKeys.STREAM_DEPTH + MapKeys.COLON_SPACE + inMap.get(MapKeys.STREAM_DEPTH) + MapKeys.DOUBLE_LF);
            }
            if (inMap.containsKey(MapKeys.MAP_KEY)) {
                strBuf.append(MapKeys.MAP_KEY + MapKeys.COLON_SPACE + inMap.get(MapKeys.MAP_KEY) + MapKeys.DOUBLE_LF);
            }

            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.SPEC_MAP_KEY + i);

                if (mapStr != null) {
                    if (i == 0) {
                        strBuf.append(MapKeys.SPEC_MAP_KEY + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a list of protection entries.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapProtectionEntriesMap(Map<String, Object> inMap,
                                                 StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.PROTECTIONS_KEY + MapKeys.COLON_LF);
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.PROTECTIONS_KEY + i);

                if (mapStr != null) {
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a stream map.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapStreamMap(Map<String, Object> inMap, StringBuffer strBuf) {
        if (inMap == null || strBuf == null) {
            return;
        }

        // Remove extraTag keys (like firmerThanParent)
        inMap.remove("altArg");
        inMap.remove("specFormatted");
        inMap.remove("Comments");
        for (int i = 0; ; i++) {
            String extraTag = (String) inMap.get(MapKeys.EXTRATAG_KEY + i);
            if (extraTag != null) {
                inMap.remove(extraTag);
            } else {
                break;
            }
        }

        for (Map.Entry<String, Object> entry : inMap.entrySet()) {
            String key = entry.getKey();
            String value = (String) entry.getValue();

            Pattern pattern = Pattern.compile("([a-zA-Z]+)([0-9]+)");
            Matcher matcher = pattern.matcher(key);
            if (!matcher.matches()) {
                value = (value.contains(MapKeys.LF)) ? replaceNewlines(value) : value;
                strBuf.append(key + MapKeys.COLON_SPACE + (value == null ? MapKeys.EMPTY : value) + MapKeys.DOUBLE_LF);
            }
        }

        String[] viewKeys = {MapKeys.PATHS_KEY, MapKeys.REMAPPED_KEY, MapKeys.IGNORED_KEY};

        for (String key : viewKeys) {
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(key + i);
                String mapStrComment = (String) inMap.get(key + "Comment" + i);
                if (mapStrComment == null) {
                    mapStrComment = "";
                } else {
                    mapStrComment = mapStrComment;
                }

                if (mapStr != null || mapStrComment != "") {
                    if (i == 0) {
                        strBuf.append(key + MapKeys.COLON_LF);
                    }
                    strBuf.append(MapKeys.TAB + mapStr + mapStrComment + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Unmap a list of trigger entries.
     *
     * @param inMap
     * @param strBuf
     */
    public static void unmapTriggerEntriesMap(Map<String, Object> inMap,
                                              StringBuffer strBuf) {
        if ((inMap != null) && (strBuf != null)) {
            strBuf.append(MapKeys.TRIGGERS_KEY + MapKeys.COLON_LF);
            for (int i = 0; ; i++) {
                String mapStr = (String) inMap.get(MapKeys.TRIGGERS_KEY + i);

                if (mapStr != null) {
                    strBuf.append(MapKeys.TAB + mapStr + MapKeys.LF);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Replace all but the last newline in the incoming string with
     * newline / tab pairs. Useful for various multi-line form inputs.
     *
     * @param str
     * @return replaced string
     */
    public static String replaceNewlines(String str) {
        if (str != null) {
            String[] strs = str.split(MapKeys.LF);

            if (strs.length == 1) {
                return MapKeys.TAB + str + MapKeys.LF;
            } else {
                StringBuilder retStr = new StringBuilder();

                for (String s : strs) {
                    retStr.append(MapKeys.TAB);
                    retStr.append(s);
                    retStr.append(MapKeys.LF);
                }

                return retStr.toString();
            }
        }

        return null;
    }
}
