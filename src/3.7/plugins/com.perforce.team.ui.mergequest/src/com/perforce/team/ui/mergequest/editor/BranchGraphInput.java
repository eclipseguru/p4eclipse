/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.mergequest.Messages;

import java.text.MessageFormat;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphInput extends PlatformObject implements IEditorInput,
        IPersistableElement {

    private IP4Connection connection;

    /**
     * 
     * @param connection
     */
    public BranchGraphInput(IP4Connection connection) {
        this.connection = connection;
    }

    /**
     * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
     */
    @Override
    public Object getAdapter(Class adapter) {
        if (IP4Connection.class == adapter) {
            return this.connection;
        }
        return super.getAdapter(adapter);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj instanceof BranchGraphInput) {
			return this.connection.equals(((BranchGraphInput) obj).connection)
					&& super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
    	if(this.connection!=null)
    		return this.connection.hashCode();
    	return super.hashCode();
    }
    
    /**
     * @see org.eclipse.ui.IEditorInput#exists()
     */
    public boolean exists() {
        return true;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
     */
    public ImageDescriptor getImageDescriptor() {
        return null;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getName()
     */
    public String getName() {
        return connection.getParameters().getPort();
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getPersistable()
     */
    public IPersistableElement getPersistable() {
        return this;
    }

    /**
     * @see org.eclipse.ui.IEditorInput#getToolTipText()
     */
    public String getToolTipText() {
        return MessageFormat.format(Messages.BranchGraphInput_ToolTip,
                connection.getParameters().getPort());
    }

    /**
     * @see org.eclipse.ui.IPersistableElement#getFactoryId()
     */
    public String getFactoryId() {
        return BranchGraphInputFactory.ID;
    }

    /**
     * @see org.eclipse.ui.IPersistable#saveState(org.eclipse.ui.IMemento)
     */
    public void saveState(IMemento memento) {
        memento.putString(BranchGraphInputFactory.CONNECTION, this.connection
                .getParameters().toString());
    }

}
