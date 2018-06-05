/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.P4ConnectionManager;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditAction extends OpenAction {

	
    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() throws TeamException {
        boolean enabled = containsOnlineConnection();
        if (enabled) {
            enabled = containsContainers();
            if (!enabled) {
                P4Collection collection = super.getResourceSelection();
                IP4Resource[] resources = collection.members();
                int size = resources.length;
                if (size > 0) {
                    for (IP4Resource resource : resources) {
                        if (resource instanceof IP4File) {
                            IP4File file = (IP4File) resource;
                            if (isValidFile(file)) {
                                enabled = true;
                                break;
                            }
                        } else {
                            enabled = true;
                            break;
                        }
                    }
                }
            }
        }
        return enabled;
    }

    private void edit(final P4Collection collection, final int changelist,
            final String description, final boolean setActive) {
    	    
    	final boolean[] doNotEdit = new boolean [1];
    	doNotEdit[0] = true;
    	Display display = Display.getDefault();
    	display.syncExec (new Runnable () {
      
    		public void run () {      	    	  
    	    boolean unsyncedFiles = false;        	     	 
    	    IP4Resource[] resources = collection.members();
            int size = resources.length;
            if (size > 0) {
              for (IP4Resource resource : resources) {
                  if (resource instanceof IP4File) {
                      IP4File file = (IP4File) resource;
                      if (!file.isSynced())
                     	 unsyncedFiles = true;
                  }
              }
              
              if (unsyncedFiles == false)
            	  doNotEdit[0] = false;
          }
                    
          if (unsyncedFiles)          
          {        	
        	  String [] dialogButtonLabels = {Messages.EditAction_GetLatest, Messages.EditAction_DontGetLatest, Messages.EditAction_Cancel};
        	  int buttonId=1; // default: don't get latest. This is matching test cases
        	  if(!P4ConnectionManager.getManager().isSuppressErrors()){
	        	  MessageDialog dlg = new MessageDialog(getShell(), 
	        			  								Messages.EditAction_CheckOut, 
	        			  								null, 
	        			  								Messages.EditAction_UnsyncedFiles, 
	        			  								MessageDialog.WARNING, 
	        			  								dialogButtonLabels, 
	        			  								0);           	        	 
	        	  buttonId = dlg.open();
        	  }
        	  if (buttonId == 0)
        		  {
        		  	doNotEdit[0] = false;
        		  	SyncAction action = new SyncAction();       	        		 
        		  	action.setAsync(false);
        		  	action.setCollection(getResourceSelection());
        		  	action.runAction();
        		  }  
        	  else if (buttonId == 1)
        	  {
        		  doNotEdit[0] = false;
        	  }
        	  else if (buttonId == 2)
        	  {
        		  doNotEdit[0] = true;       		     	        	  
        		  return;
        	  }
          }
   }});

    	if (doNotEdit[0] == true)        		
    		return;
    	
        IP4Runnable runnable = new P4Runnable() {

            @Override
            public String getTitle() {
                return getJobTitle();
            }

            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(getTitle(), 1);
                monitor.subTask(generateTitle(null, collection));
                collection.edit(changelist, description, setActive);
                monitor.worked(1);
                monitor.done();

                collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                updateActionState();
            }

        };
        runRunnable(runnable);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#runModifyAction(int,
     *      java.lang.String, com.perforce.team.core.p4java.P4Collection,
     *      boolean)
     */
    @Override
    protected void runModifyAction(int changelist, String description,
            P4Collection collection, boolean setActive) {
        edit(collection, changelist, description, setActive);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getComboTitle()
     */
    @Override
    public String getComboTitle() {
        return Messages.EditAction_OpenInChangelist;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDialogTitle()
     */
    @Override
    public String getDialogTitle() {
        return Messages.EditAction_CheckOut;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#isValidFile(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected boolean isValidFile(IP4File file) {
        return file.getP4JFile() != null && !file.isOpened();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getJobTitle()
     */
    @Override
    protected String getJobTitle() {
        return Messages.EditAction_CheckingOut;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.OpenAction#getDefaultDescription()
     */
    @Override
    protected String getDefaultDescription() {
        return P4Collection.EDIT_DEFAULT_DESCRIPTION;
    }
}
