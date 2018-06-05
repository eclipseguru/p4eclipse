package com.perforce.team.ui.markers;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.ui.p4java.actions.AddAction;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IMarkerResolution;

/**
 * Open for add resolution
 */
public class AddSourceResolution implements IMarkerResolution {

    /**
     * @see org.eclipse.ui.IMarkerResolution#getLabel()
     */
    public String getLabel() {
        return Messages.AddSourceResolution_AddResourceToPerforce;
    }

    /**
     * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
     */
    public void run(IMarker marker) {
        IFile file = (IFile) marker.getResource();
        IP4Resource resource = P4Workspace.getWorkspace().getResource(file);
        if (resource instanceof IP4File) {
            AddAction add = new AddAction();
            add.setAsync(false);
            add.selectionChanged(null, new StructuredSelection(file));
            add.run(null);
            try {
                marker.delete();
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }
}
