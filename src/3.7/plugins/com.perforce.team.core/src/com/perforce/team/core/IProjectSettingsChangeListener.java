package com.perforce.team.core;

/*
 * Copyright (c) 2003, 2004 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.core.resources.IProject;

/**
 * Listener for project setting changes
 */
public interface IProjectSettingsChangeListener {

    /**
     * The project settings have changed
     * 
     * @param project
     *            the project for which management has changed
     * @param params
     *            the new connection parameters for the project
     */
    void projectSettingsChanged(IProject project, ConnectionParameters params);
}
