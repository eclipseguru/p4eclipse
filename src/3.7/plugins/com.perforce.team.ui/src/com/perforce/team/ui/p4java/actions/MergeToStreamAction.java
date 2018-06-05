package com.perforce.team.ui.p4java.actions;

import org.eclipse.swt.widgets.Shell;

import com.perforce.p4java.option.Options;
import com.perforce.p4java.option.client.MergeFilesOptions;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Stream;
import com.perforce.team.core.p4java.P4FileIntegration;
import com.perforce.team.ui.p4java.dialogs.IntegrateToStreamDialog;
import com.perforce.team.ui.p4java.dialogs.MergeToStreamDialog;


/**
 * Action contribute to workbench selected objects. It is also called by the
 * MergeToStreamHandler for merge to stream only.
 * 
 * <p/>
 * 
 * Note: The enablement of the class is different from the MergeToStreamHandler,
 * which defined in StreamPropertyTester.
 * 
 */
public class MergeToStreamAction extends AbstractStreamIntegAction {
	
    @Override
    protected IntegrateToStreamDialog createIntegrateDialog(Shell shell,
            IP4Resource resource) {
        return new MergeToStreamDialog(shell, resource);
    }

    @Override
    protected IP4Resource[] doIntegrate(IP4Connection connection,
            P4FileIntegration integration, Options options, String changeDesc) {
        if(options instanceof MergeFilesOptions)
            return connection.mergeStream(integration, changeDesc, (MergeFilesOptions) options);
        return null;
    }
    
    public void mergeToStream(IP4Stream target){
        if (target != null) {
        	doRun(target);
        }
    }

}
