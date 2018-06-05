package com.perforce.team.ui.mergequest;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class P4BranchGraphPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.mergequest"; //$NON-NLS-1$

    /**
     * ELEMENT_DESCRIPTORS_ID
     */
    public static final String ELEMENT_DESCRIPTORS_ID = PLUGIN_ID
            + ".elementDescriptors"; //$NON-NLS-1$

    // The shared instance
    private static P4BranchGraphPlugin plugin;

    /**
     * The constructor
     */
    public P4BranchGraphPlugin() {
    }

    /**
     * Get image descriptor
     * 
     * @see #imageDescriptorFromPlugin(String, String)
     * @param path
     * @return - image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     *      )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     *      )
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static P4BranchGraphPlugin getDefault() {
        return plugin;
    }

}
