package com.perforce.team.core.p4java;

/**
 * Base class for progress presenter.
 * 
 * @author ali
 *
 */
public class AbstractP4ProgressPresenter implements IP4ProgressPresenter {
	protected String command;

	public void begin(String command, int total) {
		this.command=command;
	}

	public void update(int total, int current, int delta, String fileName) {
		// TODO Auto-generated method stub

	}

	public void end() {
		// TODO Auto-generated method stub

	}

	public boolean isCancelled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(long totalFileCount, long currentCount, String depotFile,
			int thisPercent, long thisSize) {
		// TODO Auto-generated method stub
		
	}

}
