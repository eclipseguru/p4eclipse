package com.perforce.team.core.p4java;

import java.util.List;

import com.perforce.p4java.core.IStream;
import com.perforce.p4java.core.IStreamSummary;
import com.perforce.p4java.exception.P4JavaException;

/**
 * 
 * @author ali
 *
 */
public interface IP4Stream extends IP4Container{
	IStreamSummary getStreamSummary();
	IStream fetchDetail() throws P4JavaException;
	List<IP4Stream> getChildren();
	List<String> getClientView();
}
