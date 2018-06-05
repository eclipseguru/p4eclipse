/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.synchronize;

import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.synchronize.PerforceSynchronizeWizard;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.team.internal.ui.synchronize.GlobalRefreshResourceSelectionPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SyncWizardTest extends ProjectBasedTestCase {

    /**
     * Test sync wizard
     */
    public void testSyncWizard() {
        PerforceSynchronizeWizard wizard = new PerforceSynchronizeWizard();
        WizardDialog dialog = new WizardDialog(Utils.getShell(), wizard);
        dialog.setBlockOnOpen(false);
        try {
            dialog.open();
            assertNotNull(wizard.getPages());
            assertEquals(1, wizard.getPageCount());
            IWizardPage page = wizard.getPages()[0];
            assertNotNull(page.getTitle());
            assertTrue(page instanceof GlobalRefreshResourceSelectionPage);
            GlobalRefreshResourceSelectionPage resourcePage = (GlobalRefreshResourceSelectionPage) page;
            assertNotNull(resourcePage.getRootResources());
            assertTrue(resourcePage.getRootResources().length > 0);
            boolean found = false;
            for (IResource resource : resourcePage.getRootResources()) {
                if (resource.equals(project)) {
                    found = true;
                    break;
                }
            }
            assertTrue("Project not found", found);
        } finally {
            dialog.close();
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p07.2/p4-eclipse/native/com.perforce.p4api.cnative";
    }

}
