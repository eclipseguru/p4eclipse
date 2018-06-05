/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.actions;

import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.folder.diff.editor.input.ContainerDiffInput;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;

import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DiffContainersAction extends DiffFoldersAction {

    private IContainer leftContainer;
    private IP4Folder leftFolder;
    private IContainer rightContainer;
    private IP4Folder rightFolder;

    private IP4Folder[] getContainerFolders(IContainer left, IContainer right) {
        if (left == null || right == null) {
            return null;
        }
        IP4Resource leftResource = P4ConnectionManager.getManager()
                .getResource(left);
        if (leftResource instanceof IP4Folder) {
            IP4Resource rightResource = P4ConnectionManager.getManager()
                    .getResource(right);
            if (rightResource instanceof IP4Folder) {
                return new IP4Folder[] { (IP4Folder) leftResource,
                        (IP4Folder) rightResource };
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.actions.DiffFoldersAction#getFolders()
     */
    @Override
    protected IP4Folder[] getFolders() {
        List<IResource> resources = getLocalResourceSelection();
        if (resources != null) {
            if (resources.size() == 1) {
                IResource both = resources.get(0);
                if (both instanceof IContainer) {
                    this.leftContainer = (IContainer) both;
                    this.rightContainer = this.leftContainer;
                }
            } else if (resources.size() == 2) {
                IResource left = resources.get(0);
                IResource right = resources.get(1);
                if (left instanceof IContainer && right instanceof IContainer) {
                    this.leftContainer = (IContainer) left;
                    this.rightContainer = (IContainer) right;
                }
            }
            IP4Folder[] folders = getContainerFolders(this.leftContainer,
                    this.rightContainer);
            if (folders != null) {
                this.leftFolder = folders[0];
                this.rightFolder = folders[1];
            }
            return folders;
        }
        return null;
    }

    /**
     * Generate input
     * 
     * @param folder1
     * @param folder2
     * @return non-null folder diff input
     */
    @Override
    protected IFolderDiffInput generateInput(IP4Folder folder1,
            IP4Folder folder2) {
        final ContainerDiffInput input = new ContainerDiffInput(
                folder1.getConnection());
        if (folder1 == leftFolder) {
            input.setLeftContainer(leftContainer);
        }
        if (folder1 == rightFolder) {
            input.setRightContainer(rightContainer);
        }
        input.addPaths(folder1.getActionPath(), folder2.getActionPath());
        return input;
    }
}
