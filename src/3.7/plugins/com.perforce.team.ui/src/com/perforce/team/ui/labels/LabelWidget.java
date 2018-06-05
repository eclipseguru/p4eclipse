/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import com.perforce.p4java.core.ILabelMapping;
import com.perforce.p4java.core.ViewMap;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.ui.P4UIUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelWidget {

    private Composite displayArea;
    private Text labelText;
    private Text accessText;
    private Text updateText;
    private Text ownerText;
    private Text descriptionText;
    private Text revisionText;
    private Button lockedButton;
    private Text viewText;

    /**
     * Create label widget with defaults
     * 
     * @param parent
     */
    public LabelWidget(Composite parent) {
        this(parent, 5, 5, true);
    }

    /**
     * Create label widget
     * 
     * @param parent
     * @param marginWidth
     * @param marginHeight
     * @param editable
     */
    public LabelWidget(Composite parent, int marginWidth, int marginHeight,
            boolean editable) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(4, false);
        daLayout.marginHeight = marginHeight;
        daLayout.marginWidth = marginWidth;
        displayArea.setLayout(daLayout);

        GridData singleTextData = new GridData(SWT.FILL, SWT.FILL, true, false);
        GridData multiTextData = new GridData(SWT.FILL, SWT.FILL, true, true);
        multiTextData.horizontalSpan = 3;
        multiTextData.heightHint = 75;

        Label nameLabel = new Label(displayArea, SWT.LEFT);
        nameLabel.setText(Messages.LabelWidget_Name);
        labelText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        labelText.setLayoutData(singleTextData);

        Label accessLabel = new Label(displayArea, SWT.LEFT);
        accessLabel.setText(Messages.LabelWidget_AccessTime);
        accessText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        accessText.setLayoutData(singleTextData);

        Label updateLabel = new Label(displayArea, SWT.LEFT);
        updateLabel.setText(Messages.LabelWidget_UpdateTime);
        updateText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        updateText.setLayoutData(singleTextData);

        Label ownerLabel = new Label(displayArea, SWT.LEFT);
        ownerLabel.setText(Messages.LabelWidget_Owner);
        ownerText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        ownerText.setLayoutData(singleTextData);

        Label descriptionLabel = new Label(displayArea, SWT.LEFT);
        descriptionLabel.setText(Messages.LabelWidget_Description);
        descriptionText = new Text(displayArea, SWT.MULTI | SWT.BORDER
                | SWT.WRAP);
        descriptionText.setLayoutData(multiTextData);

        Label optionsLabel = new Label(displayArea, SWT.LEFT);
        optionsLabel.setText(Messages.LabelWidget_Options);
        lockedButton = new Button(displayArea, SWT.CHECK);
        lockedButton.setText(Messages.LabelWidget_Locked);
        GridData lbData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        lbData.horizontalSpan = 3;
        lockedButton.setLayoutData(lbData);

        Label revisionLabel = new Label(displayArea, SWT.LEFT);
        revisionLabel.setText(Messages.LabelWidget_Revision);
        revisionText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
        GridData rtData = new GridData(SWT.FILL, SWT.FILL, true, false);
        rtData.horizontalSpan = 3;
        revisionText.setLayoutData(rtData);

        Label viewLabel = new Label(displayArea, SWT.LEFT);
        viewLabel.setText(Messages.LabelWidget_View);
        viewText = new Text(displayArea, SWT.MULTI | SWT.BORDER | SWT.WRAP);
        viewText.setLayoutData(multiTextData);

        setEditable(editable);
    }

    /**
     * Set controls as non-editable
     * 
     * @param editable
     */
    public void setEditable(boolean editable) {
        viewText.setEditable(editable);
        labelText.setEditable(editable);
        accessText.setEditable(editable);
        updateText.setEditable(editable);
        ownerText.setEditable(editable);
        descriptionText.setEditable(editable);
        revisionText.setEditable(editable);
        lockedButton.setEnabled(editable);
    }

    /**
     * Update the widget with the information from the specified label. If the
     * specified label is null all fields in the widget will be cleared
     * 
     * @param label
     */
    public void update(IP4Label label) {
        clear();
        if (label != null) {
            String name = label.getName();
            if (name != null) {
                labelText.setText(name);
            }
            accessText
                    .setText(P4UIUtils.formatLabelDate(label.getAccessTime()));
            updateText
                    .setText(P4UIUtils.formatLabelDate(label.getUpdateTime()));
            String owner = label.getOwner();
            if (owner != null) {
                ownerText.setText(owner);
            }
            String description = label.getDescription();
            if (description != null) {
                descriptionText.setText(description);
            }
            String revision = label.getRevision();
            if (revision != null) {
                revisionText.setText(revision);
            }

            ViewMap<ILabelMapping> view = label.getView();
            if (view != null) {
                StringBuilder builder = new StringBuilder();
                for (ILabelMapping mapping : view) {
                    builder.append(mapping.getViewMapping());
                    builder.append('\n');
                }
                viewText.setText(builder.toString());
            }

            lockedButton.setSelection(label.isLocked());
        }
    }

    /**
     * Get content of label name text field
     * 
     * @return - name
     */
    public String getLabelName() {
        return this.labelText.getText();
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
     * Get content of revision text field
     * 
     * @return - revision
     */
    public String getRevision() {
        return this.revisionText.getText();
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
        labelText.setText(empty);
        accessText.setText(empty);
        updateText.setText(empty);
        ownerText.setText(empty);
        descriptionText.setText(empty);
        revisionText.setText(empty);
        lockedButton.setSelection(false);
    }

    /**
     * Get main label widget control
     * 
     * @return - display area
     */
    public Composite getControl() {
        return this.displayArea;
    }

}
