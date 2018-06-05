/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.server.IServer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4ServerOperation {

    /**
     * Run a server-based operation
     * 
     * @param server
     * @throws P4JavaException
     * @throws P4JavaError
     */
    void run(IServer server) throws P4JavaException, P4JavaError;

    /**
     * P4JavaException callback when encountered from {@link #run(IServer)}
     * 
     * @param exception
     */
    void exception(P4JavaException exception);

}
