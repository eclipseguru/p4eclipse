package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareEditorInput;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Compare dialog
 */
public class CompareDialog extends ResizableDialog implements
        IPropertyChangeListener {

    private CompareEditorInput fCompareEditorInput;
    private Button fCommitButton;

    CompareDialog(Shell shell, CompareEditorInput input) {
        super(shell, null);

        // Assert.isNotNull(input);
        fCompareEditorInput = input;
        fCompareEditorInput.addPropertyChangeListener(this);
        // setHelpContextId(ICompareContextIds.COMPARE_DIALOG);
    }

    /**
     * Opens a compare dialog if comparison succeeds
     * 
     * @param shell
     * @param input
     * @return - result of comparison dialog
     */
    public static int openCompareDialog(Shell shell,
            final CompareEditorInput input) {
        CompareDialog dialog = createCompareDialog(shell, input);
        int rc = CANCEL;
        if (dialog != null) {
            rc = dialog.open();
        }
        return rc;
    }

    /**
     * Creates a compare dialog
     * 
     * @param shell
     * @param input
     * @return - compare dialog if comparison succeeds or null if it fails
     */
    public static CompareDialog createCompareDialog(Shell shell,
            final CompareEditorInput input) {
        CompareDialog dialog = null;
        if (compareResultOK(shell, input)) {
            dialog = new CompareDialog(shell, input);
        }
        return dialog;
    }

    /**
     * @return <code>true</code> if compare result is OK to show,
     *         <code>false</code> otherwise
     */
    private static boolean compareResultOK(Shell shell, CompareEditorInput input) {
        try {

            // run operation in separate thread and make it canceable
            new ProgressMonitorDialog(shell).run(true, true, input);

            String message = input.getMessage();
            if (message != null) {
                MessageDialog.openError(shell, "Compare Failed", message); //$NON-NLS-1$
                return false;
            }

            if (input.getCompareResult() == null) {
                MessageDialog
                        .openInformation(
                                shell,
                                "Compare", "There are no differences between the selected inputs."); //$NON-NLS-2$ //$NON-NLS-1$
                return false;
            }

            return true;

        } catch (InterruptedException x) {
            // cancelled by user
        } catch (InvocationTargetException x) {
            MessageDialog.openError(shell,
                    "Compare Failed", x.getTargetException().getMessage()); //$NON-NLS-1$
        }
        return false;
    }

    /**
     * @see com.perforce.team.ui.dialogs.ResizableDialog#close()
     */
    @Override
    public boolean close() {
        if (super.close()) {
            if (fCompareEditorInput != null) {
                fCompareEditorInput.addPropertyChangeListener(this);
            }
            return true;
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (fCompareEditorInput instanceof PerforceEditorInput
                && ((PerforceEditorInput) fCompareEditorInput).allowMerge()) {
            fCommitButton = createButton(parent, IDialogConstants.OK_ID,
                    "Commit", true); //$NON-NLS-1$
            fCommitButton.setEnabled(false);
            createButton(parent, IDialogConstants.CANCEL_ID,
                    IDialogConstants.CANCEL_LABEL, false);
        } else {
            createButton(parent, IDialogConstants.OK_ID,
                    IDialogConstants.OK_LABEL, false);
        }
    }

    /**
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent event) {
        if (fCommitButton != null && fCompareEditorInput != null) {
            fCommitButton.setEnabled(fCompareEditorInput.isSaveNeeded());
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent2) {

        Composite parent = (Composite) super.createDialogArea(parent2);

        Control c = fCompareEditorInput.createContents(parent);
        c.setLayoutData(new GridData(GridData.FILL_BOTH));

        Shell shell = c.getShell();
        shell.setText(fCompareEditorInput.getTitle());
        shell.setImage(fCompareEditorInput.getTitleImage());
        applyDialogFont(parent);
        return parent;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        if (fCompareEditorInput.isSaveNeeded()) {

            WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {

                @Override
                public void execute(IProgressMonitor pm) throws CoreException {
                    fCompareEditorInput.saveChanges(pm);
                }
            };

            Shell shell = getParentShell();
            ProgressMonitorDialog pmd = new ProgressMonitorDialog(shell);
            try {
                // operation.run(pmd.getProgressMonitor());
                pmd.run(false, false, operation); // Fix for standard
                // CompareDialog

            } catch (InterruptedException x) {
                // NeedWork
            } catch (OperationCanceledException x) {
                // NeedWork
            } catch (InvocationTargetException x) {
                String title = Messages.CompareDialog_SaveErrorTitle;
                String msg = Messages.CompareDialog_SaveErrorMessage;
                MessageDialog.openError(shell, title, msg
                        + x.getTargetException().getMessage());
            }
        }
        super.okPressed();
    }
}
