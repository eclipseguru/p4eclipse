/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.admin.IProtectionEntry;
import com.perforce.p4java.admin.IProtectionsTable;
import com.perforce.p4java.admin.ITriggerEntry;
import com.perforce.p4java.admin.ITriggersTable;
import com.perforce.p4java.client.IClient;
import com.perforce.p4java.client.IClientSummary.IClientOptions;
import com.perforce.p4java.client.IClientSummary.IClientSubmitOptions;
import com.perforce.p4java.client.IClientViewMapping;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpec;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.core.IExtension;
import com.perforce.p4java.core.ILabel;
import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ILicense;
import com.perforce.p4java.core.IMapEntry;
import com.perforce.p4java.core.IReviewSubscription;
import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamIgnoredMapping;
import com.perforce.p4java.core.IStreamRemappedMapping;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.core.IStreamViewMapping;
import com.perforce.p4java.core.IUser;
import com.perforce.p4java.core.IUserGroup;
import com.perforce.p4java.core.ViewMap;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.client.ClientView;
import com.perforce.p4java.impl.mapbased.MapKeys;
import com.perforce.p4java.server.CustomSpec;
import com.perforce.p4java.server.IServer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A useful class with methods to map certain classes to maps suitable for feeding
 * to the IServer execMapCmd method's input map. Use of these methods outside
 * their very limited initial applications is not guaranteed to work...
 */

public class InputMapper {

	public static final String DATE_PATTERN = "yyyy/MM/dd HH:mm:ss";

	private static Map<CustomSpec, Map<String, Object>> specs;

	private static Map<CustomSpec, Map<String, Object>> getDefaultMaps(IServer server) {
		if (specs == null) {
			specs = new HashMap<>();
			for (CustomSpec spec : CustomSpec.values()) {
				try {
					specs.put(spec, server.getSpec(spec));
				} catch (P4JavaException e) {
					Log.error("Unable to fetch spec: " + spec + "\nWith error: " + e.getLocalizedMessage());
				}
			}
		}
		return specs;
	}

	/**
	 * Map a P4Java changelist to an IServer input map.
	 *
	 * @param change            candidate changelist
	 * @param allowUnknownFiles
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IChangelist change, boolean allowUnknownFiles) {
		Map<String, Object> changeMap = new HashMap<String, Object>();

		if (change != null) {
			if (change.getId() == IChangelist.UNKNOWN) {
				changeMap.put("Change", "new");
			} else {
				changeMap.put("Change", "" + change.getId());
			}

			changeMap.put("Description", change.getDescription());
			if (change.getClientId() != null) {
				changeMap.put("Client", change.getClientId());
			}
			if (change.getUsername() != null) {
				changeMap.put("User", change.getUsername());
			}
			if (change.getVisibility() != null) {
				changeMap.put("Type", change.getVisibility().toString().toLowerCase());
			}
			if (change.getDate() != null) {
				DateFormat dateFormat = new SimpleDateFormat(DATE_PATTERN);
				changeMap.put("Date", dateFormat.format(change.getDate()));
			}
			if (change.getStatus() != null) {
				changeMap.put("Status", change.getStatus().toString().toLowerCase());
			}
			if (change.getChangelistStream() != null) {
				changeMap.put("Stream", change.getChangelistStream());
			}

			try {
				if ((allowUnknownFiles || (change.getId() != IChangelist.UNKNOWN
						&& change.getStatus() != ChangelistStatus.SUBMITTED))
						&& (change.getFiles(false) != null)) {
					int i = 0;
					for (IFileSpec spec : change.getFiles(false)) {
						changeMap.put("Files" + i++, spec.getDepotPathString());
					}
				}

				List<String> jobIdList = change.getJobIds();
				if ((jobIdList != null) && (jobIdList.size() > 0)) {
					int i = 0;
					for (String jobId : jobIdList) {
						changeMap.put("Jobs" + i++, jobId);
					}
				}
			} catch (P4JavaException exc) {
				Log.error("Unexpected exception in InputMapper.map(IChangelist change): "
						+ exc.getLocalizedMessage());
			}
		}

		return changeMap;
	}

	/**
	 * Map a P4Java changelist to an IServer input map.
	 *
	 * @param change candidate changelist
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IChangelist change) {
		return map(change, false);
	}

	/**
	 * Map a P4Java client object to an IServer input map.
	 *
	 * @param client candidate client
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IClient client) {
		Map<String, Object> clientMap = new HashMap<String, Object>();

		if (client != null) {
			clientMap.put("Client", client.getName());
			clientMap.put("Owner", client.getOwnerName());
			clientMap.put("Description", client.getDescription());

			if (client.getHostName() != null) {
				clientMap.put("Host", client.getHostName());
			}

			clientMap.put("Root", client.getRoot());

			if (client.getAlternateRoots() != null) {
				int i = 0;

				for (String altRoot : client.getAlternateRoots()) {
					if (altRoot != null) {
						clientMap.put("AltRoots" + i++, altRoot);
					}
				}
			}

			if (client.getLineEnd() != null) {
				clientMap.put("LineEnd", client.getLineEnd().toString().toLowerCase(Locale.ENGLISH));
			}
			IClientOptions opts = client.getOptions();
			IClientSubmitOptions subOpts = client.getSubmitOptions();
			ClientView view = client.getClientView();

			ArrayList<String> changeView = client.getChangeView();
			if (changeView != null && !changeView.isEmpty()) {
				int i = 0;

				for (String changeEntry : changeView) {
					if (changeEntry != null) {
						clientMap.put("ChangeView" + i++, changeEntry);
					}
				}
			}

			if (opts != null) {
				String optStr = ""
						+ (opts.isAllWrite() ? "allwrite " : "noallwrite ")
						+ (opts.isClobber() ? "clobber " : "noclobber ")
						+ (opts.isCompress() ? "compress " : "nocompress ")
						+ (opts.isLocked() ? "locked " : "unlocked ")
						+ (opts.isModtime() ? "modtime " : "nomodtime ")
						+ (opts.isRmdir() ? "rmdir" : "normdir");
				clientMap.put("Options", optStr);
			}

			if (subOpts != null) {
				String subOptsStr = "";
				if (subOpts.isSubmitunchanged()) {
					subOptsStr = IClient.IClientSubmitOptions.SUBMIT_UNCHANGED;
				} else if (subOpts.isSubmitunchangedReopen()) {
					subOptsStr = IClient.IClientSubmitOptions.SUBMIT_UNCHANGED_REOPEN;
				} else if (subOpts.isLeaveunchanged()) {
					subOptsStr = IClient.IClientSubmitOptions.LEAVE_UNCHANGED;
				} else if (subOpts.isLeaveunchangedReopen()) {
					subOptsStr = IClient.IClientSubmitOptions.LEAVE_UNCHANGED_REOPEN;
				} else if (subOpts.isRevertunchanged()) {
					subOptsStr = IClient.IClientSubmitOptions.REVERT_UNCHANGED;
				} else if (subOpts.isRevertunchangedReopen()) {
					subOptsStr = IClient.IClientSubmitOptions.REVERT_UNCHANGED_REOPEN;
				}
				clientMap.put("SubmitOptions", subOptsStr);
			}

			if ((view != null) && (view.getEntryList() != null)) {
				List<IClientViewMapping> viewList = view.getEntryList();

				for (IClientViewMapping mapping : viewList) {
					clientMap.put(MapKeys.VIEW_KEY + mapping.getOrder(), mapping.toString(" ", true));
				}
			}

			if (client.getStream() != null) {
				clientMap.put("Stream", client.getStream());
			}
			if (client.getServerId() != null) {
				clientMap.put("ServerID", client.getServerId());
			}
			if (client.getStreamAtChange() != IChangelist.UNKNOWN) {
				clientMap.put("StreamAtChange", client.getStreamAtChange());
			}
			if (client.getType() != null) {
				clientMap.put("Type", client.getType());
			}
			if (client.getBackup() != null) {
				clientMap.put("Backup", client.getBackup());
			}
		}

		return clientMap;
	}

	/**
	 * Map a P4Java label object to an IServer input map.
	 *
	 * @param label candidate label
	 * @return non-null map suitable for use with execMapCmd
	 */
	public static Map<String, Object> map(ILabel label) {
		final String LOCKED = "locked";
		final String UNLOCKED = "unlocked";
		final String AUTORELOAD = "autoreload";
		final String NOAUTORELOAD = "noautoreload";

		Map<String, Object> labelMap = new HashMap<String, Object>();

		if (label != null) {

			labelMap.put(MapKeys.LABEL_KEY, label.getName());
			labelMap.put(MapKeys.OWNER_KEY, label.getOwnerName());
			labelMap.put(MapKeys.DESCRIPTION_KEY, label.getDescription());
			labelMap.put(MapKeys.OPTIONS_KEY, (label.isLocked() ? LOCKED : UNLOCKED) + " " + (label.isAutoReload() ? AUTORELOAD : NOAUTORELOAD));
			if (label.getRevisionSpec() != null) {
				labelMap.put(MapKeys.REVISION_KEY, label.getRevisionSpec());
			}
			if (label.getLastUpdate() != null) {
				labelMap.put(MapKeys.UPDATE_KEY, label.getLastUpdate().toString());
			}
			if (label.getLastAccess() != null) {
				labelMap.put(MapKeys.ACCESS_KEY, label.getLastAccess().toString());
			}

			List<ILabelMapping> viewMaps = label.getViewMapping().getEntryList();

			if (viewMaps != null) {
				for (IMapEntry mapping : viewMaps) {
					labelMap.put(MapKeys.VIEW_KEY + mapping.getOrder(), mapping.toString(" ", true));
				}
			}
		}

		return labelMap;
	}

	/**
	 * Map a P4Java IUsers object to an IServer input map.
	 *
	 * @param user candidate user object
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IUser user) {

		Map<String, Object> userMap = new HashMap<String, Object>();

		if (user != null) {
			userMap.put(MapKeys.USER_KEY, user.getLoginName());
			userMap.put(MapKeys.EMAIL_KEY, user.getEmail());
			if (user.getFullName() != null) userMap.put(MapKeys.FULLNAME_KEY, user.getFullName());
			if (user.getJobView() != null) userMap.put(MapKeys.JOBVIEW_KEY, user.getJobView());
			if (user.getPassword() != null) userMap.put(MapKeys.PASSWORD_KEY, user.getPassword());
			if (user.getType() != null) userMap.put(MapKeys.TYPE_KEY, user.getType().toString().toLowerCase());

			if (user.getReviewSubscriptions() != null) {
				List<IReviewSubscription> reviewSubs = user.getReviewSubscriptions().getEntryList();

				if (reviewSubs != null) {
					int i = 0;
					for (IReviewSubscription mapping : reviewSubs) {
						userMap.put(MapKeys.REVIEWS_KEY + i, mapping.getSubscription());
						i++;
					}
				}
			}
		}

		return userMap;
	}

	/**
	 * Map a P4Java IUserGroup object to an IServer input map.
	 *
	 * @param group candidate user group object
	 * @return non-null map suitable for use with execMapCmd
	 */
	public static Map<String, Object> map(IUserGroup group) {
		Map<String, Object> groupMap = new HashMap<String, Object>();

		if (group != null) {
			if (group.getName() != null) groupMap.put(MapKeys.GROUP_KEY, group.getName());
			groupMap.put(MapKeys.MAXRESULTS_KEY, getUGValue(group.getMaxResults()));
			groupMap.put(MapKeys.TIMEOUT_KEY, getUGValue(group.getTimeout()));
			groupMap.put(MapKeys.MAXSCANROWS_KEY, getUGValue(group.getMaxScanRows()));
			groupMap.put(MapKeys.MAXLOCKTIME_KEY, getUGValue(group.getMaxLockTime()));
			if (group.getMaxOpenFiles() != -Integer.MAX_VALUE){
				groupMap.put(MapKeys.MAXOPENFILES_KEY, getUGValue(group.getMaxOpenFiles()));
			}
			groupMap.put(MapKeys.PASSWORD_TIMEOUT_KEY, getUGValue(group.getPasswordTimeout()));
			if (group.getSubgroups() != null) {
				int i = 0;
				for (String subGroup : group.getSubgroups()) {
					groupMap.put(MapKeys.SUBGROUPS_KEY + i, subGroup);
					i++;
				}
			}
			if (group.getOwners() != null) {
				int i = 0;
				for (String owner : group.getOwners()) {
					groupMap.put(MapKeys.OWNERS_KEY + i, owner);
					i++;
				}
			}
			if (group.getUsers() != null) {
				int i = 0;
				for (String user : group.getUsers()) {
					groupMap.put(MapKeys.USERS_KEY + i, user);
					i++;
				}
			}
		}

		return groupMap;
	}

	public static Map<String, Object> map(IBranchSpec branchSpec) {

		final String LOCKED = "locked";
		final String UNLOCKED = "unlocked";

		Map<String, Object> branchMap = new HashMap<String, Object>();

		if (branchSpec != null) {

			branchMap.put(MapKeys.BRANCH_KEY, branchSpec.getName());
			branchMap.put(MapKeys.OWNER_KEY, branchSpec.getOwnerName());
			branchMap.put(MapKeys.DESCRIPTION_KEY, branchSpec.getDescription());
			branchMap.put(MapKeys.OPTIONS_KEY, branchSpec.isLocked() ? LOCKED : UNLOCKED);

			if (branchSpec.getUpdated() != null) {
				branchMap.put(MapKeys.UPDATE_KEY, branchSpec.getUpdated().toString());
			}
			if (branchSpec.getAccessed() != null) {
				branchMap.put(MapKeys.ACCESS_KEY, branchSpec.getAccessed().toString());
			}

			ViewMap<IBranchMapping> viewMaps = branchSpec.getBranchView();

			if (viewMaps != null) {
				for (IBranchMapping mapping : viewMaps.getEntryList()) {
					if (mapping != null) {
						branchMap.put(MapKeys.VIEW_KEY + mapping.getOrder(),
								mapping.toString(" ", true));
					}
				}
			}
		}

		return branchMap;
	}

	public static Map<String, Object> map(IDepot depotSpec) {
		Map<String, Object> depotMap = new HashMap<String, Object>();

		if (depotSpec != null) {
			depotMap.put(MapKeys.DEPOT_KEY, depotSpec.getName());
			depotMap.put(MapKeys.OWNER_KEY, depotSpec.getOwnerName());
			depotMap.put(MapKeys.DESCRIPTION_KEY, depotSpec.getDescription());
			if (depotSpec.getDepotType() != null) {
				depotMap.put(MapKeys.TYPE_KEY, depotSpec.getDepotType().toString().toLowerCase());
			}
			if (depotSpec.getModDate() != null) {
				depotMap.put(MapKeys.DATE_KEY, depotSpec.getModDate());
			}
			if (depotSpec.getAddress() != null) {
				depotMap.put(MapKeys.ADDRESS_KEY, depotSpec.getAddress());
			}
			if (depotSpec.getSuffix() != null) {
				depotMap.put(MapKeys.SUFFIX_KEY, depotSpec.getSuffix());
			}
			if (depotSpec.getStreamDepth() != null) {
				depotMap.put(MapKeys.STREAM_DEPTH, depotSpec.getStreamDepth());
			}
			depotMap.put(MapKeys.MAP_KEY, depotSpec.getMap());

			ViewMap<IMapEntry> specMap = depotSpec.getSpecMap();
			if (specMap != null) {
				for (IMapEntry mapping : specMap.getEntryList()) {
					if (mapping != null) {
						depotMap.put(MapKeys.SPEC_MAP_KEY + mapping.getOrder(),
								mapping.toString(" ", true));
					}
				}
			}
		}

		return depotMap;
	}

	/**
	 * Map a list of P4Java IProtectionEntry object to an IServer input map.
	 *
	 * @param protectionsTable table - list of protection entries
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IProtectionsTable protectionsTable) {
		Map<String, Object> protectionsMap = new HashMap<String, Object>();

		if (protectionsTable != null && protectionsTable.getEntries() != null) {
			int count = 0;
			for (IProtectionEntry entry : protectionsTable.getEntries()) {
				StringBuilder line = new StringBuilder();
				if (entry.getMode() != null) {
					line.append(entry.getMode());
				}
				line.append(" ").append(entry.isGroup() ? "group" : "user");
				if (entry.getName() != null) {
					line.append(" ").append(entry.getName());
				}
				if (entry.getHost() != null) {
					line.append(" ").append(entry.getHost());
				}
				if (entry.getPath() != null) {
					line.append(" ").append(entry.getPath());
				}
				// Using the innate ordering (count) of the Java List, instead of the entry.getOrder()
				// See job070733
				protectionsMap.put(MapKeys.PROTECTIONS_KEY + count++, line.toString());
			}
		}

		return protectionsMap;
	}

	/**
	 * Map a P4Java stream object to an IServer input map.
	 *
	 * @param stream candidate stream
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(IStream stream, IServer server) throws P4JavaException {
		Map<String, Object> streamMap = new HashMap<String, Object>();
		if (stream == null) {
			return streamMap;
		}

		// set default spec data
		streamMap.putAll(getDefaultMaps(server).get(CustomSpec.STREAM));

		streamMap.putAll(stream.getRawFields());

		streamMap.putAll(parseMap(stream.getStreamView(), MapKeys.PATHS_KEY));
		streamMap.putAll(parseMap(stream.getRemappedView(), MapKeys.REMAPPED_KEY));
		streamMap.putAll(parseMap(stream.getIgnoredView(), MapKeys.IGNORED_KEY));

		// For pre 21.1 servers without ParentView.
		IStreamSummary.ParentView parentView;

		// task and Virtual streams have always parentView = inherit
		if (stream.getType() == IStreamSummary.Type.TASK || stream.getType() == IStreamSummary.Type.VIRTUAL) {
			parentView = IStreamSummary.ParentView.INHERIT;
		} else {
			String streamPath = stream.getStream();
			IStream tempStream = server.getStream(streamPath);
			parentView = tempStream.getParentView();
		}

		// If server returned it, and it was not provided, use one based on dm.stream.parentview.
		if (stream.getParentView() == null && parentView != null) {
			streamMap.put(MapKeys.PARENT_VIEW_KEY, parentView.toString());
		}

		// If server doesn't support ParentView, remove it.'
		if (server.getServerVersion() < 20211) {
			streamMap.remove(MapKeys.PARENT_VIEW_KEY);
		}

		return streamMap;
	}

	/**
	 * Map a list of P4Java ITriggerEntry object to an IServer input map.
	 *
	 * @param triggersTable table - list of trigger entries
	 * @return non-null map suitable for use with execMapCmd
	 */

	public static Map<String, Object> map(ITriggersTable triggersTable) {
		Map<String, Object> triggersMap = new HashMap<String, Object>();

		if (triggersTable != null && triggersTable.getEntries() != null) {
			for (ITriggerEntry entry : triggersTable.getEntries()) {
				StringBuilder line = new StringBuilder();
				if (entry.getName() != null) {
					line.append(entry.getName());
				}
				if (entry.getTriggerType() != null) {
					line.append(" ").append(entry.getTriggerType().toString());
				}
				if (entry.getPath() != null) {
					line.append(" ").append(entry.getPath());
				}
				if (entry.getCommand() != null) {
					line.append(" ").append(entry.getCommand());
				}
				triggersMap.put(MapKeys.TRIGGERS_KEY + entry.getOrder(), line.toString());
			}
		}

		return triggersMap;
	}

	public static Map<String, Object> map(ILicense licenseSpec) {
		Map<String, Object> licenseMap = new HashMap<String, Object>();

		if (licenseSpec != null) {
			licenseMap.put(MapKeys.LICENSE_KEY, licenseSpec.getLicense());
			licenseMap.put(MapKeys.LICENSE_EXPIRES_KEY, licenseSpec.getLicenseExpires());
			licenseMap.put(MapKeys.LICENSE_SUPPORT_KEY, licenseSpec.getSupportExpires());
			licenseMap.put(MapKeys.LICENSE_CUSTOMER_KEY, licenseSpec.getCustomer());
			licenseMap.put(MapKeys.LICENSE_APPLICATION_KEY, licenseSpec.getApplication());
			licenseMap.put(MapKeys.LICENSE_IPADDRESS_KEY, licenseSpec.getIpaddress());
			licenseMap.put(MapKeys.LICENSE_PLATFORM_KEY, licenseSpec.getPlatform());
			licenseMap.put(MapKeys.LICENSE_CLIENTS_KEY, String.valueOf(licenseSpec.getClients()));
			licenseMap.put(MapKeys.LICENSE_USERS_KEY, String.valueOf(licenseSpec.getUsers()));

			if (licenseSpec.getCapabilities() != null) {
				for (int i = 0; i < licenseSpec.getCapabilities().size(); i++) {
					licenseMap.put(MapKeys.LICENSE_CAPABILITIES_KEY + i, licenseSpec.getCapabilities().get(i));
				}
			}
		}

		return licenseMap;
	}

	public static Map<String, Object> map(IExtension extensionSpec) {
		Map<String, Object> extensionMap = new HashMap<String, Object>();

		if (extensionSpec != null) {
			//TODO null checks?
			extensionMap.put(MapKeys.EXTENSION_NAME_KEY, extensionSpec.getExtName());
			extensionMap.put(MapKeys.EXTENSION_DESCRIPTION_KEY, extensionSpec.getDescription());
			extensionMap.put(MapKeys.EXTENSION_VERSION_KEY, extensionSpec.getExtVersion());
			extensionMap.put(MapKeys.EXTENSION_UUID_KEY, extensionSpec.getExtUUID());
			extensionMap.put(MapKeys.EXTENSION_REV_KEY, extensionSpec.getExtRev());
			extensionMap.put(MapKeys.EXTENSION_SCRIPT_TIME_KEY, extensionSpec.getExtMaxScriptTime());
			extensionMap.put(MapKeys.EXTENSION_SCRIPT_MEMORY_KEY, extensionSpec.getExtMaxScriptMem());
			extensionMap.put(MapKeys.EXTENSION_ENABLED_KEY, extensionSpec.getExtEnabled());
			extensionMap.put(MapKeys.EXTENSION_NAME_SPACE_KEY, extensionSpec.getNameSpace());
			extensionMap.put(MapKeys.EXTENSION_OWNER_KEY, extensionSpec.getOwner());
			extensionMap.put(MapKeys.EXTENSION_UPDATE_KEY, extensionSpec.getUpdate());
			extensionMap.put(MapKeys.EXTENSION_DESCRIPTION_KEY, extensionSpec.getDescription());
			extensionMap.put(MapKeys.EXTENSION_CONFIG_KEY, extensionSpec.getExtConfig());
			extensionMap.put(MapKeys.EXTENSION_GROUPS_KEY, extensionSpec.getExtAllowedGroups());
			extensionMap.put(MapKeys.EXTENSION_P4USER_KEY, extensionSpec.getExtP4USER());
			extensionMap.put(MapKeys.EXTENSION_DEBUG, extensionSpec.getExtDebug());
		}

		return extensionMap;

	}

	private static String getUGValue(int val) {
		if (val == IUserGroup.UNLIMITED) {
			return "unlimited";
		} else if (val == IUserGroup.UNSET) {
			return "unset";
		} else if (val == IUserGroup.UNDEFINED) {
			return String.valueOf(IUserGroup.UNDEFINED);
		} else {
			return "" + val;
		}
	}

	private static Map<String, Object> parseMap(ViewMap<?> viewMap, String type) {
		Map<String, Object> returnMap = new HashMap<>();

		if (viewMap == null) {
			return returnMap;
		}

		for (IMapEntry mapping : viewMap.getEntryList()) {
			if (mapping == null) {
				continue;
			}

			String value = mapping.toString(" ", true);
			returnMap.put(type + mapping.getOrder(), value);
		}

		return returnMap;
	}

}
