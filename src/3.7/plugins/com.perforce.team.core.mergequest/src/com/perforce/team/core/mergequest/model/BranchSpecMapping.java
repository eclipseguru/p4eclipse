/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.IBranchSpecSummary;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.IChangelistSummary;
import com.perforce.p4java.core.IMapEntry.EntryType;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.BranchSpecSummary;
import com.perforce.p4java.impl.generic.core.Changelist;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ServerOperation;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Branch;
import com.perforce.team.core.p4java.P4ClientOperation;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4ServerOperation;
import com.perforce.team.core.p4java.P4SubmittedChangelist;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchSpecMapping extends Mapping {

    private static class BranchContext {

        IP4Branch branch;
        String[] sourcePaths;
        String[] targetPaths;
        boolean source = true;

        /**
         * Create an empty context
         */
        public BranchContext() {

        }

        /**
         * Create a context from the specific context
         * 
         * @param context
         */
        public BranchContext(BranchContext context) {
            if (context != null) {
                this.branch = context.branch;
                this.sourcePaths = context.sourcePaths;
                this.targetPaths = context.targetPaths;
                this.source = context.source;
            }
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            } else if (other instanceof BranchContext) {
                BranchContext otherContext = (BranchContext) other;
                if (this.source != otherContext.source) {
                    return false;
                }
                if (!branch.getName().equals(otherContext.branch.getName())) {
                    return false;
                }
                if (!branch.getConnection().equals(
                        otherContext.branch.getConnection())) {
                    return false;
                }
                if (!(sourcePaths == null && otherContext.sourcePaths == null)
                        || !Arrays
                                .equals(sourcePaths, otherContext.sourcePaths)) {
                    return false;
                }
                if (!(targetPaths == null && otherContext.targetPaths == null)
                        || !Arrays
                                .equals(targetPaths, otherContext.targetPaths)) {
                    return false;
                }
                return true;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
        	if(branch!=null)
        		return branch.hashCode();
        	// to prevent coverity complain HE_EQUALS_NO_HASHCODE
        	return super.hashCode();
        }

    }

    /**
     * TYPE
     */
    public static final String TYPE = "branchSpec"; //$NON-NLS-1$

    /**
     * @param id
     * @param graph
     */
    public BranchSpecMapping(String id, IBranchGraph graph) {
        super(id, graph);
    }

    /**
     * @param id
     */
    public BranchSpecMapping(String id) {
        super(id);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.BranchGraphElement#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof BranchSpecMapping && super.equals(obj);
    }

    /**
     * Generate an {@link IP4Branch} from the current mapping using the
     * specified non-null connection
     * 
     * @param connection
     * @return - p4 branch
     */
    public IP4Branch generateBranch(IP4Connection connection) {
        IBranchSpecSummary summary = new BranchSpecSummary();
        summary.setName(getName());
        return new P4Branch(connection, summary, false);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshSourceInterchanges(com.perforce.team.core.p4java.IP4Connection)
     */
    @Override
    public IP4SubmittedChangelist[] refreshSourceInterchanges(
            IP4Connection connection) throws MappingException {
        IP4SubmittedChangelist[] lists = null;
        if (Direction.TARGET != getDirection() && connection != null) {
            lists = getInterchanges(generateBranch(connection), true);
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
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshTargetInterchanges(IP4Connection
     *      connection)
     */
    @Override
    public IP4SubmittedChangelist[] refreshTargetInterchanges(
            IP4Connection connection) throws MappingException {
        IP4SubmittedChangelist[] lists = null;
        if (Direction.SOURCE != getDirection() && connection != null) {
            lists = getInterchanges(generateBranch(connection), false);
            if (lists != null) {
                setSourceToTargetCount(lists.length);
            }
        }
        if (lists == null) {
            lists = EMPTY_LISTS;
        }
        return lists;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getType()
     */
    @Override
    public String getType() {
        return TYPE;
    }

    private IP4SubmittedChangelist getLatestChangelist(final String[] paths,
            final IP4Connection connection) throws MappingException {
        final IP4SubmittedChangelist[] list = new IP4SubmittedChangelist[] { null };
        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                List<IChangelistSummary> lists = server.getChangelists(1,
                        P4FileSpecBuilder.makeFileSpecList(paths), null, null,
                        false, Type.SUBMITTED, false);
                IChangelistSummary latestSummary = null;
                for (IChangelistSummary summary : lists) {
                    if (latestSummary == null
                            || summary.getId() > latestSummary.getId()) {
                        latestSummary = summary;
                    }
                }
                if (latestSummary != null) {
                    list[0] = new P4SubmittedChangelist(connection,
                            new Changelist(latestSummary, server, false));
                }
            }
        };
        runOperation(connection, operation);
        return list[0];
    }

    private IP4SubmittedChangelist[] getInterchanges(final IP4Branch branch,
            final boolean reverse) throws MappingException {
        if (branch == null) {
            return null;
        }

        @SuppressWarnings("unchecked")
        final List<IP4SubmittedChangelist>[] submittedLists = new List[] { null };

        final IP4Connection connection = branch.getConnection();

        IP4ServerOperation operation = new P4ServerOperation() {

            public void run(IServer server) throws P4JavaException, P4JavaError {
                List<IChangelist> lists = server.getInterchanges(
                        branch.getName(), null, null, false, true, -1, reverse,
                        false);
                List<IP4SubmittedChangelist> submitted = new ArrayList<IP4SubmittedChangelist>(
                        lists.size());
                for (IChangelist list : lists) {
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
     * @see com.perforce.team.core.mergequest.model.Mapping#refreshLatestSourceChange(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public IP4SubmittedChangelist refreshLatestSourceChange(
            IP4Connection connection, Object context) throws MappingException {
        IP4SubmittedChangelist latest = null;
        if (context instanceof BranchContext) {
            latest = getLatestChangelist(((BranchContext) context).sourcePaths,
                    connection);
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
        if (context instanceof BranchContext) {
            latest = getLatestChangelist(((BranchContext) context).targetPaths,
                    connection);
        }
        if (latest != null) {
            setLatestTarget(latest.getId());
        }
        return latest;
    }

    private BranchContext getContext(IP4Connection connection) {
        BranchContext context = null;
        if (connection != null) {
            IP4Branch branch = generateBranch(connection);
            if (branch != null) {
                context = new BranchContext();
                context.branch = branch;
                branch.refresh();
                List<String> sourcePaths = new ArrayList<String>();
                List<String> targetPaths = new ArrayList<String>();
                ViewMap<IBranchMapping> view = branch.getView();
                if (view != null) {
                    for (IBranchMapping entry : view) {
                        if (EntryType.EXCLUDE != entry.getType()) {
                            sourcePaths.add(entry.getLeft(false));
                            targetPaths.add(entry.getRight(false));
                        }
                    }
                }
                context.sourcePaths = sourcePaths
                        .toArray(new String[sourcePaths.size()]);
                context.targetPaths = targetPaths
                        .toArray(new String[targetPaths.size()]);
            }
        }
        return context;
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getSourceContext(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public Object getSourceContext(IP4Connection connection, Object context) {
        if (context instanceof BranchContext) {
            BranchContext bContext = (BranchContext) context;
            if (bContext.sourcePaths != null) {
                if (!bContext.source) {
                    bContext = new BranchContext(bContext);
                    bContext.source = true;
                }
                return bContext;
            }

        }
        return getContext(connection);
    }

    /**
     * @see com.perforce.team.core.mergequest.model.Mapping#getTargetContext(com.perforce.team.core.p4java.IP4Connection,
     *      java.lang.Object)
     */
    @Override
    public Object getTargetContext(IP4Connection connection, Object context) {
        if (context instanceof BranchContext) {
            BranchContext bContext = (BranchContext) context;
            if (bContext.targetPaths != null) {
                if (bContext.source) {
                    bContext = new BranchContext(bContext);
                    bContext.source = false;
                }
                return bContext;
            }
            return context;
        }
        return getContext(connection);
    }

    private ChangeType changesExist(IP4Connection connection,
            final boolean reverse) throws MappingException {
        final ChangeType[] changes = new ChangeType[] { ChangeType.UNKNOWN };
        IP4ClientOperation operation = new P4ClientOperation() {

            public void run(IClient client) throws P4JavaException, P4JavaError {
                P4IntegrationOptions options = createIntegrationOptions();
                options.setReverseMapping(reverse);
                List<IFileSpec> files = client.integrateFiles(null, null, getName(),options.createIntegrateFilesOptions(-1, true));
                if (files.size() > 0) {
                    changes[0] = getChangeType(files.get(0));
                }
            }
        };
        runOperation(connection, operation);
        return changes[0];
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
            changes = changesExist(connection, true);
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
            changes = changesExist(connection, false);
            setTargetChanges(changes);
        }
        return changes == ChangeType.VISIBLE_CHANGES;
    }
}
