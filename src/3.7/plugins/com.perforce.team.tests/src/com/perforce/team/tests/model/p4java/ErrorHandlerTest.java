/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.file.FileSpec;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.ErrorHandler;
import com.perforce.team.core.p4java.IErrorHandler;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.P4Connection;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.tests.P4TestCase;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ErrorHandlerTest extends P4TestCase {

    /**
     * Base test of error handler
     */
    public void testErrorHandler() {
        IErrorHandler handler = new ErrorHandler();
        try {
            handler.handleErrorSpecs(null);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
        assertFalse(handler.shouldRetry(null, null));
    }

    /**
     * Test an error handler directly part of p4 resource
     */
    public void testResource() {
        IP4Connection connection = new P4Connection(new ConnectionParameters());
        final boolean[] rcs = new boolean[] { false, false };
        IErrorHandler handler = new IErrorHandler() {

            public boolean shouldRetry(IP4Connection connection,
                    P4JavaException exception) {
                rcs[0] = true;
                return false;
            }

            public void handleErrorSpecs(IFileSpec[] specs) {
                rcs[1] = true;
            }
        };
        IP4File file = new P4File(connection, "/test");
        file.setErrorHandler(handler);
        file.handleError(new P4JavaException("test"));
        FileSpec spec = new FileSpec(FileSpecOpStatus.ERROR, "error");
        spec.setCodes(1075978254);// MsgClient_Memory "Out of memory!"
        file.handleErrors(new IFileSpec[] { spec });
        assertTrue(rcs[0]);
        assertTrue(rcs[1]);
    }
}
