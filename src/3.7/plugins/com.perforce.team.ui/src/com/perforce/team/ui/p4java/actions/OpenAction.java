/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.OpenDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class OpenAction extends P4Action {

    /**
     * Was the dialog cancelled or were no items selected from the table?
     */
    protected boolean wasDialogCancelled = false;

    /**
     * Get the dialog title to show when the pref to open with dialog is set
     * 
     * @return - open dialog title
     */
    public abstract String getDialogTitle();

    /**
     * Get the combo title to show next to the changelist combo
     * 
     * @return - combo title
     */
    public abstract String getComboTitle();

    /**
     * Was the changelist selection dialog cancelled or were no items checked?
     * 
     * @return - true if the changelist selection dialog was cancelled or no
     *         items were checked
     */
    public boolean wasDialogCancelled() {
        return this.wasDialogCancelled;
    }

    /**
     * Should the specified file be show in the dialog and be part of the adding
     * being run?
     * 
     * @param file
     * @return - true if valid file
     */
    protected abstract boolean isValidFile(IP4File file);

    /**
     * Get job title when looking for files to open
     * 
     * @return - non-null string for job title
     */
    protected abstract String getJobTitle();

    /**
     * Get default pending changelist description when new option is selected
     * 
     * @return - non-null default description
     */
    protected abstract String getDefaultDescription();

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return getJobTitle();
            }

            @Override
            public void run(IProgressMonitor monitor) {
                final P4Collection collection = getFileSelection();
                boolean showDialog = showDialog();
                Map<IP4Connection, List<IP4Resource>> connections = new HashMap<IP4Connection, List<IP4Resource>>();
                for (IP4Resource resource : collection.members()) {
                    boolean valid = true;
                    // Only validate files if showDialog is true
                    if (showDialog) {
                        if (resource instanceof IP4File) {
                            valid = isValidFile((IP4File) resource);
                        } else {
                            valid = true;
                        }
                    }
                    if (valid) {
                        IP4Connection connection = resource.getConnection();
                        if (connection != null) {
                            List<IP4Resource> resources = connections
                                    .get(connection);
                            if (resources == null) {
                                resources = new ArrayList<IP4Resource>();
                                connections.put(connection, resources);
                            }
                            resources.add(resource);
                        }
                    }
                }
                for (Map.Entry<IP4Connection, List<IP4Resource>> entry: connections.entrySet()) {
                	final IP4Connection connection =entry.getKey();
                	final List<IP4Resource> resources = entry.getValue();
                    IP4PendingChangelist active = connection
                            .getActivePendingChangelist();

                    if (showDialog && active == null) {
                        // The current thread may not be the UI-thread if this
                        // is open request if coming from a refactoring
                        // operation so run the dialog on the UI-thread if
                        // needed
                        Runnable dialogRunnable = new Runnable() {

                            public void run() {
                                OpenDialog dialog = new OpenDialog(
                                        P4UIUtils.getDialogShell(),
                                        resources.toArray(new IP4Resource[0]),
                                        connection, getDialogTitle(),
                                        getComboTitle(),
                                        getDefaultDescription());
                                if (OpenDialog.OK == dialog.open()) {
                                    IP4Resource[] selected = dialog
                                            .getSelectedFiles();
                                    if (selected.length > 0) {
                                        P4Collection subCollection = createCollection(selected);
                                        subCollection.setType(collection
                                                .getType());
                                        int listId = dialog
                                                .getSelectedChangeId();
                                        String description = dialog
                                                .getDescription();
                                        boolean useSelected = dialog
                                                .useSelected();
                                        runModifyAction(listId, description,
                                                subCollection, useSelected);
                                    } else {
                                        wasDialogCancelled = true;
                                    }
                                } else {
                                    wasDialogCancelled = true;
                                }
                            }
                        };
                        if (PerforceUIPlugin.isUIThread()) {
                            dialogRunnable.run();
                        } else {
                            PerforceUIPlugin.syncExec(dialogRunnable);
                        }
                    } else if (active != null) {
                        P4Collection subCollection = new P4Collection(
                                resources.toArray(new IP4Resource[resources
                                        .size()]));
                        runModifyAction(active.getId(), subCollection);
                    } else {
                        P4Collection subCollection = new P4Collection(
                                resources.toArray(new IP4Resource[resources
                                        .size()]));
                        runModifyAction(0, subCollection);
                    }

                }
            }

        };
        runRunnable(runnable);
    }

    /**
     * Runs an open action for the specified changelist and collection of p4
     * resources
     * 
     * @param changelist
     * @param collection
     */
    protected void runModifyAction(int changelist, P4Collection collection) {
        runModifyAction(changelist, null, collection);
    }

    /**
     * Runs an open action for the specified changelist and collection of p4
     * resources
     * 
     * @param changelist
     * @param description
     * @param collection
     */
    protected void runModifyAction(int changelist, String description,
            P4Collection collection) {
        runModifyAction(changelist, description, collection, false);
    }

    /**
     * Runs an open action for the specified changelist and collection of p4
     * resources
     * 
     * @param changelist
     * @param description
     * @param collection
     * @param setActive
     */
    protected abstract void runModifyAction(int changelist, String description,
            P4Collection collection, boolean setActive);

    /**
     * Is the show dialog preference set?
     * 
     * @return - true if show dialog on open pref is set, false otherwise
     */
    protected boolean showDialog() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_OPEN_DEFAULT);
    }

}
