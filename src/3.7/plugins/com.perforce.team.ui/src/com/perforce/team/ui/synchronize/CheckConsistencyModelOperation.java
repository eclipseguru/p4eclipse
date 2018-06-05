/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.synchronize;

import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.actions.CheckConsistencyAction;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.team.ui.synchronize.ISynchronizeModelElement;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CheckConsistencyModelOperation extends PerforceSyncModelOperation {

    private ISelectionProvider selectionProvider = null;

    /**
     * @param configuration
     * @param elements
     */
    public CheckConsistencyModelOperation(
            ISynchronizePageConfiguration configuration, IDiffElement[] elements) {
        super(configuration, elements);
        this.selectionProvider = configuration.getSite().getSelectionProvider();
    }

    /**
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public void run(IProgressMonitor monitor) throws InvocationTargetException,
            InterruptedException {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                List<IResource> resources = new ArrayList<IResource>();
                if (selectionProvider != null) {
                    ISelection selection = selectionProvider.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        for (Object select : ((IStructuredSelection) selection)
                                .toArray()) {
                            if (select instanceof ISynchronizeModelElement) {
                                IResource resource = ((ISynchronizeModelElement) select)
                                        .getResource();
                                if (resource != null) {
                                    resources.add(resource);
                                }
                            }
                        }
                    }
                }

                if (!resources.isEmpty()) {
                    CheckConsistencyAction action = new CheckConsistencyAction();
                    action.setAsync(true);
                    action.selectionChanged(null, new StructuredSelection(
                            resources));
                    action.run(null);
                }
            }
        });
    }
}
