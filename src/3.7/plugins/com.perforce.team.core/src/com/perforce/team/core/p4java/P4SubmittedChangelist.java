/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.PerforceProviderPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SubmittedChangelist extends P4Changelist implements
        IP4SubmittedChangelist {

    /**
     * @param connection
     * @param list
     */
    public P4SubmittedChangelist(IP4Connection connection, IChangelist list) {
        super(connection, list);
        this.readOnly = false;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IP4SubmittedChangelist && super.equals(obj);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getFiles()
     */
    public IP4Resource[] getFiles() {
        List<IP4Resource> files = new ArrayList<IP4Resource>();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4SubmittedFile) {
                files.add(resource);
            }
        }
        return files.toArray(new IP4Resource[files.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getActionPath()
     */
    @Override
    public String getActionPath() {
        StringBuilder path = null;
        if (this.cachedFiles.isEmpty()) {
            refresh();
        }
        String[] paths = null;
        if (this.cachedFiles != null) {
            for (IP4Resource resource : this.cachedFiles) {
                if (resource instanceof IP4SubmittedFile) {
                    String[] segments = resource.getRemotePath().split("/"); //$NON-NLS-1$
                    if (paths != null) {
                        int common = 0;
                        int length = Math.min(paths.length, segments.length);
                        for (int i = 0; i < length; i++) {
                            if (!segments[i].equals(paths[i])) {
                                break;
                            } else {
                                common++;
                            }
                        }
                        String[] newPaths = new String[common];
                        System.arraycopy(segments, 0, newPaths, 0, common);
                        paths = newPaths;
                    } else {
                        paths = new String[segments.length - 1];
                        System.arraycopy(segments, 0, paths, 0,
                                segments.length - 1);
                    }
                }
            }
        }
        if (paths != null) {
            path = new StringBuilder("//"); //$NON-NLS-1$
            for (String segment : paths) {
                if (segment.length() > 0) {
                    path.append(segment);
                    path.append('/');
                }
            }
            path.append("..."); //$NON-NLS-1$
        }
        if (path == null) {
            path = new StringBuilder("//..."); //$NON-NLS-1$
        }
        return path.toString();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    @Override
    public void refresh() {
        if (this.changelist != null) {
            try {
                Set<IP4Resource> resources = new HashSet<IP4Resource>();
                List<IJob> jobs = this.changelist.getJobs();
                for (IJob job : jobs) {
                    IP4Job p4Job = new P4Job(job, getConnection(), this);
                    resources.add(p4Job);
                }

                this.changelist.refresh();
                List<IFileSpec> files = this.changelist.getFiles(false);
                for (IFileSpec file : files) {
                    if (file != null
                            && FileSpecOpStatus.VALID == file.getOpStatus()) {
                        IP4SubmittedFile submittedFile = new P4SubmittedFile(
                                new P4File(file, this) {

                                    @Override
                                    public void refresh() {
                                        // Ignore refresh calls
                                        return;
                                    }

                                    @Override
                                    public void setFileSpec(IFileSpec spec) {
                                        // Don't allow file spec updates
                                        return;
                                    }

                                }, this);
                        resources.add(submittedFile);
                    }
                }
                this.cachedFiles = resources;
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        this.needsRefresh = false;
    }

    @Override
    public String toString() {
    	return ("P4SubmittedChangelist:["+getDescription()+"]").replaceAll("[\n|\r]", ""); //$NON-NLS-1$
    }
}
