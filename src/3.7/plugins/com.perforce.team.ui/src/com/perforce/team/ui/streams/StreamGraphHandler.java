package com.perforce.team.ui.streams;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4v.StreamsRunner;
import com.perforce.team.ui.views.DepotView;

/**
 * Hander for showing stream graph in p4v.
 * @author ali
 *
 */
public class StreamGraphHandler extends AbstractHandler implements IHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
    	ISelection sel=PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        IP4Connection connection = null;
        if(sel instanceof IStructuredSelection){
            Object obj = ((IStructuredSelection) sel).getFirstElement();
            if(obj instanceof IP4Resource){
                connection = ((IP4Resource) obj).getConnection();
            }else{
                StreamsViewControl viewer = StreamsView.showView().getPerforceViewControl();
                if(viewer!=null){
                    connection=viewer.getConnection();
                }
            }
        }

        if(connection!=null){
            StreamsRunner runner = new StreamsRunner(connection);
            runner.run();
        }else{
        	MessageDialog.openInformation(P4UIUtils.getShell(), "P4V", "Please select a Perforce connection or object first.");
        }

        return null;
    }
    
}
