/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.osgi.util.NLS;

import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.team.core.P4ClientUtil;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.actions.Messages;
import com.perforce.team.ui.dialogs.CompareDialog;
import com.perforce.team.ui.dialogs.PerforceEditorInput;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ManualResolveAction extends P4Action {

    /**
     * Runs a manual resolve on the file and returns the files that were
     * resolved
     * 
     * @return - array of resolved files
     */
    public IP4File runManualResolve(IP4File file, int whichIntegSpec) {
        CompareDialog dialog = createManualResolveDialog(file, whichIntegSpec);
        if (dialog == null || dialog.open() != Dialog.OK) {
            return null;
        }
        P4Collection single = createCollection(new IP4Resource[] { file });
        single.setType(IP4Resource.Type.LOCAL);
		try {
			IFileSpec spec = file.getIntegrationSpecs()[whichIntegSpec];
			IP4Resource[] resolved = single
			        .resolve(file.getLocalFiles()[0].getContents(),true,spec.getStartFromRev(),spec.getEndFromRev());
            if (resolved.length == 1 && resolved[0] instanceof IP4File){
            	return (IP4File) resolved[0];
            }
			return null;
		} catch (CoreException e) {
			PerforceProviderPlugin.logError(e);
			return null;
		}

    }

    /**
     * Creates a manual resolve compare dialog for a given unresolved p4 file
     * 
     * @param file
     * @return - created dialog or null if comparison fails
     */
    public CompareDialog createManualResolveDialog(IP4File file, int which) {
        CompareDialog dialog = null;
        // createCollection(new IP4Resource[] { file }).resolve(
        // new
        // ResolveFilesAutoOptions().setShowActionsOnly(true).setShowBase(true).setResolveFileContentChanges(true));
        IFileSpec spec = file.getIntegrationSpecs()[which];
        if (spec != null) {
            IP4File from = file;
            String fromFile = spec.getFromFile();

            // for non-text type, no baseFile and baseRev, replace them
            int baseRev = P4ClientUtil.getBaseRev(spec);
            String baseFile = P4ClientUtil.getBaseFile(spec);

            // Correct revs since they can be -1 if the 'none' is in the output
            // map

            // int fromRev = Math.max(1,spec.getStartFromRev());
            if (fromFile != null) {
                IP4File foundFromFile = file.getConnection().getFile(fromFile);
                if (foundFromFile != null) {
                    from = foundFromFile;
                }
            }
            
            String toRev=P4ClientUtil.computeTheirRev(spec);
            
            IP4File base = file.getConnection().getFile(baseFile);
            File ancestor = null;
            if(baseRev!=IFileSpec.NO_FILE_REVISION && base!=null)
            	ancestor = P4CoreUtils.createFile(base.getRemoteContents(baseRev));
            // File ancestor = P4CoreUtils.createFile(from
            // .getRemoteContents(fromRev));
            File depot = P4CoreUtils.createFile(from.getRemoteContents(toRev));
            File current = new File(file.getLocalPath());
            if (depot != null && current != null) {
                String type = new Path(file.getLocalPath()).getFileExtension();
                PerforceEditorInput input = new PerforceEditorInput(
                        Messages.ManualResolveAction_EDITORTITLE, true, type,
                        file.getLocalPath(), NLS.bind(
                                Messages.ManualResolveAction_FILETITLE,
                                file.getName()), depot.getPath(), NLS.bind(
                                Messages.ManualResolveAction_DEPOTTITLE,
                                from.getName(), toRev), ancestor!=null?ancestor.getPath():null,
                        NLS.bind(Messages.ManualResolveAction_BASEFILE,
                                base!=null?base.getName():"", baseRev));
                dialog = CompareDialog.createCompareDialog(getShell(), input);
            }
        }
        return dialog;
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    protected void runAction() {
        // runManualResolve();
    }
}
