package com.perforce.team.ui.shelve;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.perforce.team.core.p4java.IP4ShelvedChangelist;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.changelists.DescriptionViewer;
import com.perforce.team.ui.dialogs.FileListViewer;
import com.perforce.team.ui.dialogs.P4StatusDialog;

/**
 *  This dialog is for submitting the shelve change list.
 *  <p/>
 *  No configuration is allowed when submitting shelved changelist.
 * 
 */
public class SubmitShelveDialog extends P4StatusDialog {

	private Label countLabel;
	private FileListViewer viewer;
	private DescriptionViewer descriptionViewer;
	private IP4ShelvedChangelist changelist;

	public SubmitShelveDialog(Shell parent, String title, IP4ShelvedChangelist changelist) {
		super(parent, title);
		this.changelist=changelist;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(dialogArea, SWT.NONE);
        GridLayout cLayout = new GridLayout(2, false);
        cLayout.marginHeight = 0;
        cLayout.marginWidth = 0;
        composite.setLayout(cLayout);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        countLabel = new Label(composite, SWT.LEFT);
        GridData clData = new GridData(SWT.FILL, SWT.FILL, true, false);
        clData.verticalIndent = 5;
        clData.horizontalSpan = 2;
        countLabel.setLayoutData(clData);

        viewer = new FileListViewer(composite, this.changelist.members(), this.changelist.members(), false);
        ((GridData) viewer.getTable().getLayoutData()).horizontalSpan = 2;

        Label descriptionLabel = new Label(composite, SWT.NONE);
        descriptionLabel.setText(Messages.ConfirmShelveDialog_Description);

        descriptionViewer = new DescriptionViewer(changelist.getConnection());
        descriptionViewer.createControl(composite, changelist.getDescription());

        StyledText styledText = descriptionViewer.getViewer().getTextWidget();
        ((GridData) styledText.getLayoutData()).heightHint = P4UIUtils
                .computePixelHeight(styledText.getFont(), 5);
        styledText.setSelection(0, 0);
        descriptionViewer.setFocus();

        // disable all the controls and won't allow user to change anything.
        viewer.getTable().setEnabled(false);
        styledText.setEnabled(false);

        updateCount();
        
        return dialogArea;
	}
	
    private void updateCount() {
        int count = viewer.getCheckedElements().length;
        int max = viewer.getTable().getItemCount();
        countLabel.setText(MessageFormat.format(
                Messages.ConfirmShelveDialog_FilesSelected, count, max));
    }

}
