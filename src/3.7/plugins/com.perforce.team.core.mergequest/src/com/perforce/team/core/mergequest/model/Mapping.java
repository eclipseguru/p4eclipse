/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.AccessException;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4ClientOperation;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ServerOperation;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4Workspace;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class Mapping extends BranchGraphElement {

    /**
     * CONNECTED
     */
    public static final String CONNECTED = "connected"; //$NON-NLS-1$

    /**
     * DIRECTION
     */
    public static final String DIRECTION = "direction"; //$NON-NLS-1$

    /**
     * SOURCE_ANCHOR
     */
    public static final String SOURCE_ANCHOR = "sourceAnchor"; //$NON-NLS-1$

    /**
     * TARGET_ANCHOR
     */
    public static final String TARGET_ANCHOR = "targetAnchor"; //$NON-NLS-1$

    /**
     * SOURCE_CHANGE_TYPE
     */
    public static final String SOURCE_CHANGE_TYPE = "sourceChangeType"; //$NON-NLS-1$

    /**
     * TARGET_CHANGE_TYPE
     */
    public static final String TARGET_CHANGE_TYPE = "targetChangeType"; //$NON-NLS-1$

    /**
     * LATEST_SOURCE_CHANGE
     */
    public static final String LATEST_SOURCE_CHANGE = "latestSourceChange"; //$NON-NLS-1$

    /**
     * LATEST_TARGET_CHANGE
     */
    public static final String LATEST_TARGET_CHANGE = "latestTargetChange"; //$NON-NLS-1$

    /**
     * SOURCE_CHANGE_COUNT
     */
    public static final String SOURCE_CHANGE_COUNT = "sourceChangeCount"; //$NON-NLS-1$

    /**
     * TARGET_CHANGE_COUNT
     */
    public static final String TARGET_CHANGE_COUNT = "targetChangeCount"; //$NON-NLS-1$

    /**
     * JOINTS
     */
    public static final String JOINTS = "joints"; //$NON-NLS-1$

    /**
     * ANCHOR_MIN
     */
    public static final int ANCHOR_MIN = 0;

    /**
     * ANCHOR_MAX
     */
    public static final int ANCHOR_MAX = 9;

    /**
     * EMPTY_LISTS
     */
    public static final IP4SubmittedChangelist[] EMPTY_LISTS = new IP4SubmittedChangelist[0];

    /**
     * Direction of mapping
     */
    public static enum Direction {

        /**
         * To source only
         */
        SOURCE,

        /**
         * To target only
         */
        TARGET,

        /**
         * Both directions
         */
        BOTH;

        /**
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return name().toLowerCase(Locale.ENGLISH);
        }

    }

    /**
     * Mapping joint class
     */
    public static class Joint {

        int x1;
        int y1;
        int x2;
        int y2;

        /**
         * Create mapping joint
         * 
         * @param x1
         * @param y1
         * @param x2
         * @param y2
         */
        public Joint(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return this.x1 + ", " + this.y1 + ", " + this.x2 + ", " + this.y2; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        /**
         * @return x1
         */
        public int getX1() {
            return this.x1;
        }

        /**
         * @return y1
         */
        public int getY1() {
            return this.y1;
        }

        /**
         * @return x2
         */
        public int getX2() {
            return this.x2;
        }

        /**
         * @return y2
         */
        public int getY2() {
            return this.y2;
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            return (x1 + x2 + 1) * (y1 + y2 + 1) ^ x1 ^ y1;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof Joint) {
                Joint other = (Joint) obj;
                return this.x1 == other.x1 && this.x2 == other.x2
                        && this.y1 == other.y1 && this.y2 == other.y2;
            }
            return false;
        }

    }

    /**
     * Descriptor
     */
    protected MappingDescriptor descriptor;

    private Direction direction = Direction.BOTH;
    private String sourceId;
    private String targetId;
    private int sourceAnchor = 0;
    private int targetAnchor = 0;
    private List<Joint> joints = new ArrayList<Mapping.Joint>();

    /**
     * Create a mapping
     * 
     * @param id
     */
    public Mapping(String id) {
        this(id, null);
    }

    /**
     * Create a mapping
     * 
     * @param id
     * @param graph
     */
    public Mapping(String id, IBranchGraph graph) {
        this(id, id, graph);
    }

    /**
     * Create mapping
     * 
     * @param id
     * @param name
     * @param graph
     */
    public Mapping(String id, String name, IBranchGraph graph) {
        super(id, name, graph);
        this.descriptor = new MappingDescriptor();
    }

    /**
     * Add joint
     * 
     * @param joint
     */
    public void addJoint(Joint joint) {
        if (joint != null) {
            joints.add(joint);
            this.changeSupport.firePropertyChange(JOINTS, null, joint);
        }
    }

    /**
     * Add joint at index
     * 
     * @param index
     * @param joint
     */
    public void addJoint(int index, Joint joint) {
        if (joint != null & index >= 0 && index <= joints.size()) {
            joints.add(index, joint);
            this.changeSupport.firePropertyChange(JOINTS, null, joint);
        }
    }

    /**
     * Remove joint at index
     * 
     * @param index
     * @return removed joint or null if not removed
     */
    public Joint removeJoint(int index) {
        Joint removed = null;
        if (index >= 0 && index < joints.size()) {
            removed = joints.remove(index);
            this.changeSupport.firePropertyChange(JOINTS, removed, null);
        }
        return removed;
    }

    /**
     * Remove joint at index
     * 
     * @param index
     * @param joint
     * @return joint previously at index or null if none
     */
    public Joint setJoint(int index, Joint joint) {
        Joint removed = null;
        if (joint != null && index >= 0 && index < joints.size()) {
            removed = joints.set(index, joint);
            this.changeSupport.firePropertyChange(JOINTS, removed, joint);
        }
        return removed;
    }

    /**
     * Get joints
     * 
     * @return non-null but possibly empty array
     */
    public Joint[] getJoints() {
        return joints.toArray(new Joint[joints.size()]);
    }

    /**
     * Get number of joints in mapping
     * 
     * @return number of joints
     */
    public int getJointCount() {
        return this.joints.size();
    }

    private int getValidAnchor(int terminal) {
        return Math.min(ANCHOR_MAX, Math.max(ANCHOR_MIN, terminal));
    }

    /**
     * Set source anchor index
     * 
     * @param index
     */
    public void setSourceAnchor(int index) {
        index = getValidAnchor(index);
        if (index != this.sourceAnchor) {
            int previous = this.sourceAnchor;
            this.sourceAnchor = index;
            this.changeSupport.firePropertyChange(SOURCE_ANCHOR, previous,
                    this.sourceAnchor);
        }
    }

    /**
     * Get source anchor index
     * 
     * @return index
     */
    public int getSourceAnchor() {
        return this.sourceAnchor;
    }

    /**
     * Set target anchor index
     * 
     * @param anchor
     */
    public void setTargetAnchor(int anchor) {
        anchor = getValidAnchor(anchor);
        if (anchor != this.targetAnchor) {
            int previous = this.targetAnchor;
            this.targetAnchor = anchor;
            this.changeSupport.firePropertyChange(SOURCE_ANCHOR, previous,
                    this.targetAnchor);
        }
    }

    /**
     * Get target anchor index
     * 
     * @return index
     */
    public int getTargetAnchor() {
        return this.targetAnchor;
    }

    /**
     * Set source to target count
     * 
     * @param count
     * @return true if set, false otherwise
     */
    public boolean setSourceToTargetCount(int count) {
        int previous = this.descriptor.getTargetCount();
        boolean set = this.descriptor.setTargetCount(count);
        if (set) {
            this.changeSupport.firePropertyChange(TARGET_CHANGE_COUNT,
                    previous, this.descriptor.getTargetCount());
        }
        return set;
    }

    /**
     * Set target to source count
     * 
     * @param count
     * @return true if set, false otherwise
     */
    public boolean setTargetToSourceCount(int count) {
        int previous = this.descriptor.getSourceCount();
        boolean set = this.descriptor.setSourceCount(count);
        if (set) {
            this.changeSupport.firePropertyChange(SOURCE_CHANGE_COUNT,
                    previous, this.descriptor.getSourceCount());
        }
        return set;
    }

    /**
     * Set the type of changes to integrate to source
     * 
     * @param type
     * @return true if set, false otherwise
     */
    public boolean setSourceChanges(ChangeType type) {
        ChangeType previous = this.descriptor.getSourceChangeType();
        boolean set = this.descriptor.setSourceChangeType(type);
        if (set) {
            this.changeSupport.firePropertyChange(SOURCE_CHANGE_TYPE, previous,
                    this.descriptor.getSourceChangeType());
        }
        return set;
    }

    /**
     * Get source change type. This is the change type from the target to the
     * source.
     * 
     * @return - type
     */
    public ChangeType getSourceChange() {
        return this.descriptor.getSourceChangeType();
    }

    /**
     * Get target change type. This is the change type from the source to the
     * target.
     * 
     * @return - type
     */
    public ChangeType getTargetChange() {
        return this.descriptor.getTargetChangeType();
    }

    /**
     * Set the type of changes to integrate to target
     * 
     * @param type
     * @return true if set, false otherwise
     */
    public boolean setTargetChanges(ChangeType type) {
        ChangeType previous = this.descriptor.getTargetChangeType();
        boolean set = this.descriptor.setTargetChangeType(type);
        if (set) {
            this.changeSupport.firePropertyChange(TARGET_CHANGE_TYPE, previous,
                    this.descriptor.getTargetChangeType());
        }
        return set;
    }

    private boolean hasChanges(ChangeType type) {
        return type == ChangeType.NO_PERMISSION
                || type == ChangeType.VISIBLE_CHANGES;
    }

    /**
     * Do source changes exist?
     * 
     * @return - true if exist, false otherwise
     */
    public boolean hasSourceChanges() {
        return hasChanges(this.descriptor.getSourceChangeType());
    }

    /**
     * Do target changes exist?
     * 
     * @return - true if exist, false otherwise
     */
    public boolean hasTargetChanges() {
        return hasChanges(this.descriptor.getTargetChangeType());
    }

    /**
     * Get source to target count
     * 
     * @return - count
     */
    public int getSourceToTargetCount() {
        return this.descriptor.getTargetCount();
    }

    /**
     * Get target to source count
     * 
     * @return - count
     */
    public int getTargetToSourceCount() {
        return this.descriptor.getSourceCount();
    }

    /**
     * @return the latestSource
     */
    public int getLatestSource() {
        return this.descriptor.getLatestSource();
    }

    /**
     * @param latestSource
     *            the latestSource to set
     * @return true if set, false otherwise
     */
    public boolean setLatestSource(int latestSource) {
        int previous = this.descriptor.getLatestSource();
        boolean set = this.descriptor.setLatestSource(latestSource);
        if (set) {
            this.changeSupport.firePropertyChange(LATEST_SOURCE_CHANGE,
                    previous, this.descriptor.getLatestSource());
        }
        return set;
    }

    /**
     * @return the latestTarget
     */
    public int getLatestTarget() {
        return this.descriptor.getLatestTarget();
    }

    /**
     * @param latestTarget
     *            the latestTarget to set
     * @return true if updated, false otherwise
     */
    public boolean setLatestTarget(int latestTarget) {
        int previous = this.descriptor.getLatestTarget();
        boolean set = this.descriptor.setLatestTarget(latestTarget);
        if (set) {
            this.changeSupport.firePropertyChange(LATEST_TARGET_CHANGE,
                    previous, this.descriptor.getLatestTarget());
        }
        return set;
    }

    private Branch getBranch(String id) {
        IBranchGraph graph = getGraph();
        return graph != null && id != null ? graph.getElementById(id,
                Branch.class) : null;
    }

    /**
     * Get source branch
     * 
     * @return - branch
     */
    public Branch getSource() {
        return getBranch(this.sourceId);
    }

    /**
     * Get target branch
     * 
     * @return - branch
     */
    public Branch getTarget() {
        return getBranch(this.targetId);
    }

    /**
     * Compute interchanges going to the target of the mapping from the source
     * using specified connection.
     * 
     * @param connection
     * @return - non-null but possibly empty array of submitted changelists
     * @throws MappingException
     */
    public abstract IP4SubmittedChangelist[] refreshTargetInterchanges(
            IP4Connection connection) throws MappingException;

    /**
     * Compute interchanges going to the source from the target using specified
     * connection.
     * 
     * @param connection
     * @return - non-null but possibly empty array of submitted changelists
     * @throws MappingException
     */
    public abstract IP4SubmittedChangelist[] refreshSourceInterchanges(
            IP4Connection connection) throws MappingException;

    /**
     * Get source context
     * 
     * @param connection
     * @param context
     * @return - context
     */
    public abstract Object getSourceContext(IP4Connection connection,
            Object context);

    /**
     * Get target context
     * 
     * @param connection
     * @param context
     * @return - context
     */
    public abstract Object getTargetContext(IP4Connection connection,
            Object context);

    /**
     * Get latest changelist at target.
     * 
     * @param connection
     * @param context
     * @return - p4 submitted changelist or null if no changelists submitted to
     *         target
     * @throws MappingException
     *             if changelist look up fails
     */
    public abstract IP4SubmittedChangelist refreshLatestTargetChange(
            IP4Connection connection, Object context) throws MappingException;

    /**
     * Get latest changelist at source.
     * 
     * @param connection
     * @param context
     * @return - p4 submitted changelist or null if no changelist submitted to
     *         source
     * @throws MappingException
     *             if changelist lookup fails
     */
    public abstract IP4SubmittedChangelist refreshLatestSourceChange(
            IP4Connection connection, Object context) throws MappingException;

    /**
     * Changes exist to integrate to the source? Refresh whether any pending
     * integrations exist going from the target to the source.
     * 
     * @param connection
     * @param context
     * @return - true if changes exist, false otherwise
     * @throws MappingException
     */
    public abstract boolean refreshSourceStatus(IP4Connection connection,
            Object context) throws MappingException;

    /**
     * Changes exists to integrate to the target? Refresh whether any pending
     * integrations exist going from the source to the target.
     * 
     * @param connection
     * @param context
     * @return - true if changes exist, false otherwise
     * @throws MappingException
     */
    public abstract boolean refreshTargetStatus(IP4Connection connection,
            Object context) throws MappingException;

    /**
     * Get mapping type
     * 
     * @return - mapping type
     */
    public abstract String getType();

    /**
     * Get change type
     * 
     * @param spec
     * @return - change type
     */
    protected ChangeType getChangeType(IFileSpec spec) {
        ChangeType type = ChangeType.UNKNOWN;
        if (FileSpecOpStatus.ERROR != spec.getOpStatus()) {
            type = ChangeType.VISIBLE_CHANGES;
        } else {
            String message = spec.getStatusMessage();
            if (message != null) {
                if (message
                        .endsWith("- no permission for operation on file(s).")) { //$NON-NLS-1$
                    type = ChangeType.NO_PERMISSION;
                } else if (message
                        .endsWith("- all revision(s) already integrated.")) { //$NON-NLS-1$
                    type = ChangeType.NO_CHANGES;
                } else if (message.endsWith("- no such file(s).")) { //$NON-NLS-1$
                    type = ChangeType.NO_CHANGES;
                } else if (message
                        .endsWith("- no target file(s) in both client and branch view.")) { //$NON-NLS-1$
                    type = ChangeType.NO_CHANGES;
                } else if (message.endsWith("File(s) not in client view.")) { //$NON-NLS-1$
                    type = ChangeType.NO_CHANGES;
                }
            }
        }
        return type;
    }

    /**
     * Use the workspace error handler for the specified access exception to
     * determine whether retry should occur.
     * 
     * @param connection
     * @param exception
     * @return true to retry, false otherwise
     */
    protected boolean retryAfterLogin(IP4Connection connection,
            AccessException exception) {
        if (connection == null || exception == null) {
            return false;
        }

        IErrorHandler handler = connection.getErrorHandler();
        if (handler == null) {
            handler = P4Workspace.getWorkspace().getErrorHandler();
        }
        if (handler != null) {
            return handler.shouldRetry(connection, exception);
        } else {
            return false;
        }
    }

    /**
     * Run client operation
     * 
     * @param connection
     * @param operation
     * @throws MappingException
     */
    protected void runOperation(IP4Connection connection,
            IP4ClientOperation operation) throws MappingException {
        if (connection == null || operation == null) {
            return;
        }
        IClient client = connection.getClient();
        boolean retry = true;
        while (retry) {
            retry = false;
            if (client == null) {
                throw new MappingException("Client not set on connection"); //$NON-NLS-1$
            }
            try {
                operation.run(client);
            } catch (P4JavaException e) {
                if (e instanceof AccessException) {
                    retry = retryAfterLogin(connection, (AccessException) e);
                }
                if (retry) {
                    client = connection.getClient();
                } else {
                    throw new MappingException(e);
                }
            } catch (P4JavaError e) {
                throw new MappingException(e);
            }
        }
    }

    /**
     * Run server operation
     * 
     * @param connection
     * @param operation
     * @throws MappingException
     */
    protected void runOperation(IP4Connection connection,
            IP4ServerOperation operation) throws MappingException {
        if (connection == null || operation == null) {
            return;
        }
        IServer server = connection.getServer();
        boolean retry = true;
        while (retry) {
            retry = false;
            if (server == null) {
                throw new MappingException("Server not set on connection"); //$NON-NLS-1$
            }
            try {
                operation.run(server);
            } catch (P4JavaException e) {
                if (e instanceof AccessException) {
                    retry = retryAfterLogin(connection, (AccessException) e);
                }
                if (retry) {
                    server = connection.getServer();
                } else {
                    throw new MappingException(e);
                }
            } catch (P4JavaError e) {
                throw new MappingException(e);
            }
        }
    }

    /**
     * Disconnect this mapping from the configured branch ids
     * 
     * @return true if disconnected, false otherwise
     */
    public boolean disconnect() {
        boolean removed = false;
        Branch source = getSource();
        if (source != null) {
            removed = source.remove(this);
        }
        Branch target = getTarget();
        if (target != null) {
            removed = target.remove(this) || removed;
        }
        if (removed) {
            this.changeSupport.firePropertyChange(CONNECTED, true, false);
        }
        return removed;
    }

    /**
     * Associate this mapping with the specified branches
     * 
     * @param source
     * @param target
     * @return true if connected, false otherwise
     */
    public boolean connect(Branch source, Branch target) {
        boolean added = false;
        if (source != null && target != null && !source.equals(target)) {
            disconnect();
            setSourceId(source.getId());
            setTargetId(target.getId());
            added = connect();
        }
        return added;
    }

    /**
     * Connection this mapping to the configured branch ids
     * 
     * @return true if connected, false otherwise
     */
    public boolean connect() {
        boolean added = false;
        Branch source = getSource();
        if (source != null) {
            added = source.add(this);
        }
        Branch target = getTarget();
        if (target != null) {
            added = target.add(this) || added;
        }
        if (added) {
            this.changeSupport.firePropertyChange(CONNECTED, false, true);
        }
        return added;
    }

    /**
     * @return the sourceId
     */
    public String getSourceId() {
        return this.sourceId;
    }

    /**
     * @param sourceId
     *            the sourceId to set
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the targetId
     */
    public String getTargetId() {
        return this.targetId;
    }

    /**
     * @param targetId
     *            the targetId to set
     */
    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    /**
     * Set the mapping direction
     * 
     * @param direction
     * @return true if direction set, false otherwise
     */
    public boolean setDirection(Direction direction) {
        boolean set = false;
        if (direction != null && this.direction != direction) {
            Direction previous = this.direction;
            this.direction = direction;
            set = true;
            this.changeSupport.firePropertyChange(DIRECTION, previous,
                    this.direction);
        }
        return set;
    }

    /**
     * Get direction of mapping
     * 
     * @return direction
     */
    public Direction getDirection() {
        return this.direction;
    }

    /**
     * Create standard integration options used to refresh status
     * 
     * @return integration options
     */
    protected P4IntegrationOptions createIntegrationOptions() {
    	IServer server = getGraph().getConnection().getServer();
    	P4IntegrationOptions options = P4IntegrationOptions.createInstance(server);
    	if(options instanceof P4IntegrationOptions2){
			((P4IntegrationOptions2) options).setBaselessMerge(true);
    	}
		options.setMaxFiles(1);
    	options.setIntegrateAroundDeleted(true);
        return options;
    }

}
