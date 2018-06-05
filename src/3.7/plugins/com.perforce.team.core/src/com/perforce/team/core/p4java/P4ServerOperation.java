/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.core.p4java;

import com.perforce.p4java.exception.P4JavaException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4ServerOperation implements IP4ServerOperation {

    /**
     * @see com.perforce.team.core.p4java.IP4ServerOperation#exception(com.perforce.p4java.exception.P4JavaException)
     */
    public void exception(P4JavaException exception) {
        // Subclasses should override
    }

}
