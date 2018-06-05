/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.tests.P4TestCase;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.core.TeamException;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ProviderPluginTest extends P4TestCase {

    /**
     * Test p4eclipse logging method on core plugin
     */
    public void testLogging() {
        final List<IStatus> logs = new ArrayList<IStatus>();
        final List<String> plugins = new ArrayList<String>();
        ILogListener listener = new ILogListener() {

            public void logging(IStatus status, String plugin) {
                logs.add(status);
                plugins.add(plugin);
            }
        };
        PerforceProviderPlugin.getPlugin().getLog().addLogListener(listener);

        IStatus status = new Status(IStatus.WARNING, PerforceUIPlugin.ID,
                Status.OK, "test status", null);
        PerforceProviderPlugin.log(status);

        TeamException exception = new TeamException("test team exception",
                new NullPointerException());
        PerforceProviderPlugin.log(exception);

        PerforceProviderPlugin.logError("test error");
        PerforceProviderPlugin.logError((String) null);

        PerforceProviderPlugin.logError(new IllegalArgumentException(
                "illegal argument exception"));
        PerforceProviderPlugin.logError((Throwable) null);

        PerforceProviderPlugin.logError("error message",
                new NullPointerException("message"));
        PerforceProviderPlugin.logError(null, new NullPointerException(
                "no message"));
        PerforceProviderPlugin.logError("just error message", null);
        PerforceProviderPlugin.logError(null, null);

        PerforceProviderPlugin.logWarning("test warning");
        PerforceProviderPlugin.logWarning((String) null);

        PerforceProviderPlugin.logWarning(new IllegalArgumentException(
                "illegal argument exception"));
        PerforceProviderPlugin.logWarning((Throwable) null);

        PerforceProviderPlugin.logWarning("warning message",
                new NullPointerException("message"));
        PerforceProviderPlugin.logWarning(null, new NullPointerException(
                "no message"));
        PerforceProviderPlugin.logWarning("just warning message", null);
        PerforceProviderPlugin.logWarning(null, null);

        PerforceProviderPlugin.logInfo("test info");
        PerforceProviderPlugin.logInfo((String) null);

        PerforceProviderPlugin.logInfo(new IllegalArgumentException(
                "illegal argument exception"));
        PerforceProviderPlugin.logInfo((Throwable) null);

        PerforceProviderPlugin.logInfo("info message",
                new NullPointerException("message"));
        PerforceProviderPlugin.logInfo(null, new NullPointerException(
                "no message"));
        PerforceProviderPlugin.logInfo("just info message", null);
        PerforceProviderPlugin.logInfo(null, null);

        PerforceProviderPlugin.getPlugin().getLog().removeLogListener(listener);

        assertEquals(17, logs.size());
        for (IStatus log : logs) {
            assertNotNull(log.getMessage());
            assertNotNull(log.getPlugin());
        }
        assertEquals(17, plugins.size());
        for (String plugin : plugins) {
            assertEquals(PerforceProviderPlugin.ID, plugin);
        }
    }
}
