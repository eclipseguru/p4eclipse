package com.perforce.team.ui.folder;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PerforceUiFolderPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.folder"; //$NON-NLS-1$

    /**
     * Get image descriptor from this plugin
     * 
     * @param path
     * @return image descriptor
     */
    public static ImageDescriptor getDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    // The shared instance
    private static PerforceUiFolderPlugin plugin;

    /**
     * The constructor
     */
    public PerforceUiFolderPlugin() {
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
    public static PerforceUiFolderPlugin getDefault() {
        return plugin;
    }

}
