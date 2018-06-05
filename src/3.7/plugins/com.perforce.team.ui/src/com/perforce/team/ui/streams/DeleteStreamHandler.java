package com.perforce.team.ui.streams;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.client.IClientSummary;
import com.perforce.p4java.option.server.GetClientsOptions;
import com.perforce.p4java.option.server.StreamOptions;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.PerforceUIPlugin;

public class DeleteStreamHandler extends AbstractStreamCommandHandler implements
		IHandler {

	final String OP = Messages.Stream_Delete;

	public Object execute(ExecutionEvent event) throws ExecutionException {

		final IP4Stream stream = getSelectedStream();
		if (stream != null) {

			if (checkAndPromptActiveProjects(stream, OP)) {
				return null;
			}

			final String deleteStream=MessageFormat.format(Messages.DeleteStreamHandler_Delete_stream, stream.getStreamSummary().getStream());
	        P4Runner.schedule(new P4Runnable() {

	            @Override
	            public String getTitle() {
	                return deleteStream;
	            }

	            @Override
	            public void run(IProgressMonitor monitor) {
	                GetClientsOptions opts = new GetClientsOptions();
	                opts.setStream(stream.getStreamSummary().getStream());
	                final List<IClientSummary> clients = stream.getConnection().getClients(opts);
	                
	                PerforceUIPlugin.syncExec(new Runnable() {

	                    public void run() {
	            			if(!clients.isEmpty()){
	            				String pattern = Messages.DeleteStreamHandler_Active_clients;
	            				StringBuilder sb=new StringBuilder(MessageFormat.format(pattern, clients.size()));
	            				for(IClientSummary c: clients){
	            					sb.append(c.getName());
	            					sb.append("("); //$NON-NLS-1$
	            					sb.append(c.getRoot());
	            					sb.append(")"); //$NON-NLS-1$
	            					sb.append("\n"); //$NON-NLS-1$
	            				}
	            				MessageDialog.openWarning(null, deleteStream, sb.toString());
	            				return;
	            			}
	            			
	            			boolean ok = MessageDialog.openQuestion(null, OP, MessageFormat
	            					.format(Messages.StreamOperation_Confirmation,
	            							OP.toLowerCase(), stream.getName()));
	            			if (ok) {
	            				stream.getConnection().deleteStream(
	            						stream.getStreamSummary().getStream(),
	            						new StreamOptions());
	            				
	            				StreamsView.showView().refresh(true, true);
	            			}
	                    }
	                });
	            }

	        });
		}
		return null;
	}

}
