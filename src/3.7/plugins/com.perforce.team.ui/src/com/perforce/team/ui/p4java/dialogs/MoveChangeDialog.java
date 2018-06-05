package com.perforce.team.ui.p4java.dialogs;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.DescriptionViewer;
import com.perforce.team.ui.changelists.Messages;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.PlatformUI;

/*
 * Copyright (c) 2003, 2004 Perforce Software.  All rights reserved.
 *
 */

/**
 * Dialog box used for moving files to another changelist.
 */
public class MoveChangeDialog extends P4StatusDialog {

    /**
     * MOVE_DEFAULT_DESCRIPTION
     */
    public static final String MOVE_DEFAULT_DESCRIPTION = Messages.MoveChangeDialog_MoveDefaultDescription;

    // List of changes
    private IP4PendingChangelist[] changes;

    // List of changes;
    private TableViewer changelistViewer;
    private DescriptionViewer viewer;

    // Stored selected change;
    private int selectedChange;
    private String description = MOVE_DEFAULT_DESCRIPTION;

    private Object newChangelist = new Object();

    /**
     * Constructor.
     * 
     * @param parent
     *            parent window
     * @param changes
     *            - p4 changelists
     */
    public MoveChangeDialog(Shell parent, IP4PendingChangelist[] changes) {
        super(parent, Messages.MoveChangeDialog_MoveFileToChangelist);
        this.changes = changes;
        setModalResizeStyle();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        final Composite dialogArea = (Composite) super.createDialogArea(parent);

        Composite displayArea = new Composite(dialogArea, SWT.NONE);
        GridLayout daLayout = new GridLayout(2, false);
        displayArea.setLayout(daLayout);
        GridData daData = new GridData(SWT.FILL, SWT.FILL, true, true);
        displayArea.setLayoutData(daData);

        Label headerLabel = new Label(displayArea, SWT.NONE);
        headerLabel.setText(Messages.MoveChangeDialog_MoveToPendingChangelist);
        GridData hlData = new GridData(SWT.FILL, SWT.FILL, true, false);
        hlData.horizontalSpan = 2;
        headerLabel.setLayoutData(hlData);

        changelistViewer = new TableViewer(displayArea, SWT.SINGLE | SWT.BORDER
                | SWT.V_SCROLL | SWT.H_SCROLL);

        final Image newImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_NEW_CHANGELIST).createImage();
        P4UIUtils.registerDisposal(changelistViewer.getTable(), newImage);
        GridData cvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        cvData.horizontalSpan = 2;
        cvData.widthHint = 400;
        cvData.heightHint = P4UIUtils.computePixelHeight(changelistViewer
                .getTable().getFont(), 10);
        changelistViewer.getTable().setLayoutData(cvData);
        changelistViewer.setLabelProvider(new PerforceLabelProvider(false) {

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (element == newChangelist) {
                    return newImage;
                }
                return super.getColumnImage(element, columnIndex);
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element == newChangelist) {
                    return Messages.MoveChangeDialog_NewPendingChangelist;
                }
                return super.getColumnText(element, columnIndex);
            }

        });
        changelistViewer.setContentProvider(new ArrayContentProvider());

        // Sort changelists in ascending order
        P4CoreUtils.sort(changes);

        Object[] changesWithNew = new Object[changes.length + 1];
        changesWithNew[0] = newChangelist;
        System.arraycopy(changes, 0, changesWithNew, 1, changes.length);

        changelistViewer.setInput(changesWithNew);

        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(changelistViewer.getTable(),
                        IHelpContextIds.MOVE_CHANGE_CHANGELISTS);

        // Treat a double-click the same as OK being pressed
        changelistViewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                okPressed();
            }
        });

        changelistViewer.getTable().select(1);
        changelistViewer.getTable().setFocus();

        IP4Connection connection = null;
        if (changes.length > 0) {
            connection = changes[0].getConnection();
        }

        final Label descriptionLabel = new Label(displayArea, SWT.NONE);
        final GridData descriptionData = new GridData(SWT.FILL, SWT.CENTER,
                false, false);
        descriptionData.exclude = true;
        descriptionLabel.setText(Messages.MoveChangeDialog_Description);
        descriptionLabel.setLayoutData(descriptionData);
        descriptionLabel.setVisible(false);

        viewer = new DescriptionViewer(connection);
        viewer.createControl(displayArea, description);
        viewer.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                description = viewer.getDocument().get();
                validate();
            }

            public void documentAboutToBeChanged(DocumentEvent event) {

            }
        });

        final StyledText styledText = viewer.getViewer().getTextWidget();
        final GridData commentData = (GridData) styledText.getLayoutData();
        commentData.heightHint = P4UIUtils.computePixelHeight(
                styledText.getFont(), 5);
        commentData.exclude = true;
        styledText.setLayoutData(commentData);
        styledText.setVisible(false);

        changelistViewer
                .addSelectionChangedListener(new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        int index = changelistViewer.getTable()
                                .getSelectionIndex();
                        if (index > 0) {
                            commentData.exclude = true;
                            descriptionData.exclude = true;
                            styledText.setVisible(false);
                            descriptionLabel.setVisible(false);
                        } else if (index == 0) {
                            commentData.exclude = false;
                            descriptionData.exclude = false;
                            styledText.setVisible(true);
                            descriptionLabel.setVisible(true);
                        }
                        dialogArea.layout(true, true);
                        if (styledText.isVisible()) {
                            styledText.setFocus();
                        }
                        validate();
                    }

                });

        daData.heightHint = cvData.heightHint + commentData.heightHint;
        return dialogArea;
    }

    /**
     * Get the UI control containing the pending changelists
     * 
     * @return - table control
     */
    public Table getChangesList() {
        return this.changelistViewer != null
                ? this.changelistViewer.getTable()
                : null;
    }

    /**
     * Return selected changelist
     * 
     * @return selected change
     */
    public int getSelectedChange() {
        return this.selectedChange;
    }

    /**
     * Get new pending changelist description
     * 
     * @return - description
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * OK button pressed
     */
    @Override
    protected void okPressed() {
        int index = changelistViewer.getTable().getSelectionIndex();
        if (index == 0) {
            this.selectedChange = IP4PendingChangelist.NEW;
        } else {
            this.selectedChange = changes[index - 1].getId();
        }
        super.okPressed();
    }

    /**
     * @see com.perforce.team.ui.BaseErrorProvider#validate()
     */
    public void validate() {
        String errorMessage = null;
        if (this.changelistViewer.getTable().getSelectionIndex() == 0
                && description.length() == 0) {
            errorMessage = Messages.PendingCombo_EnterChangelistDescription;
        }
        setErrorMessage(errorMessage);
    }
}
