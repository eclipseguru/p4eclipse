/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.decorator;

import java.util.Arrays;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;

import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IProjectSettingsChangeListener;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.tests.ProjectBasedTestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.IconCache;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.EditAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DecoratorComparisonTest extends ProjectBasedTestCase {

    /**
     * The Perforce decorator. The decorator now computes all decorations in the
     * foreground. This is because a) it is reasonably fast and b) it is very
     * reliable. Computing decorations in a background thread always has
     * problems when firing a LabelProviderChangedEvent since these sometimes
     * get ignored and the label will not get refreshed!
     */
    public class PerforceDecorator extends LabelProvider implements
            ILabelDecorator, IProjectSettingsChangeListener,
            IPropertyChangeListener, IP4Listener {

        /**
         * The ID for this decorator
         */
        public static final String ID = "com.perforce.team.ui.decorator.PerforceDecorator";

        // File and Project text decoration format
        private int fileFormat;
        private int projectFormat;
        private boolean decorateIgnored;

        private static final String IGNORED_TEXT = " <ignored>";

        // The cache for storing previously gotten images
        private IconCache iconCache = new IconCache();

        // If true then this decorator is used for resources, if false
        // then it means it is the decorator for the pending changelist
        // or depot view.
        private boolean resourceDecorator;

        private boolean suspendDecoration = false;

        /**
         * Decorator constructor.
         */
        public PerforceDecorator() {
            resourceDecorator = true;
            init();
        }

        /**
         * Inlined here since between Eclipse 3.2 and 3.3 the hierarchy of
         * LabelProvider was changed and this function was moved to
         * BaseLabelProvider. This causes issues because pluginbuilder builds
         * against 3.3 and therefore injects a reference to BaseLabelProvider
         * when compiling that then causes {@link NoClassDefFoundError}
         * exceptions when the plugin is installed on Eclipse 3.2.
         * 
         * Fixes job032543.
         * 
         * @see org.eclipse.jface.viewers.LabelProvider#fireLabelProviderChanged(org.eclipse.jface.viewers.LabelProviderChangedEvent)
         */
        @Override
        protected void fireLabelProviderChanged(
                final LabelProviderChangedEvent event) {
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; ++i) {
                if (listeners[i] instanceof ILabelProviderListener) {
                    final ILabelProviderListener l = (ILabelProviderListener) listeners[i];
                    SafeRunnable.run(new SafeRunnable() {

                        public void run() {
                            l.labelProviderChanged(event);
                        }
                    });
                }
            }
        }

        /**
         * Decorator constructor.
         * 
         * @param resourceDecorator
         *            set to false if this is to be the decorator for the
         *            pending changelist or depot view.
         */
        public PerforceDecorator(boolean resourceDecorator) {
            this.resourceDecorator = resourceDecorator;
            init();
        }

        /**
         * Shutting down
         */
        @Override
        public void dispose() {
            super.dispose();

            // Remove listeners for change events
            if (resourceDecorator) {
                PerforceProviderPlugin
                        .removeProjectSettingsChangeListener(this);
            }

            // Remove file state listeners
            P4ConnectionManager.getManager().removeListener(this);

            // Remove property store listener
            IPreferenceStore store = PerforceUIPlugin.getPlugin()
                    .getPreferenceStore();
            store.removePropertyChangeListener(this);
        }

        /**
         * Capture when projects are managed or unmanaged or connection settings
         * are changed.
         * 
         * @param project
         * @param params
         */
        public void projectSettingsChanged(IProject project,
                ConnectionParameters params) {

            // Refresh the project
            resourceStateChanged(new IResource[] { project });
        }

        /**
         * Capture changes to decoration preferences. Note that we piggyback the
         * PREF_MARK_UNMANAGED_FILES event here as they're properly handled in
         * the same icon cache code. This will <i>definitely</i> need revisiting
         * later -- HR.
         * 
         * @param event
         *            the property change event
         */
        public void propertyChange(PropertyChangeEvent event) {
            String property = event.getProperty();
            if (property == IPerforceUIConstants.PREF_FILE_FORMAT
                    || property == IPerforceUIConstants.PREF_PROJECT_FORMAT
                    || property == IPerforceUIConstants.PREF_IGNORED_TEXT
                    || property == IPerforceUIConstants.PREF_FILE_OPEN_ICON
                    || property == IPerforceUIConstants.PREF_FILE_SYNC_ICON
                    || property == IPerforceUIConstants.PREF_FILE_SYNC2_ICON
                    || property == IPerforceUIConstants.PREF_FILE_UNRESOLVED_ICON
                    || property == IPerforceUIConstants.PREF_FILE_LOCK_ICON
                    || property == IPerforceUIConstants.PREF_MARK_UNMANAGED_FILES
                    || property == IPerforceUIConstants.PREF_FILE_OTHER_ICON) {

                // Discard all icons
                iconCache.clear();

                // Go get the latest preferences
                getPreferences();

                // Refresh everything
                fireLabelProviderChanged(new LabelProviderChangedEvent(this));
            }
        }

        /**
         * This method is called if resources have changed or need to be
         * refreshed
         * 
         * @param resources
         *            the list of resources which have changed
         */
        public void resourceStateChanged(final IResource[] resources) {
            boolean gotFolder = false;
            // Drop any caches
            for (int i = 0; i < resources.length; i++) {
                int type = resources[i].getType();
                if (type == IResource.PROJECT || type == IResource.FOLDER) {
                    gotFolder = true;
                }
            }
            // If we have Projects or Folders then refresh everything
            // Otherwise just do the specific resources.
            final boolean gotFolder2 = gotFolder;

            PerforceUIPlugin.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    if (gotFolder2) {
                        fireLabelProviderChanged(new LabelProviderChangedEvent(
                                PerforceDecorator.this));
                    } else {
                        fireLabelProviderChanged(new LabelProviderChangedEvent(
                                PerforceDecorator.this, resources));
                    }

                }
            });

        }

        /**
         * Provide image decoration for this object
         * 
         * @param image
         *            the image to decorate
         * @param o
         *            the object to decorate
         * @return the decorated image
         */
        public Image decorateImage(Image image, Object o) {
            if (suspendDecoration || image == null) {
                return null;
            }

            IResource resource = PerforceProviderPlugin.getResource(o);
            boolean managedFile = resource != null;

            if (resource instanceof IProject) {
                image = iconCache.getProjectImage(image, (IProject) resource);
            } else if (o instanceof IP4File) {
                IP4File p4Resource = (IP4File) o;
                image = iconCache.getImage(image, p4Resource, false,
                        managedFile);
            } else if (o instanceof IFile) {
                IP4Resource p4Resource = P4ConnectionManager.getManager()
                        .asyncGetResource((IFile) o);
                if (p4Resource instanceof IP4File) {
                    image = iconCache.getImage(image, (IP4File) p4Resource,
                            false, managedFile);
                }
            }
            return image;
        }

        /**
         * Provide text decoration for this object
         * 
         * @param text
         *            the text to decorate
         * @param o
         *            the object to decorate
         * @return the decorated text
         */
        public String decorateText(String text, Object o) {
            if (text != null && !suspendDecoration) {
                if (o instanceof IP4File) {
                    return getFileText(text, (IP4File) o);
                } else if (o instanceof IP4Connection) {
                    if (((IP4Connection) o).isOffline()) {
                        return text + " ["+IPerforceUIConstants.DEC_OFFLINE+"]";
                    }
                } else if (o instanceof IProject) {
                    PerforceTeamProvider provider = PerforceProviderPlugin
                            .getPerforceProviderFor((IResource) o);
                    if (provider != null) {
                        return getProjectText(text, (IProject) o);
                    }
                } else if (o instanceof IResource) {
                    IResource resource = (IResource) o;
                    String decorated = null;
                    if (resource instanceof IFile) {
                        IP4Resource p4Resource = P4ConnectionManager
                                .getManager().asyncGetResource(resource);
                        if (p4Resource instanceof IP4File) {
                            decorated = getFileText(text, (IP4File) p4Resource);
                        }
                    }
                    if (decorateIgnored && IgnoredFiles.isIgnored(resource)) {
                        decorated = text + IGNORED_TEXT;
                    }
                    if (decorated != null) {
                        return decorated;
                    }
                } else if (o instanceof ISynchronizeModelElement) {
                    IResource resource = ((ISynchronizeModelElement) o)
                            .getResource();
                    if (resource instanceof IFile) {
                        IP4Resource p4Resource = P4ConnectionManager
                                .getManager().asyncGetResource(resource);
                        if (p4Resource instanceof IP4File) {
                            return getFileText(text, (IP4File) p4Resource);
                        }
                    }
                }
            }
            return null;
        }

        /**
         * Get decoration text for projects
         * 
         * @param text
         *            the text to decorate
         * @return the decorated text
         */
        private String getProjectText(String text, IProject project) {
            IP4Connection connection = P4ConnectionManager.getManager()
                    .getConnection(project);
            StringBuffer projectText = new StringBuffer();
            if (connection != null) {
                ConnectionParameters params = connection.getParameters();
                boolean decorated = false;
                if ((projectFormat & IPerforceUIConstants.SHOW_PROJECT_PORT) != 0) {
                    append(projectText, params.getPortNoNull(), "");
                    decorated = true;
                }
                if ((projectFormat & IPerforceUIConstants.SHOW_PROJECT_CLIENT) != 0) {
                    append(projectText, params.getClientNoNull(), ", ");
                    decorated = true;
                }
                if ((projectFormat & IPerforceUIConstants.SHOW_PROJECT_USER) != 0) {
                    append(projectText, params.getUserNoNull(), ", ");
                    decorated = true;
                }
                if (connection.isOffline()) {
                    if (projectText.length() > 0) {
                        if (decorated) {
                            text = text + " ["+IPerforceUIConstants.DEC_OFFLINE+", "
                                    + projectText.toString() + "]";
                        } else {
                            text = text + " ["+IPerforceUIConstants.DEC_OFFLINE+"]";
                        }
                    }
                } else if (projectText.length() > 0) {
                    text = text + " [" + projectText.toString() + "]";
                }
            }
            return text;
        }

        /**
         * Get the decorated file text
         * 
         * @param text
         * @param file
         * @return - decorated file text
         */
        public String getFileText(String text, IP4File file) {
            StringBuffer fileText = new StringBuffer();
            if ((fileFormat & IPerforceUIConstants.SHOW_FILE_REVISION) != 0) {
                if (file.getHeadRevision() > 0) {
                    int haveRev = file.getHaveRevision();
                    append(fileText,
                            "#" + haveRev + "/" + file.getHeadRevision(), "");
                }
            }
            if ((fileFormat & IPerforceUIConstants.SHOW_FILE_TYPE) != 0) {
                String type = file.getOpenedType();
                if (type == null) {
                    type = file.getHeadType();
                }
                if (type != null) {
                    append(fileText, "<" + type + ">", " ");
                }
            }
            if ((fileFormat & IPerforceUIConstants.SHOW_FILE_ACTION) != 0) {
                FileAction action = file.getAction();
                String actionText = action != null ? action.toString()
                        .toLowerCase() : null;
                if (action == null) {
                    if (file.getHeadAction() != null
                            && FileAction.DELETE.equals(file.getHeadAction())) {
                        if (file.getHaveRevision() == 0) {
                            actionText = "-deleted-";
                        } else {
                            actionText = "-head rev deleted-";
                        }
                    }
                }
                if (actionText != null) {
                    if (fileText.length() > 0
                            && (fileText.charAt(fileText.length() - 1) != '>')) {
                        fileText.append(" ");
                    }
                    append(fileText, "<" + actionText + ">", "");
                }
            }
            if (fileText.length() > 0) {
                text = text + " " + fileText;
            }
            return text;
        }

        /**
         * Fire event to say that resource labels need to be updated
         * 
         * @param event
         *            the event to broadcast
         */
        private void postLabelEvent(final LabelProviderChangedEvent event) {
            PerforceUIPlugin.asyncExec(new Runnable() {

                public void run() {
                    fireLabelProviderChanged(event);
                }
            });
        }

        /**
         * Append text to buffer, put in seperator if there is already content
         * in the buffer
         * 
         * @param buffer
         *            the string buffer
         * @param text
         *            the text to append
         * @param sep
         *            the seperator to use if we have previous content
         */
        private void append(StringBuffer buffer, String text, String sep) {
            if (buffer.length() > 0) {
                buffer.append(sep);
            }
            buffer.append(text);
        }

        /**
         * Do initialization
         */
        private void init() {

            getPreferences();

        }

        /**
         * Go get the decoration preferences
         */
        private void getPreferences() {
            IPreferenceStore store = PerforceUIPlugin.getPlugin()
                    .getPreferenceStore();
            fileFormat = store.getInt(IPerforceUIConstants.PREF_FILE_FORMAT);
            projectFormat = store
                    .getInt(IPerforceUIConstants.PREF_PROJECT_FORMAT);
            decorateIgnored = store
                    .getBoolean(IPerforceUIConstants.PREF_IGNORED_TEXT);
        }

        /**
         * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
         */
        public void resoureChanged(P4Event event) {
            if (resourceDecorator) {
                postLabelEvent(new LabelProviderChangedEvent(this,
                        event.getLocalResources()));
            } else {
                postLabelEvent(new LabelProviderChangedEvent(this,
                        event.getResources()));
            }
        }

		@Override
		public String getName() {
			return getClass().getSimpleName();
		}
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#setUp()
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (project.members().length <= 2) {
            createLargeProject();
        }
    }

    /**
     * @see com.perforce.team.tests.ProjectBasedTestCase#getPath()
     */
    @Override
    public String getPath() {
        return "//depot/project_decorator_comparison";
    }

    private void createLargeProject() {
        IP4Connection connection = P4Workspace.getWorkspace().getConnection(
                project);
        assertNotNull(connection);

        IFile[] added = new IFile[1000];
        long stamp = System.currentTimeMillis();
        for (int i = 0; i < added.length; i++) {
            added[i] = project.getFile("largeAdd_" + stamp + "_" + i + ".txt");
            assertFalse(added[i].exists());
            try {
                Utils.fillFile(added[i]);
            } catch (Exception e) {
                assertFalse("Failed filling " + added[i].getName(), true);
            }
        }

        AddAction add = new AddAction();
        add.setAsync(false);
        add.selectionChanged(null, new StructuredSelection(added));
        add.run(null);

        IP4File[] p4Files = new IP4File[added.length];
        for (int j = 0; j < added.length; j++) {
            IFile addFile = added[j];
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertTrue(p4File.openedForAdd());
            p4Files[j] = p4File;
        }
        IP4PendingChangelist defaultList = connection.getPendingChangelist(0);
        assertNotNull(defaultList);
        assertEquals(0, defaultList.getId());
        assertTrue(defaultList.isDefault());

        int id = defaultList.submit(
                "unit test large submit for decorator comparison", p4Files, new NullProgressMonitor());
        assertTrue(id > 0);

        for (IFile addFile : added) {
            IP4Resource resource = P4Workspace.getWorkspace().getResource(
                    addFile);
            assertNotNull(resource);
            assertTrue(resource instanceof IP4File);
            IP4File p4File = (IP4File) resource;
            assertFalse(p4File.isOpened());
            assertEquals(1, p4File.getHeadRevision());
            assertNull(p4File.getAction());
            assertFalse(Arrays.asList(defaultList.members()).contains(p4File));
        }

        IP4SubmittedChangelist list = connection.getSubmittedChangelistById(id);
        assertNotNull(list);
        assertEquals(id, list.getId());
        list.refresh();
        IP4Resource[] members = list.members();
        assertNotNull(members);
        assertEquals(added.length, members.length);
    }

    /**
     * Test classic decoration performance
     */
    public void testClassic() {
        final PerforceDecorator classic = new PerforceDecorator(true);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(this.project));
        edit.run(null);
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    assertNotNull(P4ConnectionManager.getManager().getResource(
                            resource));
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        final int[] visited = new int[] { 0 };
        long start = System.currentTimeMillis();
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    if (resource instanceof IFile) {
                        visited[0]++;
                        classic.decorateText(resource.getName(), resource);
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        System.out.println("Decorated: " + visited[0]);
        System.out.println("Elapsed time: "
                + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Test new decoration performance
     */
    public void testNew() {
        final com.perforce.team.ui.decorator.PerforceDecorator updated = new com.perforce.team.ui.decorator.PerforceDecorator(
                true);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(this.project));
        edit.run(null);
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    assertNotNull(P4ConnectionManager.getManager().getResource(
                            resource));
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        final int[] visited = new int[] { 0 };
        long start = System.currentTimeMillis();
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    if (resource instanceof IFile) {
                        visited[0]++;
                        updated.decorateText(resource.getName(), resource);
                    }
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        System.out.println("Decorated: " + visited[0]);
        System.out.println("Elapsed time: "
                + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Compare old and new decoration output
     */
    public void testCompare() {
        final com.perforce.team.ui.decorator.PerforceDecorator updated = new com.perforce.team.ui.decorator.PerforceDecorator(
                true);
        final PerforceDecorator classic = new PerforceDecorator(true);
        EditAction edit = new EditAction();
        edit.setAsync(false);
        edit.selectionChanged(null, new StructuredSelection(this.project));
        edit.run(null);
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    assertNotNull(P4ConnectionManager.getManager().getResource(
                            resource));
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        final int[] visited = new int[] { 0 };
        try {
            this.project.accept(new IResourceVisitor() {

                public boolean visit(IResource resource) throws CoreException {
                    visited[0]++;
                    PerforceDecorator a = classic;
                    String old = a.decorateText(resource.getName(),
                            resource);

                    com.perforce.team.ui.decorator.PerforceDecorator b = updated;
                    String current = b.decorateText(resource.getName(),
                            resource);
                    if(old!=null && current!=null)
                    	assertEquals(old, current);
                    return true;
                }
            });
        } catch (CoreException e) {
            assertFalse("Core exception thrown", true);
        }
        System.out.println("Compared: " + visited[0]);
    }

}
