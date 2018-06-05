package com.perforce.team.core.p4java;

import java.text.NumberFormat;

/**
 * Report progress in plain text.
 * 
 * @author ali
 *
 */
public class TextProgressPresenter extends AbstractP4ProgressPresenter {

	@Override
	public void begin(String command, int total) {
		super.begin(command, total);
		System.out.println(command + ">>>begin"+", total="+total); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public void update(int total, int current, int delta, String fileName) {
		System.out.println(String.format("%s>>>total=%d, current=%d, worked=%d, file=%s",command, total, current, delta, fileName)); //$NON-NLS-1$
	}

	@Override
	public void update(long total, long current, String fileName, int thisPercent, long thisSize) {
		System.out.println(String.format("%s>>>total=%d, current=%d, file=%s, %d%% of %s",command, total, current, fileName, thisPercent, NumberFormat.getInstance().format(thisSize))); //$NON-NLS-1$
	}

	@Override
	public void end() {
		System.out.println(command + ">>>end"); //$NON-NLS-1$
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

}
