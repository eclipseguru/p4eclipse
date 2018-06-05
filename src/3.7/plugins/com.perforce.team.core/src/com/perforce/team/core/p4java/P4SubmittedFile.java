/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileSpec;

import org.eclipse.core.runtime.IAdaptable;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4SubmittedFile extends P4Resource implements IP4SubmittedFile,
        IAdaptable {

    private IP4SubmittedChangelist changelist;
    private IP4File file;

    /**
     * Create a p4 submitted file
     * 
     * @param file
     * @param list
     */
    public P4SubmittedFile(IP4File file, IP4SubmittedChangelist list) {
        this.changelist = list;
        this.file = file;
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
        if (!(obj instanceof IP4SubmittedFile)) {
            return false;
        }
        return super.equals(obj)
                && getChangelist() == ((IP4SubmittedFile) obj).getChangelist();
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode()+getChangelist().hashCode();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4SubmittedFile#getChangelist()
     */
    public IP4SubmittedChangelist getChangelist() {
        return this.changelist;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4SubmittedFile#getFile()
     */
    public IP4File getFile() {
        return this.file;
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
        return this.changelist;
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

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        refresh(0);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4SubmittedFile#getAction()
     */
    public FileAction getAction() {
        return this.file.getAction();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4SubmittedFile#getFileSpec()
     */
    public IFileSpec getFileSpec() {
        return this.file.getP4JFile();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4SubmittedFile#getRevision()
     */
    public int getRevision() {
        int revision = -1;
        IFileSpec spec = getFileSpec();
        if (spec != null) {
            revision = spec.getEndRevision();
        }
        return revision;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#isFile()
     */
    @Override
    public boolean isFile() {
        return true;
    }

}
