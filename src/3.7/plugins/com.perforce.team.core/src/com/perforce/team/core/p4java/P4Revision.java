/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IRevisionIntegrationData;
import com.perforce.team.core.P4CoreUtils;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.team.core.history.provider.FileRevision;

/**
 * 
 * P4 revision class wrapping around an {@link IP4Connection} and
 * {@link IFileRevisionData}.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4Revision extends FileRevision implements IP4Revision {

    private IP4Connection connection;
    private IFileRevisionData data;

    /**
     * @param connection
     * @param data
     */
    public P4Revision(IP4Connection connection, IFileRevisionData data) {
        this.connection = connection;
        this.data = data;
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getAuthor()
     */
    @Override
    public String getAuthor() {
        return this.data.getUserName();
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getComment()
     */
    @Override
    public String getComment() {
        return this.data.getDescription();
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getContentIdentifier()
     */
    @Override
    public String getContentIdentifier() {
        return getRemotePath() + "#" + this.data.getRevision(); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.team.core.history.provider.FileRevision#getTimestamp()
     */
    @Override
    public long getTimestamp() {
        return this.data.getDate().getTime();
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getName()
     */
    public String getName() {
        String path = getRemotePath();
        if (path != null) {
            int index = path.lastIndexOf('/');
            return path.substring(index + 1);
        }
        return null;
    }

    /**
     * @see org.eclipse.team.core.history.IFileRevision#getStorage(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStorage getStorage(IProgressMonitor monitor) throws CoreException {
        return new IStorage() {

            public Object getAdapter(Class adapter) {
                return null;
            }

            public boolean isReadOnly() {
                return true;
            }

            public String getName() {
                return P4Revision.this.getName();
            }

            public IPath getFullPath() {
                return null;
            }

            public InputStream getContents() throws CoreException {
                IP4File file = connection.getFile(getRemotePath());
                if (file != null) {
                    return file.getRemoteContents(getRevision());
                }
                return null;
            }
        };
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
     * @see com.perforce.team.core.p4java.IP4Revision#getChangelist()
     */
    public int getChangelist() {
        return this.data.getChangelistId();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getAction()
     */
    public FileAction getAction() {
        return this.data.getAction();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getRemotePath()
     */
    public String getRemotePath() {
        return this.data.getDepotFileName();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getType()
     */
    public String getType() {
        return this.data.getFileType();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getRevision()
     */
    public int getRevision() {
        return this.data.getRevision();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getIntegrationData()
     */
    public IRevisionIntegrationData[] getIntegrationData() {
        List<IRevisionIntegrationData> integs = this.data
                .getRevisionIntegrationData();
        if (integs != null) {
            return integs.toArray(new IRevisionIntegrationData[integs.size()]);
        } else {
            return new IRevisionIntegrationData[0];
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getClient()
     */
    public String getClient() {
        return this.data.getClientName();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getCharset()
     */
    public String getCharset() {
        return this.connection.getParameters().getCharsetNoNone();
    }

    /**
     * @param revision
     * @return - int comparison
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IP4Revision revision) {
        if (this.getChangelist() > IChangelist.DEFAULT) {
            return this.getChangelist() - revision.getChangelist();
        } else {
            return this.getRevision() - revision.getRevision();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Revision#isModifiedByOwner()
     */
    public boolean isModifiedByOwner() {
        return this.connection.isOwner(getAuthor());
    }

    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
		return obj instanceof P4Revision
				&& P4CoreUtils.equals(this.connection,((P4Revision) obj).getConnection())
				&& this.getRevision() == ((P4Revision) obj).getRevision();
    }
    
    @Override
    public int hashCode() {
    	int hash=0;
    	hash+=P4CoreUtils.hashCode(this.connection);
    	hash+=this.getRevision()*31;
    	if(hash>0)
    		return hash;
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
