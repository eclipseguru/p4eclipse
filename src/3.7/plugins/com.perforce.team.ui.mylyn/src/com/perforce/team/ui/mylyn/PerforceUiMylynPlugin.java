package com.perforce.team.ui.mylyn;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PerforceUiMylynPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.mylyn"; //$NON-NLS-1$

    // The shared instance
    private static PerforceUiMylynPlugin plugin;

    private Map<ImageDescriptor, Image> images = new HashMap<ImageDescriptor, Image>();

    /**
     * The constructor
     */
    public PerforceUiMylynPlugin() {
    }

    /**
     * Get cached image from a path
     * 
     * @param path
     * @return - image or null if not found
     */
    public static Image getImage(String path) {
        return plugin != null ? plugin.getPluginImage(path) : null;
    }

    /**
     * Get cached image from a path
     * 
     * @param path
     * @return - image or null if not found
     */
    public Image getPluginImage(String path) {
        Image image = null;
        if (path != null) {
            ImageDescriptor descriptor = getImageDescriptor(path);
            if (descriptor != null) {
                image = this.images.get(descriptor);
                if (image == null || image.isDisposed()) {
                    image = descriptor.createImage();
                    images.put(descriptor, image);
                }
            }
        }
        return image;
    }

    /**
     * Get image description from plugin at specified path
     * 
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
    public static PerforceUiMylynPlugin getDefault() {
        return plugin;
    }

}
