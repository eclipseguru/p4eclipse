/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.resource;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ResourceBrowserDialog extends P4StatusDialog {

    private IP4Resource[] resources = null;
    private IP4Resource selected = null;

    protected ResourceBrowserWidget widget = null;

    /**
     * @param parent
     * @param resources
     */
    public ResourceBrowserDialog(Shell parent, IP4Resource[] resources) {
        super(parent);
        setTitle(Messages.ResourceBrowserDialog_SelectAResource);
        setModalResizeStyle();
        this.resources = resources;
    }

    /**
     * Get selected resource
     * 
     * @return - selected resource
     */
    public IP4Resource getSelectedResource() {
        return this.selected;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.selected = this.widget.getSelectedResource();
        super.okPressed();
    }

    /**
     * Get the viewer
     * 
     * @return - viewer
     */
    public TreeViewer getViewer() {
        return this.widget != null ? this.widget.getViewer() : null;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        widget = new ResourceBrowserWidget(this.resources);
        widget.createControl(c);
        widget.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (widget.getSelectedResource() != null) {
                    okPressed();
                }
            }
        });
        widget.setErrorDisplay(this);

        return c;
    }
}
