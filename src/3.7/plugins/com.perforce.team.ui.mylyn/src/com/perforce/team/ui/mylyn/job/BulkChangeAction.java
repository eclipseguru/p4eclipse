/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkChangeAction extends P4Action {

    private Map<IP4Connection, Set<IJobProxy>> generateConnectionMap() {
        Map<IP4Connection, Set<IJobProxy>> connectionJobs = new HashMap<IP4Connection, Set<IJobProxy>>();
        IStructuredSelection jobSelection = this.getSelection();
        if (jobSelection != null) {
            for (Object resource : jobSelection.toArray()) {
                if (resource instanceof JobProxyContainer) {
                    JobProxyContainer container = (JobProxyContainer) resource;
                    IP4Connection connection = container.getConnection();
                    if (connection != null) {
                        if (!connectionJobs.containsKey(connection)) {
                            connectionJobs.put(connection,
                                    new HashSet<IJobProxy>());
                        }
                        Set<IJobProxy> jobs = connectionJobs.get(connection);
                        jobs.addAll(Arrays.asList(container.getJobs()));
                    }
                } else if (resource instanceof IJobProxy) {
                    IJobProxy proxy = (IJobProxy) resource;
                    IP4Connection connection = proxy.getConnection();
                    if (connection != null) {
                        Set<IJobProxy> jobs = connectionJobs.get(connection);
                        if (jobs == null) {
                            jobs = new HashSet<IJobProxy>();
                            connectionJobs.put(connection, jobs);
                        }
                        jobs.add(proxy);
                    }
                }
            }
        }
        return connectionJobs;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        if (this.getSelection() == null || this.getSelection().isEmpty()) {
            return;
        }
        Map<IP4Connection, Set<IJobProxy>> connectionJobs = generateConnectionMap();
        for (Map.Entry<IP4Connection, Set<IJobProxy>> entry : connectionJobs.entrySet()) {
        	final IP4Connection connection=entry.getKey();
            final Set<IJobProxy> jobs = entry.getValue();
            
            if(connection.getJobSpec()==null 
            		|| connection.getJobs(1).length!=1){
            	MessageDialog.openWarning(null, Messages.BulkJobChangeAction_Warning, Messages.BulkJobChangeAction_WarningDetail);
            	return;
            }
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    try {
                        BulkJobInput input = null;
                        if (jobs == null) {
                            input = new BulkJobInput(connection);
                        } else {
                            input = new BulkJobInput(connection, jobs
                                    .toArray(new IJobProxy[jobs.size()]));
                        }
                        IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                                BulkJobEditor.ID);
                    } catch (PartInitException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            });
        }
    }
}
