package com.perforce.team.ui.streams;

import org.eclipse.jface.wizard.WizardDialog;

import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.p4java.actions.P4DoubleClickAction;
import com.perforce.team.ui.streams.wizard.EditStreamWizard;
import com.perforce.team.ui.streams.wizard.EditStreamWizardDialog;

/**
 * Handler for edit stream spec command in Streams view
 * 
 * @author ali
 *
 */
public class EditStreamAction extends P4DoubleClickAction {

	@Override
	protected void runAction() {
	    if(this.getSelection()!=null && !this.getSelection().isEmpty()){
	        IP4Stream stream=(IP4Stream) getSelection().getFirstElement();
	        if(stream!=null){
    	        EditStreamWizard wizard = new EditStreamWizard(stream,true);
    	        // Instantiates the wizard container with the wizard and opens it
    	        WizardDialog dialog = new EditStreamWizardDialog(getShell(), wizard);
    	        dialog.create();
    	        dialog.open();
	        }
	    }
	}

}
