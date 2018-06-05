/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import com.perforce.team.ui.connection.BasicConnectionWidget;
import com.perforce.team.ui.dialogs.SetConnectionDialog;

import org.eclipse.swt.widgets.Shell;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ReuseConnectionDialog extends SetConnectionDialog {

    private boolean reuse = false;

    /**
     * @param parent
     */
    public ReuseConnectionDialog(Shell parent) {
        super(parent);
    }

    /**
     * Should the connection parameters settings be re-used?
     * 
     * @return - true to re-use, false otherwise
     */
    public boolean reuse() {
        return this.reuse;
    }

    /**
     * @see com.perforce.team.ui.dialogs.SetConnectionDialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.reuse = ((ReuseConnectionWidget) getWidget()).reuse();
        super.okPressed();
    }

    /**
     * @see com.perforce.team.ui.dialogs.SetConnectionDialog#createWidget()
     */
    @Override
    protected BasicConnectionWidget createWidget() {
        return new ReuseConnectionWidget();
    }

}
