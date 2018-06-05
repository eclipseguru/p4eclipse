package com.perforce.team.ui.ruby;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Perforce ui ruby plugin.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PerforceUiRubyPlugin extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.team.ui.ruby"; //$NON-NLS-1$

    // The shared instance
    private static PerforceUiRubyPlugin plugin;

    /**
     * The constructor
     */
    public PerforceUiRubyPlugin() {
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
    public static PerforceUiRubyPlugin getDefault() {
        return plugin;
    }

}
