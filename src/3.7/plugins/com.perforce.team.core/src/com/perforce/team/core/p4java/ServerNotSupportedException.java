/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ServerNotSupportedException extends P4JavaException {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * @param message
     */
    public ServerNotSupportedException(String message) {
        super(message);
    }

}
