/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ContainerDiffInputFactory extends FolderDiffInputFactory {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.folder.diff.editor.ContainerDiffInputFactory"; //$NON-NLS-1$

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInputFactory#createElement(com.perforce.team.core.p4java.IP4Connection,
     *      org.eclipse.ui.IMemento)
     */
    @Override
    protected IAdaptable createElement(IP4Connection connection,
            IMemento memento) {
        ContainerDiffInput input = null;
        String left = memento.getString(LEFT_PATH);
        String right = memento.getString(RIGHT_PATH);
        if (left != null && right != null) {
            IPath leftPath = Path.fromPortableString(left);
            IPath rightPath = Path.fromPortableString(right);
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IResource leftResource = root.findMember(leftPath);
            if (leftResource instanceof IContainer
                    && leftResource.getProject().isAccessible()) {
                IResource rightResource = root.findMember(rightPath);
                if (rightResource != null
                        && rightResource.getProject().isAccessible()) {
                    input = new ContainerDiffInput(connection);
                    input.setLeftContainer((IContainer) leftResource);
                    input.setRightContainer((IContainer) rightResource);
                    input.getLeftConfiguration()
                            .getOptions()
                            .load(memento
                                    .getChild(IFolderDiffInput.LEFT_OPTIONS));
                    input.getRightConfiguration()
                            .getOptions()
                            .load(memento
                                    .getChild(IFolderDiffInput.RIGHT_OPTIONS));
                }
            }
        }
        return input;
    }
}
