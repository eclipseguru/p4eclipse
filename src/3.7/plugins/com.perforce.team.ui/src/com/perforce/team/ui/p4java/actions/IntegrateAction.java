/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;

import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.dialogs.IntegrateDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = false;
        if (containsOnlineConnection()) {
            enabled = this.getSelection() != null && this.getSelection().size() == 1;
            if (enabled && !containsContainers()) {
                IP4Resource[] resources = getResourceSelection().members();
                if (resources.length == 1) {
                    if (resources[0] instanceof IP4Connection
                            || resources[0] instanceof IP4SubmittedChangelist) {
                        enabled = true;
                    } else if (resources[0] instanceof IP4File) {
                        IP4File resource = (IP4File) resources[0];
                        enabled = resource.getHeadRevision() > 0;
                    }
                } else {
                    enabled = false;
                }
            }
        }
        return enabled;
    }

    /**
     * Integrates the specified source and target for the connection found as
     * the first and only element in the collection returned from
     * {@link #getResourceSelection()}
     * 
     * @param source
     * @param target
     * @param start
     * @param end
     * @param changelist
     * @param options
     */
    public void integrate(String source, String target, String start,
            String end, int changelist, P4IntegrationOptions options) {
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1) {
            P4FileIntegration integration = new P4FileIntegration();
            integration.setTarget(target);
            integration.setSource(source);
            integration.setEnd(end);
            integration.setStart(start);
            final IP4Connection connection = members[0].getConnection();
            integrate(connection, integration, changelist, options);
        }
    }

    /**
     * Integrates the specified source and target for the connection specified
     * 
     * @param connection
     * @param integration
     * @param changelist
     * @param options
     */
    public void integrate(IP4Connection connection,
            P4FileIntegration integration, int changelist,
            P4IntegrationOptions options) {
        integrate(connection, integration, null, changelist, options);
    }

    /**
     * Integrates the specified source and target for the connection specified
     * 
     * @param connection
     * @param integration
     * @param changelist
     * @param branch
     * @param options
     */
    public void integrate(final IP4Connection connection,
            final P4FileIntegration integration, final String branch,
            final int changelist, final P4IntegrationOptions options) {
        integrate(connection, integration, branch, changelist, null, options);
    }

    /**
     * Integrates the specified source and target for the connection specified
     * 
     * @param connection
     * @param integration
     * @param changelist
     * @param branch
     * @param description
     * @param options
     */
    public void integrate(final IP4Connection connection,
            final P4FileIntegration integration, final String branch,
            final int changelist, final String description,
            final P4IntegrationOptions options) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                IP4Resource[] integrated = connection.integrate(integration,
                        branch, changelist, description, false, false, options);
                if (integrated!=null && integrated.length > 0) {
                    P4Collection collection = createCollection(integrated);
                    collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                }
            }

            @Override
            public String getTitle() {
                return Messages.IntegrateAction_IntegratingFiles;
            }

        };
        runRunnable(runnable);
    }

    /**
     * Integrate a submitted changelist
     * 
     * @param changelist
     */
    public void integrate(IP4SubmittedChangelist changelist) {
        integrateResource(changelist);
    }

    /**
     * Run an integrate action with specified connection and initial source and
     * target paths
     * 
     * @param resource
     * @param source
     * @param target
     * @param limit
     */
    public void integratePaths(IP4Resource resource, String source,
            String target, IP4SubmittedChangelist limit) {
        if (resource != null) {
            IntegrateDialog dialog = new IntegrateDialog(getShell(),
                    resource.getConnection(), source, target);
            dialog.setDefaultLimit(limit);
            if (dialog.open() == IntegrateDialog.OK) {
                processDialog(dialog, resource);
            }
        }
    }

    /**
     * Run an integrate action with specified connection and initial source and
     * target paths
     * 
     * @param resource
     * @param source
     * @param target
     */
    public void integratePaths(IP4Resource resource, String source,
            String target) {
        integratePaths(resource, source, target, null);
    }

    /**
     * Integrate the specified branch
     * 
     * @param branch
     * @param reverse
     * @param limit
     */
    public void integrateBranch(IP4Branch branch, boolean reverse,
            IP4SubmittedChangelist limit) {
        if (branch != null) {
            IntegrateDialog dialog = new IntegrateDialog(getShell(), branch);
            dialog.setDefaultLimit(limit);
            if (reverse) {
//                P4IntegrationOptions options = new P4IntegrationOptions();
            	P4IntegrationOptions options = P4IntegrationOptions.createInstance(branch.getServer());
                options.setReverseMapping(true);
                dialog.setDefaultOptions(options);
            }
            if (IntegrateDialog.OK == dialog.open()) {
                processDialog(dialog, branch);
            }
        }
    }

    /**
     * Integrate the specified branch
     * 
     * @param branch
     * @param reverse
     */
    public void integrateBranch(IP4Branch branch, boolean reverse) {
        integrateBranch(branch, reverse, null);
    }

    private void processDialog(IntegrateDialog dialog, IP4Resource resource) {
        P4FileIntegration integration = dialog.getIntegration();
        String branch = dialog.getBranch();
        int changelist = dialog.getChangelist();
        String description = dialog.getDescription();
        P4IntegrationOptions options = dialog.getSelectedOptions();
        integrate(resource.getConnection(), integration, branch, changelist,
                description, options);
    }

    private void integrateResource(IP4Resource resource) {
        IntegrateDialog dialog = new IntegrateDialog(getShell(), resource);
        if (dialog.open() == IntegrateDialog.OK) {
            processDialog(dialog, resource);
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        P4Collection collection = getResourceSelection();
        IP4Resource[] members = collection.members();
        if (members.length == 1) {
            integrateResource(members[0]);
        }
    }
}
