package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.option.Options;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Runnable;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog;


public abstract class AbstractStreamIntegAction extends AbstractStreamAction {

    abstract protected IntegrateToStreamDialog createIntegrateDialog(Shell shell,
            IP4Resource resource);

    abstract protected IP4Resource[] doIntegrate(IP4Connection connection, P4FileIntegration integration, Options options, String changeDesc);
    
    @Override
    public boolean isEnabledEx() {
    	return super.isEnabledEx() && (isSelectionInClientStream() || isDiffStreamDiffClientSwitch() || isConnectedToSandBox());
    }

    protected void doRun(final IP4Resource resource){
    	if(!switchStreams(resource,true))
    		return;
        IntegrateToStreamDialog dialog = createIntegrateDialog(getShell(), resource);
        if (dialog.open() == Window.OK) {
            final IP4Connection connection = resource.getConnection();
            final P4FileIntegration integration = dialog.getFileIntegration();
            final Options options = dialog.getOptions();
            final String desc = dialog.getChangeListDescription();

            IP4Runnable runnable = new P4Runnable() {
                @Override
                public void run(IProgressMonitor monitor) {
                    IP4Resource[] integrated = doIntegrate(connection,integration, options, desc);
                    if (integrated!=null && integrated.length > 0) {
                        P4Collection collection = createCollection(integrated);
                        collection.refreshLocalResources(IResource.DEPTH_INFINITE);
                        resource.refresh();
                    }
                }

                @Override
                public String getTitle() {
                    return Messages.IntegrateAction_IntegratingFiles;
                }

            };
            runRunnable(runnable);
        }

    }

}
