/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.JobFixDialog;
import com.perforce.team.ui.jobs.JobsWidget;
import com.perforce.team.ui.mylyn.IP4MylynUiConstants;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.actions.OpenTaskAction;
import com.perforce.team.ui.mylyn.job.BulkJobUpdater.JobCallback;
import com.perforce.team.ui.mylyn.job.JobFieldEntry.FieldChange;

import java.text.MessageFormat;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BulkJobPage extends FormPage implements IBulkChange {

    private Map<String, IJobProxy> jobs;
    private IP4Connection connection;

    private Composite body = null;
    private Section fields = null;
    private Section jobsSection = null;
    private ToolItem addItem = null;
    private ToolItem removeItem = null;
    private Action submitAction = null;
    private JobsWidget jobsWidget;
    private JobFieldManager manager;
    private Link progressLabel;
    private String errors = null;
    private ProgressBar bar;
    private IProgressMonitor monitor = new IProgressMonitor() {

        private int allWork = 0;
        private int worked = 0;
        private boolean cancelled = false;

        public void worked(int work) {
            worked += work;
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(bar)) {
                        bar.setSelection(worked);
                    }
                }
            });
        }

        public void subTask(String name) {

        }

        public void setTaskName(final String name) {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(progressLabel)) {
                        if (name != null) {
                            progressLabel.setText(name);
                        } else {
                            progressLabel.setText(""); //$NON-NLS-1$
                        }
                    }
                }
            });
        }

        public void setCanceled(boolean value) {
            this.cancelled = value;
        }

        public boolean isCanceled() {
            return this.cancelled;
        }

        public void internalWorked(double work) {

        }

        public void done() {
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(body)) {
                        progressLabel.setText(""); //$NON-NLS-1$
                        bar.setVisible(false);
                        jobsWidget.getViewer().getTree().setEnabled(true);
                        manager.setEnabled(true);
                        setActionsEnabled(true);
                    }
                }
            });
        }

        public void beginTask(String name, int totalWork) {
            this.allWork = totalWork;
            this.worked = 0;
            PerforceUIPlugin.syncExec(new Runnable() {

                public void run() {
                    if (P4UIUtils.okToUse(bar)) {
                        bar.setMinimum(0);
                        bar.setSelection(0);
                        bar.setMaximum(allWork);
                    }
                }
            });
        }
    };

    /**
     * @param editor
     * @param connection
     * @param initialJobs
     */
    public BulkJobPage(FormEditor editor, IP4Connection connection,
            IJobProxy[] initialJobs) {
        super(editor, "bulkJobChanges", Messages.BulkJobPage_Jobs); //$NON-NLS-1$
        this.connection = connection;
        this.jobs = new TreeMap<String, IJobProxy>();
        for (IJobProxy job : initialJobs) {
            this.jobs.put(job.getId(), job);
        }
    }

    private void createChangesSection(Composite parent, FormToolkit toolkit) {
        this.fields = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        fields.setText(Messages.BulkJobPage_JobFields);
        fields.setLayout(new GridLayout(2, true));
        fields.setExpanded(true);
        fields.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        P4FormUIUtils.registerExpansionSpaceGrabber(fields, parent);

        Composite fieldsArea = toolkit.createComposite(fields);
        fieldsArea.setLayout(new GridLayout(2, false));
        fieldsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        fields.setClient(fieldsArea);

        manager = new JobFieldManager();
        manager.createControl(fieldsArea, this.connection);
        manager.loadDefaults();
    }

    private void createJobsSection(Composite parent, final FormToolkit toolkit) {
        this.jobsSection = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE);
        jobsSection.setText(Messages.BulkJobPage_Jobs);
        jobsSection.setLayout(new GridLayout(1, true));
        jobsSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        jobsSection.setExpanded(true);
        P4FormUIUtils.registerExpansionSpaceGrabber(jobsSection, parent);

        Composite jobsArea = toolkit.createComposite(jobsSection);
        jobsArea.setLayout(new GridLayout(2, false));
        GridData jaData = new GridData(SWT.FILL, SWT.FILL, true, true);
        jobsArea.setLayoutData(jaData);
        jobsSection.setClient(jobsArea);

        this.jobsWidget = new JobsWidget(true) {

            @Override
            protected void createToolbar(Composite parent) {
                ToolBar toolbar = P4FormUIUtils.createSectionToolbar(toolkit,
                        jobsSection);
                fillToolbar(toolbar);
            }

            @Override
            protected ITreeContentProvider createContentProvider(
                    TreeViewer viewer) {
                return new PerforceContentProvider(viewer, true) {

                    @Override
                    public Object[] getElements(Object inputElement) {
                        return jobs.values().toArray();
                    }

                };
            }

            @Override
            protected void handleDoubleClick(Object selected) {
                IP4Job job = P4CoreUtils.convert(selected, IP4Job.class);
                if (job != null) {
                    EditJobTaskAction open = new EditJobTaskAction();
                    open.selectionChanged(null, new StructuredSelection(job));
                    open.run(null);
                } else {
                    ITask task = P4CoreUtils.convert(selected, ITask.class);
                    if (task != null) {
                        OpenTaskAction open = new OpenTaskAction();
                        open.selectionChanged(null, new StructuredSelection(
                                task));
                        open.run(null);
                    }
                }

            }

            @Override
            protected void fillToolbar(ToolBar toolbar) {
                addItem = new ToolItem(toolbar, SWT.PUSH);
                addItem.setToolTipText(Messages.BulkJobPage_AddJobs);
                Image addImage = PerforceUIPlugin.getDescriptor(
                        IPerforceUIConstants.IMG_ADD).createImage();
                P4UIUtils.registerDisposal(addItem, addImage);
                addItem.setImage(addImage);
                addItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        JobFixDialog dialog = new JobFixDialog(P4UIUtils
                                .getDialogShell(), connection,
                                Messages.BulkJobPage_SelectJobs);
                        if (dialog.open() == JobFixDialog.OK) {
                            IP4Job[] selected = dialog.getSelectedJobs();
                            if (selected != null && selected.length > 0) {
                                for (IP4Job job : selected) {
                                    jobs.put(job.getId(), new JobProxy(job));
                                }
                                setViewerInput();
                            }
                        }
                    }

                });

                removeItem = new ToolItem(toolbar, SWT.PUSH);
                Image removeImage = PerforceUIPlugin.getDescriptor(
                        IPerforceUIConstants.IMG_DELETE).createImage();
                P4UIUtils.registerDisposal(removeItem, removeImage);
                removeItem.setImage(removeImage);
                removeItem.setToolTipText(Messages.BulkJobPage_RemoveJobs);
                removeItem.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        IStructuredSelection selection = (IStructuredSelection) jobsWidget
                                .getViewer().getSelection();
                        for (Object element : selection.toArray()) {
                            if (element instanceof IJobProxy) {
                                jobs.remove(((IJobProxy) element).getId());
                            }
                        }
                        setViewerInput();
                    }

                });
            }

        };
        this.jobsWidget.createControl(jobsArea);
        this.jobsWidget.getViewer().addDropSupport(
                DND.DROP_DEFAULT | DND.DROP_LINK | DND.DROP_MOVE,
                new Transfer[] { LocalSelectionTransfer.getTransfer() },
                new BulkChangeDropAdapter(jobsWidget.getViewer(), this));
        setViewerInput();
    }

    private void updateJobSectionHeader() {
        this.jobsSection.setText(MessageFormat.format(
                Messages.BulkJobPage_JobsWithAmount, jobs.size()));
        this.jobsSection.layout(true);

    }

    private void setViewerInput() {
        this.jobsWidget.getViewer().setInput(
                jobs.values().toArray(new IJobProxy[jobs.size()]));
        updateJobSectionHeader();
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        body = managedForm.getForm().getBody();
        body.setLayout(new GridLayout(1, true));
        FormToolkit toolkit = managedForm.getToolkit();

        progressLabel = new Link(body, SWT.NONE);
        progressLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));
        progressLabel.setBackground(body.getBackground());
        progressLabel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (errors == null) {
                    monitor.setCanceled(true);
                } else {
                    BulkJobErrorDialog errorDialog = new BulkJobErrorDialog(
                            progressLabel.getShell(), errors);
                    errorDialog.open();
                }
            }
        });

        bar = new ProgressBar(body, SWT.HORIZONTAL);
        bar.setVisible(false);
        bar.setBackground(body.getBackground());
        bar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        createChangesSection(body, toolkit);
        createJobsSection(body, toolkit);
    }

    private void refreshJobs() {
        PerforceUIPlugin.syncExec(new Runnable() {

            public void run() {
                if (P4UIUtils.okToUse(jobsWidget.getViewer())) {
                    jobsWidget.getViewer().refresh();
                    updateJobSectionHeader();
                }
            }
        });
    }

    private void setActionsEnabled(boolean enabled) {
        addItem.setEnabled(enabled);
        removeItem.setEnabled(enabled);
        submitAction.setEnabled(enabled);
    }

    /**
     * Submit changes to jobs
     */
    public void submit() {
        IJobProxy[] updateJobs = this.jobs.values().toArray(
                new IJobProxy[this.jobs.size()]);
        FieldChange[] filters = this.manager.getFields();
        IJobSpec spec = this.connection.getJobSpec();
        if (updateJobs.length > 0 && filters.length > 0 && spec != null) {
            errors = null;
            bar.setVisible(true);
            bar.setMinimum(0);
            int count = 0;
            bar.setSelection(count);
            bar.setMaximum(updateJobs.length);
            monitor.setCanceled(false);
            jobsWidget.getViewer().getTree().setEnabled(false);
            manager.setEnabled(false);
            setActionsEnabled(false);
            final BulkJobUpdater updater = new BulkJobUpdater(updateJobs, spec,
                    filters);
            Job updateJob = new Job(Messages.BulkJobPage_UpdatingJobs) {

                @Override
                protected IStatus run(IProgressMonitor monitor) {
                    errors = updater.run(BulkJobPage.this.monitor,
                            new JobCallback() {

                                public void updated(IP4Job oldJob, IP4Job newJob) {
                                    IJobProxy updated = new JobProxy(newJob);
                                    jobs.put(updated.getId(), updated);
                                    refreshJobs();
                                }
                            });
                    return Status.OK_STATUS;
                }
            };
            updateJob.setSystem(true);
            updateJob.schedule();
        }

    }

    /**
     * Fill header toolbar
     * 
     * @param manager
     */
    public void fillHeaderToolbar(IToolBarManager manager) {
        this.submitAction = new Action(Messages.BulkJobPage_SubmitJobChanges,
                PerforceUiMylynPlugin
                        .getImageDescriptor(IP4MylynUiConstants.IMG_JOB_SUBMIT)) {

            @Override
            public void run() {
                submit();
            }

        };
        this.submitAction.setToolTipText(Messages.BulkJobPage_SubmitJobChanges);
        manager.add(this.submitAction);
    }

    /**
     * @see com.perforce.team.ui.mylyn.job.IBulkChange#add(com.perforce.team.ui.mylyn.job.IJobProxy[])
     */
    public void add(IJobProxy[] jobs) {
        if (jobs != null) {
            for (IJobProxy job : jobs) {
                this.jobs.put(job.getId(), job);
            }
            refreshJobs();
        }
    }

    /**
     * @see com.perforce.team.ui.mylyn.job.IBulkChange#getConnection()
     */
    public IP4Connection getConnection() {
        return this.connection;
    }
}
