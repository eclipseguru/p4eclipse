/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor.input;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.preferences.IPreferenceConstants;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ContainerDiffInput extends FolderDiffInput {

    private IContainer leftContainer;
    private IContainer rightContainer;

    /**
     * @param connection
     */
    public ContainerDiffInput(IP4Connection connection) {
        super(connection);
    }

    /**
     * @param connection
     * @param left
     * @param right
     * @param header
     */
    public ContainerDiffInput(IP4Connection connection,
            IDiffConfiguration left, IDiffConfiguration right,
            IDiffConfiguration header) {
        super(connection, left, right, header);
    }

    /**
     * Get project image descriptor
     * 
     * @return image descriptor
     */
    protected ImageDescriptor getProjectDescriptor() {
        return PlatformUI.getWorkbench().getSharedImages()
                .getImageDescriptor(SharedImages.IMG_OBJ_PROJECT);
    }

    /**
     * Set left container
     * 
     * @param container
     */
    public void setLeftContainer(IContainer container) {
        this.leftContainer = container;
        if (this.leftContainer != null) {
            if (this.leftContainer instanceof IProject) {
                getLeftConfiguration().setImageDescriptor(
                        getProjectDescriptor());
                getLeftConfiguration().setLabel(this.leftContainer.getName());
            } else {
                getLeftConfiguration().setLabel(
                        this.leftContainer.getFullPath().makeRelative()
                                .toPortableString());
                getLeftConfiguration()
                        .setImageDescriptor(
                                PerforceUIPlugin
                                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER));
            }
        }
    }

    /**
     * Set right container
     * 
     * @param container
     */
    public void setRightContainer(IContainer container) {
        this.rightContainer = container;
        if (this.rightContainer != null) {
            if (this.rightContainer instanceof IProject) {
                getRightConfiguration().setImageDescriptor(
                        getProjectDescriptor());
                getRightConfiguration().setLabel(this.rightContainer.getName());
            } else {
                getRightConfiguration().setLabel(
                        this.rightContainer.getFullPath().makeRelative()
                                .toPortableString());
                getRightConfiguration()
                        .setImageDescriptor(
                                PerforceUIPlugin
                                        .getDescriptor(IPerforceUIConstants.IMG_DEPOT_FOLDER));
            }
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput#addPaths(java.lang.String,
     *      java.lang.String,
     *      com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    protected void addPaths(final String leftFilter, final String rightFilter,
            final FileDiffContainer container) {
        String leftAction = P4CoreUtils.getResourceActionPath(leftContainer);
        String rightAtion = P4CoreUtils.getResourceActionPath(rightContainer);
        if (leftAction != null && rightAtion != null) {
            container.addMapping(leftAction, rightAtion);
            container.add(
                    connection.getDiffs(leftAction + leftFilter, rightAtion
                            + rightFilter), connection);
        }
        if (PerforceUiFolderPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.DIFF_LINKED_RESOURCES)
                && leftContainer != null
                && leftContainer.equals(rightContainer)) {
            try {
                leftContainer.accept(new IResourceProxyVisitor() {

                    public boolean visit(IResourceProxy proxy)
                            throws CoreException {
                        if (proxy.isLinked()) {
                            String linkedPath = P4CoreUtils
                                    .getResourceActionPath(proxy
                                            .requestResource());
                            if (linkedPath != null) {
                                container.addMapping(linkedPath, linkedPath);
                                container.add(
                                        connection.getDiffs(linkedPath
                                                + leftFilter, linkedPath
                                                + rightFilter), connection);
                            }
                        }
                        return true;
                    }
                }, IResource.DEPTH_INFINITE);
            } catch (CoreException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput#exists()
     */
    @Override
    public boolean exists() {
        return isValid() ? super.exists() : false;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput#getPersistable()
     */
    @Override
    public IPersistableElement getPersistable() {
        return isValid() ? super.getPersistable() : null;
    }

    /**
     * Is this input currently valid
     * 
     * @return true if valid, false otherwise
     */
    protected boolean isValid() {
        if (leftContainer == null) {
            return false;
        }
        if (rightContainer == null) {
            return false;
        }
        IProject project = leftContainer.getProject();
        if (project == null || !project.isAccessible()) {
            return false;
        }
        project = rightContainer.getProject();
        if (project == null || !project.isAccessible()) {
            return false;
        }
        return true;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput#savePaths(org.eclipse.ui.IMemento)
     */
    @Override
    protected void savePaths(IMemento memento) {
        if (leftContainer != null) {
            IPath path = leftContainer.getFullPath();
            if (path != null) {
                memento.putString(ContainerDiffInputFactory.LEFT_PATH,
                        path.toPortableString());
            }
        }

        if (rightContainer != null) {
            IPath path = rightContainer.getFullPath();
            if (path != null) {
                memento.putString(ContainerDiffInputFactory.RIGHT_PATH,
                        path.toPortableString());
            }
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.editor.input.FolderDiffInput#getFactoryId()
     */
    @Override
    public String getFactoryId() {
        return ContainerDiffInputFactory.ID;
    }


    @Override
    public boolean equals(Object obj) {
    	// override to prevent coverity from complaining.
    	return obj instanceof ContainerDiffInput && super.equals(obj);
    }
    
    @Override
    public int hashCode() {
    	// override to prevent coverity from complaining: FB.EQ_COMPARETO_USE_OBJECT_EQUALS
    	return super.hashCode();
    }

}
