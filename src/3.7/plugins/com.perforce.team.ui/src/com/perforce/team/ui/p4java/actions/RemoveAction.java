/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.actions.RefreshAction;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Folder;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.SWTUtils;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class RemoveAction extends P4Action {
	
	static class RemoveFromWorkspaceDialog extends MessageDialog {

		private Button noPromptButton;

		RemoveFromWorkspaceDialog(Shell parentShell, P4Collection collection) {
			super(parentShell, Messages.RemoveAction_RemoveDialogTitle, null, // accept the
					// default window
					// icon
					getMessage(collection), MessageDialog.QUESTION, new String[] {
							IDialogConstants.YES_LABEL,
							IDialogConstants.NO_LABEL }, 0); // yes is the
			setShellStyle(getShellStyle() | SWT.SHEET);
		}

		static String getMessage(P4Collection collection) {
			IP4Resource[] folders = collection.members();
			if (folders.length == 1) {
				return MessageFormat.format(Messages.RemoveAction_RemoveSingleFolder, folders[0].getName());
			}
			return MessageFormat.format(Messages.RemoveAction_RemoveMultipleFolders, new Integer(folders.length));
		}

		/*
		 * (non-Javadoc) Method declared on Window.
		 */
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
		}

		protected Control createCustomArea(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayout(new GridLayout());
			noPromptButton = SWTUtils.createCheckBox(parent, Messages.RemoveAction_NotPromptAgain);
			noPromptButton.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setNeedConfirm(!noPromptButton.getSelection());
				}
			});
			return composite;
		}
	}

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        final P4Collection collection = getResourceSelection();
        if (!collection.isEmpty()) {
        	if(isNeedConfirm()){
        		RemoveFromWorkspaceDialog dlg = new RemoveFromWorkspaceDialog(null, collection);
        		if(Window.OK==dlg.open())
        			sync(collection);
        	}else{
        		sync(collection);
        	}
        }
    }

    private void sync(final P4Collection collection) {
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.RemoveAction_RemovingFromWorkspace;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                collection.sync("#0", monitor); //$NON-NLS-1$
                collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                updateActionState();
                
                boolean needRefresh=false;
                IStructuredSelection selectionToRefresh = RemoveAction.this.getSelection();
                Object[] selected = RemoveAction.this.getSelection().toArray();
                for (Object select : selected) {
                    if (select instanceof IProject) {
                      IProject prj=(IProject) select;
                      if(prj!=null && prj.getLocation()!=null && prj.getLocation().toFile()!=null){
	                      if(!prj.getLocation().toFile().exists()){
	                        needRefresh=true;
	                        break;
	                      }
                      }
                    }else if(select instanceof P4Folder){
                        String localPath = ((P4Folder)select).getLocalPath();
                        IProject[] prjs = ResourcesPlugin.getWorkspace().getRoot().getProjects();
                        for(IProject prj:prjs){
                            if(prj.isOpen() && prj.getLocation().toOSString().equals(localPath)){
                                needRefresh=true;
                                selectionToRefresh=new StructuredSelection(prj);
                                break;
                            }
                        }
                    }
                }
                if(needRefresh){
                    RefreshAction refresh = new RefreshAction(new IShellProvider() {
                        public Shell getShell() {
                            return RemoveAction.this.getShell();
                        }
                    });
                    refresh.selectionChanged(selectionToRefresh);
                    refresh.refreshAll();
                }                
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = getResourceSelection();
                for (IP4Resource resource : collection.members()) {
                    if (resource.isContainer()) {
                        enabled = true;
                        break;
                    } else if (resource instanceof IP4File) {
                        IP4File file = (IP4File) resource;
                        if (file.getHaveRevision() > 0
                                && file.getAction() == null) {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;

    }

	public static boolean isNeedConfirm() {
		IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
		return store.getBoolean(IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS);
	}

	public static void setNeedConfirm(boolean needConfirm) {
		IPreferenceStore store = PerforceUIPlugin.getPlugin().getPreferenceStore();
		store.setValue(IPerforceUIConstants.PREF_PROMPT_ON_DELETING_MANAGED_FOLDERS,needConfirm);
	}

}
