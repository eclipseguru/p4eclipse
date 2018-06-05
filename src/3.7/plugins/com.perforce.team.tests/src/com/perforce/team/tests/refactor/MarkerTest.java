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
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.PerforceMarkerManager;
import com.perforce.team.ui.markers.AddSourceResolution;
import com.perforce.team.ui.markers.IgnoreResolution;
import com.perforce.team.ui.p4java.actions.AddAction;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.StructuredSelection;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MarkerTest extends ProjectBasedTestCase {

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        addFile(project.getFile("plugin.xml"));
    }

    /**
     * Tests the add marker
     */
    public void testMarkerAdd() {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_SHOW_MARKERS, true);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        IFile addFile = project.getFile("newFileToAdd1.txt");
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
            Utils.waitForBuild();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
                    IResourceChangeEvent.POST_BUILD);
            addFile.create(fileUrl.openStream(), true, null);
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
            }
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    listener);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(1, markers.length);
            IMarker marker = markers[0];
            assertEquals(PerforceMarkerManager.ADDITION_MARKER,
                    marker.getType());
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
    }

    /**
     * Tests the add marker
     */
    public void testMarkerDelete() {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_SHOW_MARKERS, true);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        IFile addFile = project.getFile("newFileToAdd2.txt");
        assertFalse(addFile.exists());
        URL fileUrl = PerforceTestsPlugin.getDefault().getBundle()
                .getEntry("/resources/Test.txt");
        assertNotNull(fileUrl);
        try {
            fileUrl = FileLocator.toFileURL(fileUrl);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        }
        try {
            Utils.waitForBuild();
            addFile.create(fileUrl.openStream(), true, null);
            assertTrue(addFile.exists());
            IMarker added = addFile
                    .createMarker(PerforceMarkerManager.ADDITION_MARKER);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(1, markers.length);
            assertEquals(added, markers[0]);
        } catch (IOException e) {
            assertFalse("IO exception thrown", true);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        try {
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(addFile));
            add.run(null);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(0, markers.length);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
    }

    /**
     * Test the ignore resolution
     */
    public void testIgnoreResolution() {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_SHOW_MARKERS, true);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        IFile addFile = project.getFile("newFileToAdd3.txt");
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
            Utils.waitForBuild();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
                    IResourceChangeEvent.POST_BUILD);
            addFile.create(fileUrl.openStream(), true, null);
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
            }
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    listener);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(1, markers.length);
            IMarker marker = markers[0];
            assertEquals(PerforceMarkerManager.ADDITION_MARKER,
                    marker.getType());
            IgnoreResolution resolution = new IgnoreResolution();
            assertNotNull(resolution.getLabel());
            resolution.run(marker);
            assertTrue(IgnoredFiles.isIgnored(addFile));
            markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(0, markers.length);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
    }

    /**
     * Test that the ignore resolution gracefully fails when run when an invalid
     * marker
     */
    public void testIgnoreResolutionFailure() {
        final IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IMarker marker = new IMarker() {

            public Object getAdapter(Class adapter) {
                return null;
            }

            public void setAttributes(String[] attributeNames, Object[] values)
                    throws CoreException {

            }

            public void setAttributes(Map attributes) throws CoreException {

            }

            public void setAttribute(String attributeName, boolean value)
                    throws CoreException {

            }

            public void setAttribute(String attributeName, Object value)
                    throws CoreException {

            }

            public void setAttribute(String attributeName, int value)
                    throws CoreException {

            }

            public boolean isSubtypeOf(String superType) throws CoreException {
                return false;
            }

            public String getType() throws CoreException {
                return null;
            }

            public IResource getResource() {
                return file;
            }

            public long getId() {
                return 0;
            }

            public long getCreationTime() throws CoreException {
                return 0;
            }

            public Object[] getAttributes(String[] attributeNames)
                    throws CoreException {
                return null;
            }

            public Map getAttributes() throws CoreException {
                return null;
            }

            public boolean getAttribute(String attributeName,
                    boolean defaultValue) {
                return false;
            }

            public String getAttribute(String attributeName, String defaultValue) {
                return null;
            }

            public int getAttribute(String attributeName, int defaultValue) {
                return 0;
            }

            public Object getAttribute(String attributeName)
                    throws CoreException {
                return null;
            }

            public boolean exists() {
                return false;
            }

            public void delete() throws CoreException {
                throw new CoreException(Status.CANCEL_STATUS);
            }

        };
        IgnoreResolution resolution = new IgnoreResolution();
        assertNotNull(resolution.getLabel());
        // Test that the resolution doesn't thrown any exceptions when run
        try {
            resolution.run(marker);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
        }
    }

    /**
     * Tests the add resolution
     */
    public void testAddResolution() {
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_SHOW_MARKERS, true);
        Utils.getUIPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_NEW_OPEN_ADD, false);
        IFile addFile = project.getFile("newFileToAdd4.txt");
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
            Utils.waitForBuild();
            ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
                    IResourceChangeEvent.POST_BUILD);
            addFile.create(fileUrl.openStream(), true, null);
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
            }
            ResourcesPlugin.getWorkspace().removeResourceChangeListener(
                    listener);
            IMarker[] markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(1, markers.length);
            IMarker marker = markers[0];
            assertEquals(PerforceMarkerManager.ADDITION_MARKER,
                    marker.getType());
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertFalse(p4File.isOpened());
            assertFalse(p4File.openedForAdd());
            AddSourceResolution resolution = new AddSourceResolution();
            assertNotNull(resolution.getLabel());
            resolution.run(marker);
            resource = P4Workspace.getWorkspace().getResource(addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            p4File = (IP4File) resource;
            assertTrue(p4File.isOpened());
            assertTrue(p4File.openedForAdd());
            markers = addFile.findMarkers(
                    PerforceMarkerManager.ADDITION_MARKER, false,
                    IResource.DEPTH_INFINITE);
            assertNotNull(markers);
            assertEquals(0, markers.length);
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
    }

    /**
     * Test that the add resolution gracefully fails when run when an invalid
     * marker
     */
    public void testAddResolutionFailure() {
        final IFile file = project.getFile("plugin.xml");
        assertTrue(file.exists());
        IMarker marker = new IMarker() {

            public Object getAdapter(Class adapter) {
                return null;
            }

            public void setAttributes(String[] attributeNames, Object[] values)
                    throws CoreException {

            }

            public void setAttributes(Map attributes) throws CoreException {

            }

            public void setAttribute(String attributeName, boolean value)
                    throws CoreException {

            }

            public void setAttribute(String attributeName, Object value)
                    throws CoreException {

            }

            public void setAttribute(String attributeName, int value)
                    throws CoreException {

            }

            public boolean isSubtypeOf(String superType) throws CoreException {
                return false;
            }

            public String getType() throws CoreException {
                return null;
            }

            public IResource getResource() {
                return file;
            }

            public long getId() {
                return 0;
            }

            public long getCreationTime() throws CoreException {
                return 0;
            }

            public Object[] getAttributes(String[] attributeNames)
                    throws CoreException {
                return null;
            }

            public Map getAttributes() throws CoreException {
                return null;
            }

            public boolean getAttribute(String attributeName,
                    boolean defaultValue) {
                return false;
            }

            public String getAttribute(String attributeName, String defaultValue) {
                return null;
            }

            public int getAttribute(String attributeName, int defaultValue) {
                return 0;
            }

            public Object getAttribute(String attributeName)
                    throws CoreException {
                return null;
            }

            public boolean exists() {
                return false;
            }

            public void delete() throws CoreException {
                throw new CoreException(Status.CANCEL_STATUS);
            }

        };
        AddSourceResolution resolution = new AddSourceResolution();
        assertNotNull(resolution.getLabel());
        // Test that the resolution doesn't thrown any exceptions when run
        try {
            resolution.run(marker);
        } catch (Exception e) {
            assertFalse("Exception thrown", true);
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
