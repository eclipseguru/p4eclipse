/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.refactor;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.PerforceTestsPlugin;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceMarkerManager;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.widgets.Display;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class AutoAddTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/p08.1/p4-eclipse/com.perforce.team.plugin";
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#tearDown()
     */
    @Override
    public void tearDown() throws Exception {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        super.tearDown();
    }

    /**
     * Test auto add
     */
    public void testAutoAdd() {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_SHOW_MARKERS, false);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, true);
        IFile addFile = project.getFile("newFileToAdd.txt");
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        final List<IResourceChangeEvent> events = new ArrayList<IResourceChangeEvent>();
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        IResourceChangeListener listener = new IResourceChangeListener() {

            public void resourceChanged(IResourceChangeEvent event) {
                events.add(event);
            }

        };
        try {

            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
                    IResourceChangeEvent.POST_BUILD);
            addFile.create(fileUrl.openStream(), true, null);
            Utils.waitForBuild();
            assertTrue(addFile.exists());
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        try {
            while (events.isEmpty()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
                try {
                    while (Display.getCurrent().readAndDispatch())
                        ;
                } catch (Exception e) {
                } catch (Error e) {
                }
            }
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    listener);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(0, markers.length);
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertTrue(p4File.openedForAdd());
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
    }

}
