package com.perforce.team.ui.dialogs;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.p4java.dialogs.ChangeSpecDialog;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

/**
 * List control to allow user to select from a list of files
 */
public class JobListViewer {

    // The parent change spec dialog
    private ChangeSpecDialog parentDialog;

    // Default dimensions
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 75;

    /**
     * Label provider
     */
    private static class JobListLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        // The job image
        private Image jobImage = null;

        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (jobImage == null) {
                    jobImage = getJobImage();
                }
                return jobImage;
            } else {
                return null;
            }
        }

        @Override
        public void dispose() {
            if (jobImage != null) {
                jobImage.dispose();
                jobImage = null;
            }
        }

        private Image getJobImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_CHG_JOB)
                    .createImage();
        }

        /**
         * Get file text
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            final IP4Job job = (IP4Job) element;
            String text = job.getId();
            String desc = job.getShortDescription();
            if (desc != null) {
                text += " - " + P4CoreUtils.removeWhitespace(desc); //$NON-NLS-1$
            }
            return text;
        }
    }

    private TableViewer jobsTable;

    /**
     * Constructor. Takes list of jobs to display.
     * 
     * @param parent
     *            parent window
     * @param dialog
     * @param jobs
     *            list of files to display
     */
    public JobListViewer(Composite parent, ChangeSpecDialog dialog,
            Set<IP4Job> jobs) {
        jobsTable = new TableViewer(parent, SWT.BORDER | SWT.MULTI
                | SWT.FULL_SELECTION);
        this.parentDialog = dialog;

        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = DEFAULT_WIDTH;
        data.heightHint = DEFAULT_HEIGHT;
        jobsTable.getControl().setLayoutData(data);

        jobsTable.setContentProvider(new ArrayContentProvider());
        jobsTable.setLabelProvider(new JobListLabelProvider());

        final Action unfixAction = new Action(
                Messages.JobListViewer_RemoveFromChangelist, PerforceUIPlugin
                        .getPlugin().getImageDescriptor(
                                IPerforceUIConstants.IMG_DELETE)) {

            @Override
            public void run() {
                IStructuredSelection selection = (IStructuredSelection) jobsTable
                        .getSelection();
                if (selection != null && !selection.isEmpty()) {
                    List<?> jobList = selection.toList();
                    IP4Job[] jobs = (IP4Job[]) jobList
                            .toArray(new IP4Job[jobList.size()]);
                    parentDialog.removeJobs(jobs);
                }
            }
        };

        MenuManager manager = new MenuManager();
        Table tab = jobsTable.getTable();
        Menu menu = manager.createContextMenu(tab);
        manager.addMenuListener(new IMenuListener() {

            public void menuAboutToShow(IMenuManager manager) {
                if (jobsTable.getTable().getSelectionCount() > 0) {
                    manager.add(unfixAction);
                }
            }
        });
        manager.setRemoveAllWhenShown(true);
        tab.setMenu(menu);

        jobsTable.setInput(jobs);
    }

    /**
     * Refresh the job table
     */
    public void refresh() {
        if (jobsTable != null) {
            jobsTable.refresh();
        }
    }

    /**
     * Get main control
     * 
     * @return - control
     */
    public Control getControl() {
        Control control = null;
        if (jobsTable != null) {
            control = jobsTable.getControl();
        }
        return control;
    }

    /**
     * Get the currently selected jobs
     * 
     * @return - p4 jobs
     */
    public IP4Job[] getSelectedJobs() {
        IStructuredSelection selection = (IStructuredSelection) jobsTable
                .getSelection();
        Object[] selected = selection.toArray();
        IP4Job[] jobs = new IP4Job[selected.length];
        System.arraycopy(selected, 0, jobs, 0, selected.length);
        return jobs;
    }

    /**
     * Remove a job from the table
     * 
     * @param job
     */
    public void remove(IP4Job job) {
        if (jobsTable != null && job != null) {
            jobsTable.remove(job);
        }
    }

    /**
     * Remove an array of jobs from the table
     * 
     * @param jobs
     */
    public void remove(IP4Job[] jobs) {
        if (jobsTable != null && jobs != null) {
            jobsTable.remove(jobs);
        }
    }

    /**
     * Add a job to the table
     * 
     * @param job
     */
    public void add(IP4Job job) {
        if (jobsTable != null && job != null) {
            jobsTable.add(job);
        }
    }

    /**
     * Add an array of jobs to the table
     * 
     * @param jobs
     */
    public void add(IP4Job[] jobs) {
        if (jobsTable != null && jobs != null) {
            jobsTable.add(jobs);
        }
    }

}