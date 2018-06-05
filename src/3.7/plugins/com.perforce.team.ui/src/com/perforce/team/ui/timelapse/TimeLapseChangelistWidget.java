/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;

import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.p4java.core.file.FileAction;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class TimeLapseChangelistWidget {

    private ScrolledComposite scrolls;
    private Text revisionText;
    private Text submittedText;
    private Text userText;
    private Text workspaceText;
    private Text actionText;
    private Link changelistText;
    private Text typeText;
    private SourceViewer descriptionText;

    private IP4Revision currentRevision = null;

    /**
     * Create a changelist widget
     * 
     * @param parent
     */
    public TimeLapseChangelistWidget(Composite parent) {
        scrolls = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        scrolls.setExpandHorizontal(true);
        scrolls.setExpandVertical(true);
        scrolls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        Composite displayArea = new Composite(scrolls, SWT.NONE);
        scrolls.setContent(displayArea);
        GridLayout daLayout = new GridLayout(4, false);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        GridData threeColData = new GridData(SWT.FILL, SWT.FILL, true, false);
        threeColData.horizontalSpan = 3;

        GridData oneColData = new GridData(SWT.FILL, SWT.FILL, true, false);

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Revision);
        revisionText = createText(displayArea, threeColData);

        createLabel(displayArea,
                Messages.TimeLapseChangelistWidget_DateSubmitted);
        submittedText = createText(displayArea, oneColData);

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Changelist);
        changelistText = new Link(displayArea, SWT.NONE);
        changelistText.setLayoutData(oneColData);
        changelistText.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentRevision != null) {
                    ViewChangelistAction view = new ViewChangelistAction();
                    view.view(currentRevision.getChangelist(),
                            currentRevision.getConnection(), Type.SUBMITTED);
                }
            }

        });

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Submitted);
        userText = createText(displayArea, oneColData);

        createLabel(displayArea,
                Messages.TimeLapseChangelistWidget_PerforceFiletype);
        typeText = createText(displayArea, oneColData);

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Workspace);
        workspaceText = createText(displayArea, oneColData);

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Action);
        actionText = createText(displayArea, oneColData);

        createLabel(displayArea, Messages.TimeLapseChangelistWidget_Description);

        descriptionText = new SourceViewer(displayArea, null, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY);
        TextSourceViewerConfiguration config = P4UIUtils
                .createSourceViewerConfiguration(new IAdaptable() {

                    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                        IP4Revision revision = currentRevision;
                        if (revision != null) {
                            if (IP4Connection.class == adapter
                                    || IP4Resource.class == adapter) {
                                return revision.getConnection();
                            } else {
                                return Platform.getAdapterManager().getAdapter(
                                        revision, adapter);
                            }
                        }
                        return null;
                    }
                });
        descriptionText.configure(config);
        descriptionText.setDocument(new Document());

        GridData dtData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dtData.horizontalSpan = 3;
        dtData.heightHint = P4UIUtils.computePixelHeight(descriptionText
                .getTextWidget().getFont(), 4);
        descriptionText.getTextWidget().setLayoutData(dtData);

        scrolls.setMinSize(displayArea.computeSize(SWT.DEFAULT, SWT.DEFAULT));
    }

    private Label createLabel(Composite parent, String text) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(text);
        return label;
    }

    private Text createText(Composite parent, int flags, GridData data) {
        Text text = new Text(parent, flags);
        text.setLayoutData(data);
        return text;
    }

    private Text createText(Composite parent, GridData data) {
        return createText(parent, SWT.SINGLE | SWT.READ_ONLY, data);
    }

    /**
     * Show a revision
     * 
     * @param revision
     */
    public void showRevision(IP4Revision revision) {
        this.currentRevision = revision;
        if (revision != null) {
            revisionText.setText(revision.getContentIdentifier());
            submittedText
                    .setText(P4UIUtils.formatDate(revision.getTimestamp()));
            changelistText.setText(MessageFormat.format(
                    "<a href=\"open\">{0,number,#}</a>", //$NON-NLS-1$
                    revision.getChangelist()));
            userText.setText(revision.getAuthor());
            typeText.setText(revision.getType());
            workspaceText.setText(revision.getClient());
            FileAction action = revision.getAction();
            if (action != null) {
                actionText.setText(action.toString().toLowerCase());
            } else {
                actionText.setText(""); //$NON-NLS-1$
            }

            String comment = revision.getComment();
            if (comment == null) {
                comment = ""; //$NON-NLS-1$
            }
            descriptionText.getTextWidget().setText(comment);
        } else {
            String empty = ""; //$NON-NLS-1$
            revisionText.setText(empty);
            submittedText.setText(empty);
            changelistText.setText(empty);
            userText.setText(empty);
            typeText.setText(empty);
            workspaceText.setText(empty);
            actionText.setText(empty);
            descriptionText.getTextWidget().setText(empty);
        }
    }

    /**
     * Get main control
     * 
     * @return - control
     */
    public Control getControl() {
        return this.scrolls;
    }
}
