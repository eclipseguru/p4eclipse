/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.mylyn.job.JobFieldEntry.FieldChange;
import com.perforce.team.ui.mylyn.search.IFilterListener;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobFieldManager extends BaseErrorProvider {

    /**
     * Default fields to show when filter manager is empty
     */
    public static final int DEFAULT_FIELDS = 3;

    private IJobSpec spec;
    private ScrolledComposite scrolls;
    private Composite displayArea;
    private List<JobFieldEntry> filters = new ArrayList<JobFieldEntry>();
    private ListenerList listeners = new ListenerList();

    private Listener listener = new Listener() {

        public void handleEvent(Event event) {
            checkQuery();
        }
    };

    private void checkQuery() {
        errorMessage = null;
        validate();
    }

    /**
     * Set the manager as enabled or disabled
     * 
     * @param enabled
     */
    public void setEnabled(boolean enabled) {
        this.displayArea.setEnabled(enabled);
    }

    /**
     * Load default filter entries
     */
    public void loadDefaults() {
        for (int i = 0; i < DEFAULT_FIELDS; i++) {
            JobFieldEntry entry = new JobFieldEntry(this, spec);
            entry.createControl(displayArea);
            add(entry);
        }
    }

    /**
     * Get valid filter fields
     * 
     * @return - non-null but possibly empty array
     */
    public FieldChange[] getFields() {
        List<FieldChange> entries = new ArrayList<FieldChange>();
        for (JobFieldEntry entry : filters) {
            FieldChange filter = entry.getFilter();
            if (filter != null && filter.name != null && filter.value != null) {
                entries.add(filter);
            }
        }
        return entries.toArray(new FieldChange[entries.size()]);
    }

    /**
     * Create filter manager
     * 
     * @param parent
     * @param connection
     */
    public void createControl(Composite parent, IP4Connection connection) {
        scrolls = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.BORDER);
        scrolls.setExpandHorizontal(true);
        scrolls.setExpandVertical(true);
        scrolls.setBackground(parent.getBackground());
        scrolls.setBackgroundMode(SWT.INHERIT_DEFAULT);
        displayArea = new Composite(scrolls, SWT.NONE);
        scrolls.setContent(displayArea);
        GridData sData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sData.heightHint = 150;
        scrolls.setLayoutData(sData);
        displayArea.setLayout(new GridLayout(1, false));
        GridData baData = new GridData(SWT.FILL, SWT.FILL, true, true);
        baData.horizontalIndent = 15;
        displayArea.setLayoutData(baData);

        Label rulesLabel = new Label(displayArea, SWT.NONE);
        rulesLabel.setText(Messages.JobFieldManager_ChangeFields);

        this.spec = connection.getJobSpec();
    }

    /**
     * Remove a filter entry to this manager
     * 
     * @param entry
     */
    public void add(JobFieldEntry entry) {
        if (entry != null) {
            if (filters.size() == 1) {
                filters.get(0).setRemoveEnabled(true);
            }
            filters.add(entry);
            entry.addListener(this.listener);
            entry.setRemoveEnabled(filters.size() > 1);
            scrolls.setMinSize(displayArea
                    .computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
    }

    /**
     * Remove a filter entry from this manager
     * 
     * @param entry
     */
    public void remove(JobFieldEntry entry) {
        if (entry != null) {
            if (filters.remove(entry)) {
                entry.removeListener(this.listener);
                if (filters.size() == 1) {
                    filters.get(0).setRemoveEnabled(false);
                }
                scrolls.setMinSize(displayArea.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT));
            }
        }
    }

    /**
     * Add a listener to this manager
     * 
     * @param listener
     */
    public void addListener(IFilterListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Remove a listener from this manager
     * 
     * @param listener
     */
    public void removeListener(IFilterListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

}