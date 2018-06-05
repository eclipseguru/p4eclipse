/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mergequest.builder.IBranchGraphBuilder;
import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ImportSelectionPage extends WizardPage {

    private CheckboxBranchGraphViewer viewer;
    private IBranchGraph[] selected = new IBranchGraph[0];
    private IBranchGraph[] graphs = new IBranchGraph[0];

    /**
     */
    public ImportSelectionPage() {
        super("importSelection"); //$NON-NLS-1$
        setTitle(Messages.ImportSelectionPage_Title);
        setDescription(Messages.ImportSelectionPage_Description);
    }

    /**
     * Get selected graphs
     * 
     * @return - branch graphs
     */
    public IBranchGraph[] getGraphs() {
        return this.selected;
    }

    private void validate() {
        String message = null;
        if (message == null && selected.length == 0) {
            message = Messages.ImportSelectionPage_Description;
        }
        setPageComplete(message == null);
        setErrorMessage(message);
    }

    private void showLoadError(Throwable e) {
        if (e.getCause() != null) {
            e = e.getCause();
        }
        final String message = MessageFormat.format(
                Messages.ImportSelectionPage_LoadErrorMessage, e.getClass()
                        .getCanonicalName(), e.getLocalizedMessage());
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                P4ConnectionManager.getManager()
                        .openError(P4UIUtils.getDialogShell(),
                                Messages.ImportSelectionPage_ErrorLoadingPages,
                                message);
            }
        });
    }

    /**
     * occurred
     * 
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            final IBranchGraphBuilder builder = ((ImportBranchGraphWizard) getWizard())
                    .getBuilder();
            try {
                getContainer().run(true, true, new IRunnableWithProgress() {

                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        monitor.beginTask(
                                Messages.ImportSelectionPage_LoadingGraphs, 1);
                        if (builder != null) {
                            try {
                                graphs = builder.load().getGraphs();
                            } catch (IOException e) {
                                graphs = new IBranchGraph[0];
                                showLoadError(e);
                            }
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
            viewer.getViewer().setInput(this.graphs);
            viewer.getViewer().setAllChecked(true);
            updateChecked();
        } else {
            setPageComplete(false);
        }
        super.setVisible(visible);
    }

    private void updateChecked() {
        List<BranchGraph> graphSelections = new ArrayList<BranchGraph>();
        for (Object selected : viewer.getViewer().getCheckedElements()) {
            if (selected instanceof BranchGraph) {
                graphSelections.add((BranchGraph) selected);
            }
        }
        selected = graphSelections.toArray(new BranchGraph[graphSelections
                .size()]);
        validate();
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        new Label(displayArea, SWT.NONE)
                .setText(Messages.ImportSelectionPage_AvailableGraphs);

        viewer = new CheckboxBranchGraphViewer(this.graphs);
        viewer.createControl(displayArea);
        viewer.getViewer().addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateChecked();
            }
        });
        setControl(displayArea);
        setPageComplete(false);
    }

}
