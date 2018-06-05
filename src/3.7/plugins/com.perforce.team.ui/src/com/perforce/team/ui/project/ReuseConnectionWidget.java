/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import com.perforce.team.ui.connection.BasicConnectionWidget;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ReuseConnectionWidget extends BasicConnectionWidget {

    private Button reuse;

    /**
     * Should the connection settings be re-used?
     * 
     * @return - true to reuse, false otherwise
     */
    public boolean reuse() {
        return this.reuse.getSelection();
    }

    /**
     * @see com.perforce.team.ui.connection.BasicConnectionWidget#createControl(org.eclipse.swt.widgets.Composite,
     *      boolean)
     */
    @Override
    public Composite createControl(Composite parent, boolean wrapInGroup) {
        Composite displayArea = super.createControl(parent, wrapInGroup);
        reuse = new Button(displayArea, SWT.CHECK);
        reuse.setText(Messages.ReuseConnectionWidget_ReuseSettingsForThisServer);
        return displayArea;
    }
}
