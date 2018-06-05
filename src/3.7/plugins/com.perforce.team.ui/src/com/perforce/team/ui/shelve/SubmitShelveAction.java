/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IExtendedFileSpec;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.p4java.impl.mapbased.server.Server;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.Messages;
import com.perforce.team.ui.p4java.actions.P4Action;

public class SubmitShelveAction extends P4Action {

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    public boolean isEnabledEx() {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = getResourceSelection();
                for (IP4Resource resource : collection.members()) {
                    if (resource instanceof IP4ShelvedChangelist) {
                    	IP4ShelvedChangelist list = (IP4ShelvedChangelist) resource;
                        if (!list.isReadOnly() && list.getFiles().length > 0) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    private void submitListsFromCollection(final P4Collection collection,
            final boolean showDialog) {
        IP4Runnable runnable = new P4Runnable() {

			@Override
			public void run(IProgressMonitor monitor) {

				List<IP4ShelvedChangelist> changes = new ArrayList<IP4ShelvedChangelist>();
				List<IExtendedFileSpec> nonSubmittables=new ArrayList<IExtendedFileSpec>();
				for (IP4Resource resource : collection.members()) {
					if (resource instanceof IP4ShelvedChangelist) {
						IP4ShelvedChangelist sl = (IP4ShelvedChangelist) resource;
						List<IExtendedFileSpec> openedFiles = sl.getOpenedSpecs();
						nonSubmittables.addAll(openedFiles);
						IP4ShelvedChangelist list = (IP4ShelvedChangelist) resource;
						changes.add(list);

						if(nonSubmittables.isEmpty())
							showSubmitDialogs(changes, showDialog);
						else{
							final StringBuilder sb=new StringBuilder();
							sb.append(MessageFormat.format(Messages.SubmitShelveAction_ChangelistContainsNonShelvedFiles,IConstants.EMPTY_STRING+sl.getId()));
							if(nonSubmittables.size()<20){
								sb.append(Messages.SubmitShelveAction_MoveFollowingFIlesToAnotherChangelist);
								for(IExtendedFileSpec s: nonSubmittables){
									sb.append("  "); //$NON-NLS-1$
									sb.append(s.getDepotPathString());
									sb.append(IConstants.RETURN);
								}
							}else{
								sb.append(Messages.SubmitShelveAction_MoveFilesToAnotherCHangelist);
							}
							PerforceUIPlugin.syncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(getShell(), getTitle(), sb.toString());
								}
							});
						}
					}
				}
				
			}

            @Override
            public String getTitle() {
                return com.perforce.team.ui.p4java.actions.Messages.SubmitAction_SubmittingChangelistTitle;
            }
        };
        runRunnable(runnable);
    }

    private void showSubmitDialogs(
            final List<IP4ShelvedChangelist> lists,
            final boolean showDialog) {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (!lists.isEmpty()) {
                    for (final IP4ShelvedChangelist list : lists) {
                        if (showDialog) {
                        	SubmitShelveDialog dialog = new SubmitShelveDialog(getShell(), MessageFormat.format(Messages.SubmitShelveAction_Submit_shelved_change,list.getId()), list);
                        	if(Window.OK==dialog.open()){
                        		 submit(list);
                        	}
                        } else {
                        	submit(list);
                        }
                    }
                } else {
                    P4ConnectionManager.getManager().openInformation(
                            getShell(),
                            com.perforce.team.ui.p4java.actions.Messages.SubmitAction_NoFilesToSubmitTitle,
                            com.perforce.team.ui.p4java.actions.Messages.SubmitAction_NoFilesToSubmitMessage);
                }
            }
        });
    }

    /**
     * Runs the submit and optionally shows it as a dialog or just submits with
     * the current collection and settings
     * 
     * @param showDialog
     */
    public void runAction(boolean showDialog) {
        P4Collection collection = getResourceSelection();
        submitListsFromCollection(collection, showDialog);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        runAction(true);
    }
    
    private void submit(IP4ShelvedChangelist changelist){
    	IP4Connection connection = changelist.getConnection();
    	Server server=(Server) connection.getServer();
    	List<Map<String, Object>> retMaps = connection.execMapCmdList(CmdSpec.SUBMIT.name(), new String[]{"-e"+changelist.getId()}, new HashMap<String, Object>()); //$NON-NLS-1$
    	try {
			List<IFileSpec> fileList = new ArrayList<IFileSpec>();
						
			final String SUBMITTED_MSG="Submitted as change "; //$NON-NLS-1$
			final String KEY_SUBMITTEDCHANGE="submittedChange";//$NON-NLS-1$
			final String KEY_LOCK="locked"; //$NON-NLS-1$
			if (retMaps != null) {
				for (Map<String, Object> map : retMaps) {
					if (map.get(KEY_SUBMITTEDCHANGE) != null) { 
						Integer id = new Integer((String) map.get(KEY_SUBMITTEDCHANGE));
						fileList.add(new FileSpec(FileSpecOpStatus.INFO,
								SUBMITTED_MSG + id));
					} else if (map.get(KEY_LOCK) != null) { //$NON-NLS-1$
						// disregard this message for now
					} else {
						fileList.add(server.handleFileReturn(map));
					}
				}
			}
			
            for (IFileSpec spec : fileList) {
                if (spec.getStatusMessage()!=null && !spec.getStatusMessage().contains(SUBMITTED_MSG)) {
                	MessageDialog.openError(getShell(), Messages.SubmitShelveAction_Error, spec.getStatusMessage());
                	break;
                }else{
                	updateChanges(changelist);
                }
            }
		} catch (P4JavaException e) {
			e.printStackTrace();
		}
    }

	private void updateChanges(IP4ShelvedChangelist changelist) {
		changelist.refresh();
        // Send update shelve event
        P4Workspace.getWorkspace().notifyListeners(
                new P4Event(EventType.SUBMIT_SHELVEDCHANGELIST,
                        changelist));
	}
}
