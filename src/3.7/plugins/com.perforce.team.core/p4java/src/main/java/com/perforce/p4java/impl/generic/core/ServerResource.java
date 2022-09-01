/*
 * Copyright 2009 Perforce Software Inc., All Rights Reserved.
 */
package com.perforce.p4java.impl.generic.core;

import com.perforce.p4java.Log;
import com.perforce.p4java.core.IServerResource;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.ConnectionException;
import com.perforce.p4java.exception.RequestException;
import com.perforce.p4java.exception.UnimplementedError;
import com.perforce.p4java.option.Options;
import com.perforce.p4java.server.IServer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract implementation class for the IServerResource interface.<p>
 * <p>
 * Implementations of interfaces that extend IServerResource should
 * use this as a superclass unless there are good reasons not to.
 */

public abstract class ServerResource implements IServerResource {
	/**
	 * Raw Spec Map
	 */
	private Map<String, Object> rawFields = new HashMap<>();

	/**
	 * Refreshable flag
	 */
	protected boolean refreshable = false;

	/**
	 * Updateable flag
	 */
	protected boolean updateable = false;

	/**
	 * Server instance
	 */
	protected IServer server = null;

	/**
	 * Default constructor -- sets complete to true,
	 * completable, refreshable, and updateable to false,
	 * and server to null.
	 */
	protected ServerResource() {
	}

	/**
	 * Sets complete to true, completable, refreshable, and
	 * updateable to false, and server to the passed-in value.
	 */
	protected ServerResource(IServer server) {
		this.server = server;
	}

	/**
	 * Explicit some-value constructor; sets server to null.
	 */
	protected ServerResource(boolean refreshable, boolean updateable) {
		this(refreshable, updateable, null);
	}

	/**
	 * Explicit all-value constructor.
	 */
	protected ServerResource(
			boolean refreshable, boolean updateable, IServer server) {
		this(server);
		this.refreshable = refreshable;
		this.updateable = updateable;
	}


	/**
	 * @see com.perforce.p4java.core.IServerResource#canRefresh()
	 */
	public boolean canRefresh() {
		return this.refreshable && this.server != null;
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#canUpdate()
	 */
	public boolean canUpdate() {
		return this.updateable && this.server != null;
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#complete()
	 */
	public void complete() throws ConnectionException, RequestException, AccessException {
		throw new UnimplementedError("called default IServerResourceImpl.complete");
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#refresh()
	 */
	public void refresh() throws ConnectionException, RequestException, AccessException {
		throw new UnimplementedError("called default IServerResourceImpl.refresh");
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#update()
	 */
	public void update() throws ConnectionException, RequestException, AccessException {
		throw new UnimplementedError("called default IServerResourceImpl.update");
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#update(boolean)
	 */
	public void update(boolean force) throws ConnectionException, RequestException, AccessException {
		throw new UnimplementedError("called IServerResourceImpl.update(force)");
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#update(com.perforce.p4java.option.Options)
	 */
	public void update(Options opts) throws ConnectionException, RequestException, AccessException {
		throw new UnimplementedError("called IServerResourceImpl.update(opts)");
	}

	/**
	 * Set the resource as refreshable
	 *
	 * @param refreshable
	 */
	public void setRefreshable(boolean refreshable) {
		this.refreshable = refreshable;
	}

	/**
	 * @see com.perforce.p4java.core.IServerResource#setServer(com.perforce.p4java.server.IServer)
	 */
	public void setServer(IServer server) {
		this.server = server;
	}

	public Object getRawField(String field) {
		return rawFields.get(field);
	}

	public void setRawField(String field, Object value) {
		rawFields.put(field, value);
	}

	public boolean hasRawField(String field) {
		return rawFields.containsKey(field);
	}

	public Map<String, Object> getRawFields() {
		return rawFields;
	}

	public void setRawFields(Map<String, Object> map) {
		rawFields.putAll(map);
	}

	public void clearRawFields() {
		rawFields = new HashMap<>();
	}

	private final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";

	protected Date parseDate(String value) {
		// return null if map's value is null|empty
		if (value == null || value.isEmpty()) {
			return null;
		}

		// Look for numeric date (summary spec date)
		try {
			long d = Long.parseLong(value);
			return new Date(d * 1000);
		} catch (NumberFormatException e) {
			// continue
		}

		// look for string formatted date (spec -o output)
		try {
			return new SimpleDateFormat(DATE_FORMAT).parse(value);
		} catch (ParseException e) {
			Log.warn("Unexpected date format: " + e.getMessage());
			Log.exception(e);
		}
		return null;
	}

	protected String toDateString(Date date) {
		if (date == null) {
			return "0";  // epoch 0 - undefined date
		}
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
		return simpleDateFormat.format(date);
	}
    /*
    protected String parseComment(String line){
        if (!line.contains("##")){
            return line;
        }

        String[] parts = line.split("##");
        for (String part :parts){

        }


    }
    */
     
}
