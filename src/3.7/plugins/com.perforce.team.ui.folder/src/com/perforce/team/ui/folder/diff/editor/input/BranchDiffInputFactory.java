/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.connection.ConnectionElementFactory;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchDiffInputFactory extends ConnectionElementFactory {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.folder.diff.editor.BranchDiffInputFactory"; //$NON-NLS-1$

    /**
     * BRANCH_NAME
     */
    public static final String BRANCH_NAME = "branchName"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.ui.connection.ConnectionElementFactory#createElement(com.perforce.team.core.p4java.IP4Connection,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    protected IAdaptable createElement(IP4Connection connection,
            IMemento memento) {
        IFolderDiffInput input = null;
        String name = memento.getString(BRANCH_NAME);
        if (name != null) {
            input = new BranchDiffInput(name, connection);
            input.getLeftConfiguration().getOptions()
                    .load(memento.getChild(IFolderDiffInput.LEFT_OPTIONS));
            input.getRightConfiguration().getOptions()
                    .load(memento.getChild(IFolderDiffInput.RIGHT_OPTIONS));
        }
        return input;
    }

}
