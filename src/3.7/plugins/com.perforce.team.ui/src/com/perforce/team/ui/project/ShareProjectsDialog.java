/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ShareProjectsDialog extends P4StatusDialog {

    private IP4Connection connection = null;
    private ShareProjectsWidget shareWidget = null;

    /**
     * Creates a new dialog to share one or more projects with a specified
     * connection.
     * 
     * @param parent
     * @param connection
     */
    public ShareProjectsDialog(Shell parent, IP4Connection connection) {
        super(parent, Messages.ShareProjectsDialog_ShareProjects);
        this.connection = connection;
        setModalResizeStyle();
    }

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#create()
     */
    @Override
    public void create() {
        super.create();
        shareWidget.validateCreate();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        shareWidget = new ShareProjectsWidget(this.connection);
        shareWidget.setErrorDisplay(this);
        shareWidget.createControl(c);
        shareWidget.validate();
        return c;
    }

    /**
     * Get selected projects
     * 
     * @return - projects
     */
    public IProject[] getSelectedProjects() {
        return shareWidget.getProjects();
    }

}
