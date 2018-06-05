/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4v;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class P4VAction extends P4Action {

    /**
     * Is this action running on windows?
     */
    protected boolean windows = P4CoreUtils.isWindows();

    /**
     * Should this action be enabled for the specified non-null file
     * 
     * @param file
     * @return - true to enable, false otherwise
     */
    protected abstract boolean enableFor(IP4File file);

    /**
     * Should this p4v action be enabled on windows? This accounts for only
     * running on paths that contains only ASCII characters
     * 
     * @param collection
     * @param type
     * @return - true if enabled, false otherwise
     */
    protected boolean isEnabledWindows(P4Collection collection,
            IP4Resource.Type type) {
        boolean enabled = false;
        IP4Resource[] resources = collection.members();
        int size = resources.length;
        if (size > 0) {
            for (IP4Resource resource : resources) {
                if (resource instanceof IP4File) {
                    enabled = true;
                    IP4File file = (IP4File) resource;
                    if (enableFor(file)) {
                        String path = file.getActionPath(type);
                        try {
                            // On windows the paths must encode to ascii
                            // to actually be lauchable through p4merge.
                            // See:
                            // http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4947220
                            Charset ascii = Charset.forName("US-ASCII"); //$NON-NLS-1$
                            CharsetEncoder encoder = ascii.newEncoder();
                            encoder.onMalformedInput(CodingErrorAction.REPORT);
                            encoder.onUnmappableCharacter(CodingErrorAction.REPORT);
                            enabled = encoder.canEncode(path);
                        } catch (Throwable e) {
                            // Ignore and set enabled to false
                            enabled = false;
                        }
                    }
                    // Break if any files processed should not have this
                    // action be enabled
                    if (!enabled) {
                        break;
                    }
                }
            }
        }
        return enabled;
    }

}
