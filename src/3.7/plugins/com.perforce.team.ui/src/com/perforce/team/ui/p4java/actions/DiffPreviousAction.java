/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.osgi.util.NLS;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.actions.Messages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class DiffPreviousAction extends DiffAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getCompareStream(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected InputStream getCompareStream(IP4File file) {
        InputStream stream = null;
        int revision = file.getHeadRevision() - 1;
        if (revision > 0) {
            stream = file.getRemoteContents(revision);
        } else {
            stream = new ByteArrayInputStream(new byte[0]);
        }
        return stream;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getCompareString(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected String getCompareString(IP4File file) {
        int previous = Math.max(0, file.getHeadRevision() - 1);
        return NLS.bind(Messages.DiffDepotAction_DIFFHAVE, previous);
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getEditorTitle(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected String getEditorTitle(IP4File file) {
        return Messages.DiffDepotAction_DIFF1;
    }

}
