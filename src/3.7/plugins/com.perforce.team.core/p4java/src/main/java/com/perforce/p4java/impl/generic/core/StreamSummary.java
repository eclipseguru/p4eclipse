/**
 *
 */
package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.impl.mapbased.MapKeys;

import java.util.Date;
import java.util.Map;

/**
 * Default implementation class for the IStreamSummary interface.
 */
public class StreamSummary extends ServerResource implements IStreamSummary {

	/**
	 * Simple default generic IOptions implementation class.
	 */
	public static class Options implements IOptions {

		private boolean ownerSubmit = false;
		private boolean locked = false;
		private boolean noToParent = false;
		private boolean noFromParent = false;
		private boolean mergeAny = false;

		/**
		 * Default constructor; sets all fields to false.
		 */
		public Options() {
		}

		/**
		 * Explicit-value constructor.
		 */
		public Options(boolean ownerSubmit, boolean locked, boolean noToParent, boolean noFromParent) {
			this(ownerSubmit, locked, noToParent, noFromParent, false);
		}

		/**
		 * Explicit-value constructor.
		 */
		public Options(boolean ownerSubmit, boolean locked, boolean noToParent, boolean noFromParent, boolean mergeAny) {
			setOwnerSubmit(ownerSubmit);
			setLocked(locked);
			setNoToParent(noToParent);
			setNoFromParent(noFromParent);
			setMergeAny(mergeAny);
		}

		/**
		 * Attempts to construct a stream Options object from a typical p4 cmd
		 * options string, e.g.
		 * "allsubmit/ownersubmit, [un]locked, [no]toparent, [no]fromparent". If
		 * optionsString is null, this is equivalent to calling the default
		 * constructor.
		 */
		public Options(String optionsString) {
			if (optionsString == null) {
				return;
			}

			for (String str : optionsString.split(" ")) {
				if (str.equalsIgnoreCase("ownersubmit")) {
					setOwnerSubmit(true);
				} else if (str.equalsIgnoreCase("locked")) {
					setLocked(true);
				} else if (str.equalsIgnoreCase("notoparent")) {
					setNoToParent(true);
				} else if (str.equalsIgnoreCase("nofromparent")) {
					setNoFromParent(true);
				} else if (str.equalsIgnoreCase("mergeany")) {
					setMergeAny(true);
				}
			}
		}

		/**
		 * Return a Perforce-standard representation of these options. This
		 * string is in the same format as used by the stream Options(String
		 * optionsString) constructor.
		 */
		public String toString() {
			return (isOwnerSubmit() ? "ownersubmit" : "allsubmit")
					+ (isLocked() ? " locked" : " unlocked")
					+ (isNoToParent() ? " notoparent" : " toparent")
					+ (isNoFromParent() ? " nofromparent" : " fromparent")
					+ (isMergeAny() ? " mergeany" : " mergedown");
		}

		public boolean isOwnerSubmit() {
			return ownerSubmit;
		}

		public void setOwnerSubmit(boolean ownerSubmit) {
			this.ownerSubmit = ownerSubmit;
		}

		public boolean isLocked() {
			return locked;
		}

		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		public boolean isNoToParent() {
			return noToParent;
		}

		public void setNoToParent(boolean noToParent) {
			this.noToParent = noToParent;
		}

		public boolean isNoFromParent() {
			return noFromParent;
		}

		public void setNoFromParent(boolean noFromParent) {
			this.noFromParent = noFromParent;
		}

		public boolean isMergeAny() {
			return mergeAny;
		}

		public void setMergeAny(boolean mergeAny) {
			this.mergeAny = mergeAny;
		}
	}

	/**
	 * Default constructor -- sets all fields to null or false.
	 */
	public StreamSummary() {
		// Set default fields
		setDefaults();
	}

	/**
	 * Default constructor; same as no-argument default constructor, except that
	 * it sets the ServerResource superclass fields appropriately for summary
	 * only (everything false) or full stream spec (updateable and refreshable).
	 */
	public StreamSummary(boolean summaryOnly) {
		super(!summaryOnly, !summaryOnly);
	}

	/**
	 * Explicit-value constructor. If summaryOnly is true, refreshable and
	 * updeateable are set true in the ServerResource superclass, otherwise
	 * they're set false.
	 */
	public StreamSummary(boolean summaryOnly, String stream, Date accessed,
						 Date updated, String name, String ownerName, String description,
						 String parent, Type type, Options options) {
		super(!summaryOnly, !summaryOnly);
		setStream(stream);
		setAccessed(accessed);
		setUpdated(updated);
		setName(name);
		setOwnerName(ownerName);
		setDescription(description);
		setParent(parent);
		setType(type);
		setOptions(options);
	}

	/**
	 * Construct a StreamSummary from a map returned by the Perforce server. If
	 * summaryOnly is true, this map was returned by the IOptionsServer
	 * getStreamSummaryList or similar summary-only method; otherwise it's
	 * assumed to be the full stream spec.
	 * <p>
	 *
	 * If map is null, this is equivalent to calling the default
	 * summaryOnly-argument constructor.
	 */
	public StreamSummary(Map<String, Object> map, boolean summaryOnly) {
		super(!summaryOnly, !summaryOnly);

		// Set default fields
		if (!summaryOnly) {
			setDefaults();
		}

		if (map != null) {
			setRawFields(map);
		}
	}

	private void setDefaults() {
		setDescription("");
		setOptions(new Options());
		setParent("none");
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getStream()
	 */
	public String getStream() {
		return (String) getRawField(MapKeys.STREAM_KEY);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setStream(String)
	 */
	public void setStream(String stream) {
		setRawField(MapKeys.STREAM_KEY, stream);
		if (getName() == null) {
			stream = stream.substring(stream.lastIndexOf("/") + 1);
			setName(stream);
		}
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getAccessed()
	 */
	public Date getAccessed() {
		return parseDate((String) getRawField(MapKeys.ACCESS_KEY));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setAccessed(java.util.Date)
	 */
	public void setAccessed(Date accessed) {
		setRawField(MapKeys.ACCESS_KEY, toDateString(accessed));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getUpdated()
	 */
	public Date getUpdated() {
		return parseDate((String) getRawField(MapKeys.UPDATE_KEY));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setUpdated(java.util.Date)
	 */
	public void setUpdated(Date updated) {
		setRawField(MapKeys.UPDATE_KEY, toDateString(updated));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getName()
	 */
	public String getName() {
		return (String) getRawField(MapKeys.NAME_KEY);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setName(java.lang.String)
	 */
	public void setName(String name) {
		setRawField(MapKeys.NAME_KEY, name);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getOwnerName()
	 */
	public String getOwnerName() {
		return (String) getRawField(MapKeys.OWNER_KEY);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setOwnerName(java.lang.String)
	 */
	public void setOwnerName(String ownerName) {
		setRawField(MapKeys.OWNER_KEY, ownerName);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getDescription()
	 */
	public String getDescription() {
		String desc = (String) getRawField(MapKeys.DESC_LC_KEY);
		String description = (String) getRawField(MapKeys.DESCRIPTION_KEY);
		return (desc != null) ? desc : description;
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		setRawField(MapKeys.DESCRIPTION_KEY, description);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getParent()
	 */
	public String getParent() {
		if (!hasRawField(MapKeys.PARENT_KEY)) {
			return null;
		}
		String value = (String) getRawField(MapKeys.PARENT_KEY);
		value = (value == null) ? "none" : value;
		return value;
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setParent(java.lang.String)
	 */
	public void setParent(String parent) {
		parent = (parent == null) ? "none" : parent;
		setRawField(MapKeys.PARENT_KEY, parent);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getType()
	 */
	public Type getType() {
		String type = (String) getRawField(MapKeys.TYPE_KEY);
		if (type == null) {
			return null;
		}
		return IStreamSummary.Type.fromString(type.toUpperCase());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getParentView()
	 */
	public ParentView getParentView() {
		String parentView = (String) getRawField(MapKeys.PARENT_VIEW_KEY);
		if (parentView == null) {
			return null;
		}
		return IStreamSummary.ParentView.fromString(parentView.toUpperCase());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setType(com.perforce.p4java.core.IStreamSummary.Type)
	 */
	public void setType(Type type) {
		if (type == null) {
			return;
		}
		setRawField(MapKeys.TYPE_KEY, type.toString().toLowerCase());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setParentView(com.perforce.p4java.core.IStreamSummary.ParentView)
	 */
	public void setParentView(ParentView parentView) {
		if (parentView == null) {
			return;
		}
		setRawField(MapKeys.PARENT_VIEW_KEY, parentView.toString().toLowerCase());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getOptions()
	 */
	public IOptions getOptions() {
		return new Options((String) getRawField(MapKeys.OPTIONS_KEY));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setOptions(com.perforce.p4java.core.IStreamSummary.IOptions)
	 */
	public void setOptions(IOptions options) {
		options = (options == null) ? new Options() : options;
		setRawField(MapKeys.OPTIONS_KEY, options.toString());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#isFirmerThanParent()
	 */
	public boolean isFirmerThanParent() {
		return new Boolean((String) getRawField("firmerThanParent"));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setFirmerThanParent(boolean)
	 */
	public void setFirmerThanParent(boolean firmerThanParent) {
		setRawField("firmerThanParent", new Boolean(firmerThanParent).toString());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#isChangeFlowsToParent()
	 */
	public boolean isChangeFlowsToParent() {
		return new Boolean((String) getRawField("changeFlowsToParent"));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setChangeFlowsToParent(boolean)
	 */
	public void setChangeFlowsToParent(boolean changeFlowsToParent) {
		setRawField("changeFlowsToParent", new Boolean(changeFlowsToParent).toString());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#isChangeFlowsFromParent()
	 */
	public boolean isChangeFlowsFromParent() {
		return new Boolean((String) getRawField("changeFlowsFromParent"));
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setChangeFlowsFromParent(boolean)
	 */
	public void setChangeFlowsFromParent(boolean changeFlowsFromParent) {
		setRawField("changeFlowsFromParent", new Boolean(changeFlowsFromParent).toString());
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#getBaseParent()
	 */
	@Deprecated
	public String getBaseParent() {
		return (String) getRawField("baseParent");
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#setBaseParent(java.lang.String)
	 */
	@Deprecated
	public void setBaseParent(String baseParent) {
		setRawField("baseParent", baseParent);
	}

	/**
	 * @see com.perforce.p4java.core.IStreamSummary#isUnloaded()
	 */
	public boolean isUnloaded() {
		String value = (String) getRawField("IsUnloaded");
		return "1".equals(value);
	}
}
