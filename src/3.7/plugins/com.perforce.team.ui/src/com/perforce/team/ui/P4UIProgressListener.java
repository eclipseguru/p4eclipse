package com.perforce.team.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;

import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.CmdSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4ProgressListener;
import com.perforce.team.core.p4java.ProgressMonitorProgressPresenter;
import com.perforce.team.ui.views.ConsoleView;

public class P4UIProgressListener extends P4ProgressListener{

	public P4UIProgressListener(CmdSpec cmd, IP4Connection connection, IProgressMonitor monitor) {
		this(cmd.name().toLowerCase(), connection,monitor);
	}

	public P4UIProgressListener(String cmd, IP4Connection connection, IProgressMonitor monitor) {
		super(cmd, connection);
		ProgressMonitorProgressPresenter presenter = new ProgressMonitorProgressPresenter();
		presenter.setMonitor(monitor);
		setPresenter(presenter);
	}
	
	@Override
	public boolean endResults(int key) throws P4JavaException {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				ConsoleView view = ConsoleView.getView();
				if(view!=null)
					view.outputInfo(P4CoreUtils.getFileSpecsActionReport(getFileSpecs()));
			}
		});
		return super.endResults(key);
	}
	
}
