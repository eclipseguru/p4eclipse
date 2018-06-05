/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.job;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.DatePicker;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.job.JobFieldEntry.FieldChange.Type;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;
import com.perforce.team.ui.mylyn.search.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class JobFieldEntry {

    /**
     * Field change
     */
    public static class FieldChange {

        enum Type {
            APPEND, PREPEND, REPLACE,
        }

        /**
         * Name of job spec field
         */
        public String name;

        /**
         * Value of the field
         */
        public String value;

        /**
         * Change type
         */
        public Type type = Type.REPLACE;

    }

    /**
     * TEXT_OPTIONS
     */
    public static final String[] TEXT_OPTIONS = new String[] {
            Messages.JobFieldEntry_Append, Messages.JobFieldEntry_Prepend,
            Messages.JobFieldEntry_Replace };

    private Composite displayArea;
    private Combo field;
    private Combo modifier;
    private Text textValue;
    private Combo selectValue;
    private ToolItem add;
    private ToolItem remove;
    private DatePicker date;

    private IJobSpec spec;
    private IJobSpecField[] fields;
    private Map<String, String[]> sortedValues;
    private JobFieldManager manager;

    private ListenerList listeners = new ListenerList();

    /**
     * Create the filter entry around the specified job spec
     * 
     * @param manager
     * 
     * @param spec
     */
    public JobFieldEntry(JobFieldManager manager, IJobSpec spec) {
        this.manager = manager;
        this.spec = spec;
        List<IJobSpecField> jobFields = this.spec.getFields();
        List<IJobSpecField> editableFields = new ArrayList<IJobSpecField>();
        for (IJobSpecField field : jobFields) {
            String fieldType = field.getFieldType();
            if (IP4Job.JOB_NAME_CODE != field.getCode()
                    && !IP4Job.ONCE_FIELD_TYPE.equals(fieldType)
                    && !IP4Job.ALWAYS_FIELD_TYPE.equals(fieldType)) {
                editableFields.add(field);
            }
        }
        this.fields = editableFields.toArray(new IJobSpecField[editableFields
                .size()]);
        Arrays.sort(this.fields, new Comparator<IJobSpecField>() {

            public int compare(IJobSpecField o1, IJobSpecField o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
        this.sortedValues = new HashMap<String, String[]>();
    }

    /**
     * Set the name for this filter
     * 
     * @param name
     */
    public void setName(String name) {
        if (name != null) {
            this.field.setText(name);
        }
    }

    /**
     * /** Set the value for this filter
     * 
     * @param value
     */
    public void setValue(String value) {
        if (value != null) {
            updateValues();
            this.selectValue.setText(value);
        }
    }

    private void updateFields() {
        field.removeAll();
        for (IJobSpecField field : fields) {
            this.field.add(field.getName());
        }
    }

    private boolean shouldSortValues() {
        return PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SORT_JOB_VALUES);
    }

    private void updateValues() {
        selectValue.removeAll();
        int index = field.getSelectionIndex();
        if (index >= 0) {
            IJobSpecField selected = fields[index];
            String data = selected.getDataType();
            if (IP4Job.SELECT_DATA_TYPE.equals(data)) {
                String[] sorted = this.sortedValues.get(selected.getName());
                if (sorted == null) {
                    List<String> values = spec.getFieldValues(selected
                            .getName());
                    sorted = values.toArray(new String[values.size()]);
                    if (shouldSortValues()) {
                        Arrays.sort(sorted, new Comparator<String>() {

                            public int compare(String o1, String o2) {
                                return o1.compareToIgnoreCase(o2);
                            }
                        });
                    }
                    this.sortedValues.put(selected.getName(), sorted);
                }
                for (String value : sorted) {
                    this.selectValue.add(value);
                }
            }
        }
    }

    /**
     * Get filter
     * 
     * @return - filter string
     */
    public FieldChange getFilter() {
        FieldChange filter = null;
        String name = null;
        String value = null;
        if (this.selectValue.isVisible()) {
            value = this.selectValue.getText().trim();
        } else if (this.textValue.isVisible()) {
            value = this.textValue.getText();
        }
        if (value != null) {
            int index = field.getSelectionIndex();
            IJobSpecField selected = null;
            if (index >= 0) {
                filter = new FieldChange();
                selected = fields[index];
                name = selected.getName();
                filter.name = name;
                filter.value = value;
                if (modifier.isVisible()) {
                    int modIndex = modifier.getSelectionIndex();
                    switch (modIndex) {
                    case 0:
                        filter.type = Type.APPEND;
                        break;
                    case 1:
                        filter.type = Type.PREPEND;
                        break;
                    case 2:
                        filter.type = Type.REPLACE;
                        break;

                    default:
                        break;
                    }
                }
            }
        }
        return filter;
    }

    private void updateModifiers() {
        int index = field.getSelectionIndex();
        if (index >= 0) {
            IJobSpecField selected = fields[index];
            String data = selected.getDataType();
            boolean isText = IP4Job.TEXT_DATA_TYPE.equals(data);
            boolean isDate = !isText && IP4Job.DATE_DATA_TYPE.equals(data);
            selectValue.setVisible(!isText);
            ((GridData) selectValue.getLayoutData()).exclude = isText;
            date.setVisible(isDate);
            modifier.setVisible(isText);
            ((GridData) modifier.getLayoutData()).exclude = !isText;
            textValue.setVisible(isText);
            ((GridData) textValue.getLayoutData()).exclude = !isText;
            layout();
        }
    }

    private void createButtonArea(final Composite parent,
            final Composite displayArea, final Composite layoutParent) {
        final ToolBar buttonArea = new ToolBar(parent, SWT.FLAT);
        GridLayout baLayout = new GridLayout(2, true);
        baLayout.marginHeight = 0;
        baLayout.marginWidth = 0;
        baLayout.horizontalSpacing = 0;
        buttonArea.setLayout(baLayout);

        remove = new ToolItem(buttonArea, SWT.PUSH | SWT.FLAT);
        Image removeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();
        P4UIUtils.registerDisposal(remove, removeImage);
        remove.setImage(removeImage);
        remove.setToolTipText(Messages.FilterEntry_RemoveFilter);
        remove.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                parent.setVisible(false);
                ((GridData) parent.getLayoutData()).exclude = true;
                boolean allInvisible = true;
                for (Control control : displayArea.getChildren()) {
                    if (control.getVisible()) {
                        allInvisible = false;
                        break;
                    }
                }
                if (allInvisible) {
                    displayArea.setVisible(false);
                    ((GridData) displayArea.getLayoutData()).exclude = true;
                }
                layoutParent.layout(true, true);
                manager.remove(JobFieldEntry.this);
                notifyListeners();
            }

        });

        add = new ToolItem(buttonArea, SWT.PUSH | SWT.FLAT);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(add, addImage);
        add.setImage(addImage);
        add.setToolTipText(Messages.FilterEntry_AddFilter);
        add.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                JobFieldEntry entry = new JobFieldEntry(manager, spec);
                entry.createControl(displayArea, layoutParent, true);
                layoutParent.layout(true, true);
                manager.add(entry);
            }

        });
    }

    private void layout() {
        displayArea.getParent().layout(true, true);
    }

    /**
     * Create the filter entry
     * 
     * @param parent
     * @param layoutParent
     * @param useParent
     */
    public void createControl(Composite parent, final Composite layoutParent,
            boolean useParent) {
        if (!useParent) {
            parent = new Composite(parent, SWT.NONE);
            GridLayout daLayout = new GridLayout(1, true);
            daLayout.marginHeight = 0;
            daLayout.marginWidth = 0;
            parent.setLayout(daLayout);
            GridData daData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            parent.setLayoutData(daData);
        }

        displayArea = parent;
        final Composite filter = new Composite(displayArea, SWT.NONE);
        GridLayout fLayout = new GridLayout(6, false);
        fLayout.marginHeight = 0;
        fLayout.marginWidth = 0;
        filter.setLayout(fLayout);
        GridData fData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        filter.setLayoutData(fData);

        field = new Combo(filter, SWT.READ_ONLY | SWT.DROP_DOWN);
        updateFields();
        field.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                updateValues();
                updateModifiers();
                notifyListeners();
            }

        });

        date = new DatePicker();
        date.createControl(filter);
        date.setVisible(false);

        modifier = new Combo(filter, SWT.READ_ONLY | SWT.DROP_DOWN);
        for (String option : TEXT_OPTIONS) {
            modifier.add(option);
        }
        modifier.select(0);
        GridData mData = new GridData();
        mData.exclude = true;
        modifier.setLayoutData(mData);
        modifier.setVisible(false);

        textValue = new Text(filter, SWT.MULTI | SWT.BORDER);
        textValue.setVisible(false);
        GridData tvData = new GridData(SWT.FILL, SWT.FILL, true, false);
        tvData.heightHint = P4UIUtils
                .computePixelHeight(textValue.getFont(), 5);
        tvData.exclude = true;
        textValue.setLayoutData(tvData);

        selectValue = new Combo(filter, SWT.DROP_DOWN);
        selectValue
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        selectValue.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyListeners();
            }
        });
        date.setCombo(selectValue);

        createButtonArea(filter, displayArea, layoutParent);
    }

    /**
     * Create the filter entry
     * 
     * @param parent
     */
    public void createControl(final Composite parent) {
        createControl(parent, parent, false);
    }

    /**
     * Set remove button enabled or disabled
     * 
     * @param enabled
     */
    public void setRemoveEnabled(boolean enabled) {
        this.remove.setEnabled(enabled);
    }

    private void notifyListeners() {
        Event event = new Event();
        event.data = this;
        for (Object listener : this.listeners.getListeners()) {
            ((Listener) listener).handleEvent(event);
        }
    }

    /**
     * Add a listener to this entry
     * 
     * @param listener
     */
    public void addListener(Listener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Remove a listener from this entry
     * 
     * @param listener
     */
    public void removeListener(Listener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

}
