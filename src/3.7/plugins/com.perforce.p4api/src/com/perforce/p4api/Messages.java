package com.perforce.p4api;

import org.eclipse.osgi.util.NLS;

/**
 * Messages class
 */
public final class Messages extends NLS{

    private static final String BUNDLE_NAME = "com.perforce.p4api.messages"; //$NON-NLS-1$
    
    public static String IChangelist_DESCR;

    static {
  		// initialize resource bundles
  		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
  	}

    private Messages() {
    }

}
