/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.jobs;

import com.perforce.p4java.core.IJob;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.p4java.impl.generic.core.Job;
import com.perforce.team.core.ConnectionParameters;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.P4ConnectionManager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class EditJobDialog extends BaseJobDialog {

    private IP4Job job;
    private IJob updatedJob;
    private List<IP4Changelist> initialChangelists;
    private Set<IP4Changelist> addedChangelists;
    private Set<IP4Changelist> removedChangelists;

    /**
     * @param parent
     * @param job
     * @param changelists
     */
    public EditJobDialog(Shell parent, IP4Job job, IP4Changelist[] changelists) {
        super(parent);
        setType(Type.EDIT);
        if (job != null && job.getId() != null && job.getConnection() != null) {
            this.job = job;
            ConnectionParameters params = this.job.getConnection()
                    .getParameters();
            setTitle(MessageFormat
                    .format(Messages.EditJobDialog_EditJobTitle, job.getId(),
                            params.getPortNoNull(), params.getUserNoNull()));
        }
        if (changelists != null) {
            this.initialChangelists = Arrays.asList(changelists);
        } else {
            this.initialChangelists = new ArrayList<IP4Changelist>();
        }
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#isReadonly(com.perforce.p4java.core.IJobSpec.IJobSpecField,
     *      java.lang.String)
     */
    @Override
    protected boolean isReadonly(IJobSpecField field, String preset) {
        if (field.getCode() == IP4Job.JOB_NAME_CODE) {
            return true;
        }
        return super.isReadonly(field, preset);
    }

    /**
     * Get the updated p4j job
     * 
     * @return - p4j job
     */
    public IJob getUpdatedJob() {
        return this.updatedJob;
    }

    /**
     * Get any added changelists
     * 
     * @return - added changelists
     */
    public IP4Changelist[] getAddedChangelists() {
        return this.addedChangelists != null ? this.addedChangelists
                .toArray(new IP4Changelist[0]) : null;
    }

    /**
     * Get any removed changelists
     * 
     * @return - removed changelists
     */
    public IP4Changelist[] getRemovedChangelists() {
        return this.removedChangelists != null ? this.removedChangelists
                .toArray(new IP4Changelist[0]) : null;
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#getConnection()
     */
    @Override
    protected IP4Connection getConnection() {
        IP4Connection connection = null;
        if (this.job != null) {
            connection = this.job.getConnection();
        }
        return connection;
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#addChangelists(org.eclipse.jface.viewers.TableViewer)
     */
    @Override
    protected void addChangelists(TableViewer viewer) {
        for (IP4Changelist list : this.initialChangelists) {
            addChangelist(list, false);
        }
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#setField(org.eclipse.swt.widgets.Text,
     *      com.perforce.p4java.core.IJobSpec.IJobSpecField, java.lang.String)
     */
    @Override
    protected void setField(Text text, IJobSpecField field, String preset) {
        Object fieldValue = job.getField(field.getName());
        if (fieldValue != null) {
            text.setText(fieldValue.toString());
        } else if (IP4Job.ALWAYS_FIELD_TYPE.equals(field.getFieldType())
                && preset != null) {
            text.setText(preset);
        }
    }

    /**
     * Load the state of the current fields into a map to be used to update the
     * existing job
     * 
     * @param fields
     */
    public void loadFields(Map<String, Object> fields) {
        Widget[] widgets = getFields();
        for (Widget widget : widgets) {
            IJobSpecField field = (IJobSpecField) widget.getData();
            if (field != null) {
                String value = null;
                if (field.getCode() != IP4Job.JOB_NAME_CODE) {
                    value = getWidgetText(widget, true);
                } else {
                    value = getWidgetText(widget, false);
                }
                if (value != null) {
                    fields.put(field.getName(), value);
                }
            }
        }
    }

    /**
     * Update the stored changes to changelists association and job fields
     */
    public void updateChanges() {
        Map<String, Object> fields = new HashMap<String, Object>();
        loadFields(fields);
        this.updatedJob = new Job(null, fields);
        this.addedChangelists = new HashSet<IP4Changelist>();
        this.removedChangelists = new HashSet<IP4Changelist>();
        for (IP4Changelist list : this.changelists) {
            if (this.initialChangelists.contains(list)) {
                if (!this.changelistTable.getChecked(list)) {
                    this.removedChangelists.add(list);
                }
            } else {
                if (this.changelistTable.getChecked(list)) {
                    this.addedChangelists.add(list);
                }
            }
        }
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#save()
     */
    @Override
    public boolean save() {
        updateChanges();
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                job.update(this.updatedJob);
            } catch (P4JavaException e) {
                retry = P4ConnectionManager.getManager().displayException(
                        job.getConnection(), e, true, true);
                if (!retry) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#setField(org.eclipse.swt.widgets.Combo,
     *      com.perforce.p4java.core.IJobSpec.IJobSpecField, java.lang.String)
     */
    @Override
    protected void setField(Combo combo, IJobSpecField field, String preset) {
        Object fieldValue = job.getField(field.getName());
        if (fieldValue != null) {
            combo.setText(fieldValue.toString());
        }
    }

}
