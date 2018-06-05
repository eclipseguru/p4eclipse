/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.editor;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JobFieldGroup {

    /**
     * BASE_FIELDS
     */
    public static final String BASE_FIELDS = "com.perforce.team.ui.mylyn.editor.PAGE_FIELDS_"; //$NON-NLS-1$

    /**
     * FIELDS
     */
    public static final String FIELDS = "fields"; //$NON-NLS-1$

    /**
     * ENTRY
     */
    public static final String ENTRY = "entry"; //$NON-NLS-1$

    /**
     * NAME
     */
    public static final String NAME = "name"; //$NON-NLS-1$

    /**
     * CORE_FIELDS
     */
    public static final String CORE_FIELDS = "Job"; //$NON-NLS-1$

    /**
     * ADVANCED_FIELDS
     */
    public static final String ADVANCED_FIELDS = "Other"; //$NON-NLS-1$

    /**
     * Load the default page for a given connection and page name
     * 
     * @param connection
     * @param pageName
     * @return - job editor page or null if page could not be loaded
     */
    public static JobFieldGroup loadDefaultPages(IP4Connection connection,
            String pageName) {
        JobFieldGroup page = null;
        if (connection != null && CORE_FIELDS.equals(pageName)) {
            IJobSpec spec = connection.getJobSpec();
            if (spec != null) {
                page = new JobFieldGroup(connection, pageName);
                for (IJobSpecField field : spec.getFields()) {
                    page.add(new JobField(page, field));
                }
            }
        }
        return page;
    }

    /**
     * Load the default page for a given task repository and page name
     * 
     * @param repository
     * @param pageName
     * @return - job editor page or null if page could not be loaded
     */
    public static JobFieldGroup loadDefaultPages(TaskRepository repository,
            String pageName) {
        return loadDefaultPages(P4MylynUtils.getConnection(repository),
                pageName);
    }

    public static JobFieldGroup loadCorePage(IP4Connection connection) {
        JobFieldGroup page = null;
        if (connection != null) {
            IJobSpec spec = connection.getJobSpec();
            if (spec != null) {
                Map<String, IJobSpecField> specFields = new HashMap<String, IJobSpecField>();
                List<IJobSpecField> totalFields = new ArrayList<IJobSpecField>();
                for (IJobSpecField field : spec.getFields()) {
                    specFields.put(field.getName(), field);
                    totalFields.add(field);
                }
                page = loadCoreGroup(connection, specFields);
                
                // Check for missing fields if advanced page is empty and
                // place on core page
                JobFieldGroup advanced = loadAdvancedGroup(connection,
                		specFields);

                if (page != null) {

					for (JobField field : page.getFields()) {
						totalFields.remove(field.getField());
					}

					if (advanced.getFields().length == 0) {
						for (IJobSpecField field : totalFields) {
							page.add(new JobField(page, field));
						}
					}
                }
            }
        }
        return page;
    }

    private static JobFieldGroup loadCoreGroup(IP4Connection connection,
            Map<String, IJobSpecField> specFields) {
        String pagePref = BASE_FIELDS + CORE_FIELDS
                + connection.getParameters().getPort();
        if (PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .isDefault(pagePref)) {
            return loadDefaultPages(connection, CORE_FIELDS);
        } else {
            JobFieldGroup page = new JobFieldGroup(connection, CORE_FIELDS);
            fillGroup(page, pagePref, specFields);
            return page;
        }
    }

    private static JobFieldGroup loadAdvancedGroup(IP4Connection connection,
            Map<String, IJobSpecField> specFields) {
        String pagePref = BASE_FIELDS + ADVANCED_FIELDS
                + connection.getParameters().getPort();
        JobFieldGroup page = new JobFieldGroup(connection, ADVANCED_FIELDS);
        fillGroup(page, pagePref, specFields);
        return page;
    }

    private static void fillGroup(JobFieldGroup page, String pagePref,
            Map<String, IJobSpecField> specFields) {
        String pageValue = PerforceUiMylynPlugin.getDefault()
                .getPreferenceStore().getString(pagePref);
        if (pageValue.length() > 0) {
            try {
                XMLMemento memento = XMLMemento
                        .createReadRoot(new StringReader(pageValue));

                for (IMemento field : memento.getChildren(ENTRY)) {
                    IJobSpecField specField = specFields.get(field
                            .getString(NAME));
                    if (specField != null) {
                        page.add(new JobField(page, specField));
                    }
                }
            } catch (WorkbenchException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    public static JobFieldGroup loadAdvancedPage(IP4Connection connection) {
        JobFieldGroup page = null;
        if (connection != null) {
            IJobSpec spec = connection.getJobSpec();
            if (spec != null) {
                Map<String, IJobSpecField> specFields = new HashMap<String, IJobSpecField>();
                List<IJobSpecField> totalFields = new ArrayList<IJobSpecField>();
                for (IJobSpecField field : spec.getFields()) {
                    specFields.put(field.getName(), field);
                    totalFields.add(field);
                }
                page = loadAdvancedGroup(connection, specFields);

                // Check for missing fields if advanced page has fields already
                // added to it and place them on advanced page
                if (page.getFields().length > 0) {
                    for (JobField field : page.getFields()) {
                        totalFields.remove(field.getField());
                    }
                    JobFieldGroup core = loadCoreGroup(connection, specFields);
                    if(core!=null){
	                    for (JobField field : core.getFields()) {
	                        totalFields.remove(field.getField());
	                    }
                    }
                    for (IJobSpecField field : totalFields) {
                        page.add(new JobField(page, field));
                    }
                }
            }
        }
        return page;
    }

    /**
     * Load the current page for a given connection and page name
     * 
     * @param connection
     * @param pageName
     * @return - job editor page or null if page could not be loaded
     */
    public static JobFieldGroup loadPage(IP4Connection connection,
            String pageName) {
        JobFieldGroup group = null;
        if (CORE_FIELDS.equals(pageName)) {
            group = loadCorePage(connection);
        } else if (ADVANCED_FIELDS.equals(pageName)) {
            group = loadAdvancedPage(connection);
        }
        return group;
    }

    /**
     * Load the current page for a given task repository and page name
     * 
     * @param repository
     * @param pageName
     * @return - job editor page or null if page could not be loaded
     */
    public static JobFieldGroup loadPage(TaskRepository repository,
            String pageName) {
        return loadPage(P4MylynUtils.getConnection(repository), pageName);
    }

    /**
     * Save the specified pages as preferences scoped to the specified
     * connection
     * 
     * @param connection
     * @param pages
     */
    public static void savePages(IP4Connection connection, JobFieldGroup[] pages) {
        if (connection != null && pages != null) {
            for (JobFieldGroup page : pages) {
                String pagePref = BASE_FIELDS + page.getTitle()
                        + connection.getParameters().getPort();
                XMLMemento memento = XMLMemento.createWriteRoot(FIELDS);
                for (JobField field : page.getFields()) {
                    IMemento child = memento.createChild(ENTRY);
                    child.putString(NAME, field.getField().getName());
                }
                StringWriter writer = new StringWriter();
                try {
                    memento.save(writer);
                    PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                            .setValue(pagePref, writer.toString());
                } catch (IOException e) {
                    PerforceProviderPlugin.logError(e);
                }
            }
        }
    }

    private IP4Connection connection;
    private String title;
    private List<JobField> fields;
    private JobField description;
    private int index = -1;

    /**
     * Create a job editor page for a non-null connection and non-null title
     * 
     * @param connection
     * @param title
     */
    public JobFieldGroup(IP4Connection connection, String title) {
        this.connection = connection;
        this.title = title;
        fields = new ArrayList<JobField>();
    }

    /**
     * Set the index of this page
     * 
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Get the page's configured index
     * 
     * @return - index
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Get the page's associated p4 connection
     * 
     * @return - p4 connection
     */
    public IP4Connection getConnection() {
        return this.connection;
    }

    /**
     * Get the page's title
     * 
     * @return - title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Add a field to this page
     * 
     * @param field
     */
    public void add(JobField field) {
        if (field != null) {
            if (field.getField().getCode() != IP4Job.JOB_DESCRIPTION_CODE) {
                fields.add(field);
            } else {
                this.description = field;
            }
        }
    }

    /**
     * Get the index of a the specified field
     * 
     * @param field
     * @return - field index
     */
    public int indexOf(JobField field) {
        int index = -1;
        if (field != null) {
            if (field.getField().getCode() != IP4Job.JOB_DESCRIPTION_CODE) {
                index = fields.indexOf(field);
            } else {
                index = fields.size();
            }
        }
        return index;
    }

    /**
     * Add all the fields from the specified collection to this page
     * 
     * @param fields
     */
    public void addAll(Collection<JobField> fields) {
        if (fields != null) {
            this.fields.addAll(fields);
        }
    }

    /**
     * Remove the specified field from this page
     * 
     * @param field
     */
    public void remove(JobField field) {
        if (field != null) {
            fields.remove(field);
        }
    }

    /**
     * Move the specified elements to the specified index
     * 
     * @param elements
     * @param index
     */
    public void move(JobField[] elements, int index) {
        if (elements != null && index > -1) {
            // Sort fields by index in group if in same group or index of parent
            // group if in different groups
            Arrays.sort(elements, new Comparator<JobField>() {

                public int compare(JobField f1, JobField f2) {
                    if (f1.getParent() == f2.getParent()) {
                        return f1.getIndex() - f2.getIndex();
                    } else {
                        return f1.getParentIndex() - f2.getParentIndex();
                    }
                }
            });
            for (JobField field : elements) {
                int found = this.fields.indexOf(field);
                if (found > -1) {
                    if (index > found) {
                        index--;
                    }
                    this.fields.remove(found);
                }
                field.setParent(this);
                if (index > -1 && index <= this.fields.size()) {
                    this.fields.add(index, field);
                }
                index++;
            }
        }
    }

    /**
     * Get a non-null but possibly empty array of fields contained in this page
     * 
     * @return - array of job editor fields
     */
    public JobField[] getFields() {
        if (this.description == null) {
            return this.fields.toArray(new JobField[fields.size()]);
        } else {
            JobField[] jobFields = this.fields.toArray(new JobField[fields
                    .size() + 1]);
            jobFields[jobFields.length - 1] = this.description;
            return jobFields;
        }
    }
}
