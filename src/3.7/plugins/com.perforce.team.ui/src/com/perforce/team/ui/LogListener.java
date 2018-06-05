/**
 *
 */
package com.perforce.team.ui;

import com.perforce.team.core.p4java.IP4CommandListener;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;

/**
 * Simple log listener to field log calls separately from the console viewer.
 * Previous versions logged as a side effect of console output; unfortunately,
 * if the console window wasn't open, it didn't log. This one hopefully does a
 * better job.... (HR)
 */

public class LogListener implements IP4CommandListener {

    /**
     * Initialize the command listening
     */
    public void init() {
        P4ConnectionManager.getManager().addCommandListener(this);
    }

    private void logLine(String line) {
        final String outLine = line;
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                PerforceUIPlugin
                        .getPlugin()
                        .getLog()
                        .log(new Status(IStatus.INFO, PerforceUIPlugin.ID,
                                IStatus.OK, outLine, null));

            }
        });
    }

    /**
     * @see com.perforce.team.core.p4java.IP4CommandListener#command(java.lang.String)
     */
    public void command(int id, String line) {
        if (PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_LOG_COMMAND)) {
            logLine("Executing " + line); //$NON-NLS-1$
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4CommandListener#error(java.lang.String)
     */
    public void error(int id, String line) {
        if (PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_LOG_COMMAND)) {
            logLine(line.replace('\n', ' '));
        }
    }

    /**
     * @see com.perforce.team.core.p4java.IP4CommandListener#info(java.lang.String)
     */
    public void info(int id, String line) {
        if (PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPerforceUIConstants.PREF_LOG_COMMAND)) {
            logLine(line.replace('\n', ' '));
        }
    }

}
