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
public class FolderDiffInputFactory extends ConnectionElementFactory {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.folder.diff.editor.FolderDiffInputFactory"; //$NON-NLS-1$

    /**
     * PAIR
     */
    public static final String PAIR = "pair"; //$NON-NLS-1$

    /**
     * LEFT_PATH
     */
    public static final String LEFT_PATH = "left"; //$NON-NLS-1$

    /**
     * RIGHT_PATH
     */
    public static final String RIGHT_PATH = "right"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.ui.connection.ConnectionElementFactory#createElement(com.perforce.team.core.p4java.IP4Connection,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    protected IAdaptable createElement(IP4Connection connection,
            IMemento memento) {
        FolderDiffInput input = null;
        IMemento[] pairs = memento.getChildren(PAIR);
        if (pairs != null && pairs.length > 0) {
            input = new FolderDiffInput(connection);
            for (IMemento pairMemento : pairs) {
                input.addPaths(pairMemento.getString(LEFT_PATH),
                        pairMemento.getString(RIGHT_PATH));
            }
            input.getLeftConfiguration().getOptions()
                    .load(memento.getChild(IFolderDiffInput.LEFT_OPTIONS));
            input.getRightConfiguration().getOptions()
                    .load(memento.getChild(IFolderDiffInput.RIGHT_OPTIONS));
        }
        return input;
    }

}