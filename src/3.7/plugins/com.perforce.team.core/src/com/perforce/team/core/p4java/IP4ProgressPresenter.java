package com.perforce.team.core.p4java;

/**
 * Presenting the progress.
 * 
 * @author ali
 *
 */
public interface IP4ProgressPresenter {

	void begin(final String command, final int total);

	void update(int total, int current, int delta, String fileName);

	void end();
	
	boolean isCancelled();

	void update(long totalFileCount, long currentCount, String depotFile, int thisPercent, long thisSize);
}
