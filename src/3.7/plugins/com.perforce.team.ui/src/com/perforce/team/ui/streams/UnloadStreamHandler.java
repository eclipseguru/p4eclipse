package com.perforce.team.ui.streams;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.option.server.UnloadOptions;
import com.perforce.team.core.p4java.IP4Stream;

public class UnloadStreamHandler extends AbstractStreamCommandHandler implements
		IHandler {

	final String OP = Messages.Stream_Unload;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IP4Stream stream = getSelectedStream();
		if (stream != null) {
			if (!ignoreActiveConnections(stream, OP)) { // check clients who are using this stream
				return null;
			}
			if (checkAndPromptActiveProjects(stream, OP)) {
				return null;
			}

			boolean ok = MessageDialog.openQuestion(null, OP, MessageFormat
					.format(Messages.StreamOperation_Confirmation,
							OP.toLowerCase(), stream.getName()));
			if (ok) {
				stream.getConnection().unloadStream(
						stream.getStreamSummary().getStream(),
						new UnloadOptions());
				
				StreamsView.showView().refresh(true, true);
			}
		}
		return null;
	}

}
