/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import junit.framework.TestCase;

import com.perforce.team.core.PerforceProviderPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4TestCase extends TestCase {

    private static final boolean RPC = System
            .getProperty("com.perforce.tests.USE_RPC") != null;

    private long start=0;
    // private static final boolean LOG = System
    // .getProperty("com.perforce.tests.LOG") != null;
    protected static void log(String msg) {
        if (true/* LOG */) {
            System.out.println("!TEST! " + msg);
            PerforceProviderPlugin.logInfo(msg);
        }
    }

    protected static void printTimestamp() {
        Date now = new Date();
        System.out.println(DateFormat.getDateTimeInstance(DateFormat.LONG,
                DateFormat.LONG).format(now));
    }

    /**
     * Rpc pref when setup
     */
    protected boolean previousRpcPref = false;

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        start=System.currentTimeMillis();
        log(MessageFormat.format("STARTING {0}",getClass().getSimpleName()));
    }

    /**
     * Use an rpc connection?
     * 
     * @return -true to use rpc connection, false otherwise
     */
    protected boolean useRpc() {
        return RPC;
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log(MessageFormat.format("STOPPING {0} time={1}(ms)",getClass().getSimpleName(),(System.currentTimeMillis()-start)));
    }

    /**
     * Fail because a throwable was thrown
     * 
     * @param throwable
     */
    protected void handle(Throwable throwable) {
        handle(null, throwable);
    }

    /**
     * Fail because a throwable was thrown
     * 
     * @param message
     * @param throwable
     */
    protected void handle(String message, Throwable throwable) {
        if (message == null) {
            message = "Throwable thrown: ";
        }
        if (throwable != null) {
            message += throwable.getMessage();
        }
        assertFalse(message, true);
    }

}
