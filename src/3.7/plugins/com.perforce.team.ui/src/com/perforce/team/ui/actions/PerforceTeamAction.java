/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.perforce.team.ui.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.ui.IWorkbenchPage;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.actions.stub.TeamActionCopy;

/**
 * The abstract superclass of all Team actions. This class contains some
 * convenience methods for getting selected objects and mapping selected objects
 * to their providers.
 * <p/>
 * Team providers may subclass this class when creating their actions. Team
 * providers may also instantiate or subclass any of the subclasses of
 * TeamAction provided in this package.
 * <p/>
 * The common pattern for using this action is to use it as command handler,
 * or use it as an action delegate:
 *
 * <pre>
 *      SyncRevisionAction action = new SyncRevisionAction();
 *      action.selectionChanged(null, new StructuredSelection(file));
 *      action.runAction(revision.getRevision());
 *      ...or
 *
 *      EditJobAction edit = new EditJobAction();
 *      edit.selectionChanged(null, new StructuredSelection(selected));
 *      edit.doubleClick(null);
 *      ...or
 *
 *      AddAction add = new AddAction();
 *      add.setAsync(false);
 *      add.selectionChanged(null, new StructuredSelection(file));
 *      add.run(null);
 * </pre>
 *
 */
public abstract class PerforceTeamAction extends TeamActionCopy {

//    /**
//     * The current selection
//     */
//    private IStructuredSelection selection;
//
//    /**
//     * The shell, required for the progress dialog
//     */
//    private Shell shell;

    private static WeakHashMap<IAction, PerforceTeamAction> storedActions = new WeakHashMap<IAction, PerforceTeamAction>();

    // Constants for determining the type of progress. Subclasses may
    // pass one of these values to the run method.

//    /**
//     * PROGRESS_DIALOG
//     */
//    public static final int PROGRESS_DIALOG = 1;
//
//    /**
//     * PROGRESS_BUSYCURSOR
//     */
//    public static final int PROGRESS_BUSYCURSOR = 2;

//    private IWorkbenchPart targetPart;

//    /**
//     * Returns the selected projects.
//     *
//     * @return the selected projects
//     */
//    protected IProject[] getSelectedProjects() {
//        List<IProject> projects = null;
//        if (!getSelection().isEmpty()) {
//            projects = new ArrayList<IProject>();
//            Iterator<?> elements = getSelection().iterator();
//            while (elements.hasNext()) {
//                Object next = elements.next();
//                if (next instanceof IProject) {
//                    projects.add((IProject) next);
//                    continue;
//                }
//                if (next instanceof IAdaptable) {
//                    IAdaptable a = (IAdaptable) next;
//                    Object adapter = a.getAdapter(IResource.class);
//                    if (adapter instanceof IProject) {
//                        projects.add((IProject) adapter);
//                        continue;
//                    }
//                }
//            }
//        }
//        if (projects != null && !projects.isEmpty()) {
//            IProject[] result = new IProject[projects.size()];
//            projects.toArray(result);
//            return result;
//        }
//        return new IProject[0];
//    }
//
//    /**
//     * Returns the selected resources.
//     *
//     * @return the selected resources
//     */
//    protected IResource[] getSelectedResources() {
//        List<IResource> resources = null;
//        if (!getSelection().isEmpty()) {
//            resources = new ArrayList<IResource>();
//            Iterator<?> elements = getSelection().iterator();
//            while (elements.hasNext()) {
//                IResource resource = getResource(elements.next());
//                if (resource != null) {
//                    resources.add(resource);
//                }
//            }
//        }
//        if (resources != null && !resources.isEmpty()) {
//            IResource[] result = new IResource[resources.size()];
//            resources.toArray(result);
//            return result;
//        }
//        return new IResource[0];
//    }

    /**
     * Get resource from object
     *
     * @param obj
     * @return - resource or null if object couldn't be converted
     */
    protected IP4Resource getP4Resource(Object obj) {
        return P4CoreUtils.convert(obj, IP4Resource.class);
    }

    /**
     * Get file from object
     *
     * @param obj
     * @return - resource or null if object couldn't be converted
     */
    protected IP4File getP4File(Object obj) {
        return P4CoreUtils.convert(obj, IP4File.class);
    }

    /**
     * Get resource from object
     *
     * @param obj
     * @return - resource or null if object couldn't be converted
     */
    protected IResource getResource(Object obj) {
        return P4CoreUtils.convert(obj, IResource.class);
    }

    /**
     * Convenience method for getting the current shell.
     *
     * @return the shell
     */
	@Override
    protected Shell getShell() {
    	Shell s = super.getShell();
        if (s != null) {
            return s;
        } else {
        	s=P4UIUtils.getShell();
        	if(s!=null)
        		return s;

        	// check if we are running inside UI thread
        	if(Display.getCurrent()!=null)
        		return Display.getCurrent().getActiveShell();
            // return
            // PerforceUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow().getShell();
        }
        return null;
    }

	@Override
	protected IStructuredSelection getSelection() {
		// TODO Auto-generated method stub
		return super.getSelection();
	}

	@Override
	protected IProject[] getSelectedProjects() {
		// TODO Auto-generated method stub
		return super.getSelectedProjects();
	}

	@Override
	protected IResource[] getSelectedResources() {
		// TODO Auto-generated method stub
		return super.getSelectedResources();
	}

	@Override
	protected IWorkbenchPage getTargetPage() {
		// TODO Auto-generated method stub
		return super.getTargetPage();
	}

//    /**
//     * sets shell for action
//     *
//     * @param shell
//     */
//    public void setShell(Shell shell) {  //can use super also
//        this.shell = shell;
//    }

    /**
     * This function is required for the Perforce main menu as the enablement
     * state is not refreshed unless the selection is changed. Therefore when an
     * action is carried out this needs to be called to refresh the state.
     *
     */
    public static void refreshActionState() {
        for (Iterator<IAction> it = storedActions.keySet().iterator(); it
                .hasNext();) {
            IAction delegate = it.next();
            PerforceTeamAction action = storedActions.get(delegate);
            action.setActionEnablement(delegate);
        }
    }

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
     *      org.eclipse.jface.viewers.ISelection)
     */
    @Override
    public void selectionChanged(IAction action, ISelection selection) {
    	super.selectionChanged(action, selection);
    	if(action!=null)
            storedActions.put(action, this);

//        if (selection instanceof IStructuredSelection) {
//            this.selection = (IStructuredSelection) selection;
//            if (action != null) {
//                setActionEnablement(action);
//                storedActions.put(action, this);
//            }
//        }
    }

    /**
     * Method invoked from <code>selectionChanged(IAction, ISelection)</code> to
     * set the enablement status of the action. The instance variable
     * <code>selection</code> will contain the latest selection so the methods
     * <code>getSelectedResources()</code> and
     * <code>getSelectedProjects()</code> will provide the proper objects.
     *
     * This method can be overridden by subclasses but should not be invoked by
     * them.
     *
     * @param action
     */
    protected void setActionEnablement(IAction action) {
    	action.setEnabled(false);
    	P4Runner.schedule( (monitor) ->  {
    		try {
                action.setEnabled(isEnabledEx());
            } catch (TeamException e) {
                if (e.getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
                    // Enable the action to allow the user to discover the problem
                    action.setEnabled(true);
                } else {
                    action.setEnabled(false);
                    // We should not open a dialog when determining menu enablements
                    // so log it instead
                    PerforceProviderPlugin.log(e.getStatus());
                }
            }
    	});

    }

    /**
     * Convenience method that maps the selected resources to their providers.
     * The returned Hashtable has keys which are ITeamProviders, and values
     * which are Lists of IResources that are shared with that provider.
     *
     * @return a hashtable mapping providers to their selected resources
     */
    protected Hashtable<RepositoryProvider, List<IResource>> getProviderMapping() {
        return getProviderMapping(getSelectedResources());
    }

    /**
     * Convenience method that maps the given resources to their providers. The
     * returned Hashtable has keys which are ITeamProviders, and values which
     * are Lists of IResources that are shared with that provider.
     *
     * @param resources
     * @return a hashtable mapping providers to their resources
     */
    protected Hashtable<RepositoryProvider, List<IResource>> getProviderMapping(
            IResource[] resources) {
        Hashtable<RepositoryProvider, List<IResource>> result = new Hashtable<RepositoryProvider, List<IResource>>();
        for (int i = 0; i < resources.length; i++) {
            RepositoryProvider provider = RepositoryProvider
                    .getProvider(resources[i].getProject());
            List<IResource> list = result.get(provider);
            if (list == null) {
                list = new ArrayList<IResource>();
                result.put(provider, list);
            }
            list.add(resources[i]);
        }
        return result;
    }

	@Override
	protected void execute(IAction action) throws InvocationTargetException,
			InterruptedException {
		run(action);
	}


    /**
     * Concrete action enablement code. Subclasses must implement.
     *
     * @return whether the action is enabled
     * @throws TeamException
     *             if an error occurs during enablement detection
     */
    protected abstract boolean isEnabledEx() throws TeamException;

    /**
     * Enable/disable the action, and execute the concrete delegate impl code.
     * When used as a handler, the action is null.
     * When used as a delegate, the action passed will be called on action.setEnabled(isEnabledEx()).
     */
	abstract public void run(IAction action);

}
