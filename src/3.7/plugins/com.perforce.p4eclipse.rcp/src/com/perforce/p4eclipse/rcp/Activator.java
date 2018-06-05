package com.perforce.p4eclipse.rcp;

import org.eclipse.core.internal.net.ProxyData;
import org.eclipse.core.internal.net.ProxySelector;
import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "com.perforce.p4eclipse.rcp"; //$NON-NLS-1$

    /**
     * MANUAL
     */
    public static final String MANUAL = "Manual";

    // The shared instance
    private static Activator plugin;

    /**
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
     *      )
     */
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        configureProxy();
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

    private void configureProxy() {
//        ProxyData[] proxies = new ProxyData[2];
//        proxies[0] = new ProxyData(IProxyData.HTTP_PROXY_TYPE, "127.0.0.1",
//                8080, false, "");
//        proxies[1] = new ProxyData(IProxyData.HTTPS_PROXY_TYPE, "127.0.0.1",
//                8080, false, "");
//        ProxySelector.setBypassHosts(MANUAL, new String[] { "127.0.0.1",
//                "localhost", "eng-jteamlinux-vm.perforce.com" });
//        ProxySelector.setProxyData(MANUAL, proxies);
//        ProxySelector.setActiveProvider(MANUAL);
    }

    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

}
