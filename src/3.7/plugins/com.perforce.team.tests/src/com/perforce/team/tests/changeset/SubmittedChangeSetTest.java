/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.changeset;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.synchronize.P4SubmittedChangeSet;
import com.perforce.team.core.p4java.synchronize.PerforceSubscriber;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.synchronize.P4ChangeSetCollector;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.ChangeSet;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedChangeSetTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.properties"));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * Test submitted change set with incoming add
     */
    public void testIncomingAdd() {
        IFile file = project.getFile("plugin.properties");
        assertTrue(file.exists());

        IP4Connection connection = createConnection();
        IP4Resource p4Resource = connection.getResource(file);
        assertNotNull(p4Resource);
        assertTrue(p4Resource instanceof IP4File);
        IP4File p4File = (IP4File) p4Resource;
        int change = p4File.getHeadChange();

        SyncRevisionAction sync = new SyncRevisionAction();
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.setAsync(false);
        sync.runAction(0);

        assertFalse(file.exists());

        try {
            PerforceSubscriber.getSubscriber().refresh(
                    new IResource[] { file }, 0, null);
            SyncInfo info = PerforceSubscriber.getSubscriber()
                    .getSyncInfo(file);
            assertNotNull(info);

            P4ChangeSetCollector collector = new P4ChangeSetCollector(null) {

                @Override
                protected void updateSets(
                        Map<P4SubmittedChangeSet, List<SyncInfo>> sets) {
                    addSets(sets.keySet());
                }

            };
            ChangeSet[] sets = collector.getSets();
            assertNotNull(sets);
            assertEquals(0, sets.length);
            collector.add(new SyncInfo[] { info });
            sets = collector.getSets();
            assertNotNull(sets);
            assertEquals(1, sets.length);
            ChangeSet set = sets[0];
            assertTrue(set instanceof P4SubmittedChangeSet);
            P4SubmittedChangeSet submitted = (P4SubmittedChangeSet) set;
            assertNotNull(submitted.getAuthor());
            assertNotNull(submitted.getComment());
            assertNotNull(submitted.getDate());
            assertNotNull(submitted.getConnection());
            assertNotNull(submitted.getChangelist());
            assertEquals(change, submitted.getId());
            assertTrue(submitted.isValid());
            assertEquals(P4SubmittedChangeSet.SUBMITTED_PRIORITY,
                    submitted.getPriority());
            assertEquals(submitted, submitted);
            assertFalse(submitted.equals("test"));
        } catch (TeamException e) {
            handle(e);
        }

    }
}
