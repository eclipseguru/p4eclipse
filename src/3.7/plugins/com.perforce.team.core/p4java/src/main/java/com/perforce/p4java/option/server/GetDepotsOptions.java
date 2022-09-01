package com.perforce.p4java.option.server;

import com.perforce.p4java.core.IDepot;
import com.perforce.p4java.exception.OptionsException;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.util.List;

public class GetDepotsOptions extends Options {

	/**
	 * p4 depots [-t type] [[-e|-E] nameFilter]
	 */
	public static final String OPTIONS_SPECS = "s:t s:e s:E";

	/**
	 * If not-null, limits qualifying depots to those matching type.
	 */
	protected IDepot.DepotType type = null;

	/**
	 * If not-null, limits qualifying depots to those matching the nameFilter pattern.
	 */
	protected String nameFilter = null;

	/**
	 * If non-null, limits output to depots whose name matches (case-insensitive)
	 * the nameFilter pattern. Corresponds to -EnameFilter flag
	 */
	protected String caseInsensitiveNameFilter = null;

	/**
	 * Default constructor; sets all fields to null, zero, or false.
	 */
	public GetDepotsOptions() {
		super();
	}

	/**
	 * Strings-based constructor; see 'p4 help [command]' for possible options.
	 * <p>
	 *
	 * <b>WARNING: you should not pass more than one option or argument in each
	 * string parameter. Each option or argument should be passed-in as its own
	 * separate string parameter, without any spaces between the option and the
	 * option value (if any).<b>
	 * <p>
	 *
	 * <b>NOTE: setting options this way always bypasses the internal options
	 * values, and getter methods against the individual values corresponding to
	 * the strings passed in to this constructor will not normally reflect the
	 * string's setting. Do not use this constructor unless you know what you're
	 * doing and / or you do not also use the field getters and setters.</b>
	 *
	 * @see com.perforce.p4java.option.Options#Options(java.lang.String...)
	 */
	public GetDepotsOptions(String... options) {
		super(options);
	}

	/**
	 * Explicit-value constructor.
	 */
/*	public GetDepotsOptions(IDepot.DepotType type, String nameFilter) {
		super();
		this.type = type;
		this.nameFilter = nameFilter;
	}*/
	@Override
	public List<String> processOptions(IServer server) throws OptionsException {
		this.optionList = this.processFields(OPTIONS_SPECS,
				this.getType() == null ? null : this.getType().toString().toLowerCase(),
				this.nameFilter,
				this.caseInsensitiveNameFilter);
		return this.optionList;
	}

	public IDepot.DepotType getType() {
		return type;
	}

	public GetDepotsOptions setType(IDepot.DepotType type) {
		this.type = type;
		return this;
	}

	public String getNameFilter() {
		return nameFilter;
	}

	public String getCaseInsensitiveNameFilter() {
		return caseInsensitiveNameFilter;
	}

	public GetDepotsOptions setNameFilter(String nameFilter) {
		this.nameFilter = nameFilter;
		return this;
	}

	public void setCaseInsensitiveNameFilter(String caseInsensitiveNameFilter) {
		this.caseInsensitiveNameFilter = caseInsensitiveNameFilter;
	}
}
