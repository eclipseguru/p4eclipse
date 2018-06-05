/*
 * Copyright 2008 Perforce Software Inc., All Rights Reserved.
 */

package com.perforce.team.ui;

import org.eclipse.team.core.ProjectSetCapability;
import org.eclipse.team.core.RepositoryProviderType;

/**
 * Perforce project provider type
 */

public class PerforceProjectProviderType extends RepositoryProviderType {

    /**
     * @see org.eclipse.team.core.RepositoryProviderType#getProjectSetCapability()
     */
    @Override
    public ProjectSetCapability getProjectSetCapability() {
        return new PerforceProjectSetSerializer();
    }
}
