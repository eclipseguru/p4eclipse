/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.model.p4java;

import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ConnectionBasedTestCase;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.ListenerList;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4WorkspaceListenerTest extends ConnectionBasedTestCase {

    /**
     * currentListeners
     */
    protected Object[] currentListeners = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        // Reset extension point listeners
        try {
            Field listeners = P4Workspace.class.getDeclaredField("listeners");
            assertNotNull(listeners);
            listeners.setAccessible(true);
            Object instanceListeners = listeners
                    .get(P4Workspace.getWorkspace());
            assertNotNull(instanceListeners);
            assertTrue(instanceListeners instanceof ListenerList);
            currentListeners = ((ListenerList) instanceListeners)
                    .getListeners();
        } catch (Exception e) {
            assertFalse("Exception resetting extension point listeners", true);
        }
    }

    /**
     * @see com.perforce.team.tests.P4TestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (currentListeners != null) {
            P4Workspace.getWorkspace().clearListeners();
            for (Object listener : currentListeners) {
                assertTrue(listener instanceof IP4Listener);
                P4Workspace.getWorkspace().addListener((IP4Listener) listener);
            }
        }
    }

}
