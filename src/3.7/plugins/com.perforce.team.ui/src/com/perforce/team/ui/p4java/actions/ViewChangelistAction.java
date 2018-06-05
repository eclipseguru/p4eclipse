/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.p4java.core.ChangelistStatus;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ChangelistEditor;
import com.perforce.team.ui.changelists.ChangelistEditorInput;
import com.perforce.team.ui.changelists.IChangelistEditorInput;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ViewChangelistAction extends P4Action {

    /**
     * Open the specified changelist id by asynchronously loading it on a
     * background job and then opening the editor if found.
     * 
     * @param id
     * @param connection
     * @param type
     */
    public void view(final int id, final IP4Connection connection,
            final IChangelist.Type type) {
        if (connection == null || type == null || id <= IChangelist.DEFAULT) {
            return;
        }
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(
                        Messages.ViewChangelistAction_OpeningChangelist, id);
            }

            @Override
            public void run(IProgressMonitor monitor) {
                if (connection != null) {
                    ChangelistStatus status = null;
                    switch (type) {
                    case PENDING:
                    case SHELVED:
                        status = ChangelistStatus.PENDING;
                        break;
                    case SUBMITTED:
                    default:
                        status = ChangelistStatus.SUBMITTED;
                        break;
                    }
                    IP4Changelist list = connection.getChangelistById(id, null,
                            true, false);
                    if (list != null) {
                        if (status == ChangelistStatus.SUBMITTED
                                && list.getStatus() == ChangelistStatus.PENDING) {
                            showPendingError(id);
                        } else if (type == IChangelist.Type.SHELVED) {
                            viewShelved(list, id);
                        } else {
                            view(list);
                        }
                    }
                }
            }

        };
        runRunnable(runnable);
    }

    private void showPendingError(int id) {
        final String message = MessageFormat.format(
                Messages.ViewChangelistAction_ChangelistIsPendingMessage, id);
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager().openError(
                        P4UIUtils.getDialogShell(),
                        Messages.ViewChangelistAction_ChangelistIsPendingTitle,
                        message);
            }
        });
    }

    private void viewShelved(IP4Changelist list, int id) {
        boolean showError = false;
        if (list instanceof IP4PendingChangelist) {
            IP4PendingChangelist pending = (IP4PendingChangelist) list;
            IP4ShelvedChangelist shelved = pending.getShelvedChanges();
            if (shelved != null) {
                shelved.refresh();
                if (shelved.members().length > 0) {
                    view(shelved);
                } else {
                    showError = true;
                }
            } else {
                showError = true;
            }
        } else {
            showError = true;
        }
        if (showError) {
            final String message = MessageFormat.format(
                    Messages.ViewChangelistAction_NoShelvedFilesMessage, id);
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    P4ConnectionManager.getManager().openError(
                            P4UIUtils.getDialogShell(),
                            Messages.ViewChangelistAction_NoShelvedFilesTitle,
                            message);
                }
            });
        }
    }

    /**
     * Open the specified changelist id by asynchronously loading it on a
     * background job and then opening the editor if found.
     * 
     * @param id
     * @param connection
     */
    public void view(final int id, final IP4Connection connection) {
        view(id, connection, Type.SUBMITTED);
    }

    /**
     * View the specified non-null non-offline changelist
     * 
     * @param changelist
     */
    protected void view(final IP4Changelist changelist) {
    	if(changelist==null || changelist.getConnection().isOffline())
    		return;
    	
    	IP4Resource[] files = changelist.getFiles();
    	if(files==null || files.length==0){
    	    changelist.refresh();
    	    files = changelist.getFiles();
    	}
        if (files!=null 
        		&& files.length>0) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    IChangelistEditorInput input = new ChangelistEditorInput(
                            changelist);
                    try {
                        IDE.openEditor(PerforceUIPlugin.getActivePage(), input,
                                ChangelistEditor.ID);
                    } catch (PartInitException e) {
                        PerforceProviderPlugin.logError(e);
                    }
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        Object[] resources = null;
        if (this.getSelection() != null) {
            resources = this.getSelection().toArray();
        } else if (this.collection != null) {
            resources = this.collection.members();
        }
        if (resources != null) {
            for (Object resource : resources) {
                IP4Changelist list = P4CoreUtils.convert(resource,
                        IP4Changelist.class);
                if (list != null) {
                    view(list);
                } else {
                    IP4Revision revision = P4CoreUtils.convert(resource,
                            IP4Revision.class);
                    if (revision != null) {
                        view(revision.getChangelist(), revision.getConnection());
                    }
                }
            }
        }
    }

}
