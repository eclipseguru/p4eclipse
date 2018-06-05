package com.perforce.team.ui.views;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPartSite;

import com.perforce.team.core.p4java.IP4Connection;

/**
 * Interface used with IPerforceViewControl.
 * 
 * @author ali
 *
 */
public interface IPerforceView {
    
    IWorkbenchPartSite getSite();

    IP4Connection getConnection();
    
    Shell getShell();
    
}
