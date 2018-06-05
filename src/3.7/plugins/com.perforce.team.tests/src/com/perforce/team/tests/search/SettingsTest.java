/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.search;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.ui.search.query.DepotPath;
import com.perforce.team.ui.search.query.P4SearchPageSettings;

import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.DialogSettings;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SettingsTest extends ProjectBasedTestCase {

    /**
     * Test empty settings
     */
    public void testEmpty() {
        IP4Connection connection = createConnection();
        DialogSettings dialogSettings = new DialogSettings("test");

        P4SearchPageSettings settings = new P4SearchPageSettings(connection,
                dialogSettings);
        assertTrue(settings.isNewSettings());

        assertNotNull(settings.getPaths());
        assertEquals(0, settings.getPaths().length);

        assertNotNull(settings.getProjects());
        assertEquals(0, settings.getProjects().length);

        assertNotNull(settings.getSelectedPaths());
        assertEquals(0, settings.getSelectedPaths().length);

        assertNotNull(settings.getSelectedProjects());
        assertEquals(0, settings.getSelectedProjects().length);

        assertEquals(0, settings.getSelectedTab());
    }

    /**
     * Test setting values in search page settings
     */
    public void testSetting() {
        IP4Connection connection = createConnection();
        DialogSettings dialogSettings = new DialogSettings("test");

        P4SearchPageSettings settings = new P4SearchPageSettings(connection,
                dialogSettings);
        settings.setSelectedProjects(null);
        assertNotNull(settings.getSelectedProjects());
        settings.setSelectedPaths(null);
        assertNotNull(settings.getSelectedPaths());

        settings.removePath(null);
        assertEquals(0, settings.getPaths().length);
        settings.addPath(new DepotPath("//test/..."));
        assertEquals(1, settings.getPaths().length);
        settings.removePath(null);
        assertEquals(1, settings.getPaths().length);
        settings.removePath(new DepotPath("//test/..."));
        assertEquals(0, settings.getPaths().length);
    }

    /**
     * Test search page settings load
     */
    public void testSave() {
        IP4Connection connection = createConnection();
        DialogSettings dialogSettings = new DialogSettings("test");

        P4SearchPageSettings settings = new P4SearchPageSettings(connection,
                dialogSettings);

        settings.addPath(new DepotPath("//test/..."));
        settings.addPath(new DepotPath("//spec/..."));
        settings.setSelectedTab(10);
        settings.setSelectedPaths(new DepotPath[] { settings.getPaths()[0] });
        settings.setSelectedProjects(new IProject[] { project });
        settings.save();

        P4SearchPageSettings newSettings = new P4SearchPageSettings(connection,
                dialogSettings);
        newSettings.load();

        assertTrue(Arrays.equals(settings.getPaths(), newSettings.getPaths()));
        assertTrue(Arrays.equals(settings.getSelectedPaths(),
                newSettings.getSelectedPaths()));
        assertTrue(Arrays.equals(settings.getSelectedProjects(),
                newSettings.getSelectedProjects()));
        assertEquals(settings.getSelectedTab(), newSettings.getSelectedTab());

    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/settings";
    }
}
