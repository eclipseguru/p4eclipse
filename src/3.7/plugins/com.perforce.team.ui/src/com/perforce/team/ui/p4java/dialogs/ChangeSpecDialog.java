/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.p4java.dialogs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;

import com.perforce.p4api.PerforceConstants;
import com.perforce.p4java.core.IChangelist;
import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.Policy;
import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.DescriptionTemplate;
import com.perforce.team.ui.changelists.DescriptionViewer;
import com.perforce.team.ui.dialogs.IHelpContextIds;
import com.perforce.team.ui.dialogs.JobFixDialog;
import com.perforce.team.ui.dialogs.JobListViewer;
import com.perforce.team.ui.dialogs.PerforceDialog;
import com.perforce.team.ui.pending.PendingChangelistFileWidget;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ChangeSpecDialog extends PerforceDialog {

    /**
     * DIALOG_SECTION
     */
    public static final String DIALOG_SECTION = "CHANGE_SPEC_DIALOG"; //$NON-NLS-1$

    /**
     * PENDING_CONTEXT
     */
    public static final String PENDING_CONTEXT = "pending.edit"; //$NON-NLS-1$

    private Set<IP4Job> changeJobs = new HashSet<IP4Job>();
    private Set<IP4Job> removedJobs = new HashSet<IP4Job>();

    private IP4File[] startingCheckedFiles = null;
    private IP4File[] selected = null;
    private IP4File[] unselected = null;
    private int id;
    private IP4PendingChangelist changelist;
    private IP4Connection connection = null;
    private String initialDescription = null;
    private IP4File[] changeFiles = null;
    private List<DescriptionTemplate> descriptions = null;

    // Holds change description
    private DescriptionViewer descEditor;
    private Combo templateCombo;
    private String description;

    // True if submit dialog, false if New Changelist or Edit Spec
    private boolean submit;

    private IP4File[] checkedFiles;

    // OK or Submit button
    private Button okButton;

    private PendingChangelistFileWidget filesViewer;

    // Reopen Files check box;
    private Button reopenCheck;

    // Job viewer
    private JobListViewer jobsViewer;

    // Add Jobs button;
    private Button addJobsButton;
    private Button removeJobsButton;

    private Label jobStatusLabel;
    private Combo jobStatusCombo;

    // True if submitted files should be reopened
    private boolean reopenFiles;

    // Non-null if the user selected a status from the combo
    private String status;

    /**
     * @param changelist
     *            - non-null pending changelist
     * @param checked
     * @param parent
     * @param submit
     * @param description
     */
    public ChangeSpecDialog(IP4PendingChangelist changelist,
            IP4Resource[] checked, Shell parent, boolean submit,
            String description) {
        super(parent, Messages.ChangeSpecDialog_PerforceChangeSpecification);
        this.submit = submit;
        this.changelist = changelist;
        this.connection = changelist.getConnection();
        if (description == null) {
            description = this.changelist.getDescription();
        }
        this.initialDescription = description;
        this.id = changelist.getId();
        this.changeFiles = changelist.getPendingFiles();
        if (checked == null) {
            checked = changelist.members();
        }
        setupChecked(checked, changelist.getJobs());
        setModalResizeStyle();
    }

    /**
     * @param changelist
     *            - non-null pending changelist
     * @param checked
     * @param parent
     * @param submit
     */
    public ChangeSpecDialog(IP4PendingChangelist changelist,
            IP4Resource[] checked, Shell parent, boolean submit) {
        this(changelist, checked, parent, submit, null);
    }

    /**
     * 
     * @param connection
     * @param files
     * @param jobs
     * @param parent
     * @param description
     */
    public ChangeSpecDialog(IP4Connection connection, IP4File[] files,
            IP4Job[] jobs, Shell parent, String description) {
        super(parent, Messages.ChangeSpecDialog_PerforceChangeSpecification);
        this.connection = connection;
        this.id = IP4PendingChangelist.NEW;
        this.initialDescription = description;
        this.submit = false;
        this.changeFiles = files;
        setupChecked(files, jobs);
        setModalResizeStyle();
    }

    /**
     * @see com.perforce.team.ui.dialogs.PerforceDialog#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return DIALOG_SECTION;
    }

    private void setupChecked(IP4Resource[] checked, IP4Job[] jobs) {
        List<IP4File> checkedFiles = new ArrayList<IP4File>();
        if (checked != null) {
            for (IP4Resource resource : checked) {
                if (resource instanceof IP4File) {
                    checkedFiles.add((IP4File) resource);
                }
            }
        }
        if (jobs != null) {
            for (IP4Job job : jobs) {
                changeJobs.add(job);
            }
        }
        startingCheckedFiles = checkedFiles.toArray(new IP4File[checkedFiles
                .size()]);
        selected = startingCheckedFiles;
        if (this.changeFiles == null) {
            this.changeFiles = new IP4File[0];
        }
    }

    /**
     * Get changelist description entered by user.
     * 
     * @return changelist description
     */
    public String getDescription() {
        StringBuilder buff = new StringBuilder(description);
        // Remove carriage returns
        int count = 0;
        for (int start = 0;;) {
            int idx = description.indexOf(0x0D, start);
            if (idx == -1) {
                break;
            }
            buff.deleteCharAt(idx - count);
            count++;
            start = idx + 1;
        }
        return buff.toString();
    }

    /**
     * Sets the description in the dialog
     * 
     * @param description
     *            - description field value
     */
    public void setDescription(String description) {
        if (descEditor != null) {
            descEditor.setText(description);
        }
    }

    /**
     * Get files selected by user.
     * 
     * @return selected changelist files
     */
    public IP4File[] getCheckedFiles() {
        return this.selected;
    }

    /**
     * Get the unchecked files
     * 
     * @return - p4 files
     */
    public IP4File[] getUncheckedFiles() {
        return this.unselected;
    }

    /**
     * Get the checked jobs
     * 
     * @return - p4 jobs
     */
    public IP4Job[] getCheckedJobs() {
        return this.changeJobs.toArray(new IP4Job[0]);
    }

    /**
     * Get the unchecked jobs
     * 
     * @return - p4 jobs
     */
    public IP4Job[] getUncheckedJobs() {
        return this.removedJobs.toArray(new IP4Job[0]);
    }

    /**
     * Is re open files selected?
     * 
     * @return - true if selected, false otherwise
     */
    public boolean reopenFiles() {
        return reopenFiles;
    }

    private void refreshFilesViewer() {
        updateCheckState();
//        filesViewer.refresh();
    }

    private boolean useSameStatus() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SAME_JOB_STATUS);
    }

    private void fillStatusCombo(Combo combo) {
        IJobSpec spec = this.connection.getJobSpec();
        if (spec != null) {
            for (IJobSpecField field : spec.getFields()) {
                if (IP4Job.JOB_STATUS_CODE == field.getCode()) {

                    // Fix for job034368, status field values may be null
                    List<String> statuses = spec
                            .getFieldValues(field.getName());
                    if (statuses != null) {
                        for (String status : statuses) {
                            combo.add(status);
                        }
                    }

                    combo.add(IP4Job.SAME_STATUS_TYPE);

                    // Set initial combo value based on status preset, fix for
                    // job034220
                    String defaultStatus = IP4Job.CLOSED_STATUS_TYPE;
                    if (!useSameStatus()) {
                        String statusPreset = spec.getFieldPreset(field
                                .getName());
                        if (statusPreset != null) {
                            int fixArea = statusPreset
                                    .indexOf(IP4Job.FIX_STATUS_PREFIX);
                            int presetStart = fixArea
                                    + IP4Job.FIX_STATUS_PREFIX.length();
                            if (fixArea != -1
                                    && presetStart < statusPreset.length()) {
                                defaultStatus = statusPreset
                                        .substring(presetStart);
                            }
                        }
                    } else {
                        defaultStatus = IP4Job.SAME_STATUS_TYPE;
                    }
                    if (combo.indexOf(defaultStatus) != -1) {
                        status = defaultStatus;
                        combo.setText(defaultStatus);
                    }

                    break;
                }
            }
        }
    }

    private void createDescriptionArea(Composite parent, IP4File[] files,
            Collection<IP4Job> jobs) {
        Composite descGroup = createTitledArea(parent, GridData.FILL_HORIZONTAL);
        GridData dgData = new GridData(SWT.FILL, SWT.FILL, true, false);
        descGroup.setLayoutData(dgData);

        Composite titleArea = new Composite(descGroup, SWT.NONE);
        GridLayout tiLayout = new GridLayout(2, false);
        tiLayout.marginHeight = 0;
        tiLayout.marginWidth = 0;
        titleArea.setLayout(tiLayout);
        titleArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Label descriptionLabel = new Label(titleArea, SWT.LEFT);
        descriptionLabel.setText(Messages.ChangeSpecDialog_Description);

        ToolBar titleBar = new ToolBar(titleArea, SWT.FLAT);
        titleBar.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        ToolItem prefItem = new ToolItem(titleBar, SWT.PUSH);
        Image prefImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_PREFERENCES)
                .createImage();
        P4UIUtils.registerDisposal(prefItem, prefImage);
        prefItem.setImage(prefImage);
        prefItem.setToolTipText(Messages.ChangeSpecDialog_OpenEditorPrefs);
        prefItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                P4UIUtils.openPreferencePage(ChangelistEditorPreferencePage.ID);
            }

        });

        String changeDesc = this.initialDescription;
        if (changeDesc == null) {
            changeDesc = PerforceConstants.DEFAULT_DESCRIPTION;
        }
        // Remove any trailing newline
        if (changeDesc.length() > 0
                && changeDesc.charAt(changeDesc.length() - 1) == 0x0A) {
            changeDesc = changeDesc.substring(0, changeDesc.length() - 1);
        }
        this.description = changeDesc;

        descEditor = new DescriptionViewer(PENDING_CONTEXT, this.changelist,
                files, jobs.toArray(new IP4Job[jobs.size()]));
        descEditor.createControl(descGroup, changeDesc);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(descEditor.getControl(),
                        IHelpContextIds.CHANGE_SPEC_DESCRIPTION);
        descEditor.getDocument().addDocumentListener(new IDocumentListener() {

            public void documentChanged(DocumentEvent event) {
                description = descEditor.getDocument().get();
                enableOk();
            }

            public void documentAboutToBeChanged(DocumentEvent event) {
                // Does nothing
            }
        });

        Composite templateArea = new Composite(descGroup, SWT.NONE);
        GridLayout taLayout = new GridLayout(2, false);
        taLayout.marginWidth = 0;
        taLayout.marginHeight = 0;
        templateArea.setLayout(taLayout);
        templateArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        templateCombo = new Combo(templateArea, SWT.SINGLE | SWT.READ_ONLY);
        templateCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        templateCombo.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent e) {
                int selected = templateCombo.getSelectionIndex();
                if (selected > 0) {
                    selected--;
                    descEditor.setText(descriptions.get(selected).getContent());
                    enableOk();
                }
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        Link configureTemplates = new Link(templateArea, SWT.NONE);
        configureTemplates.setText(Messages.ChangeSpecDialog_Configure);
        configureTemplates.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                P4UIUtils
                        .openPreferencePage(DescriptionTemplatesPreferencePage.ID);
                loadTemplates();
            }

        });

        loadTemplates();
    }

    private void loadTemplates() {
        templateCombo.removeAll();
        String previous = templateCombo.getText();
        templateCombo.add(Messages.ChangeSpecDialog_ChooseATemplate);
        templateCombo.select(0);

        this.descriptions = DescriptionTemplate.getHistory();
        for (DescriptionTemplate tpl : this.descriptions) {
            templateCombo.add(tpl.getLabel(null));
        }
        for (DescriptionTemplate tpl : DescriptionTemplate.getTemplates()) {
            this.descriptions.add(tpl);
            templateCombo.add(MessageFormat.format(
                    Messages.ChangeSpecDialog_Template, tpl.getLabel(null)));
        }
        templateCombo.setText(previous);
        this.descEditor.updateTemplates(this.descriptions);
    }

    private void createFilesArea(Composite parent, final IP4File[] files) {
        Composite filesGroup = createTitledArea(parent, GridData.FILL_BOTH);

        filesViewer = new PendingChangelistFileWidget(false, true, true);
        filesViewer.createControl(filesGroup);
		filesViewer.getViewer().setInput(new Object[]{new PerforceContentProvider.Loading()});

		getShell().getDisplay().asyncExec(new Runnable() {
					
			public void run() {
				Tracing.printExecTime(Policy.DEBUG, "SUBMIT", getClass().getSimpleName()+":"+filesViewer.getClass().getSimpleName()+".setInput", new Runnable() {
					public void run() {
						if(!filesViewer.getControl().isDisposed()){
							filesViewer.setInput(files, ChangeSpecDialog.this.startingCheckedFiles);
							updateCheckState();
						}
					}
				});
			}
		});
        checkedFiles = filesViewer.getCheckedFiles();
        filesViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                refreshFilesViewer();
            }
        });
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(filesViewer.getControl(),
                        IHelpContextIds.CHANGE_SPEC_FILES);

        Composite fileButtons = createComposite(parent, 3, SWT.NONE);
        GridData data = (GridData) fileButtons.getLayoutData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        if (submit) {
            reopenCheck = new Button(fileButtons, SWT.CHECK);
            data = new GridData(SWT.FILL, SWT.FILL, true, false);
            reopenCheck.setLayoutData(data);
            reopenCheck.setText(Messages.ChangeSpecDialog_ReopenFiles);
            PlatformUI.getWorkbench().getHelpSystem()
                    .setHelp(reopenCheck, IHelpContextIds.CHANGE_SPEC_REOPEN);
        } else {
            data = (GridData) createLabel(fileButtons, "").getLayoutData(); //$NON-NLS-1$
            data.grabExcessHorizontalSpace = true;
        }
        Button selectAll = createButton(fileButtons,
                Messages.ChangeSpecDialog_SelectAll, 0);
        selectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                filesViewer.selectAll();
                refreshFilesViewer();
            }
        });
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(selectAll, IHelpContextIds.CHANGE_SPEC_SELECT_ALL);

        Button deselectAll = createButton(fileButtons,
                Messages.ChangeSpecDialog_DeselectAll, 0);
        deselectAll.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                filesViewer.setAllChecked(false);
                refreshFilesViewer();
            }
        });
        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(deselectAll, IHelpContextIds.CHANGE_SPEC_DESELECT_ALL);
    }

    private void createJobsArea(Composite parent, Collection<IP4Job> jobs) {
        Composite jobsGroup = createTitledArea(parent, GridData.FILL_HORIZONTAL);
        createLabel(jobsGroup, Messages.ChangeSpecDialog_Jobs);
        jobsViewer = new JobListViewer(jobsGroup, this, changeJobs);
        PlatformUI
                .getWorkbench()
                .getHelpSystem()
                .setHelp(jobsViewer.getControl(),
                        IHelpContextIds.CHANGE_SPEC_JOBS);

        Composite jobButtons = new Composite(jobsGroup, SWT.NONE);
        GridLayout jbLayout = new GridLayout(4, false);
        jobButtons.setLayout(jbLayout);
        jobButtons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        // "Add Jobs..." button
        addJobsButton = new Button(jobButtons, SWT.NONE);
        addJobsButton.setText(Messages.ChangeSpecDialog_AddJobs);
        addJobsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                JobFixDialog dlg = new JobFixDialog(getShell(), connection);
                if (dlg.open() == JobFixDialog.OK) {
                    addJobs(dlg.getSelectedJobs());
                }
            }
        });
        addJobsButton
                .setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));

        removeJobsButton = new Button(jobButtons, SWT.NONE);
        removeJobsButton.setText(Messages.ChangeSpecDialog_RemoveJobs);
        removeJobsButton.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IP4Job[] jobs = jobsViewer.getSelectedJobs();
                if (jobs != null && jobs.length > 0) {
                    removeJobs(jobs);
                }
            }
        });

        jobStatusLabel = new Label(jobButtons, SWT.LEFT);
        jobStatusLabel.setText(Messages.ChangeSpecDialog_JobStatus);
        jobStatusCombo = new Combo(jobButtons, SWT.READ_ONLY | SWT.DROP_DOWN);
        fillStatusCombo(jobStatusCombo);
        jobStatusCombo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                status = jobStatusCombo.getText();
            }

        });

        // Fix for job037154, only show job status label/combo when submitting
        if (!submit) {
            jobStatusLabel.setVisible(false);
            jobStatusCombo.setVisible(false);
            GridData excludeData = new GridData();
            excludeData.exclude = true;
            jobStatusLabel.setLayoutData(excludeData);
            jobStatusCombo.setLayoutData(excludeData);
        }

        updateJobEnablement();
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite) super.createDialogArea(parent);
        Composite composite = createComposite(dialogArea, 1, GridData.FILL_BOTH);

        String idText = Messages.ChangeSpecDialog_Changelist;
        if (this.id == IChangelist.DEFAULT) {
            if (submit) {
                idText += Messages.ChangeSpecDialog_Default;
            } else {
                idText += Messages.ChangeSpecDialog_New;
            }
        } else if (this.id == IP4PendingChangelist.NEW) {
            idText += Messages.ChangeSpecDialog_New;
        } else {
            idText += this.id;
        }
        Label idLabel = createLabel(composite, idText);
        idLabel.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true,
                false));

        Tracing.printTrace(Policy.DEBUG,"SUBMIT", getClass().getSimpleName()+":createDescriptionArea");
        createDescriptionArea(composite, this.changeFiles, this.changeJobs);
        
        Tracing.printTrace(Policy.DEBUG,"SUBMIT", getClass().getSimpleName()+":createFileArea");
        createFilesArea(composite, this.changeFiles);
        
        Tracing.printTrace(Policy.DEBUG,"SUBMIT", getClass().getSimpleName()+":createJobArea");
        createJobsArea(composite, this.changeJobs);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(addJobsButton, IHelpContextIds.CHANGE_SPEC_ADD_JOBS);

        descEditor.setFocus();

        return composite;
    }

    private void updateJobEnablement() {
        removeJobsButton.setEnabled(!changeJobs.isEmpty());
        jobStatusCombo.setEnabled(!changeJobs.isEmpty());
    }

    /**
     * Create buttons.
     * 
     * @param parent
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (submit) {
            okButton = createButton(parent, IDialogConstants.OK_ID,
                    Messages.ChangeSpecDialog_Submit, true);
        } else {
            okButton = createButton(parent, IDialogConstants.OK_ID,
                    IDialogConstants.OK_LABEL, true);
        }
        enableOk();
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

    /**
     * Check if OK button should be enabled or disabled.
     */
    protected void enableOk() {
        if (okButton != null) {
            if (description.trim().equals("") || //$NON-NLS-1$
                    description.trim().equals(
                            PerforceConstants.DEFAULT_DESCRIPTION)
                    || (submit && checkedFiles.length == 0)) {
                okButton.setEnabled(false);
            } else {
                okButton.setEnabled(true);
            }
        }
    }

    /**
     * Updates the check state for the file list viewer
     */
    protected void updateCheckState() {
        if (okButton != null) {
            checkedFiles = filesViewer.getCheckedFiles();
            enableOk();
        }
    }

    /**
     * Add jobs to the dialog
     * 
     * @param jobs
     */
    public void addJobs(IP4Job[] jobs) {
        if (jobs != null && jobs.length > 0) {
            for (IP4Job job : jobs) {
                changeJobs.add(job);
            }
            jobsViewer.refresh();
            updateJobEnablement();
            descEditor.updateJobs(changeJobs);
        }
    }

    /**
     * Remove jobs from the dialog
     * 
     * @param jobs
     */
    public void removeJobs(final IP4Job[] jobs) {
        if (jobs != null && jobs.length > 0) {
            for (IP4Job job : jobs) {
                removedJobs.add(job);
                changeJobs.remove(job);
            }
            jobsViewer.refresh();
            updateJobEnablement();
            this.descEditor.updateJobs(changeJobs);
        }
    }

    /**
     * Get the job status selected
     * 
     * @return - status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        this.description = descEditor.getDescription();
        this.selected = this.checkedFiles;
        this.unselected = this.filesViewer.getUncheckedFiles();
        if (submit) {
            reopenFiles = reopenCheck.getSelection();
        }
        if (jobStatusCombo.getSelectionIndex() >= 0) {
            this.status = jobStatusCombo.getText();
        }
        DescriptionTemplate.saveHistory(this.description);
        super.okPressed();
    }

}
