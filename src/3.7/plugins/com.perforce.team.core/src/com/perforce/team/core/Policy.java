/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.perforce.team.core;

import org.eclipse.core.runtime.Platform;


/**
 * A common facility for parsing the <code>com.perforce.team.ui/.options</code>
 * file.
 * <p/>
 * A common pattern using this is:
 * <pre>
	if (Policy.DEBUG_TIME) {
		Tracing.printTrace("run command: submit", //$NON-NLS-1$
				changlist.getTitle()););
	}
 * </pre>
 * @since 2012.2
 */
public class Policy {
    public static boolean DEFAULT = false;

    public static boolean DEBUG = DEFAULT;
    
    public static boolean DEBUG_ACTIVATION = DEFAULT;

    public static boolean DEBUG_TIME = DEFAULT;

    static {
        DEBUG = getDebugOption("/debug"); //$NON-NLS-1$
        if (DEBUG) { //$NON-NLS-1$
            DEBUG_ACTIVATION = getDebugOption("/debug/activation"); //$NON-NLS-1$
            DEBUG_TIME = getDebugOption("/debug/time"); //$NON-NLS-1$
        }
    }

    private static boolean getDebugOption(String option) {
        return "true".equalsIgnoreCase(Platform.getDebugOption(PerforceProviderPlugin.ID + option)); //$NON-NLS-1$
    }
    
}
