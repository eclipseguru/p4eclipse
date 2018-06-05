package com.perforce.team.core;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

/**
 * Perforce status implementation
 */
public class PerforceStatus extends MultiStatus {

    /**
     * Create a new perforce status
     * 
     * @param severity
     * @param code
     * @param message
     * @param exception
     */
    public PerforceStatus(int severity, int code, String message,
            Throwable exception) {
        super(
                PerforceProviderPlugin.ID,
                code,
                new Status[] {
                        new Status(severity, PerforceProviderPlugin.ID, code,
                                "Provider: Perforce Software Inc.", exception), //$NON-NLS-1$
                        new Status(severity, PerforceProviderPlugin.ID, code,
                                "Plug-in: P4Eclipse", exception), //$NON-NLS-1$
                        new Status(severity, PerforceProviderPlugin.ID, code,
                                "ID: " + PerforceProviderPlugin.ID, exception), //$NON-NLS-1$
                        new Status(
                                severity,
                                PerforceProviderPlugin.ID,
                                code,
                                "Version: " + //$NON-NLS-1$
                                        PerforceProviderPlugin.getPlugin()
                                                .getBundle().getHeaders()
                                                .get("Bundle-Version"), exception), //$NON-NLS-1$
                }, message, exception);
    }
}
