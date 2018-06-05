/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.builder.FileBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.BranchGraphContainer;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraphContainer;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ExportBranchGraphWizard extends Wizard {

    private IBranchGraphContainer container;
    private ExportGraphSelectionPage selectionPage;

    /**
     * Export graph wizard
     * 
     * @param container
     */
    public ExportBranchGraphWizard(IBranchGraphContainer container) {
        this.container = container;
        setNeedsProgressMonitor(true);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#createPageControls(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPageControls(Composite pageContainer) {
        super.createPageControls(pageContainer);
        setWindowTitle(Messages.ExportBranchGraphWizard_Title);
        setDefaultPageImageDescriptor(WorkbenchImages
                .getImageDescriptor(IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ));
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
        selectionPage = new ExportGraphSelectionPage(this.container.getGraphs());
        addPage(selectionPage);
    }

    /**
     * @see org.eclipse.jface.wizard.Wizard#performFinish()
     */
    @Override
    public boolean performFinish() {
        final IBranchGraph[] graphs = selectionPage.getGraphs();
        final String path = selectionPage.getPath();
        final boolean[] finished = new boolean[] { true };
        try {
            getContainer().run(true, true, new IRunnableWithProgress() {

                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException {
                    IBranchGraphContainer subContainer = new BranchGraphContainer();
                    for (IBranchGraph graph : graphs) {
                        subContainer.add(graph);
                    }
                    monitor.beginTask(
                            MessageFormat
                                    .format(Messages.ExportBranchGraphWizard_SavingGraphs,
                                            path), 1);
                    FileBranchGraphBuilder builder = new FileBranchGraphBuilder(
                            path);
                    try {
                        builder.persist(subContainer);
                        finished[0] = true;
                    } catch (IOException e) {
                        finished[0] = false;
                        showSaveError(e);
                    }
                    monitor.worked(1);
                    monitor.done();
                }
            });
        } catch (InvocationTargetException e) {
            PerforceProviderPlugin.logError(e);
        } catch (InterruptedException e) {
            PerforceProviderPlugin.logError(e);
        }
        selectionPage.saveHistory();
        return finished[0];
    }

    private void showSaveError(final IOException e) {
        Runnable runnable = new Runnable() {

            public void run() {
                final String message = MessageFormat.format(
                        Messages.ExportBranchGraphWizard_ErrorSavingMessage, e
                                .getClass().getCanonicalName(), e
                                .getLocalizedMessage());
                P4ConnectionManager.getManager().openError(
                        P4UIUtils.getDialogShell(),
                        Messages.ExportBranchGraphWizard_ErrorSavingTitle,
                        message);
            }
        };
        PerforceUIPlugin.syncExec(runnable);
    }

}
