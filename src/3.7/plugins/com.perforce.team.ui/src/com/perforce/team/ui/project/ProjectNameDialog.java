/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.project;

import java.text.MessageFormat;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ProjectNameDialog extends P4StatusDialog {

    private String projectName = null;
    private boolean showInUseMessage = true;

    /**
     * @param parent
     * @param projectName
     * @param title
     * @param showInUseMessage
     */
    public ProjectNameDialog(Shell parent, String projectName, String title,
            boolean showInUseMessage) {
        super(parent);
        setTitle(title);
        setModalResizeStyle();
        this.projectName = projectName;
        this.showInUseMessage = showInUseMessage;
    }

    /**
     * @param parent
     * @param projectName
     */
    public ProjectNameDialog(Shell parent, String projectName) {
        this(parent, projectName, Messages.ProjectNameDialog_ProjectNameInUse,
                true);
    }

    /**
     * Get entered name
     * 
     * @return - entered name
     */
    public String getEnteredName() {
        return this.projectName;
    }

    private String getExistsMessage(String name) {
        return MessageFormat.format(
                Messages.ProjectNameDialog_ProjectAlreadyExists, name);
    }

    private void validate() {
        if (this.projectName != null) {
            IPath path = new Path(this.projectName);
            if (path.isValidSegment(this.projectName)) {
                IProject project = ResourcesPlugin.getWorkspace().getRoot()
                        .getProject(this.projectName);
                if (project != null && project.exists()) {
                    setErrorMessage(getExistsMessage(this.projectName));
                    return;
                }
            } else {
                setErrorMessage(Messages.ProjectNameDialog_ProjectNameInvalid);
                return;
            }
        }
        setErrorMessage(null);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(parent, SWT.NONE);
        displayArea.setLayout(new GridLayout(2, false));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (this.projectName != null && showInUseMessage) {
            Label originalLabel = new Label(displayArea, SWT.NONE);
            GridData olData = new GridData(SWT.FILL, SWT.FILL, true, false);
            olData.horizontalSpan = 2;
            originalLabel.setLayoutData(olData);
            originalLabel.setText(MessageFormat.format(
                    Messages.ProjectNameDialog_ProjectAlreadyExists2,
                    this.projectName));
        }

        Label projectLabel = new Label(displayArea, SWT.NONE);
        projectLabel.setText(Messages.ProjectNameDialog_ProjectName);
        final Text projectText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        GridData ptData = new GridData(SWT.FILL, SWT.FILL, true, false);
        ptData.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
        projectText.setLayoutData(ptData);
        if (this.projectName != null) {
            projectText.setText(this.projectName);
            projectText.selectAll();
            projectText.setFocus();
        }
        projectText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                projectName = projectText.getText().trim();
                validate();
            }
        });

        validate();

        return c;
    }
}
