package com.perforce.team.core.p4java;

import java.text.MessageFormat;
import java.text.NumberFormat;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Report progress in progress bar.
 * 
 * @author ali
 *
 */
public class ProgressMonitorProgressPresenter extends AbstractP4ProgressPresenter {
	
	IProgressMonitor monitor;

	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	public IProgressMonitor getMonitor() {
		return monitor;
	}

	public void begin(final String command, final int total) {
		super.begin(command, total);
		getMonitor().beginTask(command,total);
	}

	@Override
	public void update(int total, int current, int delta, String filePath) {
		if(filePath!=null){
			String fileName=getFileNameOnly(filePath);
			getMonitor().setTaskName(MessageFormat.format(Messages.ProgressMonitorProgressPresenter_CurrentOfTotalNextFile, command, current+delta, total,fileName));
		}else
			getMonitor().setTaskName(MessageFormat.format(Messages.ProgressMonitorProgressPresenter_CurrentOfTotal, command, current, total));
		
		getMonitor().worked(delta);
	}

	@Override
	public void update(long total, long current, String filePath, int thisPercent, long thisSize) {
		if(filePath!=null){
			String fileName=getFileNameOnly(filePath);
			getMonitor().setTaskName(MessageFormat.format(Messages.ProgressMonitorProgressPresenter_CurrentOfTotalNextFilePercentOfSize, command, current, total,fileName, thisPercent, NumberFormat.getInstance().format(thisSize)));
		}else
			getMonitor().setTaskName(MessageFormat.format(Messages.ProgressMonitorProgressPresenter_CurrentOfTotal, command, current, total));
	}

	private String getFileNameOnly(String filePath) {
		if(StringUtils.isNotEmpty(filePath)){
			int index=0;
			for(int i=filePath.length()-1;i>=0;i--){
				if(filePath.charAt(i)=='/' ||filePath.charAt(i)=='\\'){
					index=i;
					break;
				}
			}
			if(index+1<filePath.length())
				return filePath.substring(index+1);
		}
		return null;
	}

	public void end() {
		getMonitor().done();
	}

	public boolean isCancelled() {
		return getMonitor().isCanceled();
	}

}
