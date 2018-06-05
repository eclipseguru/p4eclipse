/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.p4java.IP4Connection;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportBranchGraphWizard extends Wizard {

    private IP4Connection connection;
    private IBranchGraph[] imported = new IBranchGraph[0];

    private ImportLocationPage locationPage;
    private ImportSelectionPage selectionPage;

    /**
     * Create new import branch graph wizard
     * 
     * @param connection
     */
    public ImportBranchGraphWizard(IP4Connection connection) {
        this.connection = connection;
        setNeedsProgressMonitor(true);
    }

    /**
     * Get selected branch graphs to import
     * 
     * @return - non-null but possibly empty array
     */
    public IBranchGraph[] getSelected() {
        return this.imported;
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        setWindowTitle(Messages.ImportBranchGraphWizard_Title);
        setDefaultPageImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ));
    }

    /**
     * Get builder
     * 
     * @return - branch graph builder
     */
    public IBranchGraphBuilder getBuilder() {
        return locationPage.getBuilder();
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        locationPage = new ImportLocationPage(connection);
        addPage(locationPage);
        selectionPage = new ImportSelectionPage();
        addPage(selectionPage);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        this.imported = selectionPage.getGraphs();
        locationPage.saveHistory();
        return true;
    }
}
