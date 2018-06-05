/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.tests.core;

import com.perforce.team.tests.P4TestCase;
import com.perforce.team.tests.Utils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.LogListener;
import com.perforce.team.ui.PerforceUIPlugin;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LogListenerTest extends P4TestCase {

    /**
     * Test persistent logging of commands
     */
    public void testLogging() {
        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .setValue(IPerforceUIConstants.PREF_LOG_COMMAND, true);
        try {
            final List<IStatus> logs = new ArrayList<IStatus>();
            ILogListener listener = new ILogListener() {

                public void logging(IStatus status, String plugin) {
                    logs.add(status);
                }
            };
            PerforceUIPlugin.getPlugin().getLog().addLogListener(listener);
            LogListener logListener = new LogListener();
            logListener.init();
            logListener.info(0,"test info");
            logListener.error(0,"test error");
            logListener.command(0,"test command");
            Utils.sleep(1);
            PerforceUIPlugin.getPlugin().getLog().removeLogListener(listener);
            assertEquals(3, logs.size());
            for (IStatus status : logs) {
                assertEquals(IStatus.INFO, status.getSeverity());
                assertNotNull(status.getMessage());
                assertNotNull(status.getPlugin());
            }
        } finally {
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(IPerforceUIConstants.PREF_LOG_COMMAND, false);
        }
    }

}
