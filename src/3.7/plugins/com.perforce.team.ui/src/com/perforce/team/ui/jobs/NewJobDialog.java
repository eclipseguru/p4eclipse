/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.jobs;

import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.p4java.IP4Changelist;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.P4ConnectionManager;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
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
public class NewJobDialog extends BaseJobDialog {

    private IP4Connection connection;
    private IP4Job createdJob;
    private Set<IP4Changelist> addedChangelists;
    private IP4Job template;

    /**
     * @param parent
     * @param connection
     * @param template
     */
    public NewJobDialog(Shell parent, IP4Connection connection, IP4Job template) {
        super(parent);
        this.connection = connection;
        this.template = template;
        if (this.connection != null) {
            setTitle(MessageFormat.format(Messages.NewJobDialog_NewJob,
                    connection.getParameters().getPortNoNull(), connection
                            .getParameters().getUserNoNull()));
        }
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#getConnection()
     */
    @Override
    protected IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#addChangelists(org.eclipse.jface.viewers.TableViewer)
     */
    @Override
    protected void addChangelists(TableViewer viewer) {

    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#setField(org.eclipse.swt.widgets.Text,
     *      com.perforce.p4java.core.IJobSpec.IJobSpecField, java.lang.String)
     */
    @Override
    protected void setField(Text text, IJobSpecField field, String preset) {
        boolean set = false;
        if (template != null) {
            Object value = template.getField(field.getName());
            if (value != null) {
                text.setText(value.toString());
                set = true;
            }
        }
        String type = field.getFieldType();
        if (IP4Job.ALWAYS_FIELD_TYPE.equals(type)
                || IP4Job.ONCE_FIELD_TYPE.equals(type)) {
            text.setEditable(false);
        }
        if (!set && preset != null) {
            if (!IP4Job.OPTIONAL_FIELD_TYPE.equals(type)) {
                if (IP4Job.BLANK_PRESET.equals(preset)) {
                    text.setText(IP4Job.BLANK_VALUE);
                } else if (IP4Job.USER_PRESET.equals(preset)) {
                    text.setText(getConnection().getParameters()
                            .getUserNoNull());
                } else if (!IP4Job.NOW_PRESET.equals(preset)) {
                    text.setText(preset);
                }
            }
        } else if (IP4Job.JOB_NAME_CODE == field.getCode()) {
            text.setText("new"); //$NON-NLS-1$
        }
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#setField(org.eclipse.swt.widgets.Combo,
     *      com.perforce.p4java.core.IJobSpec.IJobSpecField, java.lang.String)
     */
    @Override
    protected void setField(Combo combo, IJobSpecField field, String preset) {
        boolean set = false;
        if (template != null) {
            Object value = template.getField(field.getName());
            if (value != null) {
                combo.setText(value.toString());
                set = true;
            }
        }
        if (!set && preset != null) {
            // Status may contain more than just the preset but also the fix
            // preset. The actual preset will be before the comma found.
            // jobStatus,fix/fixStatus
            if (IP4Job.JOB_STATUS_CODE == field.getCode()) {
                int comma = preset.indexOf(',');
                if (comma != -1) {
                    preset = preset.substring(0, comma);
                }
            }
            combo.setText(preset);
        }
    }

    /**
     * Load the state of the current fields into a map to be used to create the
     * new job
     * 
     * @param fields
     */
    public void loadFields(Map<String, Object> fields) {
        Widget[] widgets = getFields();
        for (Widget widget : widgets) {
            IJobSpecField field = (IJobSpecField) widget.getData();
            if (field != null) {
                String value = getWidgetText(widget, true);
                if (value != null) {
                    fields.put(field.getName(), value);
                }
            }
        }
    }

    /**
     * Get the created job if the dialog was saved and creation succeeded
     * 
     * @return - created job or null if create failed or was cancelled
     */
    public IP4Job getCreatedJob() {
        return this.createdJob;
    }

    /**
     * Get initial changelists fixed by this job
     * 
     * @return - array of changelists
     */
    public IP4Changelist[] getAddedChangelists() {
        return this.addedChangelists != null ? this.addedChangelists
                .toArray(new IP4Changelist[0]) : null;
    }

    /**
     * @see com.perforce.team.ui.jobs.BaseJobDialog#save()
     */
    @Override
    public boolean save() {
        Map<String, Object> fields = new HashMap<String, Object>();
        loadFields(fields);
        this.addedChangelists = new HashSet<IP4Changelist>();
        for (IP4Changelist list : this.changelists) {
            if (this.changelistTable.getChecked(list)) {
                this.addedChangelists.add(list);
            }
        }
        boolean retry = true;
        while (retry) {
            retry = false;
            try {
                this.createdJob = connection.createJob(fields);
            } catch (P4JavaException e) {
                retry = P4ConnectionManager.getManager().displayException(
                        connection, e, true, true);
                if (!retry) {
                    return false;
                }
            }
        }
        return true;
    }

}
