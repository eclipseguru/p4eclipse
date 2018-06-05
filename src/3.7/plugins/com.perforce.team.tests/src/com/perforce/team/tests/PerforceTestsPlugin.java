package com.perforce.team.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * @version $Id:
 *          //depot/dev/jteam/p4-eclipse/3.6/plugins/com.perforce.team.tests
 *          /src/com/perforce/team/tests/PerforceTestsPlugin.java#2 $
 */
public class PerforceTestsPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.tests";

    // The shared instance
    private static PerforceTestsPlugin plugin;

    /**
     * The constructor
     */
    public PerforceTestsPlugin() {
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     * @throws Exception
     *             - if plugin starting fails
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
    }

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     * @throws Exception
     *             - if plugin stopping fails
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
    public static PerforceTestsPlugin getDefault() {
        return plugin;
    }

}
