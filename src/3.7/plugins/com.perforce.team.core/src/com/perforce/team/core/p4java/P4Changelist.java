/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IFix;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4Changelist extends P4Resource implements IP4Changelist,
        Comparable<IP4Changelist> {

    /**
     * EMPTY String
     */
    public static final String EMPTY = ""; //$NON-NLS-1$

    /**
     * SHORT_DESCRIPTION_LENGTH
     */
    public static final int SHORT_DESCRIPTION_LENGTH = 80;

    /**
     * P4 connection
     */
    protected IP4Connection connection = null;

    /**
     * P4J changelist
     */
    protected IChangelist changelist = null;

    /**
     * Changelist id
     */
    protected int id = IChangelist.UNKNOWN;

    /**
     * Cached files
     */
    protected Set<IP4Resource> cachedFiles = Collections
            .synchronizedSet(new HashSet<IP4Resource>());

    /**
     * @param connection
     * 
     */
    public P4Changelist(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * Creates a new p4 changelist
     * 
     * @param connection
     * @param list
     */
    public P4Changelist(IP4Connection connection, IChangelist list) {
        this.connection = connection;
        this.changelist = list;
        if (this.changelist != null) {
            this.id = this.changelist.getId();
        }
    }

    /**
     * Checks that the spec is valid and is a file spec not the spec that fstat
     * -e prints out to represent the changelist description
     * 
     * @param spec
     * @return - true if valid
     */
    protected boolean isValidFileSpec(IExtendedFileSpec spec) {
        return spec.getOpStatus() == FileSpecOpStatus.VALID
                && spec.getDepotPath() != null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getDescription()
     */
    public String getDescription() {
        if (this.changelist != null) {
            return this.changelist.getDescription();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        if (this.changelist != null) {
            this.changelist.setDescription(description);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath()
     */
    public String getActionPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getActionPath(com.perforce.team.core.p4java.IP4Resource.Type)
     */
    public String getActionPath(Type type) {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClient()
     */
    public IClient getClient() {
        IClient client = null;
        if (this.connection != null) {
            client = this.connection.getClient();
        }
        return client;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getClientPath()
     */
    public String getClientPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getLocalPath()
     */
    public String getLocalPath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getName()
     */
    public String getName() {
        if (this.changelist != null) {
            return MessageFormat.format(Messages.P4Changelist_0,
                    Integer.toString(this.changelist.getId()));
        }
        return ""; //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getParent()
     */
    public IP4Container getParent() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#getRemotePath()
     */
    public String getRemotePath() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#members()
     */
    public IP4Resource[] members() {
        return this.cachedFiles.toArray(new IP4Resource[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#size()
     */
    public int size() {
        return this.cachedFiles.size();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getJobs()
     */
    public IP4Job[] getJobs() {
        List<IP4Job> jobs = new ArrayList<IP4Job>();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4Job) {
                jobs.add((IP4Job) resource);
            }
        }
        return jobs.toArray(new IP4Job[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getClientName()
     */
    public String getClientName() {
        String name = null;
        if (this.changelist != null) {
            name = this.changelist.getClientId();
        } else {
            IClient client = getClient();
            if (client != null) {
                name = client.getName();
            }
        }
        return name;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getUserName()
     */
    public String getUserName() {
        String name = null;
        if (this.changelist != null) {
            return this.changelist.getUsername();
        } else {
            IClient client = getClient();
            if (client != null) {
                name = client.getOwnerName();
            }
        }
        return name;
    }

    /**
     * @param o
     * @return - int comparison
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(IP4Changelist o) {
        if (this.changelist != null && o.getChangelist() != null) {
            return this.changelist.getId() - o.getChangelist().getId();
        } else if (isDefault() && o.isDefault()) {
            return getName().compareToIgnoreCase(o.getName());
        }
        return 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#isDefault()
     */
    public boolean isDefault() {
        if (this.changelist != null) {
            return this.changelist.getId() == 0;
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#isContainer()
     */
    public boolean isContainer() {
        return true;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getDate()
     */
    public Date getDate() {
        if (this.changelist != null) {
            return this.changelist.getDate();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getId()
     */
    public int getId() {
        return this.id;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getStatus()
     */
    public ChangelistStatus getStatus() {
        if (this.changelist != null) {
            return this.changelist.getStatus();
        }
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    public void refresh() {
        if (this.changelist != null) {
            try {
                Set<IP4Resource> resources = new HashSet<IP4Resource>();
                List<IJob> jobs = this.changelist.getJobs();
                for (IJob job : jobs) {
                    IP4Job p4Job = new P4Job(job, getConnection(), this);
                    resources.add(p4Job);
                }
                List<IFileSpec> files = this.changelist.getFiles(true);
                files = P4FileSpecBuilder.getValidFileSpecs(files);
                for (IFileSpec file : files) {
                    IP4File p4File = null;
                    if (!readOnly) {
                        p4File = getConnection().getFile(file);
                    }
                    if (p4File == null) {
                        p4File = new P4File(file, this, readOnly);
                    }
                    resources.add(p4File);
                }
                this.cachedFiles = resources;
            } catch (P4JavaException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
        this.needsRefresh = false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh(int)
     */
    public void refresh(int depth) {
        refresh();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Container#getAllLocalFiles()
     */
    public IP4File[] getAllLocalFiles() {
        return null;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getChangelist()
     */
    public IChangelist getChangelist() {
        return this.changelist;
    }

    /**
     * Sub-classes should override and check class of specified other object to
     * ensure only changelists of the same type (pending, submitted, shelved)
     * are ever equal to each other.
     * 
     * @see com.perforce.team.core.p4java.P4Resource#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof IP4Changelist) {
            IP4Changelist list = (IP4Changelist) obj;
            if (!connectionEquals(list)) {
                return false;
            }
            int id = getId();
            if (id > -1 && id == list.getId()) {
                String user = getUserName();
                String client = getClientName();
                if (user != null && client != null) {
                    if (isCaseSensitive()) {
                        return user.equals(list.getUserName())
                                && client.equals(list.getClientName());
                    } else {
                        return user.equalsIgnoreCase(list.getUserName())
                                && client
                                        .equalsIgnoreCase(list.getClientName());
                    }
                }
            }
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#hashCode()
     */
    @Override
    public int hashCode() {
        int hash = getId();
        if (hash < 0) {
            hash = super.hashCode();
        }
        return hash;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getShortDescription()
     */
    public String getShortDescription() {
        return getShortenedDescription(SHORT_DESCRIPTION_LENGTH);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getShortenedDescription(int)
     */
    public String getShortenedDescription(int length) {
        String description = getDescription();
        if (length <= 0 || description == null) {
            return EMPTY;
        }
        StringBuilder desc = new StringBuilder(description.length() > length
                ? description.substring(0, length - 1) + "..." : description); //$NON-NLS-1$
        for (int i = 0; i < desc.length(); i++) {
            if (Character.isISOControl(desc.charAt(i))) {
                desc.setCharAt(i, ' ');
            }
        }
        return desc.toString();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#fix(com.perforce.team.core.p4java.IP4Job)
     */
    public IP4Job fix(IP4Job job) {
        IP4Job fixedJob = null;
        if (job != null) {
            IP4Job[] fixed = fix(new IP4Job[] { job });
            if (fixed.length > 0) {
                fixedJob = fixed[0];
            }
        }
        return fixedJob;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#fix(com.perforce.team.core.p4java.IP4Job[])
     */
    public IP4Job[] fix(IP4Job[] jobs) {
        List<IP4Job> fixed = new ArrayList<IP4Job>();
        IClient client = getClient();
        if (jobs != null && jobs.length > 0 && client != null) {
            IServer server = client.getServer();
            if (server != null) {
                List<String> jobList = new ArrayList<String>();
                for (IP4Job job : jobs) {
                    String id = job.getId();
                    if (id != null) {
                        jobList.add(id);
                    }
                }
                try {
                    List<IFix> fixes = server.fixJobs(jobList, getId(), null,
                            false);
                    for (IP4Job job : jobs) {
                        String id = job.getId();
                        if (id != null) {
                            for (IFix fix : fixes) {
                                if (id.equals(fix.getJobId())) {
                                    IP4Job fixedJob = new P4Job(job.getJob(),
                                            getConnection(), this);
                                    addJob(fixedJob);
                                    fixed.add(fixedJob);
                                    break;
                                }
                            }
                        }
                    }
                } catch (P4JavaException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return fixed.toArray(new IP4Job[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#unfix(com.perforce.team.core.p4java.IP4Job)
     */
    public IP4Job unfix(IP4Job job) {
        IP4Job unfixedJob = null;
        if (job != null) {
            IP4Job[] unfixed = unfix(new IP4Job[] { job });
            if (unfixed.length > 0) {
                unfixedJob = unfixed[0];
            }
        }
        return unfixedJob;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#unfix(com.perforce.team.core.p4java.IP4Job[])
     */
    public IP4Job[] unfix(IP4Job[] jobs) {
        List<IP4Job> unfixed = new ArrayList<IP4Job>();
        IClient client = getClient();
        if (jobs != null && jobs.length > 0 && client != null) {
            IServer server = client.getServer();
            if (server != null) {
                List<String> jobList = new ArrayList<String>();
                for (IP4Job job : jobs) {
                    String id = job.getId();
                    if (id != null) {
                        jobList.add(id);
                    }
                }
                try {
                    List<IFix> unfixes = server.fixJobs(jobList, getId(), null,
                            true);
                    for (IP4Job job : jobs) {
                        String id = job.getId();
                        if (id != null) {
                            for (IFix unfix : unfixes) {
                                if (id.equals(unfix.getJobId())) {
                                    IP4Job unfixedJob = new P4Job(job.getJob(),
                                            getConnection(), this);
                                    removeJob(unfixedJob);
                                    unfixed.add(unfixedJob);
                                    break;
                                }
                            }
                        }
                    }
                } catch (P4JavaException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
        return unfixed.toArray(new IP4Job[0]);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#addJob(com.perforce.team.core.p4java.IP4Job)
     */
    public void addJob(IP4Job job) {
        this.cachedFiles.add(job);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#removeJob(com.perforce.team.core.p4java.IP4Job)
     */
    public void removeJob(IP4Job job) {
        this.cachedFiles.remove(job);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getJobIds()
     */
    public String[] getJobIds() {
        final Set<String> ids = new HashSet<String>();
        final int id = getId();
        if (id > IChangelist.DEFAULT) {
            IP4ServerOperation op = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                    List<IFix> fixes = server.getFixList(null, id, null, false,
                            -1);
                    for (IFix fix : fixes) {
                        ids.add(fix.getJobId());
                    }
                }

            };
            runOperation(op);
        }
        return ids.toArray(new String[ids.size()]);
    }
    
    @Override
    public List<IExtendedFileSpec> getOpenedSpecs(){
    	final List<IExtendedFileSpec> result=new ArrayList<IExtendedFileSpec>();
        final int id = getId();
        if (id > IChangelist.DEFAULT) {
            IP4ServerOperation op = new P4ServerOperation() {

                public void run(IServer server) throws P4JavaException,
                        P4JavaError {
                	List<IFileSpec> simpleSpecs = server.getChangelistFiles(id);
                    FileStatOutputOptions options = new FileStatOutputOptions();
                    options.setOpenedFiles(true);
                    if(!simpleSpecs.isEmpty()){
	                    List<IExtendedFileSpec> specs= server
	                            .getExtendedFiles(simpleSpecs, 0, -1,
	                                    id, options, null);
	                    for (IExtendedFileSpec spec : specs) {
	                        if (isValidFileSpec(spec)) {
	                        	result.add(spec);
	                        }
	                    }
                    }
                }

            };
            runOperation(op);
        }

    	return result;
    }

}
