/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Depot;
import com.perforce.team.tests.ConnectionBasedTestCase;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.views.DepotView;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class PerforceContentProviderTest extends ConnectionBasedTestCase {

    /**
     * Basic provider test
     */
    public void testProvider() {
        DepotView view = DepotView.showView();
        assertNotNull(view);
        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        PerforceContentProvider provider = new PerforceContentProvider(viewer,
                false);
        assertFalse(provider.isLoadAsync());
        provider.setLoadAsync(true);
        assertTrue(provider.isLoadAsync());
        assertNull(provider.getParent(null));
        assertFalse(provider.hasChildren(null));
        assertNotNull(provider.getChildren(null));
        assertEquals(0, provider.getChildren(null).length);
    }

    /**
     * Test connection sync
     */
    public void testConnectionSync() {
        IP4Connection connection = createConnection();
        connection.markForRefresh();
        DepotView view = DepotView.showView();
        assertNotNull(view);
        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        PerforceContentProvider provider = new PerforceContentProvider(viewer,
                false);
        provider.setLoadAsync(true);
        assertNull(provider.getParent(connection));
        assertTrue(provider.hasChildren(connection));
        Object[] children = provider.getChildren(connection);
        assertNotNull(children);
        assertEquals(1, children.length);
        assertFalse(children[0] instanceof IP4Resource);
        connection.refresh();
        assertFalse(connection.needsRefresh());
        children = provider.getChildren(connection);
        assertNotNull(children);
        assertTrue(children.length > 0);
        assertTrue(children[0] instanceof P4Depot);
    }

    /**
     * Test connection async
     */
    public void testConnectionAsync() {
        IP4Connection connection = createConnection();
        connection.markForRefresh();
        DepotView view = DepotView.showView();
        assertNotNull(view);
        TreeViewer viewer = view.getViewer();
        assertNotNull(viewer);
        PerforceContentProvider provider = new PerforceContentProvider(viewer,
                false);
        provider.setLoadAsync(false);
        assertNull(provider.getParent(connection));
        assertTrue(provider.hasChildren(connection));
        Object[] children = provider.getChildren(connection);
        assertNotNull(children);
        assertTrue(children.length > 0);
        assertTrue(children[0] instanceof P4Depot);
    }

}
