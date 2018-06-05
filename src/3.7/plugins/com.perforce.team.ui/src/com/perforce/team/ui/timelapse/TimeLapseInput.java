/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseInput extends PlatformObject implements IEditorInput {

    private IP4File file;
    private boolean enableBranchHistory = true;
    private boolean useChangelists = true;

    /**
     * Creates a new time lapse input
     * 
     * @param file
     */
    public TimeLapseInput(IP4File file) {
        this.file = file;
    }

    /**
     * Creates a new time lapse input
     * 
     * @param file
     * @param enableBranchHistory
     * @param useChangelistKeys
     */
    public TimeLapseInput(IP4File file, boolean enableBranchHistory,
            boolean useChangelistKeys) {
        this.file = file;
        this.enableBranchHistory = enableBranchHistory;
        this.useChangelists = useChangelistKeys;
    }

    /**
     * Get file in for this input
     * 
     * @return - p4 file
     */
    public IP4File getFile() {
        return this.file;
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
        return PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_TIME_LAPSE);
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return file.getName();
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
        return MessageFormat.format(Messages.TimeLapseInput_TimelapseOf,
                file.getRemotePath());
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (adapter == IFile.class) {
            return file.getLocalFileForLocation();
        } else if (adapter == IP4Resource.class || adapter == IP4File.class) {
            return file;
        } else if (adapter == IP4Connection.class) {
            return file.getConnection();
        }
        return super.getAdapter(adapter);
    }

    /**
     * Should branch history option be enable?
     * 
     * @return - true to enable, false otherwise
     */
    public boolean enableBranchHistoy() {
        return this.enableBranchHistory;
    }

    /**
     * Should changelists be used as line keys?
     * 
     * @return - true to use changelists as keys, false otherwise
     */
    public boolean useChangelistKeys() {
        return this.useChangelists;
    }

}
