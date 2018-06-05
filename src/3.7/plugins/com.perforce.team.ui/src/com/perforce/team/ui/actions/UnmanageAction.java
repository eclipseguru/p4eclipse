package com.perforce.team.ui.actions;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import com.perforce.team.core.PerforceProviderPlugin;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;

/**
 * Remove project from perforce management
 */
public class UnmanageAction extends PerforceTeamAction {

    /**
     * @see org.eclipse.ui.actions.ActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @Override
    public void run(IAction action) {
        if (getSelection() != null) {
            IProject[] projects = getSelectedProjects();
            for (int i = 0; i < projects.length; i++) {
                PerforceProviderPlugin.unmanageProject(projects[i]);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.actions.PerforceTeamAction#isEnabledEx()
     */
    @Override
    protected boolean isEnabledEx() {
        return getSelection() != null && getSelection().getFirstElement() != null;
    }
}
