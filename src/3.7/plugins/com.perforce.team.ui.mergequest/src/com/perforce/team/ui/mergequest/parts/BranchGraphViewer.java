/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.parts;

import com.perforce.team.ui.mergequest.tooltip.BranchGraphToolTipManager;
import com.perforce.team.ui.mergequest.tooltip.IToolTipManager;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.swt.widgets.Control;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphViewer extends ScrollingGraphicalViewer implements
        IControlProvider, IAdaptable {

    private Control hoverControl;
    private IToolTipManager toolTipManager;

    /**
     * @see org.eclipse.gef.ui.parts.AbstractEditPartViewer#setControl(org.eclipse.swt.widgets.Control)
     */
    @Override
    public void setControl(Control control) {
        super.setControl(control);
        this.hoverControl = control;
    }

    /**
     * Dispose of the viewer
     */
    public void dispose() {
        getToolTipManager().dispose();
    }

    /**
     * Get tool tip manager
     * 
     * @return tool tip manager
     */
    public IToolTipManager getToolTipManager() {
        if (this.toolTipManager == null) {
            this.toolTipManager = new BranchGraphToolTipManager(this);
        }
        return this.toolTipManager;
    }

    /**
     * @see com.perforce.team.ui.mergequest.parts.IControlProvider#getCurrentControl()
     */
    public Control getCurrentControl() {
        return this.hoverControl;
    }

    /**
     * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class adapter) {
        if (adapter == IToolTipManager.class) {
            return getToolTipManager();
        }
        if (adapter == CommandStack.class) {
            EditDomain domain = getEditDomain();
            if (domain != null) {
                return domain.getCommandStack();
            }
        }
        return Platform.getAdapterManager().getAdapter(this, adapter);
    }

}
