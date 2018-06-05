/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.jobs.JobsWidget;

import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class ChangelistWidget {

    private TabFolder changelistFolder;

    private TabItem detailsTab;
    private ChangelistDetailsWidget details;
    private TabItem filesTab;
    private ChangelistFileWidget files;
    private TabItem jobsTab;
    private JobsWidget jobs;

    private Composite displayArea;
    private IP4Changelist input;

    /**
     * Enable double-click edit from widgets
     */
    protected boolean enableEdit = false;

    /**
     * Create changelist widget
     * 
     * @param enableEdit
     * 
     */
    public ChangelistWidget(boolean enableEdit) {
        this.enableEdit = enableEdit;
    }

    /**
     * Get input of widget
     * 
     * @return changelist
     */
    public IP4Changelist getInput() {
        return this.input;
    }

    /**
     * Get main control
     * 
     * @return composite
     */
    public Composite getControl() {
        return this.displayArea;
    }

    /**
     * Load files from changelist
     * 
     * @param list
     */
    public void loadFiles(IP4Changelist list) {
        if (list != null && this.input == list) {
            this.files.setFiles(list.getFiles());
            this.files.generateCompressedFileTree();
            this.files.generateFileTree();
        }
    }

    /**
     * Refresh file input
     */
    public void refreshFiles() {
        this.files.refreshInput();
    }

    /**
     * Set the input of the widget, must be called from ui-thread
     * 
     * @param list
     */
    public void setInput(IP4Changelist list) {
        this.input = list;
        this.details.setChangelist(list);
        IP4Resource[] resources = new IP4Resource[0];
        IP4Job[] jobs = new IP4Job[0];
        String filesTitle = Messages.ChangelistWidget_Files;
        String jobsTitle = Messages.ChangelistWidget_Jobs;
        if (list != null) {
            resources = list.getFiles();
            jobs = list.getJobs();
        }
        if (list == null || list.needsRefresh()) {
            files.setFiles(new IP4Resource[0]);
            refreshFiles();
        } else {
            this.files.showLoading();
        }
        if (list != null && !list.needsRefresh()) {
            filesTitle += " (" + resources.length + ")"; //$NON-NLS-1$ //$NON-NLS-2$
            jobsTitle += " (" + jobs.length + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        this.filesTab.setText(filesTitle);

        if (this.jobs != null) {
            this.jobs.setInput(jobs);
        }

        if (this.jobsTab != null) {
            this.jobsTab.setText(jobsTitle);
        }
    }

    /**
     * Create control
     * 
     * @param parent
     */
    public void createControl(Composite parent) {
        createControl(parent, null);
    }

    /**
     * Create control
     * 
     * @param parent
     * @param filter
     */
    public void createControl(Composite parent, ViewerFilter filter) {
        displayArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginBottom = 0;
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        changelistFolder = new TabFolder(displayArea, SWT.TOP);
        changelistFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        createDetailsTab();
        createFilesTabs(filter);
        createJobsTab();
    }

    /**
     * Create file widget
     * 
     * @return - changelist file widget
     */
    protected ChangelistFileWidget createFileWidget() {
        return new ChangelistFileWidget();
    }

    /**
     * Create details widget
     * 
     * @return - changelist details widget
     */
    protected ChangelistDetailsWidget createDetailsWidget() {
        return new ChangelistDetailsWidget();
    }

    private void createDetailsTab() {
        detailsTab = new TabItem(changelistFolder, SWT.NONE);
        detailsTab.setText(Messages.ChangelistWidget_Details);

        this.details = createDetailsWidget();
        this.details.createControl(changelistFolder);

        detailsTab.setControl(details.getControl());
    }

    private void createFilesTabs(ViewerFilter filter) {
        filesTab = new TabItem(changelistFolder, SWT.NONE);
        filesTab.setText(Messages.ChangelistWidget_Files);

        this.files = createFileWidget();
        this.files.createControl(changelistFolder, filter);

        filesTab.setControl(this.files.getControl());
    }

    /**
     * Create jobs tab. Sub-classes should override to prevent jobs tab from
     * being displayed.
     */
    protected void createJobsTab() {
        jobsTab = new TabItem(changelistFolder, SWT.NONE);
        jobsTab.setText(Messages.ChangelistWidget_Jobs);

        this.jobs = new JobsWidget(this.enableEdit);
        this.jobs.createControl(changelistFolder);

        jobsTab.setControl(this.jobs.getControl());
    }

    /**
     * Get details widget
     * 
     * @return - details
     */
    public ChangelistDetailsWidget getDetailsWidget() {
        return this.details;
    }

    /**
     * Get files widget
     * 
     * @return - files
     */
    public ChangelistFileWidget getFilesWidget() {
        return this.files;
    }

    /**
     * Get jobs widget
     * 
     * @return - jobs
     */
    public JobsWidget getJobsWidget() {
        return this.jobs;
    }

}
