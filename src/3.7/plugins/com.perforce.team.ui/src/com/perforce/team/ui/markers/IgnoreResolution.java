package com.perforce.team.ui.markers;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.ui.IgnoredFiles;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;

/**
 * Ignore resolution
 */
public class IgnoreResolution implements IMarkerResolution {

    /**
     * 
     * @see org.eclipse.ui.IMarkerResolution#getLabel()
     */
    public String getLabel() {
        return Messages.IgnoreResolution_AddToP4Ignore;
    }

    /**
     * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
     */
    public void run(IMarker marker) {
        IgnoredFiles.addIgnore(marker.getResource());
        try {
            marker.delete();
        } catch (CoreException e) {
            PerforceProviderPlugin.logError(e);
        }
    }
}
