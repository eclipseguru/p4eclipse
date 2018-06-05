package com.perforce.team.ui.streams;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.streams.wizard.EditStreamWizard;
import com.perforce.team.ui.streams.wizard.EditStreamWizardDialog;
import com.perforce.team.ui.views.DepotView;

/**
 * Handler for creating new stream spec from existing one.
 * 
 * @author ali
 *
 */
public class NewStreamFromHandler extends AbstractStreamCommandHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        EditStreamWizard wizard=null;
        
        IP4Stream stream = getSelectedStream();
        if(stream != null){
        	wizard = new EditStreamWizard(stream,false);
        }
        
        if(wizard==null){
            ISelection sel2  = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection(DepotView.VIEW_ID);
            if(sel2 instanceof StructuredSelection){
                Object element = ((StructuredSelection) sel2).getFirstElement();
                if(element instanceof IP4Resource){
                    IP4Connection conn = ((IP4Resource) element).getConnection();
                    wizard = new EditStreamWizard(conn);
                }
            }
        }
        
        if(wizard!=null){
            // Instantiates the wizard container with the wizard and opens it
            WizardDialog dialog = new EditStreamWizardDialog(P4UIUtils.getShell(), wizard);
            dialog.create();
            if(Window.OK==dialog.open()){
            	StreamsView.showView().refresh(false,false);
            }
        }
        return null;
    }

}
