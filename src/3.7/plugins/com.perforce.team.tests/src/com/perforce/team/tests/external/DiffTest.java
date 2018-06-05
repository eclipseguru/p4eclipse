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
import com.perforce.team.ui.p4merge.DiffRunner;
import com.perforce.team.ui.p4merge.P4MergeDiffAction;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffTest extends ProjectBasedTestCase {

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
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPreferenceConstants.P4MERGE_PATH, p4merge);
    }

    /**
     * Test p4 merge diffing
     */
    public void testAction() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        assertTrue(p4File.openedForEdit());
        P4MergeDiffAction diff = new P4MergeDiffAction();
        diff.selectionChanged(null, new StructuredSelection(file));
        DiffRunner[] diffs = diff.runApplication();
        assertNotNull(diffs);
        assertEquals(1, diffs.length);

        Utils.sleep(5);

        DiffRunner runner = diffs[0];
        assertNotNull(runner.getProcess());
        runner.getProcess().destroy();
    }

    /**
     * Test enablement
     */
    public void testEnablement() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());

        P4MergeDiffAction diff = new P4MergeDiffAction();

        Action wrap = Utils.getDisabledAction();
        diff.selectionChanged(wrap, new StructuredSelection(this.project));
        assertFalse(wrap.isEnabled());

        diff.selectionChanged(wrap, new StructuredSelection(file));

        assertFalse(wrap.isEnabled());

        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(file));
        edit.run(null);

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
