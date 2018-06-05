/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.folder;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IFileDiff;
import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.impl.generic.core.file.FilePath;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.impl.generic.core.file.FilePath.PathType;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Resource;

import java.io.InputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.team.core.history.IFileRevision;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DiffFile extends P4Resource implements IP4DiffFile {

    private static class DiffFile extends P4File {

        /**
         * @param fileSpec
         * @param parent
         */
        public DiffFile(IFileSpec fileSpec, IP4Container parent) {
            super(fileSpec, parent, false);
        }

        /**
         * @see com.perforce.team.core.p4java.P4File#refresh()
         */
        @Override
        public void refresh() {
            // File cannot be refreshed
        }

        /**
         * @see com.perforce.team.core.p4java.P4File#setFileSpec(com.perforce.p4java.core.file.IFileSpec)
         */
        @Override
        public void setFileSpec(IFileSpec spec) {
            // Spec cannot be updated
        }

    }

    private boolean file1 = false;
    private int hashCode = 0;
    private IFileDiff diff;
    private IP4File file;
    private IP4Revision revision;
    private IP4DiffFile pair;

    /**
     * Create a new p4 diff file
     * 
     * @param parent
     * @param diff
     * @param file1
     */
    public P4DiffFile(IP4Container parent, IFileDiff diff, boolean file1) {
        Assert.isNotNull(parent, "Parent cannot be null"); //$NON-NLS-1$
        Assert.isNotNull(diff, "Diff cannot be null"); //$NON-NLS-1$
        this.diff = diff;
        this.file1 = file1;
        String path = null;
        String type = null;
        int fileRevision = -1;
        if (file1) {
            path = this.diff.getDepotFile1();
            type = this.diff.getFileType1();
            fileRevision = this.diff.getRevision1();
        } else {
            path = this.diff.getDepotFile2();
            type = this.diff.getFileType2();
            fileRevision = this.diff.getRevision2();
        }
        if (path != null) {
            IFileSpec spec = new FileSpec(new FilePath(PathType.DEPOT, path));
            spec.setFileType(type);
            spec.setEndRevision(fileRevision);
            this.file = new DiffFile(spec, parent);
            this.hashCode = path.hashCode();
        }
    }

    /**
     * Set revision
     * 
     * @param revision
     */
    public void setRevision(IP4Revision revision) {
        this.revision = revision;
    }

    /**
     * Set diff file pair
     * 
     * @param pair
     */
    public void setPair(IP4DiffFile pair) {
        this.pair = pair;
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getRevision()
     */
    public int getRevision() {
        return getRevision(isFile1());
    }

    private int getRevision(boolean source) {
        return source ? this.diff.getRevision1() : this.diff.getRevision2();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this.file.getConnection();
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

    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {

    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#isFile()
     */
    @Override
    public boolean isFile() {
        return true;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4File.class == adapter) {
            return this.file;
        }
        if (IP4Revision.class == adapter || IFileRevision.class == adapter) {
            return this.revision;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getStatus()
     */
    public Status getStatus() {
        return this.diff.getStatus();
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getFile()
     */
    public IP4File getFile() {
        return this.file;
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getDiff()
     */
    public IFileDiff getDiff() {
        return this.diff;
    }

    private InputStream getFileContents(String path, int revision) {
        InputStream stream = null;
        if (path != null && revision > 0) {
            IP4File file = new DiffFile(new FileSpec(new FilePath(
                    PathType.DEPOT, path)), getParent());
            stream = file.getRemoteContents(revision);
        }
        return stream;
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getFile1Contents()
     */
    public InputStream getFile1Contents() {
        return getFileContents(this.diff.getDepotFile1(),
                this.diff.getRevision1());
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getFile2Contents()
     */
    public InputStream getFile2Contents() {
        return getFileContents(this.diff.getDepotFile2(),
                this.diff.getRevision2());
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof IP4DiffFile) {
            IP4DiffFile other = (IP4DiffFile) obj;
            return this.getRevision() == other.getRevision()
                    && this.isFile1() == other.isFile1() && super.equals(obj);
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#getPair()
     */
    public IP4DiffFile getPair() {
        return this.pair;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#hashCode()
     */
    @Override
    public int hashCode() {
    	if(this.hashCode>0)
    		return this.hashCode;
    	else
    		return super.hashCode();
    }

    /**
     * @see com.perforce.team.core.folder.IP4DiffFile#isFile1()
     */
    public boolean isFile1() {
        return this.file1;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.file.toString();
    }

}
