/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Container;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Resource.Type;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IgnoredFiles;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.actions.PerforceTeamAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class P4Action extends PerforceTeamAction implements IWorkbenchWindowActionDelegate {

	/**
	 * ACTION_EXTENSION_POINT
	 */
	public static final String ACTION_EXTENSION_POINT = "com.perforce.team.ui.action"; //$NON-NLS-1$

	private boolean async = true;

	/**
	 * P4 collection set by {@link #setCollection(P4Collection)}
	 */
	protected P4Collection collection = null;

	private IProgressMonitor monitor = null;

	/**
	 * Returns true if there are any containers in the current selection.
	 *
	 * @return - true if any {@link IContainer} objects are in the current selection
	 */
	protected boolean containsContainers() {
		if (getSelection() != null) {
			final Object[] selected = getSelection().toArray();
			for (Object select : selected) {
				select = getResource(select);
				if (select instanceof IContainer)
					return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if at least one online connection is in the current selection
	 *
	 * @return -true if any online collections ared in the current selection
	 */
	protected boolean containsOnlineConnection() {
		boolean contains = false;
		if (getSelection() != null) {
			final Object[] selected = getSelection().toArray();
			for (final Object select : selected) {
				IP4Connection connection = null;
				if ((select instanceof IP4Resource) && !((IP4Resource) select).isReadOnly()) {
					connection = ((IP4Resource) select).getConnection();
				} else {
					final IResource resource = getResource(select);
					if (resource != null) {
						final IProject project = resource.getProject();
						connection = P4ConnectionManager.getManager().getConnection(project, false);
					}
				}
				if ((connection != null) && !connection.isOffline()) {
					contains = true;
					break;
				}
			}
		}
		return contains;
	}

	/**
	 * Does the selection only contain resources that all come from the same online
	 * connection?
	 *
	 * @return - true if all resources in the selection are associated with the same
	 *         online connection
	 */
	protected boolean containsSingleOnlineConnection() {
		IP4Connection first = null;
		if (getSelection() != null) {
			final Object[] selected = getSelection().toArray();
			for (final Object select : selected) {
				IP4Connection connection = null;
				if ((select instanceof IP4Resource) && !((IP4Resource) select).isReadOnly()) {
					connection = ((IP4Resource) select).getConnection();
				} else {
					final IResource resource = getResource(select);
					if (resource != null) {
						final IProject project = resource.getProject();
						connection = P4ConnectionManager.getManager().getConnection(project, false);
					}
				}
				if (first == null) {
					first = connection;
				} else if (!first.equals(connection)) {
					// Duplicate found, clear first and break
					first = null;
					break;
				}
			}
		}
		return (first != null) && !first.isOffline();
	}

	/**
	 * Creates a collection with the proper error handler
	 *
	 * @return - error handling collection
	 */
	protected P4Collection createCollection() {
		return P4ConnectionManager.getManager().createP4Collection();
	}

	/**
	 * Creates a collection with the proper error handler
	 *
	 * @param resources
	 * @return = error handling collection
	 */
	protected P4Collection createCollection(final IP4Resource[] resources) {
		return P4ConnectionManager.getManager().createP4Collection(resources);
	}

	/**
	 * Generates a human-readable title for the runnable that this action is using
	 *
	 * @param command
	 * @param resources
	 * @param type
	 * @return - title
	 */
	protected String generateTitle(final String command, final IP4Resource[] resources, final Type type) {
		final StringBuilder title = new StringBuilder();
		if (command != null) {
			title.append(command);
		}
		if (resources.length == 1) {
			final String path = resources[0].getActionPath(type);
			if (path != null) {
				title.append(' ');
				title.append(path);
			}
		} else if (resources.length > 1) {
			int fileCount = 0;
			int folderCount = 0;
			for (final IP4Resource resource : resources) {
				if (resource instanceof IP4File) {
					fileCount++;
				} else if (resource instanceof IP4Container) {
					folderCount++;
				}
			}
			if ((fileCount > 0) || (folderCount > 0)) {
				if (folderCount > 0) {
					title.append(MessageFormat.format(Messages.P4Action_NumFolders, folderCount));
				}
				if (fileCount > 0) {
					title.append(", "); //$NON-NLS-1$
				}
			}
			if (fileCount > 0) {
				title.append(MessageFormat.format(Messages.P4Action_NumFiles, fileCount));
			}
		}
		return title.toString();
	}

	/**
	 * Generates a human-readable title for the runnable that this action is using
	 *
	 * @param command
	 * @param collection
	 * @return - title
	 */
	protected String generateTitle(final String command, final P4Collection collection) {
		return generateTitle(command, collection.members(), collection.getType());
	}

	/**
	 * Get all children, including files contained in sub-folders, in the container.
	 * Returns files found at all depths inside the passed in container
	 *
	 * @param container
	 * @return - list of files
	 */
	protected List<IFile> getAllChildren(final IContainer container) {
		final List<IFile> files = new ArrayList<>();
		try {
			final IResource[] resources = container.members(IContainer.EXCLUDE_DERIVED);
			for (final IResource resource : resources) {
				if (resource instanceof IContainer) {
					files.addAll(getAllChildren((IContainer) resource));
				} else if (resource instanceof IFile) {
					files.add((IFile) resource);
				}
			}
		} catch (final CoreException e) {
			PerforceProviderPlugin.logError(e);
		}
		return files;
	}

	public P4Collection getCollection(final Object[] selected) {
		final P4Collection collection = createCollection();
		for (Object select : selected) {
			if (select instanceof IP4Resource) {
				collection.add((IP4Resource) select);
			} else {
				select = getResource(select);
			}
			if (select instanceof IResource) {
				// Use local path if any local resources are in the
				// selection
				collection.setType(IP4Resource.Type.LOCAL);
				if (!PerforceProviderPlugin.isIgnoredHint((IResource) select)) {
					final IP4Resource resource = P4ConnectionManager.getManager().getResource((IResource) select);
					if (resource != null) {
						collection.add(resource);
					}
				}
			}
		}
		return collection;
	}

	/**
	 * Gets any connections in this selection or project that are associated with a
	 * connection
	 *
	 * @return - connection collection
	 */
	protected P4Collection getConnectionSelection() {
		if (collection == null) {
			final Object[] selected = getSelection().toArray();
			final P4Collection collection = createCollection();
			for (Object select : selected) {
				if (select instanceof IP4Connection) {
					collection.add((IP4Connection) select);
				} else {
					select = getResource(select);
				}
				if (select instanceof IProject) {
					final IP4Connection connection = P4ConnectionManager.getManager().getConnection((IProject) select,
							false);
					collection.add(connection);
				}
			}
			return collection;
		} else
			return collection;
	}

	/**
	 * Gets a collection of directly selected p4 files. This process only elements
	 * in the selection, not children. To get the files that are children of
	 * selection containers use {@link #getFileSelection()}
	 *
	 * @return - p4 collection containing only p4 files elements currently in the
	 *         selection object
	 */
	protected P4Collection getDirectFileSelection() {
		final P4Collection fileCollection = createCollection();
		if (collection == null) {
			final Object[] selected = getSelection().toArray();
			final P4Collection collection = createCollection();
			for (final Object select : selected) {
				if (select instanceof IP4File) {
					collection.add((IP4File) select);
				} else {
					final IP4File adapted = getP4File(select);
					if (adapted != null) {
						collection.add(adapted);
					} else {
						final IResource resource = getResource(select);
						if (resource != null) {
							if (!PerforceProviderPlugin.isIgnoredHint(resource)) {
								final IP4Resource p4resource = P4ConnectionManager.getManager().getResource(resource);
								if (p4resource instanceof IP4File) {
									collection.add(p4resource);
								}
							}
						}
					}
				}
			}
			return collection;
		} else {
			for (final IP4Resource resource : collection.members()) {
				final IP4File file = getP4File(resource);
				if (file != null) {
					fileCollection.add(file);
				}
			}
		}
		return fileCollection;
	}

	/**
	 * Gets a p4 collection of p4 files found in the selection and parent of any
	 * select elements that are containers
	 *
	 * @return - p4 collection of p4 files
	 */
	protected P4Collection getFileSelection() {
		if (collection == null) {
			final Object[] selected = getSelection().toArray();
			final P4Collection collection = createCollection();
			for (Object select : selected) {
				if (select instanceof IP4Resource) {
					collection.add((IP4Resource) select);
				} else {
					select = getResource(select);
				}
				if (select instanceof IContainer) {
					// Use local path if any local resources are in the
					// selection
					collection.setType(IP4Resource.Type.LOCAL);
					final List<IFile> files = getAllChildren((IContainer) select);
					for (final IFile file : files) {
						if (!PerforceProviderPlugin.isIgnoredHint(file)) {
							final IP4Resource resource = P4ConnectionManager.getManager().getResource(file);
							collection.add(resource);
						}
					}
				} else if (select instanceof IResource) {
					// Use local path if any local resources are in the
					// selection
					collection.setType(IP4Resource.Type.LOCAL);
					if (!PerforceProviderPlugin.isIgnoredHint((IResource) select)) {
						final IP4Resource resource = P4ConnectionManager.getManager().getResource((IResource) select);
						collection.add(resource);
					}
				}
			}
			return collection;
		} else
			return collection;
	}

	/**
	 * Get all the currently selected {@link IResource} objects. May return null if
	 * the collection has been set explicitly on this action.
	 *
	 * @return - list of resources
	 */
	protected List<IResource> getLocalResourceSelection() {
		List<IResource> resources = null;
		if (collection == null) {
			resources = new ArrayList<>();
			final Hashtable<RepositoryProvider, List<IResource>> providerResources = getProviderMapping();
			for (final Map.Entry<RepositoryProvider, List<IResource>> entry : providerResources.entrySet()) {
				final List<IResource> resourceList = entry.getValue();
				final IResource[] resourceArray = resourceList.toArray(new IResource[resourceList.size()]);
				for (final IResource resource : resourceArray) {
					resources.add(resource);
				}
			}
		}
		return resources;
	}

	protected IProgressMonitor getMonitor() {
		IProgressMonitor current = monitor;
		if (current == null) {
			current = new NullProgressMonitor();
		}
		return current;
	}

	/**
	 * Get provider map. This should be used over {@link #getProviderMapping()}
	 * since this pools resources from different projects but associated with the
	 * same connection.
	 *
	 * @return - connections mapped to resource lists
	 */
	protected Map<IP4Connection, List<IResource>> getProviderMap() {
		final Map<IP4Connection, List<IResource>> providerMap = new HashMap<>();
		final Hashtable<RepositoryProvider, List<IResource>> providerResources = getProviderMapping();
		for (final Map.Entry<RepositoryProvider, List<IResource>> entry : providerResources.entrySet()) {
			final List<IResource> resourceList = entry.getValue();
			if (resourceList.size() > 0) {
				final IResource firstResource = resourceList.get(0);
				final IP4Connection connection = P4ConnectionManager.getManager()
						.getConnection(firstResource.getProject());
				if ((connection != null) && !connection.isOffline()) {
					List<IResource> resources = providerMap.get(connection);
					if (resources == null) {
						resources = new ArrayList<>();
						providerMap.put(connection, resources);
					}
					resources.addAll(resourceList);
				}
			}
		}
		return providerMap;
	}

	/**
	 * Gets a collection of corresponding p4 resources for each local resource found
	 * in the selection
	 *
	 * @return - collection of p4 resources
	 */
	protected P4Collection getResourceSelection() {
		if (collection == null) {
			final Object[] selected = getSelection().toArray();
			return getCollection(selected);
		} else
			return collection;
	}

	/**
	 * Get number of resources in the selection
	 *
	 * @return - selection size
	 */
	protected int getSelectionSize() {
		int size = 0;
		if (collection != null) {
			size = collection.members().length;
		} else if (getSelection() != null) {
			size = getSelection().size();
		}
		return size;
	}

	/**
	 * Gets the single connection in the selection if only one exists, else returns
	 * null. To be used by actions that specify enablesFor=1 in the plugin.xml.
	 *
	 * @return - single selected connection or null
	 */
	protected IP4Connection getSingleConnectionSelection() {
		final IP4Resource resource = getSingleResourceSelection();
		if (resource instanceof IP4Connection)
			return (IP4Connection) resource;
		return null;
	}

	/**
	 * Gets the single file in the selection if only one exists, else returns null.
	 * To be used by actions that specify enablesFor=1 in the plugin.xml.
	 *
	 * @return - single selected file or null
	 */
	protected IP4File getSingleFileSelection() {
		return P4CoreUtils.convert(getSingleResourceSelection(), IP4File.class);
	}

	/**
	 * Gets the single online connection in the selection if only one exists, else
	 * returns null. To be used by actions that specify enablesFor=1 in the
	 * plugin.xml.
	 *
	 * @return - single selected online connection or null
	 */
	protected IP4Connection getSingleOnlineConnectionSelection() {
		final IP4Connection connection = getSingleConnectionSelection();
		return (connection != null) && !connection.isOffline() ? connection : null;
	}

	/**
	 * Gets the single online resource in the selection if only one exists, else
	 * returns null. To be used by actions that specify enablesFor=1 in the
	 * plugin.xml.
	 *
	 * @return - single selected online resource or null
	 */
	protected IP4Resource getSingleOnlineResourceSelection() {
		final IP4Resource resource = getSingleResourceSelection();
		return (resource != null) && !resource.getConnection().isOffline() ? resource : null;
	}

	/**
	 * Gets the single resource in the selection if only one exists, else returns
	 * null. To be used by actions that specify enablesFor=1 in the plugin.xml.
	 *
	 * @return - single selected resource or null
	 */
	protected IP4Resource getSingleResourceSelection() {
		IP4Resource resource = null;
		if (collection == null) {
			final Object[] selected = getSelection().toArray();
			if (selected.length == 1) {
				final Object select = selected[0];
				resource = P4CoreUtils.convert(select, IP4Resource.class);
				if (resource == null) {
					final IResource r = getResource(select);
					if (r != null) {
						if (!PerforceProviderPlugin.isIgnoredHint(r)) {
							resource = P4ConnectionManager.getManager().getResource(r);
						}
					}
				}
			}
		} else {
			final IP4Resource[] members = collection.members();
			if (members.length == 1) {
				resource = members[0];
			}
		}
		return resource;
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	@Override
	public void init(final IWorkbenchWindow window) {

	}

	/**
	 * @return the async
	 */
	public boolean isAsync() {
		return async;
	}

	/**
	 * @see com.perforce.team.ui.actions.PerforceTeamAction#isEnabledEx()
	 */
	@Override
	protected boolean isEnabledEx() throws TeamException {
		return true;
	}

	/**
	 * Is the resource ignored account to .p4ignore, filtered resources, and derived
	 * flag?
	 *
	 * @param resource
	 * @return - true if resource is ignored
	 */
	protected boolean isResourceIgnored(final IResource resource) {
		return PerforceProviderPlugin.isIgnoredHint(resource) || IgnoredFiles.isIgnored(resource);
	}

	/**
	 * Does the specified p4 file have at least one depot revision
	 *
	 * @param file
	 * @return - true if revision exists can be inferred through inspecting the file
	 */
	protected boolean revisionExists(final IP4File file) {
		boolean enabled = false;
		if (file != null) {
			if (file.getHeadRevision() > 0) {
				enabled = true;
			} else {
				final IFileSpec spec = file.getP4JFile();
				enabled = (spec != null) && (spec.getEndRevision() > 0);
			}
		}
		return enabled;
	}

	/**
	 * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(final IAction action) {
		if ((collection != null) || ((getSelection() != null) && !getSelection().isEmpty())) {
			runAction();
		}
	}

	/**
	 * Runs the action
	 */
	protected abstract void runAction();

	/**
	 * Runs a runnable either directly or via a call to P4Runner.schedule is
	 * {@link #isAsync()} is true
	 *
	 * @param runnable
	 * @see P4Runner#schedule(IP4Runnable)
	 */
	protected void runRunnable(final IP4Runnable runnable) {
		if (runnable != null) {
			if (isAsync()) {
				P4Runner.schedule(runnable);
			} else {
				Tracing.printTrace(this.getClass().getSimpleName(), "Running synchronously: {0}", runnable);
				runnable.run(getMonitor());
			}
		}
	}

	/**
	 * @param async the async to set
	 */
	public void setAsync(final boolean async) {
		this.async = async;
	}

	/**
	 * @param collection the collection to set
	 */
	public void setCollection(final P4Collection collection) {
		this.collection = collection;
	}

	/**
	 * @param monitor the monitor to set
	 */
	public void setMonitor(final IProgressMonitor monitor) {
		this.monitor = monitor;
	}

	/**
	 * Update the action state, should be called at the end of the runnable
	 */
	protected void updateActionState() {
		// Need to refresh main menu actions
		PerforceTeamAction.refreshActionState();
	}
}
