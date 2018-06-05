package com.perforce.team.core.p4java;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.P4CoreUtils;

/**
 * Progress presenter should worked with IP4ProgressPresenter to report the overall progress.
 * 
 * @author ali
 *
 */
public class P4ProgressListener implements IP4ProgressListener {
	private List<IFileSpec> fileSpecs=new ArrayList<IFileSpec>();
	protected String command;
	
	protected int totalFileCount;
	protected int currentCount;

	private IP4ProgressPresenter presenter=new TextProgressPresenter();
	private IP4Connection connection;
	private boolean cancelled=false;

	public P4ProgressListener(String command, IP4Connection connection) {
		this.command=command;
		this.connection=connection;
	}

	public IP4ProgressPresenter getPresenter() {
		return presenter;
	}

	public void setPresenter(IP4ProgressPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public boolean startResults(int key) throws P4JavaException {
		currentCount=0;
		return true;
	}

	@Override
	public boolean handleResult(Map<String, Object> resultMap, int key)
			throws P4JavaException {
		// System.out.println("handleResult ["+command+"] "+P4CoreUtils.printMap(resultMap)); //$NON-NLS-1$ $NON-NLS-2$
		if(presenter.isCancelled()){
			presenter.end();
			cancelled=true;
			return false;
		}
		
		if(command.startsWith("sync")){ //$NON-NLS-1$
			handleSync(resultMap,key);
		}else if(command.startsWith("submit")){ //$NON-NLS-1$
			handleSubmit(resultMap,key);
		}else{ // Just for tracking...
			fileSpecs.add(P4CoreUtils.extractFileSpec(resultMap, connection));
		}
		return true;
	}

	private void handleSubmit(Map<String, Object> resultMap, int key) throws P4JavaException {
		if(resultMap.containsKey("locked")){ // submit //$NON-NLS-1$
			String total=(String)resultMap.get("locked"); //$NON-NLS-1$
			totalFileCount=Integer.parseInt(total);
			currentCount=0;
			presenter.begin(command, totalFileCount);
		}
		processUpdate(resultMap, key);
	}

	private void handleSync(Map<String, Object> resultMap, int key) throws P4JavaException {
		if(resultMap.containsKey("totalFileCount")){ // sync //$NON-NLS-1$
			String total=(String)resultMap.get("totalFileCount"); //$NON-NLS-1$
			totalFileCount=Integer.parseInt(total);
			currentCount=0;
			presenter.begin(command, totalFileCount);
		}
		processUpdate(resultMap, key);
	}

	private void processUpdate(Map<String, Object> resultMap, int key) throws P4JavaException{
		String depotFile=(String) resultMap.get("depotFile"); //$NON-NLS-1$
		String localFile=(String) resultMap.get("path"); //$NON-NLS-1$
		if(depotFile != null){
			presenter.update(totalFileCount, currentCount, 1, depotFile);
			currentCount++;
			fileSpecs.add(P4CoreUtils.extractFileSpec(resultMap, connection));
		}else if(localFile != null){
			Object curSize=resultMap.get("currentSize"); //$NON-NLS-1$
			Object fileSize=resultMap.get("fileSize"); //$NON-NLS-1$
			long total=parseSize(fileSize);
			long cur=parseSize(curSize);
			if(total>0 && cur>0){
				double rate=(double)cur/(double)total;
				int percent=(int)(rate*100);
				presenter.update(totalFileCount, currentCount, localFile, percent, total);
			}
		}else {
			IFileSpec spec = P4CoreUtils.extractFileSpec(resultMap, connection);
			if(spec!=null)
				fileSpecs.add(spec);
		}
	}

	private long parseSize(Object size){
		long result=-1;
		if(size instanceof Integer){
			result=(Integer)size;
		}else if(size instanceof Long){
			result=(Long)size;
		}else if(size instanceof Short){
			result=(Short)size;
		}else if(size instanceof Byte){
			result=(Byte)size;
		}else if(size instanceof BigInteger){
			result=((BigInteger)size).longValue();
		}
		return result;
	}
	
	@Override
	public boolean endResults(int key) throws P4JavaException {
		presenter.end();
		return true;
	}

	@Override
	public List<IFileSpec> getFileSpecs(){
		return this.fileSpecs;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setConnection(IP4Connection conn) {
		this.connection=conn;
	}

}
