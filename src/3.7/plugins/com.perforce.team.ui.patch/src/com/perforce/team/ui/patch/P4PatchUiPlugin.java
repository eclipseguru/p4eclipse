package com.perforce.team.ui.patch;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class P4PatchUiPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.patch"; //$NON-NLS-1$

    // The shared instance
    private static P4PatchUiPlugin plugin;

    /**
     * Get image descriptor from plug-in
     * 
     * @param id
     * @return image descriptor
     */
    public static ImageDescriptor getDescriptor(String id) {
        return imageDescriptorFromPlugin(PLUGIN_ID, id);
    }

    /**
     * The constructor
     */
    public P4PatchUiPlugin() {
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
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
    public static P4PatchUiPlugin getDefault() {
        return plugin;
    }

}
