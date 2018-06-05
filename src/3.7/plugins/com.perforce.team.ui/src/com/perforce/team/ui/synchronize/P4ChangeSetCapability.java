/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.synchronize.IP4ChangeSet;
import com.perforce.team.core.p4java.synchronize.P4ChangeSetManager;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.core.p4java.synchronize.P4SubmittedChangeSet;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.EditChangelistAction;
import com.perforce.team.ui.p4java.actions.NewChangelistAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSetManager;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.ChangeSetDiffNode;
import org.eclipse.team.internal.ui.synchronize.SyncInfoSetChangeSetCollector;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class P4ChangeSetCapability extends ChangeSetCapability {

    /**
     * Create a p4 change set capability
     */
    public P4ChangeSetCapability() {
    }

    /**
     * Add change set filter to specified viewer to ensure proper changesets are
     * displayed in the different supported modes.
     * 
     * This method will listen for the first mode change event and register the
     * filter then if possible.
     * 
     * @param configuration
     */
    public void addChangeSetFilter(
            final ISynchronizePageConfiguration configuration) {
        final boolean[] filterAdded = new boolean[] { false };
        configuration.addPropertyChangeListener(new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                if (!filterAdded[0]
                        && ISynchronizePageConfiguration.P_MODE.equals(event
                                .getProperty())) {
                    ISynchronizePage page = configuration.getPage();
                    if (page != null) {
                        Viewer viewer = page.getViewer();
                        if (viewer instanceof StructuredViewer) {
                            addChangeSetFilter(configuration,
                                    (StructuredViewer) viewer);
                            filterAdded[0] = true;
                            configuration.removePropertyChangeListener(this);
                        }
                    }
                }
            }
        });
    }

    /**
     * Add change set filter to specified viewer to ensure proper changesets are
     * displayed in the different supported modes.
     * 
     * @param configuration
     * @param viewer
     */
    public void addChangeSetFilter(
            final ISynchronizePageConfiguration configuration,
            StructuredViewer viewer) {
        if (configuration != null && P4UIUtils.okToUse(viewer)) {
            viewer.addFilter(new ViewerFilter() {

                @Override
                public boolean select(Viewer viewer, Object parentElement,
                        Object element) {
                    if (element instanceof ChangeSetDiffNode) {
                        IP4ChangeSet set = P4CoreUtils.convert(element,
                                IP4ChangeSet.class);
                        if (set != null) {
                            // Don't allow submitted change sets to be displayed
                            // in outgoing mode
                            if (configuration.getMode() == ISynchronizePageConfiguration.OUTGOING_MODE
                                    && set instanceof P4SubmittedChangeSet) {
                                return false;
                            }

                            // Don't allow pending change sets to be displayed
                            // in incoming mode
                            if (configuration.getMode() == ISynchronizePageConfiguration.INCOMING_MODE
                                    && set instanceof P4PendingChangeSet) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            });
        }
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#editChangeSet(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration,
     *      org.eclipse.team.internal.core.subscribers.ActiveChangeSet)
     */
    @Override
    public void editChangeSet(ISynchronizePageConfiguration configuration,
            ActiveChangeSet set) {
        if (set instanceof P4PendingChangeSet) {
            IP4PendingChangelist list = ((P4PendingChangeSet) set)
                    .getChangelist();
            if (list != null) {
                EditChangelistAction action = new EditChangelistAction();
                action.selectionChanged(null, new StructuredSelection(list));
                action.run(null);
            }
        }
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#getActiveChangeSetManager()
     */
    @Override
    public ActiveChangeSetManager getActiveChangeSetManager() {
        return P4ChangeSetManager.getChangeSetManager();
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#createSyncInfoSetChangeSetCollector(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public SyncInfoSetChangeSetCollector createSyncInfoSetChangeSetCollector(
            ISynchronizePageConfiguration configuration) {
        return new P4ChangeSetCollector(configuration);
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#supportsActiveChangeSets()
     */
    @Override
    public boolean supportsActiveChangeSets() {
        return true;
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#supportsCheckedInChangeSets()
     */
    @Override
    public boolean supportsCheckedInChangeSets() {
        return true;
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#enableChangeSetsByDefault()
     */
    @Override
    public boolean enableChangeSetsByDefault() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SHOW_CHANGE_SETS);
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#enableCheckedInChangeSetsFor(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public boolean enableCheckedInChangeSetsFor(
            ISynchronizePageConfiguration configuration) {
        return supportsCheckedInChangeSets()
                && configuration.getMode() != ISynchronizePageConfiguration.OUTGOING_MODE;
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#enableActiveChangeSetsFor(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration)
     */
    @Override
    public boolean enableActiveChangeSetsFor(
            ISynchronizePageConfiguration configuration) {
        return supportsActiveChangeSets()
                && configuration.getMode() != ISynchronizePageConfiguration.INCOMING_MODE;
    }

    /**
     * @see org.eclipse.team.internal.ui.synchronize.ChangeSetCapability#createChangeSet(org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration,
     *      org.eclipse.team.core.diff.IDiff[])
     */
    @Override
    public ActiveChangeSet createChangeSet(
            ISynchronizePageConfiguration configuration, IDiff[] diffs) {
        if (diffs != null && diffs.length > 0) {
            List<IResource> resources = new ArrayList<IResource>();
            for (IDiff diff : diffs) {
                IResource resource = ResourceDiffTree.getResourceFor(diff);
                if (resource != null) {
                    resources.add(resource);
                }
            }
            if (!resources.isEmpty()) {
                NewChangelistAction action = new NewChangelistAction();
                action.selectionChanged(null,
                        new StructuredSelection(resources));
                action.create();
            }
        }
        return null;
    }

}
