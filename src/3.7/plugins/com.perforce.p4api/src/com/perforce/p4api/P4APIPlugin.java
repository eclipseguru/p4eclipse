package com.perforce.p4api;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The Eclipse plugin for the Perforce API
 */
public class P4APIPlugin extends AbstractUIPlugin {

    /**
     * ID - plug-in id
     */
    public static final String ID = "com.perforce.p4api"; //$NON-NLS-1$

    // The instance of this plugin
    private static P4APIPlugin instance;

    /**
	 *
	 */
    public P4APIPlugin() {
        instance = this;
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
    }

    /**
     * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext context) throws Exception {
        super.stop(context);
    }

    /**
     * Get the instance of the plugin
     * 
     * @return the instance of the plugin
     */
    public static P4APIPlugin getPlugin() {
        return instance;
    }

}
