/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.changeset;

import com.perforce.team.core.p4java.synchronize.P4ChangeSetManager;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.tests.P4TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.internal.core.subscribers.ActiveChangeSet;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeSetManagerTest extends P4TestCase {

    /**
     * Test create change set
     */
    public void testCreate() {
        P4ChangeSetManager manager = P4ChangeSetManager.getChangeSetManager();
        assertNotNull(manager);
        try {
            ActiveChangeSet set = manager.createSet("test", new IFile[0]);
            assertNotNull(set);
            assertTrue(set instanceof P4PendingChangeSet);
        } catch (CoreException e) {
            handle(e);
        }
    }

    /**
     * Test default change set
     */
    public void testDefault() {
        P4ChangeSetManager manager = P4ChangeSetManager.getChangeSetManager();
        assertNotNull(manager);
        try {
            ActiveChangeSet set = manager.createSet("test", new IFile[0]);
            assertNotNull(set);
            manager.makeDefault(set);
            assertEquals(set, manager.getDefaultSet());
        } catch (CoreException e) {
            handle(e);
        }
    }

}
