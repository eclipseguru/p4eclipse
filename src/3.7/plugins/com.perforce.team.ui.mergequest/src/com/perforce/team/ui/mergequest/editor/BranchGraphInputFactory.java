/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.connection.ConnectionElementFactory;
import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphInputFactory extends ConnectionElementFactory {

    /**
     * ID
     */
    public static final String ID = P4BranchGraphPlugin.PLUGIN_ID
            + ".BranchGraphInputFactory"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.ui.connection.ConnectionElementFactory#createElement(com.perforce.team.core.p4java.IP4Connection,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    protected IAdaptable createElement(IP4Connection connection,
            IMemento memento) {
        return new BranchGraphInput(connection);
    }

}
