/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.changeset;

import com.perforce.team.core.p4java.synchronize.P4ChangeSetManager;
import com.perforce.team.core.p4java.synchronize.P4PendingChangeSet;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.p4java.actions.EditAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.internal.core.subscribers.ChangeSet;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PendingChangeSetTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile(new Path(
                "src/com/perforce/team/ui/UITeamProvider.java")));
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.ui";
    }

    /**
     * Test changeset with file open for edit
     */
    public void testEdit() {
        IFile file = project.getFile(new Path(
                "src/com/perforce/team/ui/UITeamProvider.java"));
        assertTrue(file.exists());
        EditAction edit = new EditAction();
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.setAsync(false);
        edit.run(null);

        P4ChangeSetManager manager = P4ChangeSetManager.getChangeSetManager();
        assertNotNull(manager);
        try {
            IDiff diff = manager.getDiff(file);
            assertNotNull(diff);
            ChangeSet pending = null;
            for (ChangeSet set : manager.getSets()) {
                if (set.contains(file)) {
                    pending = set;
                    break;
                }
            }
            assertNotNull(pending);
            assertTrue(pending instanceof P4PendingChangeSet);
            P4PendingChangeSet pendingSet = (P4PendingChangeSet) pending;
            assertNotNull(pending.getComment());
            assertNotNull(pending.getName());
            assertNotNull(pending.getResources());
            assertNotNull(pendingSet.getConnection());
            assertEquals(P4PendingChangeSet.PENDING_PRIORITY,
                    pendingSet.getPriority());
            assertFalse(pendingSet.useCommentOnSubmit());
            assertTrue(pendingSet.isValid());
            assertEquals(pendingSet, pendingSet);
            assertFalse(pendingSet.equals("test"));
        } catch (CoreException e) {
            handle(e);
        }

    }
}
