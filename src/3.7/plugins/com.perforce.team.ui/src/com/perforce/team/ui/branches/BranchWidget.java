/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.perforce.p4java.core.IBranchMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.dialogs.P4FormDialog;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BranchWidget {

    private Composite displayArea;
    private Text branchText;
    private Label accessLabel;
    private Text accessText;
    private Label updateLabel;
    private Text updateText;
    private Text ownerText;
    private Text descriptionText;
    private Button lockedButton;
    private Text viewText;

    public void addFormNameValidation(P4FormDialog dialog) {
        dialog.addFormNameValidation(branchText, "branch");
    }

    /**
     * Create branch widget with defaults
     * 
     * @param parent
     */
    public BranchWidget(Composite parent) {
        this(parent, 5, 5, true);
    }

    /**
     * Create branch widget
     * 
     * @param parent
     * @param marginWidth
     * @param marginHeight
     * @param editable
     */
    public BranchWidget(Composite parent, int marginWidth, int marginHeight,
            boolean editable) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(4, false);
        daLayout.marginHeight = marginHeight;
        daLayout.marginWidth = marginWidth;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridData multiTextData = new GridData(SWT.FILL, SWT.FILL, true, true);
        multiTextData.horizontalSpan = 3;
        multiTextData.heightHint = 75;

        Label nameLabel = new Label(displayArea, SWT.LEFT);
        nameLabel.setText(Messages.BranchWidget_Name);
        branchText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        branchText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        accessLabel = new Label(displayArea, SWT.LEFT);
        accessLabel.setLayoutData(new GridData());
        accessLabel.setText(Messages.BranchWidget_AccessTime);
        accessText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        accessText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        updateLabel = new Label(displayArea, SWT.LEFT);
        updateLabel.setText(Messages.BranchWidget_UpdateTime);
        updateLabel.setLayoutData(new GridData());
        updateText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        updateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label ownerLabel = new Label(displayArea, SWT.LEFT);
        ownerLabel.setText(Messages.BranchWidget_Owner);
        ownerText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        ownerText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label descriptionLabel = new Label(displayArea, SWT.LEFT);
        descriptionLabel.setText(Messages.BranchWidget_Description);
        descriptionText = new Text(displayArea, SWT.MULTI | SWT.BORDER
                | SWT.WRAP);
        descriptionText.setLayoutData(multiTextData);

        Label optionsLabel = new Label(displayArea, SWT.LEFT);
        optionsLabel.setText(Messages.BranchWidget_Options);
        lockedButton = new Button(displayArea, SWT.CHECK);
        lockedButton.setText(Messages.BranchWidget_Locked);
        GridData lbData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        lbData.horizontalSpan = 3;
        lockedButton.setLayoutData(lbData);

        Label viewLabel = new Label(displayArea, SWT.LEFT);
        viewLabel.setText(Messages.BranchWidget_View);
        viewText = new Text(displayArea, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        viewText.setLayoutData(multiTextData);

        setEditable(editable);
    }

    /**
     * Set controls as non-editable or editable
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {
        viewText.setEditable(editable);
        branchText.setEditable(editable);
        accessText.setEditable(editable);
        updateText.setEditable(editable);
        ownerText.setEditable(editable);
        descriptionText.setEditable(editable);
        lockedButton.setEnabled(editable);
    }

    /**
     * Set branch name control as editable or non-editable
     * 
     * @param editable
     */
    public void setBranchNameEditable(boolean editable) {
        branchText.setEditable(editable);
    }

    /**
     * Set date controls as editable or non-editable
     * 
     * @param editable
     */
    public void setDatesEditable(boolean editable) {
        accessText.setEditable(editable);
        updateText.setEditable(editable);
    }

    /**
     * Set date controls as visible or hidden
     * 
     * @param visible
     */
    public void setDatesVisible(boolean visible) {
        accessLabel.setVisible(visible);
        ((GridData) accessLabel.getLayoutData()).exclude = !visible;
        accessText.setVisible(visible);
        ((GridData) accessText.getLayoutData()).exclude = !visible;

        updateLabel.setVisible(visible);
        ((GridData) updateLabel.getLayoutData()).exclude = !visible;
        updateText.setVisible(visible);
        ((GridData) updateText.getLayoutData()).exclude = !visible;
    }

    /**
     * Update the widget with the information from the specified branch. If the
     * specified branch is null all fields in the widget will be cleared
     * 
     * @param branch
     */
    public void update(IP4Branch branch) {
        clear();
        if (branch != null) {
            String name = branch.getName();
            if (name != null) {
                branchText.setText(name);
            }
            accessText
                    .setText(P4UIUtils.formatLabelDate(branch.getAccessTime()));
            updateText
                    .setText(P4UIUtils.formatLabelDate(branch.getUpdateTime()));
            String owner = branch.getOwner();
            if (owner != null) {
                ownerText.setText(owner);
            }
            String description = branch.getDescription();
            if (description != null) {
                descriptionText.setText(description);
            }

            ViewMap<IBranchMapping> view = branch.getView();
            if (view != null) {
                Collections.sort(view.getEntryList(),
                        new Comparator<IBranchMapping>() {

                            public int compare(IBranchMapping b1,
                                    IBranchMapping b2) {
                                return b1.getOrder() - b2.getOrder();
                            }
                        });
                StringBuilder builder = new StringBuilder();
                for (IBranchMapping mapping : view) {
                    builder.append(mapping.toString(" ", true)); //$NON-NLS-1$
                    builder.append('\n');
                }
                viewText.setText(builder.toString());
            }

            lockedButton.setSelection(branch.isLocked());
        }
    }

    /**
     * Get content of branch name text field
     * 
     * @return - name
     */
    public String getBranchName() {
        return this.branchText.getText();
    }

    /**
     * Set branch name
     * 
     * @param name
     */
    public void setBranchName(String name) {
        if (name != null) {
            this.branchText.setText(name);
        } else {
            this.branchText.setText(""); //$NON-NLS-1$
        }
    }

    /**
     * Get content of access text field
     * 
     * @return - access
     */
    public String getAccess() {
        return this.accessText.getText();
    }

    /**
     * Get content of update text field
     * 
     * @return - update
     */
    public String getUpdate() {
        return this.updateText.getText();
    }

    /**
     * Get content of view text field
     * 
     * @return - view
     */
    public String getView() {
        return this.viewText.getText();
    }

    /**
     * Get value of locked check box
     * 
     * @return - locked
     */
    public boolean isLocked() {
        return this.lockedButton.getSelection();
    }

    /**
     * Get content of description text field
     * 
     * @return - description
     */
    public String getDescription() {
        return this.descriptionText.getText();
    }

    /**
     * Get content of owner text field
     * 
     * @return - owner
     */
    public String getOwner() {
        return this.ownerText.getText();
    }

    /**
     * Clear all widget fields
     */
    public void clear() {
        String empty = ""; //$NON-NLS-1$
        viewText.setText(empty);
        branchText.setText(empty);
        accessText.setText(empty);
        updateText.setText(empty);
        ownerText.setText(empty);
        descriptionText.setText(empty);
        lockedButton.setSelection(false);
    }

    /**
     * Get main branch widget control
     * 
     * @return - display area
     */
    public Composite getControl() {
        return this.displayArea;
    }

}
