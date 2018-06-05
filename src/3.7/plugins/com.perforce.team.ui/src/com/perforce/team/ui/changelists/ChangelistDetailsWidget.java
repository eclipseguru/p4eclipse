/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.ui.P4UIUtils;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistDetailsWidget {

    private IP4Changelist list;

    private ScrolledComposite outer;
    private Composite displayArea;
    private Text changelistText;
    private Text workspaceText;
    private Text dateText;
    private Text userText;
    private SourceViewer descriptionViewer;

    /**
     * Create widget
     * 
     */
    public ChangelistDetailsWidget() {
    }

    /**
     * Set changelist to display. This method must be called from the UI-thread.
     * 
     * @param list
     */
    public void setChangelist(IP4Changelist list) {
        this.list = list;
        refresh();
    }

    /**
     * Refresh fields in widget. This method must be called from the UI-thread.
     * 
     */
    public void refresh() {
        if (this.list != null) {
            this.changelistText.setText(Integer.toString(this.list.getId()));
            String client = this.list.getClientName();
            if (client == null) {
                client = ""; //$NON-NLS-1$
            }
            this.workspaceText.setText(client);
            String user = this.list.getUserName();
            if (user == null) {
                user = ""; //$NON-NLS-1$
            }
            this.userText.setText(user);
            this.dateText
                    .setText(P4UIUtils.formatLabelDate(this.list.getDate()));
            String description = this.list.getDescription();
            if (description == null) {
                description = ""; //$NON-NLS-1$
            }
            this.descriptionViewer.getTextWidget().setText(description);
            this.displayArea.layout(true, true);
            this.outer.setMinSize(displayArea.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT));
        } else {
            clear();
        }
    }

    /**
     * Clear fields in widget. This method must be called from the UI-thread.
     */
    public void clear() {
        this.changelistText.setText(P4UIUtils.EMPTY);
        this.descriptionViewer.getTextWidget().setText(P4UIUtils.EMPTY);
        this.userText.setText(P4UIUtils.EMPTY);
        this.dateText.setText(P4UIUtils.EMPTY);
        this.workspaceText.setText(P4UIUtils.EMPTY);
    }

    /**
     * Get date label text
     * 
     * @return - date label text
     */
    protected String getDateLabelText() {
        return Messages.ChangelistDetailsWidget_Date;
    }

    /**
     * Get user label text
     * 
     * @return - user label text;
     */
    protected String getUserLabelText() {
        return Messages.ChangelistDetailsWidget_User;
    }

    /**
     * Create changelist details widget
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        this.outer = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        this.outer.setExpandVertical(true);
        this.outer.setExpandHorizontal(true);

        this.displayArea = new Composite(this.outer, SWT.NONE);
        this.outer.setContent(displayArea);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        GridLayout daLayout = new GridLayout(4, false);
        displayArea.setLayout(daLayout);

        GridData textData = new GridData(SWT.FILL, SWT.FILL, true, false);

        Label changelistLabel = new Label(displayArea, SWT.NONE);
        changelistLabel.setText(Messages.ChangelistDetailsWidget_Changelist);

        this.changelistText = new Text(displayArea, SWT.SINGLE | SWT.READ_ONLY);
        this.changelistText.setLayoutData(textData);
        this.changelistText.setFont(changelistLabel.getFont());

        Label workspaceLabel = new Label(displayArea, SWT.NONE);
        workspaceLabel.setText(Messages.ChangelistDetailsWidget_Workspace);

        this.workspaceText = new Text(displayArea, SWT.SINGLE | SWT.READ_ONLY);
        this.workspaceText.setLayoutData(textData);
        this.workspaceText.setFont(workspaceLabel.getFont());

        Label dateLabel = new Label(displayArea, SWT.NONE);
        dateLabel.setText(getDateLabelText());

        this.dateText = new Text(displayArea, SWT.SINGLE | SWT.READ_ONLY);
        this.dateText.setLayoutData(textData);
        this.dateText.setFont(dateLabel.getFont());

        Label userLabel = new Label(displayArea, SWT.NONE);
        userLabel.setText(getUserLabelText());

        this.userText = new Text(displayArea, SWT.SINGLE | SWT.READ_ONLY);
        this.userText.setLayoutData(textData);
        this.userText.setFont(userLabel.getFont());

        Label descriptionLabel = new Label(displayArea, SWT.NONE);
        descriptionLabel.setText(Messages.ChangelistDetailsWidget_Description);

        this.descriptionViewer = new SourceViewer(displayArea, null,
                SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
                        | SWT.WRAP);
        descriptionViewer.setDocument(new Document());

        IAdaptable adaptable = new IAdaptable() {

            public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                IP4Changelist adaptableList = list;
                return adaptableList != null ? Platform.getAdapterManager()
                        .getAdapter(adaptableList, adapter) : null;
            }
        };

        descriptionViewer.configure(P4UIUtils
                .createSourceViewerConfiguration(adaptable));
        GridData dtData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dtData.horizontalSpan = 3;
        this.descriptionViewer.getTextWidget().setLayoutData(dtData);

        this.outer
                .setMinSize(displayArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    /**
     * Get changelist text
     * 
     * @return - changelist
     */
    public String getChangelistText() {
        return this.changelistText.getText();
    }

    /**
     * Get date text
     * 
     * @return - date
     */
    public String getDateText() {
        return this.dateText.getText();
    }

    /**
     * Get user text
     * 
     * @return - user
     */
    public String getUserText() {
        return this.userText.getText();
    }

    /**
     * Get workspace text
     * 
     * @return - workspace
     */
    public String getWorkspaceText() {
        return this.workspaceText.getText();
    }

    /**
     * Get description text
     * 
     * @return - description
     */
    public String getDescriptionText() {
        return this.descriptionViewer.getTextWidget().getText();
    }

    /**
     * Get main control
     * 
     * @return - composite
     */
    public Composite getControl() {
        return this.outer;
    }
}
