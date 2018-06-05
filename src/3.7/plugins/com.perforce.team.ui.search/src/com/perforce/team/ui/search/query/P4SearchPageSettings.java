/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SearchPageSettings {

    /**
     * SELECTED_PATHS
     */
    public static final String SELECTED_PATHS = "SELECTED_PATHS"; //$NON-NLS-1$

    /**
     * ALL_PATHS
     */
    public static final String ALL_PATHS = "ALL_PATHS"; //$NON-NLS-1$

    /**
     * SELECTED_PATHS
     */
    public static final String SELECTED_PROJECTS = "SELECTED_PROJECTS"; //$NON-NLS-1$

    /**
     * SELECTED_TAB
     */
    public static final String SELECTED_TAB = "SELECTED_TAB"; //$NON-NLS-1$

    private IProject[] projects = new IProject[0];
    private IProject[] selectedProjects = new IProject[0];
    private Set<DepotPath> paths = new HashSet<DepotPath>();
    private DepotPath[] selectedPaths = new DepotPath[0];

    private IP4Connection connection;
    private IDialogSettings settings;
    private boolean created = false;
    private int tab = 0;

    /**
     * Create search page settings
     * 
     * @param connection
     * @param parent
     * 
     */
    public P4SearchPageSettings(IP4Connection connection, IDialogSettings parent) {
        this.connection = connection;
        this.settings = parent.getSection(this.connection.getName());
        if (this.settings == null) {
            this.settings = parent.addNewSection(this.connection.getName());
            created = true;
        }
    }

    /**
     * Is this the first time the settings for this connection have been loaded?
     * 
     * @return true if new settings, false if existing
     */
    public boolean isNewSettings() {
        return this.created;
    }

    /**
     * Load the settings from the dialog settings
     */
    public void load() {
        try {
            this.tab = settings.getInt(SELECTED_TAB);
        } catch (NumberFormatException nfe) {
            this.tab = 0;
        }
        this.paths.clear();
        addPaths(this.paths, settings.getArray(ALL_PATHS));
        Set<DepotPath> initialSelected = new HashSet<DepotPath>();
        addPaths(initialSelected, settings.getArray(SELECTED_PATHS));
        this.selectedPaths = initialSelected
                .toArray(new DepotPath[initialSelected.size()]);
        String[] projectNames = settings.getArray(SELECTED_PROJECTS);

        Set<String> selectedNames = new HashSet<String>();
        if (projectNames != null) {
            selectedNames.addAll(Arrays.asList(projectNames));
        }

        List<IProject> foundProjects = new ArrayList<IProject>();
        List<IProject> selected = new ArrayList<IProject>();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        for (IProject project : root.getProjects()) {
            if (project.isAccessible()
                    && connection.equals(P4ConnectionManager.getManager()
                            .getConnection(project, false))) {
                foundProjects.add(project);
                if (selectedNames.contains(project.getName())) {
                    selected.add(project);
                }
            }
        }
        this.projects = foundProjects
                .toArray(new IProject[foundProjects.size()]);
        this.selectedProjects = selected.toArray(new IProject[selected.size()]);

        // Select all by default for new settings
        if (isNewSettings() && this.selectedProjects.length == 0) {
            this.selectedProjects = this.projects;
        }
    }

    private void addPaths(Set<DepotPath> depotPaths, String[] paths) {
        if (paths != null) {
            for (String path : paths) {
                depotPaths.add(new DepotPath(path));
            }
        }
    }

    private String[] getPaths(Collection<DepotPath> depotPaths) {
        String[] paths = new String[depotPaths.size()];
        int index = 0;
        for (DepotPath path : depotPaths) {
            paths[index] = path.getPath();
            index++;
        }
        return paths;
    }

    /**
     * Save the settings
     */
    public void save() {
        String[] projectNames = new String[this.selectedProjects.length];
        for (int i = 0; i < projectNames.length; i++) {
            projectNames[i] = this.selectedProjects[i].getName();
        }
        settings.put(SELECTED_PROJECTS, projectNames);
        settings.put(SELECTED_PATHS,
                getPaths(Arrays.asList(this.selectedPaths)));
        settings.put(ALL_PATHS, getPaths(this.paths));
        settings.put(SELECTED_TAB, this.tab);
    }

    /**
     * Add path
     * 
     * @param path
     */
    public void addPath(DepotPath path) {
        if (path != null) {
            this.paths.add(path);
        }
    }

    /**
     * Remove path
     * 
     * @param path
     */
    public void removePath(DepotPath path) {
        if (path != null) {
            this.paths.remove(path);
        }
    }

    /**
     * Set selected paths
     * 
     * @param paths
     */
    public void setSelectedPaths(DepotPath[] paths) {
        if (paths == null) {
            paths = new DepotPath[0];
        }
        this.selectedPaths = paths;
    }

    /**
     * Set selected projects
     * 
     * @param projects
     */
    public void setSelectedProjects(IProject[] projects) {
        if (projects == null) {
            projects = new IProject[0];
        }
        this.selectedProjects = projects;
    }

    /**
     * Get selected projects
     * 
     * @return non-null array
     */
    public IProject[] getSelectedProjects() {
        return this.selectedProjects;
    }

    /**
     * Get projects
     * 
     * @return non-null array
     */
    public IProject[] getProjects() {
        return this.projects;
    }

    /**
     * Get depot paths
     * 
     * @return non-null array
     */
    public DepotPath[] getPaths() {
        return this.paths.toArray(new DepotPath[this.paths.size()]);
    }

    /**
     * Get selected depot path
     * 
     * @return non-null array
     */
    public DepotPath[] getSelectedPaths() {
        return this.selectedPaths;
    }

    /**
     * Get selected tab
     * 
     * @return tab index
     */
    public int getSelectedTab() {
        return this.tab;
    }

    /**
     * Set selected tab index
     * 
     * @param tab
     */
    public void setSelectedTab(int tab) {
        this.tab = tab;
    }

}
