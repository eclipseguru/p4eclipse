/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public interface IP4ClientOperation {

    /**
     * Run a client operation
     * 
     * @param client
     * @throws P4JavaException
     * @throws P4JavaError
     */
    void run(IClient client) throws P4JavaException, P4JavaError;

    /**
     * P4JException callback when encountered from {@link #run(IClient)}
     * 
     * @param exception
     */
    void exception(P4JavaException exception);

}
