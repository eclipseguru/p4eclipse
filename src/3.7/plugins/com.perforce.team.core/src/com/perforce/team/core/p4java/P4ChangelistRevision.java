/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IRevisionIntegrationData;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ChangelistRevision extends FileRevision implements
        IP4ChangelistRevision {

    private IP4Connection connection;
    private Map<String, IFileRevisionData> files;
    private int id = -1;
    private long time = -1L;
    private String client;
    private String user;
    private String description;

    /**
     * Create a p4 changelist revision
     * 
     * @param connection
     */
    public P4ChangelistRevision(IP4Connection connection) {
        this.connection = connection;
        this.files = new HashMap<String, IFileRevisionData>();
    }

    /**
     * Add revision data to this changelist revision
     * 
     * @param data
     */
    public void add(IFileRevisionData data) {
        if (data != null) {
            this.files.put(data.getDepotFileName(), data);
            client = data.getClientName();
            user = data.getUserName();
            id = data.getChangelistId();
            description = data.getDescription();
            time = data.getDate().getTime();
        }
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        return this.time;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getAction()
     */
    public FileAction getAction() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getChangelist()
     */
    public int getChangelist() {
        return this.id;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getCharset()
     */
    public String getCharset() {
        return this.connection.getParameters().getCharset();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getClient()
     */
    public String getClient() {
        return this.client;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getIntegrationData()
     */
    public IRevisionIntegrationData[] getIntegrationData() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getRemotePath()
     */
    public String getRemotePath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getRevision()
     */
    public int getRevision() {
        return 0;
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getAuthor()
     */
    @Override
    public String getAuthor() {
        return this.user;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getType()
     */
    public String getType() {
        return ""; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#isModifiedByOwner()
     */
    public boolean isModifiedByOwner() {
        return this.connection.isOwner(getAuthor());
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getName()
     */
    public String getName() {
        return ""; //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getComment()
     */
    @Override
    public String getComment() {
        return this.description;
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getStorage(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
        return null;
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#isPropertyMissing()
     */
    public boolean isPropertyMissing() {
        return false;
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#withAllProperties(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IFileRevision withAllProperties(IProgressMonitor monitor)
            throws CoreException {
        return null;
    }

    /**
     * @param revision
     * @return - comparison
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IP4Revision revision) {
        return this.getChangelist() - revision.getChangelist();
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getContentIdentifier()
     */
    @Override
    public String getContentIdentifier() {
        return MessageFormat.format(Messages.P4ChangelistRevision_0,
                getChangelist());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4ChangelistRevision#getRevisions()
     */
    public IFileRevisionData[] getRevisions() {
        return this.files.values().toArray(
                new IFileRevisionData[this.files.size()]);
    }

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof P4ChangelistRevision && this.connection.equals(((P4ChangelistRevision) obj).getConnection()) && this.getChangelist()==((P4ChangelistRevision) obj).getChangelist();
    }
    
    @Override
    public int hashCode() {
    	int hash=0;
    	if(this.connection!=null)
    		hash+=this.connection.hashCode();
    	if(this.getChangelist()>0)
    		hash+=this.getChangelist()*31;
    	if(hash>0)
    		return hash;
    	
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
