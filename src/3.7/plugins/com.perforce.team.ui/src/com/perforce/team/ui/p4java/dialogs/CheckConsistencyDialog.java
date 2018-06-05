/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.changelists.PendingCombo;
import com.perforce.team.ui.dialogs.FileListViewer;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.PerforceDialog;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class CheckConsistencyDialog extends PerforceDialog {

    private IFile[] diffFiles;
    private IFile[] missingFiles;
    private IFile[] newFiles;

    private FileListViewer diffViewer;
    private FileListViewer missingViewer;
    private FileListViewer newViewer;

    private IP4Connection connection;

    private PendingCombo changeCombo;

    /**
     * 
     * @param parent
     * @param connection
     * @param diffFiles
     * @param missingFiles
     * @param newFiles
     */
    public CheckConsistencyDialog(Shell parent, IP4Connection connection,
            IFile[] diffFiles, IFile[] missingFiles, IFile[] newFiles) {
        super(parent, Messages.CheckConsistencyDialog_ConsistencyCheckResults);
        this.diffFiles = diffFiles;
        this.missingFiles = missingFiles;
        this.newFiles = newFiles;
        this.connection = connection;
        setModalResizeStyle();
    }

    /**
     * Get the selected pending changelist id
     * 
     * @return - p4 pending changelist id
     */
    public int getSelectedChange() {
        return this.changeCombo.getSelected();
    }

    /**
     * Get the entered pending changelist description
     * 
     * @return - description
     */
    public String getSelectedDescription() {
        return this.changeCombo.getDescription();
    }

    /**
     * Get the diff files
     * 
     * @return - diff files
     */
    public IFile[] getDiffFiles() {
        return diffFiles;
    }

    /**
     * Get the missing files
     * 
     * @return - missing files
     */
    public IFile[] getMissingFiles() {
        return missingFiles;
    }

    /**
     * Get the new files
     * 
     * @return - new files
     */
    public IFile[] getNewFiles() {
        return newFiles;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(dialogArea, 1, GridData.FILL_BOTH);

        if (diffFiles.length > 0) {
            Composite diffGroup = createTitledArea(composite,
                    GridData.FILL_BOTH);
            createLabel(diffGroup,
                    Messages.CheckConsistencyDialog_UnopenedFilesThatDiffer);
            diffViewer = new FileListViewer(diffGroup, diffFiles, diffFiles,
                    false);
            PlatformUI
                    .getWorkbench()
                    .getHelpSystem()
                    .setHelp(diffViewer.getControl(),
                            IHelpContextIds.CHECK_CONSISTENCY_DIFF_FILES);
        }

        if (missingFiles.length > 0) {
            Composite missingGroup = createTitledArea(composite,
                    GridData.FILL_BOTH);
            createLabel(missingGroup,
                    Messages.CheckConsistencyDialog_UnopenedFilesMissing);
            missingViewer = new FileListViewer(missingGroup, missingFiles,
                    missingFiles, false);
            PlatformUI
                    .getWorkbench()
                    .getHelpSystem()
                    .setHelp(missingViewer.getControl(),
                            IHelpContextIds.CHECK_CONSISTENCY_MISSING_FILES);
        }

        if (newFiles.length > 0) {
            Composite newGroup = createTitledArea(composite, GridData.FILL_BOTH);
            createLabel(newGroup,
                    Messages.CheckConsistencyDialog_FilesNotUnderPerforce);
            newViewer = new FileListViewer(newGroup, newFiles, newFiles, false);
            PlatformUI
                    .getWorkbench()
                    .getHelpSystem()
                    .setHelp(newViewer.getControl(),
                            IHelpContextIds.CHECK_CONSISTENCY_NEW_FILES);
        }

        changeCombo = createChangeCombo(composite);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(changeCombo.getCombo(),
                        IHelpContextIds.CHECK_CONSISTENCY_CHANGES);

        return composite;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (diffFiles.length > 0) {
            diffFiles = getFiles(diffViewer.getCheckedElements());
        }
        if (missingFiles.length > 0) {
            missingFiles = getFiles(missingViewer.getCheckedElements());
        }
        if (newFiles.length > 0) {
            newFiles = getFiles(newViewer.getCheckedElements());
        }

        super.okPressed();
    }

    private IFile[] getFiles(Object[] objs) {
        IFile[] files = new IFile[objs.length];
        for (int i = 0; i < objs.length; i++) {
            files[i] = (IFile) objs[i];
        }
        return files;
    }

    private PendingCombo createChangeCombo(Composite parent) {
        PendingCombo combo = new PendingCombo(
                Messages.CheckConsistencyDialog_OpenInChangelist,
                this.connection);
        combo.createControl(parent);
        return combo;
    }

}
