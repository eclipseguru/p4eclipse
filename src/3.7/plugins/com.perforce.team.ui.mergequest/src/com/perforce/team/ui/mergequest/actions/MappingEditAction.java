/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.actions;

import com.perforce.team.core.mergequest.model.BranchSpecMapping;
import com.perforce.team.core.mergequest.model.DepotPathMapping;
import com.perforce.team.core.mergequest.model.Mapping;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.wizards.mapping.EditConnectorWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.branch.EditBranchConnectorWizard;
import com.perforce.team.ui.mergequest.wizards.mapping.depot.EditDepotPathConnectorWizard;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class MappingEditAction extends Action {

    private Mapping mapping;

    /**
     * Mapping edit action
     * 
     * @param mapping
     */
    public MappingEditAction(Mapping mapping) {
        this.mapping = mapping;
    }

    /**
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (mapping != null) {
            EditConnectorWizard wizard = null;
            if (DepotPathMapping.TYPE.equals(mapping.getType())) {
                wizard = new EditDepotPathConnectorWizard(mapping);
            } else if (BranchSpecMapping.TYPE.equals(mapping.getType())) {
                wizard = new EditBranchConnectorWizard(mapping);
            }
            if (wizard != null) {
                WizardDialog dialog = new WizardDialog(
                        P4UIUtils.getDialogShell(), wizard);
                dialog.open();
            }
        }
    }

}
