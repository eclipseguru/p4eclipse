/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.io.InputStream;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.ui.actions.Messages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffDepotAction extends DiffAction {

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getCompareStream(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected InputStream getCompareStream(IP4File file) {
        return file.getHeadContents();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getCompareString(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected String getCompareString(IP4File file) {
        return Messages.DiffDepotAction_DIFF3;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.DiffAction#getEditorTitle(com.perforce.team.core.p4java.IP4File)
     */
    @Override
    protected String getEditorTitle(IP4File file) {
        return Messages.DiffDepotAction_DIFF1;
    }
}
