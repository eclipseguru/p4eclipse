package com.perforce.team.core;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.team.FileModificationValidator;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.ResourceRuleFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.team.core.RepositoryProvider;

/**
 * Perforce Team Provider. This is the main class that defines an Eclipse
 * repository provider
 */
public class PerforceTeamProvider extends RepositoryProvider {

    /**
     * The ID for the provider
     */
    public static final String ID = "com.perforce.team.core.PerforceTeamProvider"; //$NON-NLS-1$

    private static FileModificationValidator modValidator = null;
    private static IMoveDeleteHook moveDeleteHook = null;

    private static final QualifiedName PERFORCE_PORT = new QualifiedName(ID,
            "port"); //$NON-NLS-1$
    private static final QualifiedName PERFORCE_CLIENT = new QualifiedName(ID,
            "client"); //$NON-NLS-1$
    private static final QualifiedName PERFORCE_USER = new QualifiedName(ID,
            "user"); //$NON-NLS-1$
    private static final QualifiedName PERFORCE_PASSWORD = new QualifiedName(
            ID, "password"); //$NON-NLS-1$
    private static final QualifiedName PERFORCE_CHARSET = new QualifiedName(ID,
            "charset"); //$NON-NLS-1$
    private static final QualifiedName PERFORCE_CONNECTION = new QualifiedName(
            ID, "connection"); //$NON-NLS-1$

    private boolean oldFormat = false;

    private static final IResourceRuleFactory PERFORCE_RULE_FACTORY = new ResourceRuleFactory() {
    };

    /**
     * @see org.eclipse.team.core.RepositoryProvider#getRuleFactory()
     */
    @Override
    public IResourceRuleFactory getRuleFactory() {
        return PERFORCE_RULE_FACTORY;
    }

    /**
	 *
	 */
    public PerforceTeamProvider() {
        super();
    }

    /**
     * We set the persistant properties here.
     * 
     * @see org.eclipse.team.core.RepositoryProvider#configureProject()
     */
    @Override
    public void configureProject() throws CoreException {
        ConnectionParameters params = PerforceProviderPlugin.getCurrParams();
        setProjectProperties(params);
    }

    /**
     * Set the Perforce properties for a project. These are the connection
     * parameters for a specific server.
     * 
     * @param params
     */
    public void setProjectProperties(final ConnectionParameters params) {
        // Make sure new settings are different from the old ones
        ISafeRunnable code = new ISafeRunnable() {

            public void run() throws Exception {
                ConnectionParameters oldParams = getProjectProperties(true);
                // Always update if we have old format properties
                // Make sure new settings are different from the old ones
                if (oldFormat || !oldParams.equals(params)) {
                    IProject project = getProject();
                    project.setPersistentProperty(PERFORCE_CONNECTION,
                            params.toString());
                    if (params.savePassword())
                        P4SecureStore.INSTANCE.put(params.getStorageKey(),
                                params.getPasswordNoNull(), true);
                    oldFormat = false;

                    // Only broadcast changes if port or client or user has
                    // changed.
                    // i.e no password stuff
                    if (!oldParams.getPortNoNull().equals(
                            params.getPortNoNull())
                            || !oldParams.getClientNoNull().equals(
                                    params.getClientNoNull())
                            || !oldParams.getUserNoNull().equals(
                                    params.getUserNoNull())
                            || !oldParams.getCharsetNoNull().equals(
                                    params.getCharsetNoNull())) {
                        PerforceProviderPlugin.broadcastProjectSettingsChanges(
                                project, params);
                    }
                }
            }

            public void handleException(Throwable e) {
                // don't log the exception....it is already being logged in
                // Platform#run
            }
        };
        SafeRunner.run(code);
    }

    /**
     * Get the Perforce properties for a project.
     * 
     * @param original
     *            true if we need to return the actual original params not the
     *            latest version.
     * @return - connection parameters
     * @throws CoreException
     */
    public ConnectionParameters getProjectProperties(boolean original)
            throws CoreException {
        IProject project = getProject();
        ConnectionParameters params;
        String conStr = project.getPersistentProperty(PERFORCE_CONNECTION);
        if (conStr != null) {
            params = new ConnectionParameters(conStr);
        } else {
            // Support for old format
            oldFormat = true;
            params = new ConnectionParameters();
            params.setPort(project.getPersistentProperty(PERFORCE_PORT));
            params.setClient(project.getPersistentProperty(PERFORCE_CLIENT));
            params.setUser(project.getPersistentProperty(PERFORCE_USER));
            // params
            // .setPassword(project
            // .getPersistentProperty(PERFORCE_PASSWORD));
        }
        // Return existing copy of params if possible
        return params;
        // return ConnectionCache.defaultCache.getParameters(params);
    }

    /**
     * @see org.eclipse.core.resources.IProjectNature#deconfigure()
     */
    public void deconfigure() throws CoreException {
        // Clear all persistant properties
        IProject project = getProject();
        project.setPersistentProperty(PERFORCE_CONNECTION, null);
        project.setPersistentProperty(PERFORCE_PORT, null);
        project.setPersistentProperty(PERFORCE_CLIENT, null);
        project.setPersistentProperty(PERFORCE_USER, null);
        project.setPersistentProperty(PERFORCE_PASSWORD, null);
        project.setPersistentProperty(PERFORCE_CHARSET, null);

        PerforceProviderPlugin.broadcastProjectSettingsChanges(project,
                new ConnectionParameters());
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#canHandleLinkedResources()
     */
    @Override
    public boolean canHandleLinkedResources() {
        return true;
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#canHandleLinkedResourceURI()
     */
    @Override
    public boolean canHandleLinkedResourceURI() {
        return true;
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#getID()
     */
    @Override
    public String getID() {
        return ID;
    }

    public static void registerFileModicationsValidator(
    		FileModificationValidator validator) {
        modValidator = validator;
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#getFileModificationValidator2()
     */
    @Override
    public FileModificationValidator getFileModificationValidator2() {
        return modValidator;
    }

    /**
     * Register a move delete hook
     * 
     * @param hook
     */
    public static void registerMoveDeleteHook(IMoveDeleteHook hook) {
        moveDeleteHook = hook;
    }

    /**
     * @see org.eclipse.team.core.RepositoryProvider#getMoveDeleteHook()
     */
    @Override
    public IMoveDeleteHook getMoveDeleteHook() {
        return moveDeleteHook;
    }

    /**
     * Get the perforce team provider for the specified resource
     * 
     * @param resource
     * @return - team provider or null if none
     */
    public static PerforceTeamProvider getPerforceProvider(IResource resource) {
        PerforceTeamProvider provider = null;
        if (resource != null) {
            IProject project = resource.getProject();
            // Fix for job036727, check for null project since
            // RepositoryProvider.getProvider will throw an NPE if the specified
            // project is null.
            if (project != null) {
                provider = (PerforceTeamProvider) RepositoryProvider
                        .getProvider(project, ID);
                // Cope with unmanaging project when we may still have a
                // provider but the connection settings are gone
                if (provider != null) {
                    try {
                        ConnectionParameters params = provider
                                .getProjectProperties(true);
                        if (params.getPort() == null) {
                            provider = null;
                        }
                    } catch (CoreException e) {
                    }
                }
            }
        }
        return provider;
    }
}
