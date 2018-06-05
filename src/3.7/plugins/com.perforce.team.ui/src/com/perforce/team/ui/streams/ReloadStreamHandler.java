package com.perforce.team.ui.streams;

import java.text.MessageFormat;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.option.server.ReloadOptions;
import com.perforce.team.core.p4java.IP4Stream;

public class ReloadStreamHandler extends AbstractStreamCommandHandler implements
		IHandler {

	final String OP = Messages.Stream_Reload;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IP4Stream stream = getSelectedStream();
		if (stream != null) {

			boolean ok = MessageDialog.openQuestion(null, OP, MessageFormat
					.format(Messages.StreamOperation_Confirmation,
							OP.toLowerCase(), stream.getName()));
			if (ok) {
				stream.getConnection().reloadStream(
						stream.getStreamSummary().getStream(),
						new ReloadOptions());
				
				StreamsView.showView().refresh(true, true);
			}
		}
		return null;
	}

}
