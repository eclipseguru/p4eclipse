package com.perforce.team.ui.streams;

import java.text.MessageFormat;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;

import com.perforce.p4java.client.IClient;
import com.perforce.team.core.IConstants;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.ui.P4ConnectionManager;

public abstract class AbstractStreamCommandHandler extends AbstractHandler implements IHandler {

	protected IP4Stream getSelectedStream(){
        ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        IP4Stream stream;
        if(selection instanceof StructuredSelection){
            Object element = ((StructuredSelection) selection).getFirstElement();
            if(element instanceof IP4Stream){
                stream =(IP4Stream) element;
                return stream;
            }
        }
        return null;
	}

	/**
	 * Verify there is active projects associated with given stream, and prompt user to remove them.
	 * <p/>
	 * This must be run within UI thread.
	 * 
	 * @param stream
	 * @param title
	 * @return true if there are active projects associated with the given stream. false otherwise.
	 */
	protected boolean checkAndPromptActiveProjects(IP4Stream stream,
			String title) {
		String streamPath = stream.getStreamSummary().getStream();
		IP4Connection[] connections = P4ConnectionManager.getManager()
				.getConnections();
		StringBuilder sb = new StringBuilder();
		int total=0;
		
		for (IP4Connection conn : connections) {
			if(!conn.isConnected()) 
				continue;
			IClient c = conn.getClient();
			String cs = c.getStream();
			if (streamPath.equals(cs)) {
				IProject[] projects = conn.getMappedProjects();
		    	if(projects.length>1){
		    		for(IProject prj:projects){
		    			total++;
		    			sb.append(IConstants.SPACE);
		    			sb.append(prj.getName());
		    			sb.append(IConstants.RETURN);
		    		}
		    	}
			}
		}
		if(total>50){
    		MessageDialog.openInformation(null, title, MessageFormat.format(Messages.Stream_ActiveProjects,total,IConstants.EMPTY_STRING));
    		return true;
		}else if(total>0){
    		MessageDialog.openInformation(null, title, MessageFormat.format(Messages.Stream_ActiveProjects,total,sb.toString()));
    		return true;
    	}
		return false;
	}

	protected boolean ignoreActiveConnections(IP4Stream stream,
			String op) {
		String streamPath = stream.getStreamSummary().getStream();
		IP4Connection[] connections = P4ConnectionManager.getManager()
				.getConnections();
		StringBuilder sb = new StringBuilder();

		for (IP4Connection conn : connections) {
			IClient c = conn.getClient();
			String cs = c.getStream();
			if (streamPath.equals(cs)) {
    			sb.append(IConstants.SPACE);
				sb.append(conn.getName());
				sb.append(IConstants.RETURN);
			}
		}
		if (sb.length() > 0) {
			sb.deleteCharAt(sb.length() - 1);

			boolean ignoreAndContinue = MessageDialog.openQuestion(null, op, MessageFormat
					.format(Messages.StreamOperation_CurrentClientConfirmation,
							stream.getStreamSummary().getStream(),
							sb.toString()));
			return ignoreAndContinue;
		}
		return true;

	}

}
