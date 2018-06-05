/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.internal;

import com.perforce.team.ui.editor.Proposal;

import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Perforce1666Proposal extends Proposal {

    /**
     * @param value
     */
    public Perforce1666Proposal(String value) {
        super(value);
    }

    /**
     * @see com.perforce.team.ui.editor.Proposal#getImage()
     */
    @Override
    public Image getImage() {
        return InternalPlugin.getDefault().getImage("icons/server_edit.png");
    }

}
