package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.p4java.core.file.FileSpecOpStatus;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.IProjectSettingsChangeListener;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.PerforceTeamProvider;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.team.ui.decorator.PerforceDecorator;
import com.perforce.team.ui.operations.RetryableOperation;
import com.perforce.team.ui.p4java.actions.AddAction;
import com.perforce.team.ui.p4java.actions.RevertEditAction;

/**
 * Perforce marker manager
 */
public class PerforceMarkerManager implements IProjectSettingsChangeListener,
        IPropertyChangeListener, IResourceChangeListener, IResourceDeltaVisitor {

    /**
     * ADDITION_MARKER
     */
    public static final String ADDITION_MARKER = "com.perforce.team.core.perforceadd"; //$NON-NLS-1$

    private List<IFile> resources = new ArrayList<IFile>();

    /**
     * Creates a new marker manager for perforce associated projects
     */
    public PerforceMarkerManager() {
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .addPropertyChangeListener(this);
        PerforceProviderPlugin.addProjectSettingsChangeListener(this);
    }

    /**
     * @see com.perforce.team.core.IProjectSettingsChangeListener#projectSettingsChanged(org.eclipse.core.resources.IProject,
     *      com.perforce.team.core.ConnectionParameters)
     */
    public void projectSettingsChanged(IProject project,
            ConnectionParameters params) {
        // If projects no longer managed by Perforce then delete markers
        if (PerforceTeamProvider.getPerforceProvider(project) == null) {
            try {
                project.deleteMarkers(ADDITION_MARKER, false,
                        IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(IPerforceUIConstants.PREF_SHOW_MARKERS)) {
			boolean show = true;
			Object newVal = event.getNewValue();
			if (newVal instanceof Boolean) {
				show = ((Boolean) newVal).booleanValue();
			} else if (newVal instanceof String) {
				try {
					show = Boolean.parseBoolean((String) newVal);
				} catch (Throwable t) {
				}
			}
			if (!show) {
				IWorkspaceRoot root = PerforceProviderPlugin.getWorkspaceRoot();
				try {
					root.deleteMarkers(ADDITION_MARKER, false,
							IResource.DEPTH_INFINITE);
				} catch (CoreException e) {
					PerforceProviderPlugin.logError(e);
				}
			}
		}
	}

    /**
     * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
     */
    public void resourceChanged(IResourceChangeEvent event) {
        final int FSTAT_BATCH_SIZE = 1000; // See usage below...
        boolean showMarkers = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_SHOW_MARKERS);
        boolean openForAdd = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_NEW_OPEN_ADD);
        boolean disableMarkerDecoration = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_DISABLE_MARKER_DECORATION);

        // Ignore marker decoration altogether
        if (disableMarkerDecoration) {
        	return;
        }

        IResourceDelta root = event.getDelta();
        IResourceDelta[] projectDeltas = root.getAffectedChildren();

        for (int i = 0; i < projectDeltas.length; i++) {
            IResourceDelta delta = projectDeltas[i];
            IResource resource = delta.getResource();
            String resPath = PerforceProviderPlugin.getResourcePath(resource);
            PerforceTeamProvider provider = PerforceTeamProvider
                    .getPerforceProvider(resource);

            if (provider != null) {
                try {
                    resources.clear();
                    delta.accept(this);

                    // Files in this.resources have been filtered through the
                    // visit() callback method
                    // called through the accept() above. Any files now in
                    // this.resources
                    // are actual non-ignored added files only; we need to
                    // process any files there
                    // to see if they need to be annotated or marked for add or
                    // just annotated as
                    // Perforce-managed.

                    if (resources.size() > 0) {
                        IFile[] markerResources = resources
                                .toArray(new IFile[resources.size()]);
                        final IP4Connection con = P4ConnectionManager
                                .getManager().getConnection(
                                        resource.getProject());
                        if (con != null && !con.isOffline()) {
                            IClient client = con.getClient();
                            if (client == null) {
                                return;
                            }
                            // Check what resources are managed by Perforce, and
                            // either annotate
                            // them accordingly, or add them to the depot.
                            String[] statFileNames = new String[resources
                                    .size()];
                            for (int j = 0; j < statFileNames.length; j++) {
                                statFileNames[j] = markerResources[j]
                                        .getLocation().toOSString();
                            }

                            // First find out which resources the P4 server
                            // thinks we have:

                            List<IFile> unmanagedList = new ArrayList<IFile>();
                            List<IFile> managedList = new ArrayList<IFile>();
                            List<IFile> managedUndeleteList = new ArrayList<IFile>();

                            // we use the fstat instead of have to include the status in
                            // pending changelist, then check the status to make sure
                            // there is no existing file spec before we add.
                            // In case pending changelists contains a just branched file,
                            // an add could change the action from branch to add.
                            // see http://caseportal.perforce.com/case/00044003

                            // split the changed resources into batches for fstat
                    		List<List<String>> batches = getBatches(Arrays.asList(statFileNames), FSTAT_BATCH_SIZE);
							if (batches != null && batches.size() > 0) {
								for (List<String> batch : batches) {
									if (batch != null && batch.size() > 0) {
										List<IFileSpec> statList = RetryableOperation.fstatWithRetry(con,
												P4FileSpecBuilder.makeFileSpecList(batch.toArray(new String[batch.size()])));
										for (int k = 0; k < batch.size(); k++) {
											if (k < statList.size() && statList.get(k).getOpStatus() == FileSpecOpStatus.VALID) {
												if (statList.get(k).getAction() == FileAction.DELETE) {
		                                    		// job068460: P4Eclipse looses checked in file
		                            				// copy and paste a file to a diff folder same name loose file
													managedUndeleteList.add(markerResources[k]);
												} else
													managedList.add(markerResources[k]);
											} else {
												unmanagedList.add(markerResources[k]);
											}
										}
									}
								}
							}

                            IFile[] resourcesToAdd = unmanagedList
                                    .toArray(new IFile[unmanagedList.size()]);
                            IFile[] resourcesToRefresh = managedList
                                    .toArray(new IFile[managedList.size()]);
                            IFile[] resourcesToUndelete = managedUndeleteList.toArray(new IFile[managedUndeleteList.size()]);

                            if(resourcesToUndelete.length>0){
                                final Display currentDisplay = PerforceUIPlugin
                                        .getDisplay();
                                final IFile[] undeleteResourcesCopy = resourcesToUndelete;

                                currentDisplay.syncExec(new Runnable() {

                                    public void run() {
                                        RevertEditAction editDeleteAction = new RevertEditAction();
                                        editDeleteAction.setAsync(false);
                                        editDeleteAction
                                                .selectionChanged(
                                                        null,
                                                        new StructuredSelection(
                                                                undeleteResourcesCopy));
                                        editDeleteAction.run(null);
                                    }
                                });

                            }

                            if (resourcesToRefresh.length > 0) {
                                PerforceDecorator decorator = PerforceDecorator
                                        .getActivePerforceDecorator();
                                if (decorator != null) {
                                    decorator
                                            .resourceStateChanged(resourcesToRefresh);
                                }
                            }

                            if (resourcesToAdd.length > 0) {
                                if (showMarkers) {
                                    for (int j = 0; j < resourcesToAdd.length; j++) {
                                        try {
                                            IMarker[] existing = resourcesToAdd[j]
                                                    .findMarkers(
                                                            ADDITION_MARKER,
                                                            false, 0);
                                            if (existing == null
                                                    || existing.length == 0) {
                                                IMarker marker = resourcesToAdd[j]
                                                        .createMarker(ADDITION_MARKER);
                                                marker.setAttribute(
                                                        IMarker.SEVERITY,
                                                        IMarker.SEVERITY_INFO);
                                                marker.setAttribute(
                                                        IMarker.MESSAGE,
                                                        Messages.PerforceMarkerManager_ADDITION);
                                            }
                                        } catch (CoreException e) {
                                            PerforceProviderPlugin.logError(e);
                                        }

                                    }
                                } else if (openForAdd) {
                                    final Display currentDisplay = PerforceUIPlugin
                                            .getDisplay();
                                    final IFile[] unmanagedResourcesCopy = resourcesToAdd;
                                    currentDisplay.syncExec(new Runnable() {

                                        public void run() {
                                            AddAction addAction = new AddAction();
                                            addAction.setAsync(false);
                                            addAction.setMakeWritable(true);
                                            addAction
                                                    .selectionChanged(
                                                            null,
                                                            new StructuredSelection(
                                                                    unmanagedResourcesCopy));
                                            addAction.run(null);
                                        }
                                    });

                                }

                            }
                        }
                    }
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                } catch (Throwable thr) {
                    PerforceUIPlugin.log(new Status(IStatus.ERROR,
                            PerforceUIPlugin.ID, IStatus.ERROR, thr
                                    .getMessage(), thr));
                }
            }
        }
    }

    /**
     * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
     */
    public boolean visit(IResourceDelta delta) {
        IResource resource = delta.getResource();
        Tracing.printTrace("VISIT DELTA","deltaType={0}, {1}", delta.getKind(), resource.getName());
        if ((delta.getKind() == IResourceDelta.ADDED || delta.getKind()==IResourceDelta.CHANGED)
                && resource instanceof IFile) {
            if (!(PerforceProviderPlugin.isIgnoredHint(resource) || IgnoredFiles
                    .isIgnored(resource))) {
                resources.add((IFile) resource);
            }
        }
        return true;
    }

    /**
     * Split a large list into multiple smaller lists.
     */
	private static <T> List<List<T>> getBatches(List<T> list, final int len) {
		if (list != null && len > 0) {
			List<List<T>> parts = new ArrayList<List<T>>();
			final int n = list.size();
			for (int i = 0; i < n; i += len) {
				parts.add(list.subList(i, Math.min(n, i + len)));
			}
			return parts;
		}

		return null;
	}
}
