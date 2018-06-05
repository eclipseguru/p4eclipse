/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.IFileSpec;

import java.io.InputStream;
import java.util.Date;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ShelveFile extends P4Resource implements IP4ShelveFile {

    /**
     * SHELVE_SPECIFIER
     */
    public static final String SHELVE_SPECIFIER = "@="; //$NON-NLS-1$

    private IP4ShelvedChangelist shelvedList;
    private IChangelist list;
    private IP4File file;

    /**
     * Create a new p4 shelve file
     * 
     * @param list
     * @param file
     * @param readOnly
     */
    public P4ShelveFile(IChangelist list, IP4File file, boolean readOnly) {
        this.list = list;
        this.file = file;
        this.shelvedList = new P4ShelvedChangelist(getConnection(), list,
                readOnly);
        this.readOnly = readOnly;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4File.class == adapter) {
            return this.file;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof IP4ShelveFile)) {
            return false;
        }
        return super.equals(obj) && getId() == ((IP4ShelveFile) obj).getId();
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode()*31+getId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getFile()
     */
    public IP4File getFile() {
        return this.file;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getId()
     */
    public int getId() {
        return this.list.getId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getDate()
     */
    public Date getDate() {
        return this.list.getDate();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getDescription()
     */
    public String getDescription() {
        return this.list.getDescription();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getUser()
     */
    public String getUser() {
        return this.list.getUsername();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getWorkspace()
     */
    public String getWorkspace() {
        return this.list.getClientId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getRemoteContents()
     */
    public InputStream getRemoteContents() {
        return this.file.getRemoteContents(getRevision());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        return this.file.getActionPath();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type preferredType) {
        return this.file.getActionPath(preferredType);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        return this.file.getClient();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return this.file.getClientPath();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this.file.getConnection();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        return this.file.getLocalPath();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
        return this.file.getName();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return this.file.getParent();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        return this.file.getRemotePath();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        refresh();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getRevision()
     */
    public String getRevision() {
        return SHELVE_SPECIFIER + this.list.getId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#getChangelist()
     */
    public IP4ShelvedChangelist getChangelist() {
        return this.shelvedList;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#unshelve()
     */
    public IFileSpec[] unshelve() {
        return unshelve(-1);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#unshelve(int)
     */
    public IFileSpec[] unshelve(int toChangelist) {
        return unshelve(toChangelist, false);
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.file.toString();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ShelveFile#unshelve(int, boolean)
     */
    public IFileSpec[] unshelve(int toChangelist, boolean overwrite) {
        return this.shelvedList.unshelve(new IP4ShelveFile[] { this },
                toChangelist, overwrite);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#isFile()
     */
    @Override
    public boolean isFile() {
        return true;
    }

}
