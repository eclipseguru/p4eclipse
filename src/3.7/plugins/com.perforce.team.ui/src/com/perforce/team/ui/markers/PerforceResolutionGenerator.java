package com.perforce.team.ui.markers;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Perforce resolution generator
 */
public class PerforceResolutionGenerator implements IMarkerResolutionGenerator {

    /**
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker) {
        return new IMarkerResolution[] { new AddSourceResolution(),
                new IgnoreResolution(), };
    }
}
