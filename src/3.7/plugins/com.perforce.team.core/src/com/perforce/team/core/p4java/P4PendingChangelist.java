/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.FileStatOutputOptions;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.option.changelist.SubmitOptions;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4PendingChangelist extends P4Changelist implements
        IP4PendingChangelist {

    private IP4ShelvedChangelist shelved;

    /**
     * User name
     */
    private final String changelistUser;

    /**
     * Client name
     */
    private final String changelistClient;

    /**
     * @param connection
     * @param list
     * @param onClient
     */
    public P4PendingChangelist(IP4Connection connection, IChangelist list,
            boolean onClient) {
        super(connection, list);
        this.readOnly = !onClient;
        if (list != null) {
            if (connection.isShelvingSupported()) {
                this.shelved = new P4ShelvedChangelist(connection, list,
                        this.readOnly);
            }
            changelistUser = list.getUsername();
            changelistClient = list.getClientId();
        } else {
            changelistUser = null;
            changelistClient = null;
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getClientName()
     */
    @Override
    public String getClientName() {
        return this.changelistClient;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#getUserName()
     */
    @Override
    public String getUserName() {
        return this.changelistUser;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof IP4PendingChangelist && super.equals(obj);
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#getFiles()
     */
    public IP4Resource[] getFiles() {
        return getPendingFiles();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#getPendingFiles()
     */
    public IP4File[] getPendingFiles() {
        List<IP4File> files = new ArrayList<IP4File>();
        for (IP4Resource resource : members()) {
            if (resource instanceof IP4File) {
                files.add((IP4File) resource);
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#members()
     */
    @Override
    public IP4Resource[] members() {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                return connection.getOpenedManager().getResources(getId());
            } else {
                return IP4Resource.EMPTY;
            }
        } else {
            return super.members();
        }
    }

    /**
     * @see com.perforce.team.core.p4java.P4Changelist#size()
     */
    @Override
    public int size() {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                return connection.getOpenedManager().getSize(getId());
            }
        }
        return super.size();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#reopen(com.perforce.team.core.p4java.IP4Resource[])
     */
    public void reopen(IP4Resource[] resources) {
        final int id = getId();
        if (resources != null && id > -1) {
            List<String> paths = new ArrayList<String>();
            for (IP4Resource resource : resources) {
                String path = resource.getActionPath();
                if (path != null) {
                    paths.add(path);
                }
            }
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(paths.toArray(new String[0]));
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    client.reopenFiles(specs, id, null);
                }
            };
            runOperation(operation);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#removeFile(com.perforce.team.core.p4java.IP4File)
     */
    public boolean removeFile(IP4File file) {
        boolean removed = false;
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                removed = connection.getOpenedManager().remove(getId(), file);
            }
        } else {
            removed = this.cachedFiles.remove(file);
        }
        return removed;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#addFile(com.perforce.team.core.p4java.IP4File)
     */
    public void addFile(IP4File file) {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                connection.getOpenedManager().addFile(getId(), file);
            }
        } else {
            this.cachedFiles.add(file);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#submit(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[])
     */
    public int submit(String description, IP4File[] subset, IProgressMonitor monitor) {
        return submit(description, subset, null,monitor);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#submit(java.lang.String,
     *      com.perforce.team.core.p4java.IP4File[],
     *      com.perforce.team.core.p4java.IP4Job[])
     */
    public int submit(String description, IP4File[] files, IP4Job[] jobs, IProgressMonitor monitor) {
        return submit(false, description, files, jobs, monitor);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#submit(boolean,
     *      java.lang.String, com.perforce.team.core.p4java.IP4File[],
     *      com.perforce.team.core.p4java.IP4Job[])
     */
    public int submit(boolean reopen, String description, IP4File[] files,
            IP4Job[] jobs, IProgressMonitor monitor) {
        return submit(reopen, description, files, jobs, null, monitor);
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#revert()
     */
    @Override
    public void revert() {
        final int id = getId();
        if (isOnClient() && id > -1) {
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(new String[] { IP4Connection.ROOT });
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    client.revertFiles(specs, false, id, false, false);
                }
            };
            runOperation(operation);
        }
    }

    private void sendSubmitJobEvent(IP4Job[] jobs) {
        if (jobs != null && jobs.length > 0) {
            P4Workspace.getWorkspace().notifyListeners(
                    new P4Event(EventType.SUBMIT_JOB, jobs));
        }
    }

    private void sendSubmitChangelistEvent(List<IP4Resource> resources) {
        P4Workspace.getWorkspace().notifyListeners(
                new P4Event(EventType.SUBMIT_CHANGELIST, resources
                        .toArray(new IP4Resource[resources.size()])));
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#submit(boolean,
     *      java.lang.String, com.perforce.team.core.p4java.IP4File[],
     *      com.perforce.team.core.p4java.IP4Job[], java.lang.String)
     */
    public int submit(final boolean reopen, final String description,
            final IP4File[] files, final IP4Job[] jobs, final String jobStatus, final IProgressMonitor monitor) {
    	monitor.beginTask("submit", 500); // $NON-NLS-1$ //$NON-NLS-1$
    	monitor.worked(100);
        final int[] submittedId = new int[] { IChangelist.UNKNOWN };
        final IP4Connection connection = getConnection();
        if (!isReadOnly() && connection != null && files != null
                && files.length > 0) {
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    IServer server = client.getServer();
                    if (server != null) {
                        List<IP4File> remaining = new ArrayList<IP4File>();
                        for (IP4File file : files) {
                            remaining.add(file);
                        }
                        String[] paths = new String[files.length];
                        for (int i = 0; i < paths.length; i++) {
                            paths[i] = files[i].getRemotePath();
                        }
                        List<IFileSpec> specs = P4FileSpecBuilder
                                .makeFileSpecList(paths);
                        IChangelist created = null;
                        int currentId = -1;
                        if (changelist != null) {
                            // Submit selected files from non-default changelist
                            currentId = changelist.getId();
                            created = changelist;

                            // Update description in existing changelist
                            if (description != null) {
                                created.setDescription(description);
                            }
                        } else {
                            // Create new p4java changelist object and submit
                            // selected files currently in default changelist
                            Changelist cl = new Changelist();
                            cl.setId(IChangelist.DEFAULT);
                            cl.setClientId(client.getName());
                            cl.setDescription(description);
                            cl.setUsername(server.getUserName());
                            created = cl;
                        }

                        // Fix for job034581, ensure right server is used, not a
                        // cached one the changelist already has
                        if (server instanceof Server
                                && created instanceof Changelist) {
                            ((Changelist) created)
                                    .setServerImpl((Server) server);
                        }

                        // Set changelist specs
                        List<IFileSpec> current = created.getFiles(false);
                        current.clear();
                        for (IFileSpec spec : specs) {
                            spec.setDepotPath(spec.getOriginalPathString());
                        }
                        current.addAll(specs);

                        final List<IFileSpec> submittedSpecs = new ArrayList<IFileSpec>();
                        final IChangelist cl=created;
						Tracing.printExecTime(() -> {
							int key = P4CoreUtils.getRandomInt();
							SubmitOptions opt = new SubmitOptions();
							opt.setReOpen(reopen);
							P4ProgressListener handler = P4CoreUtils.createStreamCallback(connection, CmdSpec.SUBMIT,
									new SubProgressMonitor(monitor, 100));
							if (jobs == null || jobs.length == 0) {
								cl.submit(opt, handler, key);
								submittedSpecs.addAll(handler.getFileSpecs());
//	                        	 submittedSpecs.addAll(cl.submit(reopen));
							} else {
								List<String> jobIds = new ArrayList<String>();
								for (IP4Job job : jobs) {
									String id = job.getId();
									if (id != null) {
										jobIds.add(job.getId());
									}
								}
//	                            submittedSpecs.addAll(cl.submit(reopen, jobIds,
//	                            		jobStatus));
								opt.setJobStatus(jobStatus);
								opt.setJobIds(jobIds);
								cl.submit(opt, handler, key);
								submittedSpecs.addAll(handler.getFileSpecs());
							}
						}, "SUBMIT", "P4 submit");

//                        List<IFileSpec> submittedSpecs = null;
//                        if (jobs == null || jobs.length == 0) {
//                            submittedSpecs = created.submit(reopen);
//                        } else {
//                            List<String> jobIds = new ArrayList<String>();
//                            for (IP4Job job : jobs) {
//                                String id = job.getId();
//                                if (id != null) {
//                                    jobIds.add(job.getId());
//                                }
//                            }
//                            submittedSpecs = created.submit(reopen, jobIds,
//                                    jobStatus);
//                        }

                        boolean errors = false;
                        // Any error specs denote a submit failed/aborted
                        for (IFileSpec spec : submittedSpecs) {
                            if (spec.getOpStatus() == FileSpecOpStatus.ERROR) {
                                errors = true;
                                break;
                            }
                        }

                        if (!errors) {
                            P4Collection submitted = P4Collection
                                    .createCollection(connection,
                                            submittedSpecs);
                            for (IP4Resource resource : submitted.members()) {
                                remaining.remove(resource);
                            }
                            if (!submitted.isEmpty()) {
                            	monitor.setTaskName(Messages.P4PendingChangelist_RefreshSubmitted);
                            	monitor.worked(100);
                                submitted.refresh();
                                for (IP4Resource file : submitted.members()) {
                                    if (file instanceof IP4File
                                            && !((IP4File) file).isOpened()) {
                                        removeFile((IP4File) file);
                                    }
                                }

                                sendSubmitJobEvent(jobs);

                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.SUBMITTED,
                                                submitted));
                                P4PendingChangelist submittedList = new P4PendingChangelist(
                                        connection, created, true);

                                // Send submit event with two changelists if the
                                // id changed during submit and this was a
                                // non-default submit
                                List<IP4Resource> submittedLists = new ArrayList<IP4Resource>(
                                        2);
                                if (changelist != null
                                        && currentId != created.getId()) {
                                    submittedLists
                                            .add(P4PendingChangelist.this);
                                }
                                submittedLists.add(submittedList);

                                monitor.setTaskName(Messages.P4PendingChangelist_SendSubmitChangelistEvent);
                                monitor.worked(100);
                                sendSubmitChangelistEvent(submittedLists);

                                List<IFileSpec> changelistSpecs = P4FileSpecBuilder
                                        .getInvalidFileSpecs(submittedSpecs);
                                for (IFileSpec spec : changelistSpecs) {
                                    String message = spec.getStatusMessage();
                                    if (message != null
                                            && message.startsWith("Submitted")) { //$NON-NLS-1$ // see Changelist.java:476
                                        int lastSpace = message
                                                .lastIndexOf(' ');
                                        if (lastSpace >= 0) {
                                            message = message
                                                    .substring(lastSpace);
                                            try {
                                                submittedId[0] = Integer
                                                        .parseInt(message
                                                                .trim());
                                                if (submittedId[0] > 0) {
                                                    break;
                                                }
                                            } catch (NumberFormatException e) {
                                                PerforceProviderPlugin
                                                        .logError(e);
                                            }
                                        }
                                    }
                                }
                                // Remove previous id in case it was changed
                                // when
                                // submitted
                                if (currentId > 0) {
                                    connection.removeChangelist(currentId);
                                }
                                if (submittedId[0] > 0) {
                                    submitted.sync("@" + submittedId[0] + ",@" //$NON-NLS-1$ //$NON-NLS-2$
                                            + submittedId[0], new SubProgressMonitor(monitor, 100));
                                    // Remove submitted id from cache
                                    connection.removeChangelist(submittedId[0]);
                                }
                            }
                        }
                        if (!remaining.isEmpty()) {
                            P4Collection failed = new P4Collection(
                                    remaining.toArray(new IP4Resource[0]));
//                            failed.refresh();
                            for (IP4Resource file : failed.members()) {
                                if (file instanceof IP4File
                                        && !((IP4File) file).isOpened()) {
                                    removeFile((IP4File) file);
                                }
                            }

                            // If a submit fails try to find the created
                            // changelist
                            // and load it
                            int failedId = findFailedListId(submittedSpecs);

                            // If failed id is found and it is not the same as
                            // the
                            // current id then it is new and needs to be loaded
                            if (failedId > IChangelist.DEFAULT
                                    && failedId != currentId) {
                                connection.loadPendingChangelist(failedId);
                            }
                            P4Workspace.getWorkspace()
                                    .notifyListeners(
                                            new P4Event(
                                                    EventType.SUBMIT_FAILED,
                                                    failed));
                        }
                        handleErrors(submittedSpecs.toArray(new IFileSpec[0]));
                    }
                }
            };
            runOperation(operation);
        }
        monitor.done();
        return submittedId[0];
    }

    private int findFailedListId(List<IFileSpec> specs) {
        int id = IChangelist.UNKNOWN;
        for (IFileSpec spec : specs) {
            if (FileSpecOpStatus.ERROR == spec.getOpStatus()) {
                String message = spec.getStatusMessage();
                if (message != null
                        && (message.contains(SUBMIT_FAILED_MESSAGE) || message
                                .contains(SUBMIT_ABORTED_MESSAGE))) {
                    int idPos = message.indexOf(SUBMIT_FAILED_CHANGELIST);
                    int endPos = message.lastIndexOf('\'');
                    if (idPos > -1 && endPos > -1) {
                        try {
                            String idSection = message
                                    .substring(
                                            idPos
                                                    + SUBMIT_FAILED_CHANGELIST
                                                            .length(), endPos);

                            id = Integer.parseInt(idSection.trim());
                            // Break if valid changelist id is found in the spec
                            // list
                            break;
                        } catch (IndexOutOfBoundsException iobe) {
                            PerforceProviderPlugin
                                    .logError(
                                            Messages.P4PendingChangelist_ErrorSubmit,
                                            iobe);
                            id = IChangelist.UNKNOWN;
                        } catch (NumberFormatException nfe) {
                            PerforceProviderPlugin
                                    .logError(
                                            Messages.P4PendingChangelist_ErrorFindingID,
                                            nfe);
                            id = IChangelist.UNKNOWN;
                        }
                    }
                }
            }
        }
        return id;
    }

    /**
     * @see com.perforce.team.core.p4java.P4Resource#delete(int)
     */
    @Override
    public void delete(int changelist) {
        final int id = getId();
        if (isOnClient() && id > -1) {
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    String message = client.getServer()
                            .deletePendingChangelist(id);
                    if (message != null
                            && message.contains("Change " + id + " deleted")) { //$NON-NLS-1$ //$NON-NLS-2$
                        IP4Connection connection = getConnection();
                        if (connection != null) {
                            connection
                                    .removeChangelist(P4PendingChangelist.this);
                        }
                        P4PendingChangelist.this.changelist = null;
                    }
                }
            };
            runOperation(op);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#isOnClient()
     */
    public boolean isOnClient() {
        return !readOnly;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#updateServerDescription(java.lang.String)
     */
    public void updateServerDescription(String description) {
        IChangelist list = this.changelist;
        if (list != null && description != null) {
            boolean retry = true;
            while (retry && list != null) {
                retry = false;
                list.setDescription(description);
                try {
                    list.getFiles(true);
                    list.update();
                    P4Workspace.getWorkspace().notifyListeners(
                            new P4Event(EventType.CHANGED, this));
                } catch (P4JavaException e) {
                    retry = handleError(e);
                    if (retry) {
                        list = this.changelist;
                    }
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#getAllMembers()
     */
    public IP4Resource[] getAllMembers() {
        List<IP4Resource> all = new ArrayList<IP4Resource>();
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                all.addAll(Arrays.asList(connection.getOpenedManager()
                        .getResources(getId())));
            }
        } else {
            all.addAll(this.cachedFiles);
        }
        if (this.shelved != null) {
            all.add(this.shelved);
        }
        return all.toArray(new IP4Resource[all.size()]);
    }

    private void refreshShelvedFiles(IP4Connection connection)
            throws P4JavaException {
        IChangelist list = changelist;
        if (connection.isShelvingSupported() && list != null) {
            shelved = new P4ShelvedChangelist(connection, list, isReadOnly());
            shelved.refresh();
            list.setShelved(shelved.members().length > 0);
        }
    }

    private void refreshClientFiles(IP4Connection connection, IServer server,
            int id, Set<IP4Resource> resources) throws P4JavaException {
        FileStatOutputOptions options = new FileStatOutputOptions();
        options.setMappedFiles(true);
        options.setOpenedFiles(true);
        List<IFileSpec> fileSpecs = P4FileSpecBuilder
                .makeFileSpecList(new String[] { connection.getRootSpec(), });
        List<IExtendedFileSpec> files = server.getExtendedFiles(fileSpecs, 0,
                -1, id, options, null);

        for (IExtendedFileSpec file : files) {
            if (isValidFileSpec(file)) {
                IP4File p4File = null;
                if (!readOnly) {
                    p4File = getConnection().getFile(file);
                }
                if (p4File == null) {
                    p4File = new P4File(file, P4PendingChangelist.this,
                            readOnly);
                }
                p4File.setFileSpec(file);
                resources.add(p4File);
            }
        }
    }

    private void refreshOtherFiles(IP4Connection connection, IServer server,
            Set<IP4Resource> resources) throws P4JavaException {
        List<IFileSpec> fileSpecs = new ArrayList<IFileSpec>();
        String path = null;
        Map<String, IP4Resource> foundFiles = new HashMap<String, IP4Resource>();
        for (IFileSpec spec : changelist.getFiles(true)) {
            path = spec.getDepotPathString();
            if (path != null) {
                IP4File file = new P4File(spec, P4PendingChangelist.this,
                        readOnly);
                // Fix for job036190, handle case where someone else had it open
                // for add and so an fstat will not return it so use the spec
                // from the describe.
                foundFiles.put(path, file);
                fileSpecs.add(new FileSpec(path));
            }
        }
        if (!fileSpecs.isEmpty()) {
            List<IExtendedFileSpec> files = server.getExtendedFiles(fileSpecs,
                    0, -1, -1, null, null);
            for (IExtendedFileSpec file : files) {
                if (isValidFileSpec(file)) {
                    IP4File p4File = null;
                    if (!readOnly) {
                        p4File = getConnection().getFile(file);
                    }
                    if (p4File == null) {
                        p4File = new P4File(file, P4PendingChangelist.this,
                                readOnly);
                    }
                    p4File.setFileSpec(file);
                    path = p4File.getRemotePath();
                    if (path != null) {
                        foundFiles.put(path, p4File);
                    }
                }
            }
        }
        resources.addAll(foundFiles.values());
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Resource#refresh()
     */
    @Override
    public void refresh() {
        final IP4Connection connection = getConnection();
        final int id = getId();
        if (connection != null && id > 0) {
            // Run in client operation since a client is required to deal with
            // changelists
            IP4ClientOperation op = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    IServer server = client.getServer();
                    if (server != null) {
                        changelist = server.getChangelist(id);

                        Set<IP4Resource> resources = new HashSet<IP4Resource>();

                        List<IJob> jobs = changelist.getJobs();
                        for (IJob job : jobs) {
                            IP4Job p4Job = new P4Job(job, getConnection(),
                                    P4PendingChangelist.this);
                            resources.add(p4Job);
                        }

                        refreshShelvedFiles(connection);

                        if (isOnClient()) {
                            refreshClientFiles(connection, server, id,
                                    resources);
                            connection.getOpenedManager().replaceResources(id,
                                    resources);
                        } else {
                            refreshOtherFiles(connection, server, resources);
                            cachedFiles = resources;
                        }
                        P4Workspace.getWorkspace().notifyListeners(
                                new P4Event(EventType.REFRESHED,
                                        P4PendingChangelist.this));
                    }
                }

            };
            runOperation(op);
        }
        this.needsRefresh = false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#isActive()
     */
    public boolean isActive() {
        IP4Connection connection = getConnection();
        if (connection != null) {
            return this.equals(connection.getActivePendingChangelist());
        }
        return false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#makeActive()
     */
    public void makeActive() {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            connection.setActivePendingChangelist(getId());
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#isDeleteable()
     */
    public boolean isDeleteable() {
        return !this.isReadOnly() && !this.isDefault() && !this.needsRefresh()
                && this.members().length == 0;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#isShelved()
     */
    public boolean isShelved() {
        return this.changelist != null ? this.changelist.isShelved() : false;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#deleteShelved()
     */
    public void deleteShelved() {
        deleteShelve(null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#shelve(com.perforce.team.core.p4java.IP4File[])
     */
    public void shelve(IP4File[] files) {
        updateShelvedFiles(files);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#replaceShelvedFiles()
     */
    public void replaceShelvedFiles() {
        final int id = getId();
        final IP4Connection connection = getConnection();
        if (id > 0 && connection != null) {
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    if (connection.isShelvingSupported()) {
                        client.shelveChangelist(id, null, false, true, false);

                        if (isShelved()) {
                            refreshShelvedFiles(connection);

                            // Send update shelve event
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.UPDATE_SHELVE,
                                            P4PendingChangelist.this));
                        } else {
                            refresh();

                            // Send create shelve event
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.CREATE_SHELVE,
                                            P4PendingChangelist.this));
                        }
                    }
                }
            };
            runOperation(operation);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#updateShelvedFiles()
     */
    public void updateShelvedFiles() {
        updateShelvedFiles(null);
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#deleteShelve(com.perforce.team.core.p4java.IP4Resource[])
     */
    public void deleteShelve(IP4Resource[] resources) {
        final int id = getId();
        final IP4Connection connection = getConnection();
        if (id > 0 && connection != null) {
            List<String> paths = new ArrayList<String>();
            if (resources != null) {
                for (IP4Resource resource : resources) {
                    String path = resource.getActionPath(Type.REMOTE);
                    if (path != null) {
                        paths.add(path);
                    }
                }
            }
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(paths.toArray(new String[paths.size()]));
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    if (connection.isShelvingSupported()) {
                        List<IFileSpec> deleteSpecs = client.shelveChangelist(
                                id, specs, false, false, true);

                        boolean deleted = false;

                        // Check last file spec for shelve delete message
                        if (deleteSpecs.size() > 0) {
                            IFileSpec last = deleteSpecs
                                    .get(deleteSpecs.size() - 1);
                            if (last.getOpStatus() == FileSpecOpStatus.INFO) {
                                String message = last.getStatusMessage();
                                deleted = message.contains("Shelve " + id //$NON-NLS-1$
                                        + " deleted."); //$NON-NLS-1$
                            }
                        }

                        refreshShelvedFiles(connection);

                        if (deleted) {
                            IChangelist list = changelist;
                            if (list.isShelved() && list instanceof Changelist) {
                                ((Changelist) list).setShelved(false);
                                // Send refresh event since changelist has
                                // changed
                                P4Workspace.getWorkspace().notifyListeners(
                                        new P4Event(EventType.REFRESHED,
                                                P4PendingChangelist.this));
                            }

                            // Send delete shelve event since completely delete
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.DELETE_SHELVE,
                                            P4PendingChangelist.this));
                        } else {
                            // Send update shelve event since not completely
                            // deleted
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.UPDATE_SHELVE,
                                            P4PendingChangelist.this));
                        }
                    }
                }
            };
            runOperation(operation);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#updateShelvedFiles(com.perforce.team.core.p4java.IP4Resource[])
     */
    public void updateShelvedFiles(IP4Resource[] resources) {
        final int id = getId();
        final IP4Connection connection = getConnection();
        if (id > 0 && connection != null) {
            List<String> paths = new ArrayList<String>();
            if (resources != null) {
                for (IP4Resource resource : resources) {
                    String path = resource.getActionPath(Type.REMOTE);
                    if (path != null) {
                        paths.add(path);
                    }
                }
            }
            final List<IFileSpec> specs = P4FileSpecBuilder
                    .makeFileSpecList(paths.toArray(new String[paths.size()]));
            IP4ClientOperation operation = new P4ClientOperation() {

                public void run(IClient client) throws P4JavaException,
                        P4JavaError {
                    if (connection.isShelvingSupported()) {
                        client.shelveChangelist(id, specs, true, false, false);

                        if (isShelved()) {
                            refreshShelvedFiles(connection);

                            // Send update shelve event
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.UPDATE_SHELVE,
                                            P4PendingChangelist.this));
                        } else {
                            refresh();

                            // Send create shelve event
                            P4Workspace.getWorkspace().notifyListeners(
                                    new P4Event(EventType.CREATE_SHELVE,
                                            P4PendingChangelist.this));
                        }
                    }
                }
            };
            runOperation(operation);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4PendingChangelist#getShelvedChanges()
     */
    public IP4ShelvedChangelist getShelvedChanges() {
        return this.shelved;
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#addJob(com.perforce.team.core.p4java.IP4Job)
     */
    @Override
    public void addJob(IP4Job job) {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                connection.getOpenedManager().addJob(getId(), job);
            }
        } else {
            super.addJob(job);
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Changelist#removeJob(com.perforce.team.core.p4java.IP4Job)
     */
    @Override
    public void removeJob(IP4Job job) {
        if (isOnClient()) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                connection.getOpenedManager().remove(getId(), job);
            }
        } else {
            super.removeJob(job);
        }
    }

    @Override
    public String toString() {
    	return ("P4PendingChangelist:["+getDescription()+"]").replaceAll("[\n|\r]", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-4$
    }

}
