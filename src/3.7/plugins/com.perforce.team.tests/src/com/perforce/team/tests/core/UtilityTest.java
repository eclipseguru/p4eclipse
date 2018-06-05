/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.markers.PerforceResolutionGenerator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class UtilityTest extends P4TestCase {

    /**
     * Tests the utility methods in the perforce ui plugin
     */
    public void testUIPlugin() {
        assertTrue(PerforceUIPlugin.isUIThread());
        assertNotNull(PerforceUIPlugin.getLabelDecorator());
        assertNotNull(PerforceUIPlugin.getPlugin());
        assertNotNull(PerforceUIPlugin.getPlugin().getBundle());
        assertNotNull(PerforceUIPlugin.getActivePage());
        assertNotNull(PerforceUIPlugin.getActiveWorkbenchWindow());
        final boolean[] onUiThread = new boolean[] { true };
        final IWorkbenchPage[] activePage = new IWorkbenchPage[] { PerforceUIPlugin
                .getActivePage() };
        assertNotNull(activePage[0]);
        Job job = new Job("Testing perforce ui plugin") {

            @Override
            protected IStatus run(IProgressMonitor monitor) {
                onUiThread[0] = PerforceUIPlugin.isUIThread();
                activePage[0] = PerforceUIPlugin.getActivePage();
                return Status.OK_STATUS;
            }
        };
        job.schedule();
        try {
            job.join();
        } catch (InterruptedException e) {
        }
        assertFalse(onUiThread[0]);
        assertNull(activePage[0]);
    }

    /**
     * Tests the utility methods in the perforce access class
     */
    public void testPerforceAccess() {
        assertNotNull(P4UIUtils.getDialogShell());
    }

    /**
     * Tests the PerforceResolutionGenerator methods used via extension point
     */
    public void testResolutionGenerator() {
        PerforceResolutionGenerator generator = new PerforceResolutionGenerator();
        assertNotNull(generator.getResolutions(null));
        assertTrue(generator.getResolutions(null).length > 0);
    }

}
