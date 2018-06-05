/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.external;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.EditAction;
import com.perforce.team.ui.p4java.actions.ScheduleResolveAction;
import com.perforce.team.ui.p4java.actions.SyncRevisionAction;
import com.perforce.team.ui.p4merge.MergeRunner;
import com.perforce.team.ui.p4merge.P4MergeResolveAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MergeTest extends ProjectBasedTestCase {

    private String p4merge = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        assertNotNull(System.getProperty("p4merge"));
        p4merge = System.getProperty("p4merge");
    }

    /**
     * @see com.perforce.team.tests.P4TestCase#useRpc()
     */
    @Override
    protected boolean useRpc() {
        return true;
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPreferenceConstants.P4MERGE_PATH, p4merge);
    }

    /**
     * Test the merge action
     */
    public void testAction() {
        Action wrap = Utils.getDisabledAction();
        P4MergeResolveAction merge = new P4MergeResolveAction();
        merge.selectionChanged(wrap, StructuredSelection.EMPTY);
        assertFalse(wrap.isEnabled());

        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.isSynced());

        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#2");

        assertFalse(p4File.isSynced());
        assertEquals(2, p4File.getHaveRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        assertTrue(p4File.openedForEdit());

        ScheduleResolveAction schedule = new ScheduleResolveAction();
        schedule.setAsync(false);
        schedule.selectionChanged(null, new StructuredSelection(file));
        schedule.run(null);

        assertTrue(p4File.isUnresolved());

        merge.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());

        MergeRunner[] merges = merge.runApplication();
        assertNotNull(merges);
        assertEquals(1, merges.length);

        Utils.sleep(5);

        MergeRunner runner = merges[0];
        assertNotNull(runner.getProcess());
        runner.getProcess().destroy();
    }

    /**
     * Test enablement
     */
    public void testEnablement() {
        Action wrap = Utils.getDisabledAction();
        P4MergeResolveAction merge = new P4MergeResolveAction();
        merge.selectionChanged(wrap, StructuredSelection.EMPTY);
        assertFalse(wrap.isEnabled());

        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        assertTrue(p4File.isSynced());

        SyncRevisionAction sync = new SyncRevisionAction();
        sync.setAsync(false);
        sync.selectionChanged(null, new StructuredSelection(file));
        sync.runAction("#2");

        assertFalse(p4File.isSynced());
        assertEquals(2, p4File.getHaveRevision());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        assertTrue(p4File.openedForEdit());

        ScheduleResolveAction schedule = new ScheduleResolveAction();
        schedule.setAsync(false);
        schedule.selectionChanged(null, new StructuredSelection(file));
        schedule.run(null);

        assertTrue(p4File.isUnresolved());

        merge.selectionChanged(wrap, new StructuredSelection(file));
        assertTrue(wrap.isEnabled());
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
