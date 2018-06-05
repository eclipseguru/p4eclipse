package com.perforce.team.core;

import com.perforce.p4java.server.callback.ICommandCallback;
import com.perforce.p4java.server.callback.ISSOCallback;
import com.perforce.p4java.server.callback.IStreamingCallback;

/**
 * An aggregation of the p4java callback.
 * 
 * @author ali
 *
 */
public interface IP4JavaCallback extends ICommandCallback, IStreamingCallback, ISSOCallback{

}
