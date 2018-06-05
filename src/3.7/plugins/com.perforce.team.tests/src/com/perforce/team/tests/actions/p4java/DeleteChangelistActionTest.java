/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.actions.p4java;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.p4java.actions.DeleteChangelistAction;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DeleteChangelistActionTest extends ConnectionBasedTestCase {

    /**
     * Tests the delete changelist action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        DeleteChangelistAction delete = new DeleteChangelistAction();
        delete.setAsync(false);
        delete.selectionChanged(wrap, new StructuredSelection());
        assertFalse(wrap.isEnabled());
        IP4Connection connection = createConnection();
        IP4Changelist list = connection.createChangelist("test: " + getName(),
                new IP4File[0]);
        assertNotNull(list);
        delete.selectionChanged(wrap, new StructuredSelection(new Object[] {
                connection, list }));
        assertFalse(wrap.isEnabled());
        assertTrue(list.getId() > 0);
        delete.selectionChanged(wrap, new StructuredSelection(list));
        assertTrue(wrap.isEnabled());

        final List<P4Event> events = new ArrayList<P4Event>();
        final IP4Listener listener = new IP4Listener() {

            public void resoureChanged(P4Event event) {
                events.add(event);
            }

			public String getName() {
				return DeleteChangelistActionTest.this.getClass().getSimpleName();
			}

        };
        P4Workspace.getWorkspace().addListener(listener);
        delete.run(wrap);
        P4Workspace.getWorkspace().removeListener(listener);
        assertNull(list.getStatus());
        assertNull(list.getChangelist());

        assertEquals(1, events.size());
        for (P4Event event : events) {
            assertEquals(EventType.DELETE_CHANGELIST, event.getType());
            assertEquals(1, event.getResources().length);
            assertEquals(list, event.getResources()[0]);
            assertEquals(1, event.getChangelists().length);
            assertEquals(list, event.getChangelists()[0]);
        }
    }

}
