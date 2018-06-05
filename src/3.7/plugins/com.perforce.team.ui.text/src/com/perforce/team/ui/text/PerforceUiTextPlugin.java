package com.perforce.team.ui.text;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PerforceUiTextPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.text"; //$NON-NLS-1$

    /**
     * IMG_AGING
     */
    public static final String IMG_AGING = "icons/age_icon.png"; //$NON-NLS-1$

    /**
     * IMG_FOLDING
     */
    public static final String IMG_FOLDING = "icons/folding.png"; //$NON-NLS-1$

    /**
     * IMG_USER
     */
    public static final String IMG_USER = "icons/user.png"; //$NON-NLS-1$

    /**
     * IMG_LINK
     */
    public static final String IMG_LINK = "icons/link.png"; //$NON-NLS-1$

    /**
     * IMG_DATE
     */
    public static final String IMG_DATE = "icons/date.png"; //$NON-NLS-1$

    /**
     * IMG_BUILD
     */
    public static final String IMG_BUILD = "icons/build.png"; //$NON-NLS-1$

    /**
     * IMG_SORT
     */
    public static final String IMG_SORT = "icons/sort.png"; //$NON-NLS-1$

    /**
     * IMG_FILTER
     */
    public static final String IMG_FILTER = "icons/filter.png"; //$NON-NLS-1$

    /**
     * IMG_WHITESPACE
     */
    public static final String IMG_WHITESPACE = "icons/whitespace_mode.png"; //$NON-NLS-1$

    /**
     * IMG_HOME
     */
    public static final String IMG_HOME = "icons/home.png"; //$NON-NLS-1$

    /**
     * IMG_UP
     */
    public static final String IMG_UP = "icons/up.png"; //$NON-NLS-1$

    /**
     * IMG_SPLIT
     */
    public static final String IMG_SPLIT = "icons/split.png"; //$NON-NLS-1$

    /**
     * IMG_COMMENT
     */
    public static final String IMG_COMMENT = "icons/comment.png"; //$NON-NLS-1$

    // The shared instance
    private static PerforceUiTextPlugin plugin;

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
     * The constructor
     */
    public PerforceUiTextPlugin() {
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
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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
    public static PerforceUiTextPlugin getDefault() {
        return plugin;
    }

}
