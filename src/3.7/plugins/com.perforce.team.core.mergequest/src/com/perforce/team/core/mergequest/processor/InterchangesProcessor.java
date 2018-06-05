/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.mergequest.processor;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.core.mergequest.model.MappingException;
import com.perforce.team.core.mergequest.model.MappingDescriptor.ChangeType;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4ConnectionProvider;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Date;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class InterchangesProcessor {

    private static class RefreshRequest {

        boolean force = false;
        Mapping[] mappings = null;
        IP4Connection connection = null;
        IProgressMonitor monitor = null;
        Runnable callback = null;

    }

    private static class Result {

        IP4SubmittedChangelist latestTarget = null;
        IP4SubmittedChangelist latestSource = null;
        IP4SubmittedChangelist[] sourceInterchanges = null;
        IP4SubmittedChangelist[] targetInterchanges = null;
        long interchangesRefreshTime = -1L;

    }

    private ISchedulingRule rule = P4Runner.createRule();
    private Map<Mapping, Result> interchanges;
    private IBranchGraph graph;
    private IP4ConnectionProvider provider;

    private IP4Listener p4Listener = new IP4Listener() {

        public void resoureChanged(P4Event event) {
            EventType type = event.getType();
            if (EventType.SUBMIT_CHANGELIST == event.getType()) {
                if (connectionMatch(event.getCommonConnections())) {
                    refresh();
                }
            } else if (EventType.REFRESHED == type) {
                refreshBranchSpecMappings(event.getBranches());
            }
        }

        private void refreshBranchSpecMappings(IP4Branch[] branches) {
            if (branches.length > 0) {
                List<Mapping> refresh = new ArrayList<Mapping>();
                for (Mapping mapping : getMappings()) {
                    if (mapping instanceof BranchSpecMapping) {
                        for (IP4Branch branch : branches) {
                            if (branch.getName().equals(mapping.getName())
                                    && connectionMatch(branch)) {
                                refresh.add(mapping);
                                break;
                            }
                        }
                    }
                }
                if (!refresh.isEmpty()) {
                    RefreshRequest request = new RefreshRequest();
                    request.mappings = refresh.toArray(new Mapping[refresh
                            .size()]);
                    request.force = true;
                    scheduleRefreshRequest(request);
                }
            }
        }

        private boolean connectionMatch(IP4Resource... resources) {
            IP4Connection connection = provider.getConnection();
            if (connection != null) {
                for (IP4Resource resource : resources) {
                    if (connection.equals(resource.getConnection())) {
                        return true;
                    }
                }
            }
            return false;
        }

		public String getName() {
			return InterchangesProcessor.this.getClass().getSimpleName();
		}

    };

    private PropertyChangeListener mappingListener = new PropertyChangeListener() {

        public void propertyChange(PropertyChangeEvent evt) {
            Object source = evt.getSource();
            if (source instanceof Mapping) {
                scheduleFullRefresh((Mapping) source);
            }
        }

    };

    private void scheduleFullRefresh(Mapping mapping) {
        if (mapping != null) {
            RefreshRequest request = new RefreshRequest();
            request.force = true;
            request.mappings = new Mapping[] { mapping };
            scheduleRefreshRequest(request);
        }
    }

    /**
     * Create an interchanges processor
     * 
     * @param provider
     * @param graph
     */
    public InterchangesProcessor(IP4ConnectionProvider provider,
            IBranchGraph graph) {
        this.interchanges = Collections
                .synchronizedMap(new HashMap<Mapping, Result>());
        this.provider = provider;
        this.graph = graph;
        P4Workspace.getWorkspace().addListener(this.p4Listener);
    }

    private void addMappingListener(Mapping mapping) {
        if (mapping != null) {
            mapping.addPropertyListener(Mapping.NAME, this.mappingListener);
            mapping.addPropertyListener(DepotPathMapping.SOURCE_PATH,
                    this.mappingListener);
            mapping.addPropertyListener(DepotPathMapping.TARGET_PATH,
                    this.mappingListener);
        }
    }

    private void removeMappingListener(Mapping mapping) {
        if (mapping != null) {
            mapping.removePropertyListener(Mapping.NAME, this.mappingListener);
            mapping.removePropertyListener(DepotPathMapping.SOURCE_PATH,
                    this.mappingListener);
            mapping.removePropertyListener(DepotPathMapping.TARGET_PATH,
                    this.mappingListener);
        }
    }

    private Mapping[] getMappings() {
        Mapping[] mappings = null;
        synchronized (interchanges) {
            mappings = this.interchanges.keySet().toArray(new Mapping[0]);
        }
        return mappings;
    }

    /**
     * Dispose of the processor
     */
    public void dispose() {
        P4Workspace.getWorkspace().removeListener(this.p4Listener);
        for (Mapping mapping : getMappings()) {
            removeMappingListener(mapping);
        }
        this.interchanges.clear();
    }

    /**
     * Get scheduling rule
     * 
     * @return scheduling rule
     */
    public ISchedulingRule getRule() {
        return this.rule;
    }

    /**
     * Refresh all mappings
     */
    public void refresh() {
        refresh((Runnable) null);
    }

    /**
     * Refresh all mappings
     * 
     * @param callback
     */
    public void refresh(Runnable callback) {
        refresh(graph.getMappings(), callback);
    }

    /**
     * Refresh the specified mappings
     * 
     * @param mappings
     */
    public void refresh(final Mapping[] mappings) {
        refresh(mappings, null);
    }

    /**
     * Refresh the specified mappings
     * 
     * @param mappings
     * @param callback
     */
    public void refresh(final Mapping[] mappings, final Runnable callback) {
        RefreshRequest request = new RefreshRequest();
        request.mappings = mappings;
        request.callback = callback;
        scheduleRefreshRequest(request);
    }

    /**
     * Schedule refresh request
     * 
     * @param request
     */
    protected void scheduleRefreshRequest(final RefreshRequest request) {
        if (request == null) {
            return;
        }
        request.connection = this.provider.getConnection();
        if (request.connection == null) {
            return;
        }
        if (request.mappings == null || request.mappings.length == 0) {
            return;
        }
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.InterchangesProcessor_RefreshingConnectors;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                request.monitor = monitor;
                refresh(request);
                if (request.callback != null) {
                    request.callback.run();
                }
            }

        }, getRule());
    }

    /**
     * Refresh the specified mappings using the specified connection.
     * 
     * @param mappings
     * @param connection
     * @param monitor
     */
    private void refresh(RefreshRequest request) {

        final IP4Connection connection = request.connection;
        final Mapping[] mappings = request.mappings;
        final IProgressMonitor monitor = request.monitor;

        monitor.beginTask("", mappings.length * 4); //$NON-NLS-1$
        Map<Object, IP4SubmittedChangelist> contexts = new HashMap<Object, IP4SubmittedChangelist>();
        for (Mapping mapping : mappings) {
            if (monitor.isCanceled()) {
                break;
            }

            monitor.subTask(MessageFormat.format(
                    Messages.InterchangesProcessor_RefreshingChange,
                    mapping.getName()));

            Result mappingResult = getResult(mapping);
            boolean error = false;

            int latestSourceList = mapping.getLatestSource();
            int latestTargetList = mapping.getLatestTarget();

            Object sourceContext = mapping.getSourceContext(connection, null);
            IP4SubmittedChangelist latestSource = null;
            if (sourceContext != null) {
                latestSource = contexts.get(sourceContext);
            }
            if (latestSource == null) {
                try {
                    latestSource = mapping.refreshLatestSourceChange(
                            connection, sourceContext);
                    if (latestSource != null && sourceContext != null) {
                        contexts.put(sourceContext, latestSource);
                    }
                } catch (MappingException e) {
                    PerforceProviderPlugin.logError(e);
                    error = true;
                }
            } else {
                mapping.setLatestSource(latestSource.getId());
            }
            monitor.worked(1);

            if (monitor.isCanceled()) {
                break;
            }

            Object targetContext = mapping.getTargetContext(connection,
                    sourceContext);
            IP4SubmittedChangelist latestTarget = null;
            if (targetContext != null) {
                latestTarget = contexts.get(targetContext);
            }
            if (latestTarget == null) {
                if (!error) {
                    try {
                        latestTarget = mapping.refreshLatestTargetChange(
                                connection, targetContext);
                        if (latestTarget != null && targetContext != null) {
                            contexts.put(targetContext, latestTarget);
                        }
                    } catch (MappingException e) {
                        PerforceProviderPlugin.logError(e);
                        error = true;
                    }
                }
            } else {
                mapping.setLatestTarget(latestTarget.getId());
            }
            monitor.worked(1);

            if (monitor.isCanceled()) {
                break;
            }

            if (latestSourceList != mapping.getLatestSource()
                    || latestTargetList != mapping.getLatestTarget()) {
                mapping.setSourceToTargetCount(0);
                mapping.setTargetToSourceCount(0);
            }

            if (!error
                    && (request.force || needsRefresh(mapping) || needsRefresh(
                            latestSource, latestTarget, mappingResult))) {
                monitor.subTask(MessageFormat.format(
                        Messages.InterchangesProcessor_RefreshingStatus,
                        mapping.getName()));

                mappingResult.latestSource = latestSource;
                mappingResult.latestTarget = latestTarget;

                try {
                    mapping.refreshSourceStatus(connection, sourceContext);
                    // Clear cache if no changes
                    if (ChangeType.NO_CHANGES == mapping.getSourceChange()) {
                        mappingResult.sourceInterchanges = null;
                    }
                } catch (MappingException e) {
                    PerforceProviderPlugin.logError(e);
                }

                monitor.worked(1);

                if (monitor.isCanceled()) {
                    break;
                }

                try {
                    mapping.refreshTargetStatus(connection, targetContext);
                    // Clear cache if no changes
                    if (ChangeType.NO_CHANGES == mapping.getTargetChange()) {
                        mappingResult.targetInterchanges = null;
                    }
                } catch (MappingException e) {
                    PerforceProviderPlugin.logError(e);
                }

                monitor.worked(1);
            } else {
                monitor.worked(2);
            }
        }
        monitor.done();
    }

    private boolean needsRefresh(Mapping mapping) {
        switch (mapping.getDirection()) {
        case SOURCE:
            return mapping.getSourceChange() == ChangeType.UNKNOWN;
        case TARGET:
            return mapping.getTargetChange() == ChangeType.UNKNOWN;
        case BOTH:
            return mapping.getSourceChange() == ChangeType.UNKNOWN
                    || mapping.getTargetChange() == ChangeType.UNKNOWN;
        }
        return false;
    }

    /**
     * Refresh interchanges for all mappings in the graph
     */
    public void refreshInterchanges() {
        refreshInterchanges((Runnable) null);
    }

    /**
     * Refresh interchanges for all mappings in the graph
     * 
     * @param callback
     *            runnable to invoke after refreshing completes
     */
    public void refreshInterchanges(Runnable callback) {
        refreshInterchanges(graph.getMappings(), callback);
    }

    /**
     * Refresh interchanges for each mapping in the specified array
     * 
     * @param mappings
     */
    public void refreshInterchanges(Mapping[] mappings) {
        refreshInterchanges(mappings, null);
    }

    /**
     * Refresh interchanges for each mapping in the specified array
     * 
     * @param mappings
     * @param callback
     *            runnable to invoke after refreshing completes
     */
    public void refreshInterchanges(final Mapping[] mappings,
            final Runnable callback) {
        if (mappings == null || mappings.length == 0) {
            return;
        }
        final IP4Connection connection = this.provider.getConnection();
        if (connection == null) {
            return;
        }
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.InterchangesProcessor_RefreshingInterchanges;
            }

            public void run(IProgressMonitor monitor) {
                refreshInterchanges(mappings, connection, monitor);
                if (callback != null) {
                    callback.run();
                }
            }

        }, getRule());
    }

    /**
     * Refresh interchanges for each mapping in the specified array
     * 
     * @param mappings
     * @param connection
     * @param monitor
     */
    private void refreshInterchanges(Mapping[] mappings,
            final IP4Connection connection, IProgressMonitor monitor) {
        monitor.beginTask("", mappings.length * 2); //$NON-NLS-1$
        for (Mapping mapping : mappings) {
            if (monitor.isCanceled()) {
                break;
            }

            Result mappingResult = getResult(mapping);

            monitor.subTask(mapping.getName());

            boolean error = false;

            if (mapping.hasSourceChanges()) {
                try {
                    mappingResult.sourceInterchanges = mapping
                            .refreshSourceInterchanges(connection);
                } catch (MappingException e) {
                    mappingResult.sourceInterchanges = null;
                    error = true;
                }
            }
            monitor.worked(1);

            if (monitor.isCanceled()) {
                break;
            }

            if (mapping.hasTargetChanges()) {
                try {
                    mappingResult.targetInterchanges = mapping
                            .refreshTargetInterchanges(connection);
                } catch (MappingException e) {
                    mappingResult.targetInterchanges = null;
                    error = true;
                }
            }
            monitor.worked(1);

            if (!error) {
                mappingResult.interchangesRefreshTime = System
                        .currentTimeMillis();
            }

        }
        monitor.done();
    }

    /**
     * Get last refresh date
     * 
     * @param mapping
     * @return - date
     */
    public Date getLastRefreshDate(Mapping mapping) {
        Date date = null;
        if (mapping != null) {
            Result result = getResult(mapping);
            long time = result.interchangesRefreshTime;
            if (time > 0L) {
                date = new Date(time);
            }
        }
        return date;
    }

    private Result getResult(Mapping mapping) {
        Result result = this.interchanges.get(mapping);
        if (result == null) {
            result = new Result();
            addMappingListener(mapping);
            this.interchanges.put(mapping, result);
        }
        return result;
    }

    private boolean needsRefresh(IP4SubmittedChangelist latestSource,
            IP4SubmittedChangelist latestTarget, Result result) {
        if (result.latestSource == null && latestSource != null) {
            return true;
        }
        if (result.latestTarget == null && latestTarget != null) {
            return true;
        }
        if (result.latestTarget != null && latestSource == null) {
            return true;
        }
        if (result.latestSource != null && latestTarget == null) {
            return true;
        }
        if (result.latestSource != null && latestSource != null
                && result.latestSource.getId() != latestSource.getId()) {
            return true;
        }
        if (result.latestTarget != null && latestTarget != null
                && result.latestTarget.getId() != latestTarget.getId()) {
            return true;
        }
        return false;
    }

    /**
     * Get cached source interchanges for specified mapping
     * 
     * @param mapping
     * @return array of submitted changelists or null if processing failed or
     *         has yet to be run
     */
    public IP4SubmittedChangelist[] getSourceInterchanges(Mapping mapping) {
        return mapping != null ? getResult(mapping).sourceInterchanges : null;
    }

    /**
     * Get cached target interchanges for specified mapping
     * 
     * @param mapping
     * @return array of submitted changelists or null if processing failed or
     *         has yet to be run
     */
    public IP4SubmittedChangelist[] getTargetInterchanges(Mapping mapping) {
        return mapping != null ? getResult(mapping).targetInterchanges : null;
    }

    /**
     * Clear a mapping from this processor's cache
     * 
     * @param mapping
     */
    public void clear(Mapping mapping) {
        if (mapping != null) {
            removeMappingListener(mapping);
            this.interchanges.remove(mapping);
        }
    }

}
