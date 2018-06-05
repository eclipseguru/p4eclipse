/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.util.ArrayList;
import java.util.List;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ServerOperation;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4ClientOperation;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4ServerOperation;
import com.perforce.team.core.p4java.P4SubmittedChangelist;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DepotPathMapping extends Mapping {

    /**
     * TYPE
     */
    public static final String TYPE = "depotPath"; //$NON-NLS-1$

    /**
     * SOURCE_PATH
     */
    public static final String SOURCE_PATH = "sourcePath"; //$NON-NLS-1$

    /**
     * TARGET_PATH
     */
    public static final String TARGET_PATH = "targetPath"; //$NON-NLS-1$

    private String sourcePath = ""; //$NON-NLS-1$
    private String targetPath = ""; //$NON-NLS-1$

    /**
     * Create a new depot path mapping
     * 
     * @param id
     */
    public DepotPathMapping(String id) {
        this(id, null);
    }

    /**
     * Create a new depot path mapping
     * 
     * @param id
     * @param graph
     */
    public DepotPathMapping(String id, IBranchGraph graph) {
        super(id, graph);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.BranchGraphElement#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof DepotPathMapping && super.equals(obj);
    }

    @Override
    public int hashCode() {
    	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
    	return super.hashCode();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getType()
     */
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Get source path
     * 
     * @return - source path
     */
    public String getSourcePath() {
        return this.sourcePath;
    }

    /**
     * Get target path
     * 
     * @return - target path
     */
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * @param sourcePath
     *            the sourcePath to set
     * @return true if changes, false otherwise
     */
    public boolean setSourcePath(String sourcePath) {
        boolean set = false;
        if (sourcePath != null) {
            if (!this.sourcePath.equals(sourcePath)) {
                String previous = this.sourcePath;
                this.sourcePath = sourcePath;
                changeSupport.firePropertyChange(SOURCE_PATH, previous,
                        sourcePath);
                set = true;
            }
        }
        return set;
    }

    /**
     * @param targetPath
     *            the targetPath to set
     * @return true if set, false otherwise
     */
    public boolean setTargetPath(String targetPath) {
        boolean set = false;
        if (targetPath != null) {
            if (!this.targetPath.equals(targetPath)) {
                String previous = this.targetPath;
                this.targetPath = targetPath;
                changeSupport.firePropertyChange(TARGET_PATH, previous,
                        targetPath);
                set = true;
            }
        }
        return set;
    }

    private IP4SubmittedChangelist[] getInterchanges(
            final IP4Connection connection, final String source,
            final String target) throws MappingException {

        @SuppressWarnings("unchecked")
        final List<IP4SubmittedChangelist>[] submittedLists = new List[] { null };

        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                IFileSpec sourceSpec = new FileSpec(source);
                IFileSpec targetSpec = new FileSpec(target);
                List<IChangelist> interchanges = server.getInterchanges(
                        sourceSpec, targetSpec, false, true, -1);
                List<IP4SubmittedChangelist> submitted = new ArrayList<IP4SubmittedChangelist>(
                        interchanges.size());
                for (IChangelist list : interchanges) {
                    submitted.add(new P4SubmittedChangelist(connection, list));
                }
                submittedLists[0] = submitted;
            }

        };
        runOperation(connection, operation);

        if (submittedLists[0] != null) {
            return submittedLists[0]
                    .toArray(new IP4SubmittedChangelist[submittedLists[0]
                            .size()]);
        } else {
            return null;
        }
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshSourceInterchanges(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    public IP4SubmittedChangelist[] refreshSourceInterchanges(
            IP4Connection connection) throws MappingException {
        IP4SubmittedChangelist[] lists = null;
        if (Direction.TARGET != getDirection() && connection != null) {
            lists = getInterchanges(connection, this.targetPath,
                    this.sourcePath);
            if (lists != null) {
                setTargetToSourceCount(lists.length);
            }
        }
        if (lists == null) {
            lists = EMPTY_LISTS;
        }
        return lists;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshTargetInterchanges(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    public IP4SubmittedChangelist[] refreshTargetInterchanges(
            IP4Connection connection) throws MappingException {
        IP4SubmittedChangelist[] lists = null;
        if (Direction.SOURCE != getDirection() && connection != null) {
            lists = getInterchanges(connection, this.sourcePath,
                    this.targetPath);
            if (lists != null) {
                setSourceToTargetCount(lists.length);
            }
        }
        if (lists == null) {
            lists = EMPTY_LISTS;
        }
        return lists;
    }

    private IP4SubmittedChangelist getLatestChangelist(
            final IP4Connection connection, final String path)
            throws MappingException {
        final IP4SubmittedChangelist[] list = new IP4SubmittedChangelist[] { null };
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                List<IChangelistSummary> lists = server.getChangelists(1,
                        P4FileSpecBuilder.makeFileSpecList(path), null, null,
                        false, Type.SUBMITTED, false);
                if (lists.size() > 0) {
                    list[0] = new P4SubmittedChangelist(connection,
                            new Changelist(lists.get(0), server, false));
                }
            }
        };
        runOperation(connection, operation);
        return list[0];
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshLatestSourceChange(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public IP4SubmittedChangelist refreshLatestSourceChange(
            IP4Connection connection, Object context) throws MappingException {
        IP4SubmittedChangelist latest = null;
        if (context instanceof String) {
            latest = getLatestChangelist(connection, (String) context);
        }
        if (latest != null) {
            setLatestSource(latest.getId());
        }
        return latest;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshLatestTargetChange(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public IP4SubmittedChangelist refreshLatestTargetChange(
            IP4Connection connection, Object context) throws MappingException {
        IP4SubmittedChangelist latest = null;
        if (context instanceof String) {
            latest = getLatestChangelist(connection, (String) context);
        }
        if (latest != null) {
            setLatestTarget(latest.getId());
        }
        return latest;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getSourceContext(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public Object getSourceContext(IP4Connection connection, Object context) {
        return getSourcePath();
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getTargetContext(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public Object getTargetContext(IP4Connection connection, Object context) {
        return getTargetPath();
    }

    private ChangeType changesExist(IP4Connection connection,
            final String path1, final String path2) throws MappingException {
        final ChangeType[] type = new ChangeType[] { ChangeType.UNKNOWN };
        IP4ClientOperation operation = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                P4IntegrationOptions options = createIntegrationOptions();
                List<IFileSpec> files = client.integrateFiles(new FileSpec(path1), new FileSpec(path2), null,options.createIntegrateFilesOptions(-1, true));

                if (files.size() > 0) {
                    type[0] = getChangeType(files.get(0));
                }
            }
        };
        runOperation(connection, operation);
        return type[0];
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshSourceStatus(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public boolean refreshSourceStatus(IP4Connection connection, Object context)
            throws MappingException {
        ChangeType changes = getSourceChange();
        if (Direction.TARGET != getDirection()) {
            changes = changesExist(connection, getTargetPath(), getSourcePath());
            setSourceChanges(changes);
        }
        return changes == ChangeType.VISIBLE_CHANGES;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshTargetStatus(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public boolean refreshTargetStatus(IP4Connection connection, Object context)
            throws MappingException {
        ChangeType changes = getTargetChange();
        if (Direction.SOURCE != getDirection()) {
            changes = changesExist(connection, getSourcePath(), getTargetPath());
            setTargetChanges(changes);
        }
        return changes == ChangeType.VISIBLE_CHANGES;
    }

}
