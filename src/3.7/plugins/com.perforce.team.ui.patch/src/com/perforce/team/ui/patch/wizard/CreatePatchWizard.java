/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.patch.wizard;

import com.perforce.team.core.p4java.P4Collection;
import com.perforce.team.ui.patch.P4PatchUiPlugin;
import com.perforce.team.ui.patch.model.IPatchStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CreatePatchWizard extends Wizard {

    private P4Collection collection;
    private LocationPage locationPage;

    /**
     * Create new patch wizard
     * 
     * @param collection
     */
    public CreatePatchWizard(P4Collection collection) {
        this.collection = collection;
        setDialogSettings(P4PatchUiPlugin.getDefault().getDialogSettings());
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        locationPage = new LocationPage(this.collection);
        addPage(locationPage);
        setDefaultPageImageDescriptor(P4PatchUiPlugin
                .getDescriptor("icons/wizard/create_patch.png")); //$NON-NLS-1$
        setWindowTitle(Messages.CreatePatchWizard_Title);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        locationPage.saveHistory();
        return true;
    }

    /**
     * Get resources to include in patch
     * 
     * @return resources
     */
    public IResource[] getResources() {
        return this.locationPage.getResources();
    }

    /**
     * Get patch stream
     * 
     * @return stream
     */
    public IPatchStream getStream() {
        return locationPage.getStream();
    }

}
