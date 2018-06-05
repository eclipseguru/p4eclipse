package com.perforce.team.ui.p4java.actions;

import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.option.Options;
import com.perforce.p4java.option.client.CopyFilesOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.ui.p4java.dialogs.CopyToStreamDialog;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog;


/**
 * Action contribute to workbench selected objects. It is also called by the
 * CopyToStreamHandler for copy to stream only.
 * 
 * <p/>
 * 
 * Note: The enablement of the class is different from the CopyToStreamHandler,
 * which defined in StreamPropertyTester.
 * 
 */
public class CopyToStreamAction extends AbstractStreamIntegAction {

	@Override
    protected IntegrateToStreamDialog createIntegrateDialog(Shell shell,
            IP4Resource resource) {
        return new CopyToStreamDialog(shell, resource);
    }

    @Override
    protected IP4Resource[] doIntegrate(IP4Connection connection,
            P4FileIntegration integration, Options options, String changeDesc) {
        if(options instanceof CopyFilesOptions)
            return connection.copyStream(integration, changeDesc, (CopyFilesOptions) options);
        return null;
    }
    
    /**
     * Called by CopyToStreamHandler only.
     * <p/>
     */
    public void copyToStream(IP4Stream target){
        if (target != null) {
        	doRun(target);
        }
    }

}
