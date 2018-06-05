/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.wizards.io;

import com.perforce.team.core.mergequest.model.BranchGraph;
import com.perforce.team.core.mergequest.model.IBranchGraph;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.views.SessionManager;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ExportGraphSelectionPage extends WizardPage {

    /**
     * PATHS
     */
    public static final String PATHS = "PATHS"; //$NON-NLS-1$

    private IBranchGraph[] graphs;

    private IBranchGraph[] selected = new IBranchGraph[0];
    private String path = ""; //$NON-NLS-1$

    private Combo fileCombo;

    /**
     * @param graphs
     */
    public ExportGraphSelectionPage(IBranchGraph[] graphs) {
        super("graphSelection"); //$NON-NLS-1$
        setTitle(Messages.ExportGraphSelectionPage_Title);
        setDescription(Messages.ExportGraphSelectionPage_Description);
        this.graphs = graphs;
    }

    /**
     * Get path
     * 
     * @return path
     */
    public String getPath() {
        return this.path;
    }

    /**
     * Get graphs
     * 
     * @return selected graphs
     */
    public IBranchGraph[] getGraphs() {
        return this.selected;
    }

    private void validate() {
        String message = null;
        if (message == null && selected.length == 0) {
            message = Messages.ExportGraphSelectionPage_Description;
        }
        if (message == null && path.length() == 0) {
            message = Messages.ExportGraphSelectionPage_EnterFileName;
        }
        setPageComplete(message == null);
        setErrorMessage(message);
    }

    /**
     * Save history
     */
    public void saveHistory() {
        if (P4UIUtils.okToUse(fileCombo)) {
            SessionManager.saveComboHistory(fileCombo, 10, PATHS);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        new Label(displayArea, SWT.NONE)
                .setText(Messages.ExportGraphSelectionPage_AvailableGraphs);

        final CheckboxBranchGraphViewer viewer = new CheckboxBranchGraphViewer(
                graphs);
        viewer.createControl(displayArea);
        selected = graphs;
        viewer.getViewer().setAllChecked(true);
        viewer.getViewer().addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                List<BranchGraph> graphSelections = new ArrayList<BranchGraph>();
                for (Object selected : viewer.getViewer().getCheckedElements()) {
                    if (selected instanceof BranchGraph) {
                        graphSelections.add((BranchGraph) selected);
                    }
                }
                selected = graphSelections
                        .toArray(new BranchGraph[graphSelections.size()]);
                validate();
            }
        });

        Label selectLabel = new Label(displayArea, SWT.NONE);
        selectLabel.setText(Messages.ExportGraphSelectionPage_Destination);
        GridData slData = new GridData(SWT.FILL, SWT.FILL, true, false);
        slData.verticalIndent = 15;
        selectLabel.setLayoutData(slData);

        Composite fileArea = new Composite(displayArea, SWT.NONE);
        GridLayout faLayout = new GridLayout(3, false);
        faLayout.marginHeight = 0;
        faLayout.marginWidth = 0;
        faLayout.marginLeft = 20;
        fileArea.setLayout(faLayout);
        fileArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        new Label(fileArea, SWT.NONE)
                .setText(Messages.ExportGraphSelectionPage_File);

        fileCombo = new Combo(fileArea, SWT.SINGLE | SWT.DROP_DOWN);
        SessionManager.loadComboHistory(fileCombo, PATHS);

        final Runnable fileComboRunnable = new Runnable() {

            public void run() {
                path = fileCombo.getText();
                validate();
            }
        };
        fileCombo.addSelectionListener(P4UIUtils
                .createComboSelectionListener(fileComboRunnable));
        fileCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                fileComboRunnable.run();
            }
        });

        fileCombo
                .setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Button browseButton = new Button(fileArea, SWT.PUSH);
        browseButton.setText(Messages.ExportGraphSelectionPage_Browse);
        browseButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dialog = new FileDialog(browseButton.getShell(),
                        SWT.SAVE);
                String selected = dialog.open();
                if (selected != null) {
                    fileCombo.setText(selected);
                }
            }

        });

        setControl(displayArea);
        setPageComplete(false);
    }
}
