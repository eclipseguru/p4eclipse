/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Listener;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.P4Event;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.P4Workspace;
import com.perforce.team.core.p4java.P4Event.EventType;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.UIJob;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class ChangelistFormPage extends FormPage implements
        IP4Listener {

    /**
     * JOBS_AREA_COLLAPSED
     */
    public static final String DETAILS_AREA_COLLAPSED = " com.perforce.team.ui.changelists.DETAILS_AREA_COLLAPSED"; //$NON-NLS-1$

    /**
     * JOBS_AREA_COLLAPSED
     */
    public static final String FILES_AREA_COLLAPSED = " com.perforce.team.ui.changelists.FILES_AREA_COLLAPSED"; //$NON-NLS-1$

    /**
     * JOBS_AREA_COLLAPSED
     */
    public static final String JOBS_AREA_COLLAPSED = " com.perforce.team.ui.changelists.JOBS_AREA_COLLAPSED"; //$NON-NLS-1$

    /**
     * PAGE_ID
     */
    public static final String PAGE_ID = "changelistPage"; //$NON-NLS-1$

    /**
     * Changelist
     */
    protected IP4Changelist changelist;

    private Composite body = null;
    private Section details;
    private Section files;
    private Section jobs;

    private Set<IP4Job> shownJobs = new HashSet<IP4Job>();

    private ChangelistFileWidget fileWidget;
    private ChangelistJobsWidget jobsWidget;

    private FormText changelistText;
    private FormText workspaceText;
    private FormText dateText;
    private FormText userText;
    private SourceViewer descriptionViewer;

    /**
     * @param editor
     * @param changelist
     */
    public ChangelistFormPage(FormEditor editor, IP4Changelist changelist) {
        super(editor, PAGE_ID, Messages.ChangelistFormPage_Overview);
        this.changelist = changelist;
        P4Workspace.getWorkspace().addListener(this);
    }

    private void registerExpansionSpaceGrabber(Section section) {
        P4FormUIUtils.registerExpansionSpaceGrabber(section, body);
    }

    private void createFilesSection(Composite parent, FormToolkit toolkit) {
        this.files = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        files.setText(Messages.ChangelistFormPage_Files);
        files.setExpanded(!PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(FILES_AREA_COLLAPSED));
        files.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, files
                .isExpanded()));
        registerExpansionSpaceGrabber(files);

        Composite filesArea = toolkit.createComposite(files);
        filesArea.setLayout(new GridLayout(2, false));
        filesArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        files.setClient(filesArea);

        this.fileWidget = createFileViewer(toolkit, files, filesArea);

        MenuManager manager = new MenuManager();
        Tree tree = this.fileWidget.getViewer().getTree();
        Menu menu = manager.createContextMenu(tree);
        tree.setMenu(menu);
        getSite().registerContextMenu(manager, this.fileWidget.getViewer());
    }

    /**
     * Create toolbar on specified section
     * 
     * @param toolkit
     * @param section
     * @return - created toolbar
     */
    protected ToolBar createSectionToolbar(FormToolkit toolkit, Section section) {
        return P4FormUIUtils.createSectionToolbar(toolkit, section);
    }

    /**
     * Create a {@link ChangelistFileWidget} that will display the files in the
     * changelist
     * 
     * @param toolkit
     * @param section
     * @param parent
     * @return - changelist file widget
     */
    protected abstract ChangelistFileWidget createFileViewer(
            final FormToolkit toolkit, final Section section, Composite parent);

    /**
     * Create jobs section. Override and do nothing to prevent a jobs section
     * from being displayed.
     * 
     * @param parent
     * @param toolkit
     */
    protected void createJobsSection(Composite parent, final FormToolkit toolkit) {
        this.jobs = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR
                | ExpandableComposite.TWISTIE);
        jobs.setText(Messages.ChangelistFormPage_Jobs);
        jobs.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        jobs.setExpanded(!PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(JOBS_AREA_COLLAPSED));

        Composite jobsArea = toolkit.createComposite(jobs);
        jobsArea.setLayout(new GridLayout(2, false));
        GridData jaData = new GridData(SWT.FILL, SWT.FILL, true, false);
        jobsArea.setLayoutData(jaData);
        jobs.setClient(jobsArea);

        this.jobsWidget = new ChangelistJobsWidget(this.changelist, true) {

            @Override
            protected void createToolbar(Composite parent) {
                ToolBar toolbar = createSectionToolbar(toolkit, jobs);
                fillToolbar(toolbar);
            }

        };
        this.jobsWidget.createControl(jobsArea);

        MenuManager manager = new MenuManager();
        Tree tree = this.jobsWidget.getViewer().getTree();
        Menu menu = manager.createContextMenu(tree);
        tree.setMenu(menu);
        getSite().registerContextMenu(manager, this.jobsWidget.getViewer());
    }

    /**
     * Get date label text
     * 
     * @return - date label text
     */
    protected String getDateLabelText() {
        return Messages.ChangelistFormPage_Date;
    }

    /**
     * Get user label text
     * 
     * @return - user label text
     */
    protected String getUserLabelText() {
        return Messages.ChangelistFormPage_User;
    }

    private void createDetailsSection(Composite parent, FormToolkit toolkit) {
        this.details = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        details.setText(Messages.ChangelistFormPage_Details);
        details.setExpanded(!PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(DETAILS_AREA_COLLAPSED));
        details.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, details
                .isExpanded()));
        registerExpansionSpaceGrabber(details);

        Composite detailsArea = toolkit.createComposite(details);
        detailsArea.setLayout(new GridLayout(4, false));
        detailsArea
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        details.setClient(detailsArea);

        toolkit.createLabel(detailsArea,
                Messages.ChangelistFormPage_Changelist, SWT.RIGHT);
        changelistText = new FormText(detailsArea, SWT.NO_FOCUS);
        changelistText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        toolkit.createLabel(detailsArea, Messages.ChangelistFormPage_Workspace);
        workspaceText = new FormText(detailsArea, SWT.NO_FOCUS);
        workspaceText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        toolkit.createLabel(detailsArea, getDateLabelText());
        dateText = new FormText(detailsArea, SWT.NO_FOCUS);
        dateText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        toolkit.createLabel(detailsArea, getUserLabelText());
        userText = new FormText(detailsArea, SWT.NO_FOCUS);
        userText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        toolkit.createLabel(detailsArea,
                Messages.ChangelistFormPage_Description);

        descriptionViewer = new SourceViewer(detailsArea, null, SWT.H_SCROLL
                | SWT.V_SCROLL | SWT.MULTI | SWT.BORDER | SWT.READ_ONLY
                | SWT.WRAP);
        descriptionViewer.setDocument(new Document());

        IAdaptable adaptable = new IAdaptable() {

            public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
                IP4Changelist adaptableList = changelist;
                return adaptableList != null ? Platform.getAdapterManager()
                        .getAdapter(adaptableList, adapter) : null;
            }
        };

        descriptionViewer.configure(P4UIUtils
                .createSourceViewerConfiguration(adaptable));
        GridData dData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dData.heightHint = 80;
        dData.horizontalSpan = 3;
        descriptionViewer.getTextWidget().setLayoutData(dData);

        resetDetailViewer();
    }

    private void loadContents() {
        UIJob loadJob = new UIJob(
                Messages.ChangelistFormPage_RefreshingChangelist
                        + this.changelist.getId()) {

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (fileWidget != null) {
                    fileWidget.showLoading();
                }
                if (jobsWidget != null) {
                    jobsWidget.showLoading();
                }
                P4Runner.schedule(new P4Runnable() {

                    @Override
                    public String getTitle() {
                        return MessageFormat.format(
                                Messages.ChangelistFormPage_LoadingChangelist,
                                changelist.getId());
                    }

                    @Override
                    public void run(IProgressMonitor monitor) {
                        if (changelist.needsRefresh()) {
                            changelist.refresh();
                        }
                        fileWidget.setFiles(changelist.getFiles());
                        fileWidget.generateFileTree();
                        fileWidget.generateCompressedFileTree();
                        PerforceUIPlugin.syncExec(new Runnable() {

                            public void run() {
                                fillViewers();
                            }
                        });
                    }

                });
                return Status.OK_STATUS;
            }
        };
        loadJob.schedule();
    }

    private void fillViewers() {
        if (P4UIUtils.okToUse(this.fileWidget.getViewer())) {
            IP4Resource[] clFiles = this.changelist.getFiles();
            this.fileWidget.refreshInput();
            this.files.setText(MessageFormat.format(
                    Messages.ChangelistFormPage_FilesNumber, clFiles.length));
            this.files.layout(true);
        }
        this.shownJobs.clear();
        this.shownJobs.addAll(Arrays.asList(this.changelist.getJobs()));
        resetJobViewer();
        resetDetailViewer();
    }

    private void resetDetailViewer() {
        if (!P4UIUtils.okToUse(this.details)) {
            return;
        }
        String user = this.changelist.getUserName();
        if (user == null) {
            user = P4UIUtils.EMPTY;
        }
        String client = this.changelist.getClientName();
        if (client == null) {
            client = P4UIUtils.EMPTY;
        }
        String description = changelist.getDescription();
        if (description == null) {
            description = P4UIUtils.EMPTY;
        }
        Date submittedDate = changelist.getDate();
        String submitted = null;
        if (submittedDate != null) {
            submitted = P4UIUtils.formatLabelDate(submittedDate);
        }
        if (submitted == null) {
            submitted = P4UIUtils.EMPTY;
        }
        String id = Integer.toString(changelist.getId());

        this.userText.setText(user, false, false);
        this.workspaceText.setText(client, false, false);
        this.dateText.setText(submitted, false, false);
        this.changelistText.setText(id, false, false);
        this.descriptionViewer.getDocument().set(description);
    }

    private void resetJobViewer() {
        if (this.jobsWidget != null && this.jobsWidget.okToUse()) {
            this.jobsWidget.setInput(this.shownJobs
                    .toArray(new IP4Job[this.shownJobs.size()]));
            this.jobs.setText(MessageFormat.format(
                    Messages.ChangelistFormPage_JobsNumber,
                    this.shownJobs.size()));
            this.jobs.layout(true, true);
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#dispose()
     */
    @Override
    public void dispose() {
        P4Workspace.getWorkspace().removeListener(this);
        IPreferenceStore store = PerforceUIPlugin.getPlugin()
                .getPreferenceStore();
        if (this.jobs != null) {
            store.setValue(JOBS_AREA_COLLAPSED, !this.jobs.isExpanded());
        }
        if (this.details != null) {
            store.setValue(DETAILS_AREA_COLLAPSED, !this.details.isExpanded());
        }
        if (this.files != null) {
            store.setValue(FILES_AREA_COLLAPSED, !this.files.isExpanded());
        }
        super.dispose();
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        body = managedForm.getForm().getBody();
        body.setLayout(new GridLayout(1, true));
        FormToolkit toolkit = managedForm.getToolkit();
        createDetailsSection(body, toolkit);
        createFilesSection(body, toolkit);
        createJobsSection(body, toolkit);

        loadContents();
    }

    /**
     * @see com.perforce.team.core.p4java.IP4Listener#resoureChanged(com.perforce.team.core.p4java.P4Event)
     */
    public void resoureChanged(final P4Event event) {
        if (event.getType() == EventType.FIXED
                || event.getType() == EventType.UNFIXED) {
            final List<IP4Job> modifiedJobs = new ArrayList<IP4Job>();
            for (IP4Job job : event.getJobs()) {
                if (this.changelist.equals(job.getParent())) {
                    modifiedJobs.add(job);
                }
            }
            if (modifiedJobs.size() > 0) {
                if (event.getType() == EventType.FIXED) {
                    shownJobs.addAll(modifiedJobs);
                } else if (event.getType() == EventType.UNFIXED) {
                    shownJobs.removeAll(modifiedJobs);
                }
                PerforceUIPlugin.asyncExec(new Runnable() {

                    public void run() {
                        if (jobsWidget != null && jobsWidget.okToUse()) {
                            resetJobViewer();
                        }
                    }
                });
            }
        }
    }

    /**
     * Refresh the page
     */
    public void refresh() {
        this.changelist.markForRefresh();
        loadContents();
    }
    
    public String getName() {
    	return ChangelistFormPage.class.getSimpleName();
    }
}
