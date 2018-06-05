/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.search;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.mylyn.P4MylynUtils;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.DatePicker;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.PerforceUiMylynPlugin;
import com.perforce.team.ui.mylyn.preferences.IPreferenceConstants;

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
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FilterEntry {

    /**
     * Filter class
     * 
     */
    public static class Filter {

        /**
         * Name of job spec field
         */
        public String name;

        /**
         * String operator of the filter
         */
        public String operator;

        /**
         * Raw value of the filter
         */
        public String rawValue;

        /**
         * Date type of job spec field
         */
        public String dateType;

        /**
         * Computed query from settings in filter entry widget
         */
        public String fullQuery;

        /**
         * Does the full query start with '^' indicating if it is negated
         */
        public boolean negated = false;

    }

    /**
     * Escape any comparators found in value string
     * 
     * @param value
     * @return - escape value string
     */
    public static String escapeValue(String value) {
        return P4MylynUtils.escapeJobQueryValue(value);
    }

    /**
     * DATE_OPTIONS
     */
    public static final String[] DATE_OPTIONS = new String[] {
            Messages.FilterEntry_OnOrBefore, Messages.FilterEntry_Before,
            Messages.FilterEntry_OnOrAfter, Messages.FilterEntry_After,
            Messages.FilterEntry_On };

    /**
     * DATE_OPERATORS
     */
    public static final String[] DATE_OPERATORS = new String[] { "<=", "<", //$NON-NLS-1$ //$NON-NLS-2$
            ">=", ">", "=" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    /**
     * WORD_OPTIONS
     */
    public static final String[] WORD_OPTIONS = new String[] {
            Messages.FilterEntry_GreaterThan,
            Messages.FilterEntry_GreaterThanEqual,
            Messages.FilterEntry_LessThan, Messages.FilterEntry_LessThanEqual };

    /**
     * WORD_OPERATORS
     */
    public static final String[] WORD_OPERATORS = new String[] { ">", ">=", //$NON-NLS-1$ //$NON-NLS-2$
            "<", "<=" }; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * TEXT_OPTIONS
     */
    public static final String[] TEXT_OPTIONS = new String[] {
            Messages.FilterEntry_Equals, Messages.FilterEntry_NotEquals,
            Messages.FilterEntry_Contains, Messages.FilterEntry_StartsWith,
            Messages.FilterEntry_EndsWith };

    private Combo field;
    private Combo modifier;
    private Combo value;
    private ToolItem add;
    private ToolItem remove;
    private DatePicker date;

    private IJobSpec spec;
    private IJobSpecField[] fields;
    private Map<String, String[]> sortedValues;
    private FilterManager manager;

    private ListenerList listeners = new ListenerList();

    /**
     * Create the filter entry around the specified job spec
     * 
     * @param manager
     * 
     * @param spec
     */
    public FilterEntry(FilterManager manager, IJobSpec spec) {
        this.manager = manager;
        this.spec = spec;
		List<IJobSpecField> jobFields = this.spec != null ? this.spec
				.getFields() : new ArrayList<IJobSpecField>();
        this.fields = jobFields.toArray(new IJobSpecField[jobFields.size()]);
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
     * Set the operator for this filter
     * 
     * @param operator
     */
    public void setOperator(String operator) {
        if (operator != null) {
            updateModifiers();
            this.modifier.setText(operator);
        }
    }

    /**
     * Set the value for this filter
     * 
     * @param value
     */
    public void setValue(String value) {
        if (value != null) {
            updateValues();
            this.value.setText(value);
        }
    }

    private void updateFields() {
        field.removeAll();
        for (IJobSpecField field : fields) {
            this.field.add(field.getName());
        }
    }

    private void updateModifiers() {
        int index = field.getSelectionIndex();
        modifier.removeAll();
        if (index >= 0) {
            IJobSpecField selected = fields[index];
            String data = selected.getDataType();
            if (IP4Job.DATE_DATA_TYPE.equals(data)) {
                date.setVisible(true);
                for (String option : DATE_OPTIONS) {
                    modifier.add(option);
                }
            } else {
                date.setVisible(false);
                if (IP4Job.WORD_DATA_TYPE.equals(data)) {
                    for (String option : WORD_OPTIONS) {
                        modifier.add(option);
                    }
                }
                for (String option : TEXT_OPTIONS) {
                    modifier.add(option);
                }
            }
            modifier.setEnabled(true);
        } else {
            modifier.add(Messages.FilterEntry_SelectField);
            modifier.setEnabled(false);
        }
    }

    private boolean shouldSortValues() {
        return PerforceUiMylynPlugin.getDefault().getPreferenceStore()
                .getBoolean(IPreferenceConstants.SORT_JOB_VALUES);
    }

    private void updateValues() {
        value.removeAll();
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
                    this.value.add(value);
                }
            }
        }
    }

    private void formatText(StringBuilder filter, String name, String value,
            int modifier) {
        filter.append(name);
        switch (modifier) {
        case 0: // Equals
            filter.append('=');
            filter.append(value);
            break;
        case 1: // Not equals
            filter.insert(0, '^');
            filter.append('=');
            filter.append(value);
            break;
        case 2: // Contains
            filter.append('=');
            filter.append('*');
            filter.append(value);
            filter.append('*');
            break;
        case 3: // Starts with
            filter.append('=');
            filter.append(value);
            filter.append('*');
            break;
        case 4: // Ends with
            filter.append('=');
            filter.append('*');
            filter.append(value);
            break;
        default:
            break;
        }
    }

    /**
     * Get filter
     * 
     * @return - filter string
     */
    public Filter getFilter() {
        Filter filter = null;
        String name = null;
        String operator = null;
        String value = this.value.getText().trim();
        if (value.length() > 0) {
            int index = field.getSelectionIndex();
            int modIndex = modifier.getSelectionIndex();
            IJobSpecField selected = null;
            boolean isText = false;
			if (index >= 0) {
				filter = new Filter();
				filter.rawValue = value;
				value = escapeValue(value);
				selected = fields[index];
				if (selected != null) {
					name = selected.getName();
					String data = selected.getDataType();
					if (modIndex >= 0) {
						filter.operator = modifier.getText();
						if (IP4Job.DATE_DATA_TYPE.equals(data)) {
							operator = DATE_OPERATORS[modIndex];
						} else if (IP4Job.WORD_DATA_TYPE.equals(data)) {
							if (modIndex < WORD_OPERATORS.length) {
								operator = WORD_OPERATORS[modIndex];
							} else {
								modIndex -= WORD_OPERATORS.length;
								isText = true;
							}
						} else {
							isText = true;
						}
					}

					StringBuilder filterQuery = new StringBuilder();
					if (name != null && value != null) {
						if (!isText) {
							if (operator != null) {
								filterQuery.append(name);
								filterQuery.append(operator);
								filterQuery.append(value);
							}
						} else {
							formatText(filterQuery, name, value, modIndex);
						}
					}
					filter.name = name;
					filter.dateType = selected.getDataType();
					filter.fullQuery = filterQuery.toString();
					if (filter.fullQuery.startsWith("^")) { //$NON-NLS-1$
						filter.negated = true;
					}
				}
            }
        }
        return filter;
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
                manager.remove(FilterEntry.this);
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
                FilterEntry entry = new FilterEntry(manager, spec);
                entry.createControl(displayArea, layoutParent, true);
                layoutParent.layout(true, true);
                manager.add(entry);
            }

        });
    }

    /**
     * Create the filter entry
     * 
     * @param parent
     * @param layoutParent
     * @param useParent
     */
    public void createControl(Composite parent, final Composite layoutParent,
            final boolean useParent) {
        if (!useParent) {
            parent = new Composite(parent, SWT.NONE);
            GridLayout daLayout = new GridLayout(1, true);
            daLayout.marginHeight = 0;
            daLayout.marginWidth = 0;
            parent.setLayout(daLayout);
            GridData daData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            parent.setLayoutData(daData);
        }

        final Composite displayArea = parent;
        final Composite filter = new Composite(displayArea, SWT.NONE);
        GridLayout fLayout = new GridLayout(5, false);
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
                updateModifiers();
                updateValues();
                int index = field.getSelectionIndex();
                if (index >= 0) {
                    IJobSpecField selected = fields[index];
                    String data = selected.getDataType();
                    int modIndex = modifier.getSelectionIndex();
                    if (modIndex == -1) {
                        if (IP4Job.DATE_DATA_TYPE.equals(data)) {
                            modifier.select(2);
                        } else if (IP4Job.WORD_DATA_TYPE.equals(data)) {
                            modifier.select(4);
                        } else {
                            modifier.select(0);
                        }
                    }
                }
                notifyListeners();
            }

        });

        modifier = new Combo(filter, SWT.READ_ONLY | SWT.DROP_DOWN);
        GridData mData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        GC gc = new GC(modifier.getDisplay());
        try {
            Point width = gc.stringExtent(WORD_OPTIONS[1]);
            mData.widthHint = width.x + 50;
        } finally {
            gc.dispose();
        }
        modifier.setLayoutData(mData);
        modifier.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyListeners();
            }
        });
        updateModifiers();

        date = new DatePicker();
        date.createControl(filter);
        date.setVisible(false);

        value = new Combo(filter, SWT.DROP_DOWN);
        value.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        value.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                notifyListeners();
            }
        });
        date.setCombo(value);

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
