/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderTimeLapseInput extends PlatformObject implements
        IEditorInput {

    private IContainer container;
    private IP4Folder folder;

    /**
     * Creates a new time lapse input
     * 
     * @param folder
     */
    public FolderTimeLapseInput(IP4Folder folder) {
        this(folder, null);
    }

    /**
     * Creates a new time lapse input
     * 
     * @param folder
     * @param container
     */
    public FolderTimeLapseInput(IP4Folder folder, IContainer container) {
        this.folder = folder;
        this.container = container;
    }

    /**
     * Get file in for this input
     * 
     * @return - p4 file
     */
    public IP4Folder getFolder() {
        return this.folder;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return false;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return PerforceUIPlugin.getPlugin().getImageDescriptor(
                IPerforceUIConstants.IMG_TIME_LAPSE);
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        String name = folder.getName();
        if (name == null && this.container != null) {
            name = this.container.getName();
        }
        if (name == null) {
            name = ""; //$NON-NLS-1$
        }
        return name;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return MessageFormat.format(Messages.FolderTimeLapseInput_Tooltip,
                folder.getActionPath());
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter == IContainer.class) {
            if (container != null) {
                return container;
            } else {
                IContainer[] containers = folder.getLocalContainers();
                if (containers != null && containers.length > 0) {
                    return containers[0];
                }
            }
        }
        return super.getAdapter(adapter);
    }

}
