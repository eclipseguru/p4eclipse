package com.perforce.team.core.mergequest;

import com.perforce.team.core.mergequest.model.registry.BranchRegistry;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class P4BranchGraphCorePlugin extends Plugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.core.mergequest"; //$NON-NLS-1$

    // The shared instance
    private static P4BranchGraphCorePlugin plugin;

    private BranchRegistry branchRegistry;

    /**
     * The constructor
     */
    public P4BranchGraphCorePlugin() {
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Get branch registry
     * 
     * @return branch registry
     */
    public BranchRegistry getBranchRegistry() {
        if (branchRegistry == null) {
            branchRegistry = new BranchRegistry(PLUGIN_ID + ".elements"); //$NON-NLS-1$
        }
        return branchRegistry;
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static P4BranchGraphCorePlugin getDefault() {
        return plugin;
    }

}
