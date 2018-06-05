package com.perforce.team.ui.views;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.P4ConnectionManager;

/**
 * View part based implmentation of IPerforceView.
 * 
 * @author ali
 *
 */
public class PartBasedView implements IPerforceView {

    private IViewPart part;

    public PartBasedView(IViewPart part){
        this.part=part;
    }

    public IWorkbenchPartSite getSite() {
        return part.getSite();
    }

    public IP4Connection getConnection() {
        ISelection selection = part.getSite().getPage().getSelection();
        IP4Connection connection = AbstractPerforceViewControl.getConnection(selection);
        
        if (connection == null) {
            // Optimization since a significant portion of users probably have a
            // single connection configured
            IP4Connection[] connections = P4ConnectionManager.getManager()
                    .getConnections();
            if (connections.length == 1) {
                connection = connections[0];
            }
        }
        if (connection == null) {
            connection = P4ConnectionManager.getManager().getSelection(true);
        }
        return connection;
    }

    public Shell getShell() {
        return part.getSite().getShell();
    }

}
