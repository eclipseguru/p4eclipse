package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.p4api.P4APIPlugin;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;

/**
 * Functions to handle .p4ignore files
 */
public class IgnoredFiles {

    private static final Path P4IGNORE = new Path(".p4ignore"); //$NON-NLS-1$
    private static final String IGNORE_WILDCARD = "*"; //$NON-NLS-1$
    private static final String IGNORE_CHARACTER = "?"; //$NON-NLS-1$

    private static final int CANCEL_IGNORE = 0;
    private static boolean updateDecorations = true;

    /**
     * Filter out ignored files
     * 
     * @param files
     * @return - non-ignored files
     */
    public static IFile[] filterAddFiles(IFile[] files) {
        List<IFile> list = new ArrayList<IFile>();
        // Delete any add file markers
        for (int i = 0; i < files.length; i++) {
            try {
                files[i].deleteMarkers(PerforceMarkerManager.ADDITION_MARKER,
                        false, IResource.DEPTH_ZERO);
            } catch (CoreException e) {
            }
            // Only keep files which are not in Team/Ignore
            if (!PerforceProviderPlugin.isIgnoredHint(files[i])) {
                list.add(files[i]);
            }
        }
        files = list.toArray(new IFile[list.size()]);

        // Remove any files contain in .ignore
        return IgnoredFiles.removeIgnoredFiles(files);
    }

    /**
     * Remove all ignored files from an array of files
     * 
     * @param files
     *            the list of files to check
     * @return the array of files with all ignored files removed
     */
    public static IFile[] removeIgnoredFiles(IFile[] files) {
        List<IFile> list = new ArrayList<IFile>();
        for (IFile file : files)
            if (!isIgnored(file))
                list.add(file);
        return list.toArray(new IFile[list.size()]);
    }

    /**
     * Checks whether or not a resource is contained within a .p4ignore file
     * 
     * @param resource
     *            the resource to check
     * @return returns true if resource is ignored
     */
    public static boolean isIgnored(IResource resource) {
        return isIgnored(resource.getFullPath(), resource.getParent());
    }

    /**
     * Adds a new resource to the .p4ignore file
     * 
     * @param resource
     *            the file to add
     */
    public static void addIgnore(IResource resource) {
        if (resource.getType() == IResource.ROOT) {
            return;
        }
        updateDecorations = true;
        IContainer container = resource.getParent();
        if (container == null || container.getType() == IResource.ROOT) {
            return;
        }
        final IFile ignore = container.getFile(P4IGNORE);
        if (ignore != null) {
            String path = resource.getName() + "\n"; //$NON-NLS-1$
            try {
            	byte[] bytes = path.getBytes(P4CoreUtils.charsetForName(ignore.getCharset()));
                if (!ignore.exists()) {
                    ignore.create(new ByteArrayInputStream(bytes),
                            false, null);
                } else {
                    ignore.appendContents(
                            new ByteArrayInputStream(bytes), false,
                            false, null);
                }
            } catch (Exception e) {
                // Inform the user that the add failed

                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {

                        // Wait for other dialogs to complete
                        while (PerforceUIPlugin.getDisplay().readAndDispatch()) {
                        }

                        String msg = MessageFormat.format(
                                Messages.IgnoredFiles_UnableToIgnoreMessage,
                                ignore.getFullPath());
                        MessageDialog errdlg = new MessageDialog(P4UIUtils
                                .getDialogShell(),
                                Messages.IgnoredFiles_CantUpdate, null, msg,
                                MessageDialog.ERROR,
                                new String[] { Messages.IgnoredFiles_OK },
                                CANCEL_IGNORE);
                        errdlg.open();
                        updateDecorations = false;
                    }
                });
            }
        }

        if (updateDecorations) {
            // Update resource decorations
            IP4Resource p4Resource = P4Workspace.getWorkspace().getResource(
                    resource);
            P4Event event = createIgnoredEvent(p4Resource);
            P4Workspace.getWorkspace().notifyListeners(event);
        }
    }

    private static P4Event createIgnoredEvent(final IP4Resource resource) {
        P4Event event = new P4Event(EventType.IGNORED, resource) {

            @Override
            public IResource[] getLocalResources() {
                if (resource instanceof IP4Container) {
                    final List<IResource> localResources = new ArrayList<IResource>();
                    IContainer[] locals = ((IP4Folder) resource)
                            .getLocalContainers();
                    for (IContainer localFolder : locals) {
                        if (localFolder != null) {
                            localResources.add(localFolder);
                            try {
                                localFolder.accept(new IResourceVisitor() {

                                    public boolean visit(IResource resource)
                                            throws CoreException {
                                        localResources.add(resource);
                                        return true;
                                    }
                                });
                            } catch (CoreException e) {
                                PerforceProviderPlugin.logError(e);
                            }
                        }
                    }
                    return localResources.toArray(new IResource[0]);
                } else {
                    return super.getLocalResources();
                }

            }
        };
        return event;
    }

    /**
     * Checks to see if a path is ignored. Recursively searches up the hierarchy
     * to look for .p4ignore files
     * 
     * @param path
     *            the path to check
     * @param container
     *            the current container to check
     * @return true if the path is ignored
     */
    private static boolean isIgnored(IPath path, IContainer container) {
        boolean found = false;
        if (container == null || container.getType() == IContainer.ROOT) {
            return false;
        }
        IFile ignore = container.getFile(P4IGNORE);
        if (ignore != null && ignore.exists()) {
            try {
                String name = path.lastSegment();
                boolean tryAgain = true;
                InputStream contents = null;
                while ((contents == null) && tryAgain && ignore.exists()) {
                    try {
                        contents = ignore.getContents();
                        tryAgain = false;
                    } catch (CoreException cx) {
                        // This is probably an out-of-sync exception, so try
                        // refreshing it...

                        PerforceProviderPlugin
                                .log(new Status(
                                        IStatus.INFO,
                                        P4APIPlugin.ID,
                                        IStatus.INFO,
                                        "Refreshing .p4ignore file '" + ignore.getFullPath() + "'", //$NON-NLS-1$ //$NON-NLS-2$
                                        null));
                        try {
                            ignore.refreshLocal(IResource.DEPTH_ZERO, null);
                            tryAgain = true;
                        } catch (CoreException e1) {
                            // Oh well. At least we tried...
                            tryAgain = false;
                        }
                    }
                }
                if (contents != null) {
                    found = matchNames(contents, name, ignore.getCharset());
                }
            } catch (Exception e) {
                PerforceProviderPlugin
                        .log(new Status(
                                IStatus.WARNING,
                                P4APIPlugin.ID,
                                IStatus.WARNING,
                                "Error reading .p4ignore file '" + ignore.getFullPath() + "'", //$NON-NLS-1$ //$NON-NLS-2$
                                null));
            }
        }
        if (!found) {
            IContainer parent = container.getParent();
            if (parent.getType() == IResource.ROOT) {
                return false;
            }
            return isIgnored(path.removeLastSegments(1), parent);
        }
        return true;
    }

    /**
     * Attempt to match a name against the lines in a particular p4ignore file.
     * Implements a very simple regex-based matching that only kicks in if the
     * line contains a "*" character; if no such character is found, it's a
     * straight match (or not). If there <i>is</i> such a character, we use the
     * simple StringMatcher class from Eclipse to so the matching, which luckily
     * matches the expected behaviour for CVS and the global ignore setups.
     * (Actually, we're forced to use our own copy of the class due to
     * visibility issues).
     * 
     * Note that this will always match against the first line that qualifies;
     * note also that currently the only way to get wildcards (etc.) into the
     * ignore file is manually.
     * 
     * @param contents
     *            -- non null InputStream to be read
     * @param name
     *            -- String name of file to be matched
     * @param charset
     *            -- charset name of contents
     * @return true iff there's at least one matching line in the file; false
     *         otherwise
     * @throws IOException
     *             if there are any IO errors during the match
     */
    private static boolean matchNames(InputStream contents, String name, String charset)
            throws IOException {

        boolean found = false;
        BufferedReader input = new BufferedReader(new InputStreamReader(
                contents, charset));

        if (input != null) {
            for (;;) {
                String line = input.readLine();
                if (line == null) {
                    break;
                } else {
                    line = line.trim();
                    // If the line contains a "*" char, try matching it,
                    // otherwise do an exact match
                    if (line.contains(IGNORE_WILDCARD)
                            || line.contains(IGNORE_CHARACTER)) {
                        PerforceStringMatcher matcher = new PerforceStringMatcher(
                                line, false, false);

                        if (matcher.match(name)) {
                            found = true;
                            break;
                        }
                    } else if (line.equals(name)) {
                        found = true;
                        break;
                    }
                }
            }
            input.close();
        }
        return found;
    }
}
