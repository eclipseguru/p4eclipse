/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import java.text.MessageFormat;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.core.p4java.IP4SubmittedChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangelistEditorInput extends PlatformObject implements
        IChangelistEditorInput {

    private IP4Changelist changelist = null;

    /**
     * 
     * @param changelist
     */
    public ChangelistEditorInput(IP4Changelist changelist) {
        this.changelist = changelist;
    }

    /**
     * @see com.perforce.team.ui.changelists.IChangelistEditorInput#getChangelist()
     */
    public IP4Changelist getChangelist() {
        return this.changelist;
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
        if (this.changelist instanceof IP4SubmittedChangelist) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_SUBMITTED_EDITOR);
        } else if (this.changelist instanceof IP4ShelvedChangelist) {
            return PerforceUIPlugin
                    .getDescriptor(IPerforceUIConstants.IMG_SHELVED_EDITOR);
        }
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return MessageFormat.format(
                Messages.ChangelistEditorInput_ChangeNumber,
                this.changelist.getId());
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
        return getName();
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof ChangelistEditorInput) {
            return this.changelist.equals(((ChangelistEditorInput) obj)
                    .getChangelist());
        }
        return false;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
        if (IP4Resource.class == adapter || IP4Changelist.class == adapter
                || IP4SubmittedChangelist.class == adapter) {
            return this.changelist;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.changelist.getId();
    }

}
