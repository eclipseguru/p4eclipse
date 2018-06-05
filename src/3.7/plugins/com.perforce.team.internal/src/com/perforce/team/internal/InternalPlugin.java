package com.perforce.team.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class InternalPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.internal";

    // The shared instance
    private static InternalPlugin plugin;

    private Map<String, Image> images = new HashMap<String, Image>();

    /**
     * Get image descriptor
     * 
     * @param path
     * @return - image descriptor
     */
    public static ImageDescriptor getDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    /**
     * Is the specified port a perforce 1666 server?
     * 
     * @param port
     * @return true if 1666, false otherwise
     */
    public static boolean isPerforceServer(String port) {
        if (port == null) {
            return false;
        }
        return port.equals("perforce:1666")
                || port.equals("server.perforce.com:1666")
                || port.equals("perforce.perforce.com:1666")
                || port.equals("p4prod.perforce.com:1666");
    }

    /**
     * The constructor
     */
    public InternalPlugin() {
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
     * Get image for bundle-relative path
     * 
     * @param path
     * @return - image or null if not found
     */
    public Image getImage(String path) {
        Image image = null;
        if (path != null) {
            image = this.images.get(image);
            if (image == null || image.isDisposed()) {
                ImageDescriptor descriptor = getDescriptor(path);
                if (descriptor != null) {
                    image = descriptor.createImage();
                    this.images.put(path, image);
                }
            }
        }
        return image;
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
    public static InternalPlugin getDefault() {
        return plugin;
    }

}
