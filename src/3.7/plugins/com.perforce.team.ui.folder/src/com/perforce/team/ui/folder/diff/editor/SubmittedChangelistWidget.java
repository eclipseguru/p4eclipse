/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.p4java.core.IChangelist.Type;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.p4java.actions.ViewChangelistAction;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SubmittedChangelistWidget {

    /**
     * REMOTE_USER
     */
    public static final String REMOTE_USER = "remote"; //$NON-NLS-1$

    private IP4Revision revision = null;

    private Composite displayArea;
    private Hyperlink changelistLink;
    private StyledText detailsText;
    private SourceViewer descriptionText;

    /**
     * Create submitted changelist widget
     * 
     * @param parent
     * @param toolkit
     */
    public SubmittedChangelistWidget(Composite parent, FormToolkit toolkit) {
        displayArea = toolkit.createComposite(parent);
        toolkit.adapt(displayArea, false, false);
        displayArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
                .create());
        displayArea.setLayoutData(GridDataFactory.fillDefaults()
                .grab(true, true).create());

        changelistLink = toolkit.createHyperlink(displayArea, "", SWT.NONE); //$NON-NLS-1$
        changelistLink.addHyperlinkListener(new HyperlinkAdapter() {

            @Override
            public void linkActivated(HyperlinkEvent e) {
                if (revision != null) {
                    ViewChangelistAction view = new ViewChangelistAction();
                    view.view(revision.getChangelist(),
                            revision.getConnection(), Type.SUBMITTED);
                }
            }
        });

        detailsText = new StyledText(displayArea, SWT.NONE);
        detailsText.setEditable(false);
        final Caret caret = new Caret(detailsText, SWT.NONE);
        caret.setSize(0, 0);
        detailsText.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                caret.dispose();
            }
        });
        detailsText.setCaret(caret);

        detailsText.setLayoutData(GridDataFactory.fillDefaults()
                .align(SWT.FILL, SWT.CENTER).grab(true, false).create());

        descriptionText = new SourceViewer(displayArea, null, SWT.BORDER
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.READ_ONLY);
        descriptionText.setDocument(new Document());
        descriptionText.getTextWidget().setFont(
                JFaceResources.getFont(JFaceResources.TEXT_FONT));
        descriptionText.getTextWidget().setLayoutData(
                GridDataFactory.fillDefaults().grab(true, true)
                        .hint(SWT.DEFAULT, P4UIUtils.VIEWER_HEIGHT_HINT)
                        .span(2, 1).create());

        IAdaptable adaptable = new IAdaptable() {

            public Object getAdapter(Class adapter) {
                IP4Revision adaptableRevision = revision;
                return adaptableRevision != null ? Platform.getAdapterManager()
                        .getAdapter(adaptableRevision, adapter) : null;
            }
        };
        descriptionText.configure(P4UIUtils
                .createSourceViewerConfiguration(adaptable));

    }

    /**
     * Set revision to display
     * 
     * @param revision
     */
    public void setRevision(IP4Revision revision) {
        this.revision = revision;
        if (revision != null) {
            changelistLink.setText(MessageFormat.format(
                    Messages.SubmittedChangelistWidget_Changelist,
                    Integer.toString(revision.getChangelist())));
            changelistLink
                    .setEnabled(!REMOTE_USER.equals(revision.getAuthor()));
            String details = MessageFormat.format(
                    Messages.SubmittedChangelistWidget_Author,
                    revision.getAuthor(), revision.getClient(),
                    P4UIUtils.formatLabelDate(revision.getTimestamp()));
            detailsText.setText(details);
            descriptionText.getDocument().set(revision.getComment());
        } else {
            changelistLink.setText(""); //$NON-NLS-1$
            detailsText.setText(""); //$NON-NLS-1$
            descriptionText.getDocument().set(""); //$NON-NLS-1$
        }
        displayArea.layout(true, true);
    }

}
