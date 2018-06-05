/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.actions;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.MessageDialog;

import com.perforce.p4java.option.client.ResolveFilesAutoOptions;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.dialogs.ResolveWizard;
import com.perforce.team.ui.p4java.dialogs.ResolveWizardDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ResolveAction extends BaseResolveAction {

    /**
     * Get collection of resources to resolve
     * 
     * @return - p4 collection
     */
    protected P4Collection getResolveCollection() {
        return getFileSelection();
    }

    /**
     * @see com.perforce.team.ui.p4java.actions.P4Action#runAction()
     */
    @Override
    public void runAction() {
        P4Collection collection = getResolveCollection();
        IP4Resource[] unresolved = collection.getUnresolved();
        if (unresolved.length > 0) {
            ResolveWizardDialog dialog = new ResolveWizardDialog(
                    P4UIUtils.getShell(), new ResolveWizard(unresolved));
//            ResolveDialog dialog = new ResolveDialog(getShell(), unresolved);
            dialog.open();
        } else {
            MessageDialog.openInformation(getShell(),
                    Messages.ResolveAction_NoUnresolvedFilesTitle,
                    Messages.ResolveAction_NoUnresolvedFilesMessage);
        }
    }

    /**
     * Resolves the unresolved files using the specified options
     * 
     * @param type
     * @param preview
     */
    public void resolve(ResolveFilesAutoOptions options) {
        P4Collection collection = getFileSelection();
        IP4Resource[] unresolved = collection.getUnresolved();
        if (unresolved.length > 0) {
            collection = createCollection(unresolved);
            if (!collection.isEmpty()) {
                collection.resolve(options);
                if (!options.isShowActionsOnly()) {
                    collection.refreshLocalResources(IResource.DEPTH_ONE);
                }
            }
        }
    }
}
