package com.perforce.team.ui.views;

import org.eclipse.swt.widgets.Composite;

import com.perforce.team.core.p4java.IP4Connection;

/**
 * A generic control which can be used by both view part and dialog.
 * 
 * @author ali
 *
 */
public interface IPerforceViewControl {
    
    public Composite createViewControl(Composite parent);

    public void setFocus();
    public void dispose();
    public void refresh();
    
    public IP4Connection getConnection();
}
