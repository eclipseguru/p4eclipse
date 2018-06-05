/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.StructuredSelection;

import com.perforce.p4java.core.IChangelist;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4IntegrationOptions;
import com.perforce.team.core.p4java.P4IntegrationOptions2;
import com.perforce.team.core.p4java.P4IntegrationOptions3;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.p4java.actions.IntegrateAction;
import com.perforce.team.ui.p4java.dialogs.IntegrateDialog;
import com.perforce.team.ui.p4java.dialogs.IntegrationPreviewDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateDialogTest extends ProjectBasedTestCase {
    @Override
    public void setUp() throws Exception {
        super.setUp();

        super.addFile(project.getFile("plugin.xml"));
        super.addFile(project.getFile("about.ini"));
    }

    /**
     * Test integrate dialog
     */
    public void testDialog() {
        IFile file = this.project.getFile("plugin.xml");
        assertTrue(file.exists());
        IP4Resource resource = P4ConnectionManager.getManager().getResource(
                file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;
        IntegrateDialog dialog = new IntegrateDialog(Utils.getShell(), p4File);
        try {
            dialog.setBlockOnOpen(true);
            dialog.open();
            assertEquals(IChangelist.DEFAULT, dialog.getChangelist());
            assertNotNull(dialog.getSource());
            assertNotNull(dialog.getTarget());
            assertNull(dialog.getEnd());
            assertNull(dialog.getStart());
            assertNull(dialog.getBranch());
            assertTrue(dialog.isFileIntegration());
            dialog.setBaselessMerge(true);
            dialog.setDisregardHistory(true);
            dialog.setDoNotCopy(true);
            dialog.setDoNotGetLatest(true);
            dialog.setPropogateFiletypes(true);
            dialog.setIntegrateAroundDeleted(true);
            dialog.updateOptions();
            P4IntegrationOptions options = dialog.getSelectedOptions();
            if(options instanceof P4IntegrationOptions2){
            	P4IntegrationOptions2 opt = (P4IntegrationOptions2)options;
	            assertTrue(opt.isBaselessMerge());
	            assertTrue(opt.isPropagateType());
            }
            assertTrue(options.isDontCopyToClient());
            assertTrue(options.isForce());
            assertTrue(options.isUseHaveRev());
            assertTrue(options.isIntegrateAroundDeleted());
            P4FileIntegration integ = dialog.getIntegration();
            assertNotNull(integ);
            assertNull(integ.getEnd());
            assertNull(integ.getStart());
            assertNotNull(integ.getSource());
            assertNotNull(integ.getTarget());
        } finally {
            dialog.close();
        }
    }

    /**
     * Test preview
     */
    public void testPreview() {
        IFile file = project.getFile("about.ini");
        assertTrue(file.exists());

        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        assertNotNull(resource);
        assertTrue(resource instanceof IP4File);
        IP4File p4File = (IP4File) resource;

        IntegrationPreviewDialog dialog = null;
        IP4File newFile = null;
        try {
            Action wrap = Utils.getDisabledAction();
            IntegrateAction integrate = new IntegrateAction();
            integrate.setAsync(false);
            integrate.selectionChanged(wrap, new StructuredSelection(file));
            assertTrue(wrap.isEnabled());

            String source = p4File.getActionPath();
            String target = "//depot/dev/testtest/about.ini";

            P4FileIntegration options = new P4FileIntegration();
            options.setSource(source);
            options.setTarget(target);

            IP4Resource[] previewed = createConnection().integrate(options, 0,
                    true, true, new P4IntegrationOptions3());

            assertNotNull(previewed);
            assertEquals(1, previewed.length);
            IP4Resource newResource = previewed[0];
            assertNotNull(newResource);
            assertTrue(newResource instanceof IP4File);
            newFile = (IP4File) newResource;
            assertTrue(newFile.isOpened());
            assertTrue(newFile.openedForAdd());
            dialog = new IntegrationPreviewDialog(Utils.getShell(),
                    new IP4Resource[] { newFile });
            dialog.setBlockOnOpen(false);
            dialog.open();

            TextViewer viewer = dialog.getViewer();
            assertNotNull(viewer);
            assertNotNull(viewer.getDocument());
            assertNotNull(viewer.getDocument().get());
            assertTrue(viewer.getDocument().get().length() > 0);
            assertTrue(viewer.getDocument().get()
                    .contains(newFile.getRemotePath()));
            assertTrue(viewer.getDocument().get().contains("from"));
            assertTrue(viewer.getDocument().get().contains(source));
        } finally {
            if (dialog != null) {
                dialog.close();
            }
            if (newFile != null) {
                newFile.revert();
                newFile.refresh();
                assertFalse(newFile.isOpened());
            }
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

}
