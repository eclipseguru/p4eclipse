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
import com.perforce.team.ui.p4v.P4VTimeLapseAction;
import com.perforce.team.ui.p4v.TimeLapseRunner;
import com.perforce.team.ui.preferences.IPreferenceConstants;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TimeLapseTest extends ProjectBasedTestCase {

    private String p4v = null;

    /**
     * @see com.perforce.team.tests.ConnectionBasedTestCase#initParameters()
     */
    @Override
    protected void initParameters() {
        super.initParameters();
        assertNotNull(System.getProperty("p4v"));
        p4v = System.getProperty("p4v");
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPreferenceConstants.P4V_PATH, p4v);
    }

    /**
     * Test p4v time lapse view
     */
    public void testAction() {
        IFile file = this.project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        P4VTimeLapseAction time = new P4VTimeLapseAction();
        time.selectionChanged(null, new StructuredSelection(file));
        TimeLapseRunner[] lapses = time.runApplication();
        assertNotNull(lapses);
        assertEquals(1, lapses.length);

        Utils.sleep(5);

        TimeLapseRunner runner = lapses[0];
        assertNotNull(runner.getProcess());
        runner.getProcess().destroy();
    }

    /**
     * Test enablement
     * 
     * @throws Exception
     */
    public void testEnablement() throws Exception {
        IFile file = this.project.getFile("about.ini");
        assertTrue(file.exists());
        IFile notInDepot = this.project.getFile("notInDepot.txt");
        Utils.fillFile(notInDepot);
        assertTrue(notInDepot.exists());

        P4VTimeLapseAction time = new P4VTimeLapseAction();

        Action wrap = Utils.getDisabledAction();
        time.selectionChanged(wrap, new StructuredSelection(this.project));
        assertFalse(wrap.isEnabled());

        time.selectionChanged(wrap, new StructuredSelection(notInDepot));

        assertFalse(wrap.isEnabled());

        time.selectionChanged(wrap, new StructuredSelection(file));

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
