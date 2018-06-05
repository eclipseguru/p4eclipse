/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.synchronize.P4SubmittedChangeSet;
import com.perforce.team.core.p4java.synchronize.PerforceSyncInfo;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ChangeSetCollector extends SyncInfoSetChangeSetCollector {

    private P4ChangeSetActionGroup actions;

    /**
     * @param configuration
     */
    public P4ChangeSetCollector(ISynchronizePageConfiguration configuration) {
        super(configuration);
        this.actions = new P4ChangeSetActionGroup();
        if (configuration != null) {
            configuration.addActionContribution(this.actions);
        }
    }

    /**
     * Mapping of p4 submitted change sets for a connect
     */
    private static class ConnectionSet {

        private Map<Integer, P4SubmittedChangeSet> sets = new HashMap<Integer, P4SubmittedChangeSet>();

        void add(P4SubmittedChangeSet set) {
            if (set != null) {
                this.sets.put(set.getId(), set);
            }
        }

        void dispose() {
            sets.clear();
        }

    }

    private Map<IP4Connection, ConnectionSet> connectionSets = new HashMap<IP4Connection, ConnectionSet>();

    private Map<P4SubmittedChangeSet, List<SyncInfo>> loadSets(
            SyncInfo[] infos, IProgressMonitor monitor) {
        Map<P4SubmittedChangeSet, List<SyncInfo>> collected = new HashMap<P4SubmittedChangeSet, List<SyncInfo>>();
        for (SyncInfo info : infos) {
            if (info instanceof PerforceSyncInfo
                    && (info.getKind() & SyncInfo.INCOMING) != 0) {
                IP4File file = ((PerforceSyncInfo) info).getP4File();
                int change = file.getHeadChange();
                if (change > 0) {
                    ConnectionSet set = connectionSets
                            .get(file.getConnection());
                    if (set == null) {
                        set = new ConnectionSet();
                        connectionSets.put(file.getConnection(), set);
                    }
                    P4SubmittedChangeSet changeSet = set.sets.get(change);
                    if (changeSet == null) {
                        monitor.subTask(MessageFormat.format(
                                Messages.P4ChangeSetCollector_LoadingChange,
                                change));
                        IP4SubmittedChangelist changelist = file
                                .getConnection().getSubmittedChangelistById(
                                        change, true);
                        if (changelist != null) {
                            changeSet = new P4SubmittedChangeSet(changelist);
                            set.add(changeSet);
                        }
                    }
                    if (changeSet != null) {
                        List<SyncInfo> toAdd = collected.get(changeSet);
                        if (toAdd == null) {
                            toAdd = new ArrayList<SyncInfo>();
                            collected.put(changeSet, toAdd);
                        }
                        toAdd.add(info);
                    }
                }
            }
            monitor.worked(1);
        }
        return collected;
    }

    /**
     * Add the non-null collection of sets to this collector
     * 
     * @param sets
     */
    protected void addSets(Collection<P4SubmittedChangeSet> sets) {
        for (P4SubmittedChangeSet set : sets) {
            add(set);
        }
    }

    /**
     * Add the specified sets to this collector
     * 
     * @param sets
     */
    protected void updateSets(
            final Map<P4SubmittedChangeSet, List<SyncInfo>> sets) {
        if (sets != null && !sets.isEmpty()) {
            performUpdate(new IWorkspaceRunnable() {

                public void run(IProgressMonitor monitor) throws CoreException {
                    for (P4SubmittedChangeSet set : sets.keySet()) {
                        List<SyncInfo> toAdd = sets.get(set);
                        if (toAdd != null && !toAdd.isEmpty()) {
                            set.add(toAdd.toArray(new SyncInfo[toAdd.size()]));
                        }
                    }
                    addSets(sets.keySet());
                }
            }, true, null);
        }
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector#add(org.eclipse.team.core.synchronize.SyncInfo[])
     */
    @Override
    public void add(final SyncInfo[] infos) {
        Job job = P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.P4ChangeSetCollector_LoadingSubmittedChangeSets;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(null, infos.length);
                Map<P4SubmittedChangeSet, List<SyncInfo>> collected = loadSets(
                        infos, monitor);
                monitor.done();
                updateSets(collected);
            }

        });
        if (job != null) {
            try {
                job.join();
            } catch (InterruptedException e) {
                // Igonore
            }
        }
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ChangeSetManager#initializeSets()
     */
    @Override
    protected void initializeSets() {
        // Does nothing
    }

    /**
     * @see org.eclipse.team.internal.core.subscribers.ChangeSetManager#dispose()
     */
    @Override
    public void dispose() {
        ISynchronizePageConfiguration configuration = getConfiguration();
        if (configuration != null) {
            configuration.removeActionContribution(this.actions);
        }
        super.dispose();
        for (ConnectionSet set : this.connectionSets.values()) {
            set.dispose();
        }
        this.connectionSets.clear();
    }

}
