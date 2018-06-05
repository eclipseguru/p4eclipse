/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mylyn.search;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.ui.BaseErrorProvider;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.mylyn.search.FilterEntry.Filter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FilterManager extends BaseErrorProvider {

    /**
     * Default fields to show when filter manager is empty
     */
    public static final int DEFAULT_FIELDS = 3;

    /**
     * FILTER_FIELD
     */
    public static final String FILTER_FIELD = "filter"; //$NON-NLS-1$

    /**
     * OPERATOR_FIELD
     */
    public static final String OPERATOR_FIELD = "operator"; //$NON-NLS-1$

    /**
     * ENTRY_FIELD
     */
    public static final String ENTRY_FIELD = "entry"; //$NON-NLS-1$

    /**
     * NAME_FIELD
     */
    public static final String NAME_FIELD = "name"; //$NON-NLS-1$

    /**
     * VALUE_FIELD
     */
    public static final String VALUE_FIELD = "value"; //$NON-NLS-1$

    /**
     * WORDS_FIELD
     */
    public static final String WORDS_FIELD = "words"; //$NON-NLS-1$

    /**
     * WORDS_TYPE_FIELD
     */
    public static final String WORDS_TYPE_FIELD = "wordsType"; //$NON-NLS-1$

    /**
     * ALL_TYPE
     */
    public static final String ALL_TYPE = "all"; //$NON-NLS-1$

    /**
     * ANY_TYPE
     */
    public static final String ANY_TYPE = "any"; //$NON-NLS-1$

    private IJobSpec spec;
    private ScrolledComposite scrolls;
    private Composite displayArea;
    private Text wordsText;
    private Combo wordsMatchCombo;
    private List<FilterEntry> filters = new ArrayList<FilterEntry>();
    private ListenerList listeners = new ListenerList();

    private Listener listener = new Listener() {

        public void handleEvent(Event event) {
            FilterEntry entry = (FilterEntry) event.data;
            for (Object listener : listeners.getListeners()) {
                ((IFilterListener) listener).modified(entry);
            }
            checkQuery();
        }
    };

    private String getWords() {
        return wordsText.getText().trim();
    }

    private void checkQuery() {
        boolean querySpecified = getWords().length() > 0;
        boolean positiveMatch = querySpecified;
        if (!positiveMatch) {
            for (FilterEntry entry : this.filters) {
                Filter filter = entry.getFilter();
                if (filter != null && filter.fullQuery != null) {
                    querySpecified = true;
                    if (!filter.negated) {
                        positiveMatch = true;
                        break;
                    }
                }
            }
        }
        if (querySpecified && !positiveMatch) {
            errorMessage = Messages.FilterManager_NoPositiveRule;
        } else {
            errorMessage = null;
        }
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
            FilterEntry entry = new FilterEntry(this, spec);
            entry.createControl(displayArea);
            add(entry);
        }
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
        displayArea = new Composite(scrolls, SWT.NONE);
        scrolls.setContent(displayArea);
        GridData sData = new GridData(SWT.FILL, SWT.FILL, true, true);
        sData.heightHint = 150;
        scrolls.setLayoutData(sData);
        displayArea.setLayout(new GridLayout(1, false));
        GridData baData = new GridData(SWT.FILL, SWT.FILL, true, true);
        baData.horizontalIndent = 15;
        displayArea.setLayoutData(baData);

        Composite wordsArea = new Composite(displayArea, SWT.NONE);
        GridLayout waLayout = new GridLayout(5, false);
        waLayout.marginHeight = 0;
        waLayout.marginWidth = 0;
        wordsArea.setLayout(waLayout);
        wordsArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        ModifyListener wordsListener = new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String text = getWords();
                for (Object listener : listeners.getListeners()) {
                    ((IFilterListener) listener).wordsModified(text);
                }
                checkQuery();
            }
        };

        Label wordsLabel = new Label(wordsArea, SWT.NONE);
        wordsLabel.setText(Messages.FilterManager_Containing);
        wordsMatchCombo = new Combo(wordsArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        wordsMatchCombo.add(Messages.FilterManager_all);
        wordsMatchCombo.add(Messages.FilterManager_any);
        wordsMatchCombo.select(0);
        wordsMatchCombo.addModifyListener(wordsListener);
        Label wordsSuffixLabel = new Label(wordsArea, SWT.NONE);
        wordsSuffixLabel.setText(Messages.FilterManager_FollowingWords);

        wordsText = new Text(wordsArea, SWT.SINGLE | SWT.BORDER);
        wordsText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        wordsText.addModifyListener(wordsListener);

        ToolBar wordBar = new ToolBar(wordsArea, SWT.WRAP);
        ToolItem clearWordsItem = new ToolItem(wordBar, SWT.PUSH);
        Image clearImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_CLEAR).createImage();
        P4UIUtils.registerDisposal(clearWordsItem, clearImage);
        clearWordsItem.setImage(clearImage);
        clearWordsItem.setToolTipText(Messages.FilterManager_ClearWords);
        clearWordsItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                wordsText.setText(""); //$NON-NLS-1$
            }

        });

        Label rulesLabel = new Label(displayArea, SWT.NONE);
        rulesLabel.setText(Messages.FilterManager_MatchingConditions);

        this.spec = connection.getJobSpec();
    }

    /**
     * Get full query from every {@link FilterEntry}
     * 
     * @return - string query
     */
    public String getQuery() {
        StringBuilder fullQuery = new StringBuilder();
        Map<String, List<Filter>> ors = new TreeMap<String, List<Filter>>();

        // Initially add date and negated queries
        for (FilterEntry entry : this.filters) {
            Filter filter = entry.getFilter();
            if (filter != null && filter.fullQuery != null) {
                if (!filter.negated
                        && !IP4Job.DATE_DATA_TYPE.equals(filter.dateType)) {
                    List<Filter> filters = null;
                    if (ors.containsKey(filter.name)) {
                        filters = ors.get(filter.name);
                    } else {
                        filters = new ArrayList<Filter>();
                        ors.put(filter.name, filters);
                    }
                    filters.add(filter);
                } else {
                    if (fullQuery.length() > 0) {
                        fullQuery.append(' ');
                    }
                    fullQuery.append(filter.fullQuery);
                }
            }
        }

        // Add 'or' queries at current query beginning
        StringBuilder orsQuery = new StringBuilder();
        for (List<Filter> filters : ors.values()) {
            if (orsQuery.length() > 0) {
                orsQuery.append(' ');
            }
            if (filters.size() > 1) {
                orsQuery.append('(');
                orsQuery.append(filters.get(0).fullQuery);
                for (int i = 1; i < filters.size(); i++) {
                    orsQuery.append('|');
                    orsQuery.append(filters.get(i).fullQuery);
                }
                orsQuery.append(')');
            } else {
                orsQuery.append(filters.get(0).fullQuery);
            }
        }
        if (fullQuery.length() > 0) {
            fullQuery.insert(0, ' ');
        }
        fullQuery.insert(0, orsQuery);

        // Add word queries at current query beginning
        String words = getWords();
        if (words.length() > 0) {
            String separator = this.wordsMatchCombo.getSelectionIndex() == 0
                    ? " " //$NON-NLS-1$
                    : " | "; //$NON-NLS-1$
            StringBuilder wordsSection = new StringBuilder();
            boolean multiple = false;
            for (String word : words.split(" ")) { //$NON-NLS-1$
                if (word.length() > 0) {
                    if (wordsSection.length() > 0) {
                        wordsSection.append(separator);
                        multiple = true;
                    }
                    wordsSection.append(FilterEntry.escapeValue(word.trim()));
                }
            }
            if (wordsSection.length() > 0) {
                if (fullQuery.length() > 0) {
                    fullQuery.insert(0, ' ');
                }
                if (multiple) {
                    wordsSection.insert(0, '(');
                    wordsSection.append(')');
                }
                fullQuery.insert(0, wordsSection);
            }

        }
        return fullQuery.toString();
    }

    /**
     * Convert all entries to a storable string
     * 
     * @return - storable string of all filter entries
     */
    public String toStorageString() {
        StringWriter writer = new StringWriter();
        XMLMemento memento = XMLMemento.createWriteRoot(FILTER_FIELD);
        for (FilterEntry entry : this.filters) {
            Filter filter = entry.getFilter();
            if (filter != null) {
                IMemento child = memento.createChild(ENTRY_FIELD);
                child.putString(NAME_FIELD, filter.name);
                child.putString(OPERATOR_FIELD, filter.operator);
                child.putString(VALUE_FIELD, filter.rawValue);
            }
        }
        memento.putString(WORDS_FIELD, getWords());
        if (wordsMatchCombo.getSelectionIndex() == 0) {
            memento.putString(WORDS_TYPE_FIELD, ALL_TYPE);
        } else {
            memento.putString(WORDS_TYPE_FIELD, ANY_TYPE);
        }
        try {
            memento.save(writer);
        } catch (IOException e) {
            PerforceProviderPlugin.logError(e);
        }
        return writer.toString();
    }

    /**
     * Convert the specified string to ui filter entry elements
     * 
     * @param storageString
     * 
     */
    public void fromStorageString(String storageString) {
        if (storageString != null) {
            try {
                XMLMemento memento = XMLMemento
                        .createReadRoot(new StringReader(storageString));
                for (IMemento child : memento.getChildren(ENTRY_FIELD)) {
                    String name = child.getString(NAME_FIELD);
                    String operator = child.getString(OPERATOR_FIELD);
                    String value = child.getString(VALUE_FIELD);
                    if (name != null && operator != null && value != null) {
                        FilterEntry filter = new FilterEntry(this, spec);
                        filter.createControl(displayArea);
                        filter.setName(name);
                        filter.setOperator(operator);
                        filter.setValue(value);
                        add(filter);
                    }
                }
                if (filters.size() < DEFAULT_FIELDS) {
                    for (int i = filters.size(); i < DEFAULT_FIELDS; i++) {
                        FilterEntry entry = new FilterEntry(this, spec);
                        entry.createControl(displayArea);
                        add(entry);
                    }
                }
                String words = memento.getString(WORDS_FIELD);
                if (words != null && words.length() > 0) {
                    wordsText.setText(words);
                }
                String wordsType = memento.getString(WORDS_TYPE_FIELD);
                if (ANY_TYPE.equals(wordsType)) {
                    wordsMatchCombo.select(1);
                }
            } catch (WorkbenchException e) {
                PerforceProviderPlugin.logError(e);
            }
        }
    }

    /**
     * Remove a filter entry to this manager
     * 
     * @param entry
     */
    public void add(FilterEntry entry) {
        if (entry != null) {
            if (filters.size() == 1) {
                filters.get(0).setRemoveEnabled(true);
            }
            filters.add(entry);
            entry.addListener(this.listener);
            entry.setRemoveEnabled(filters.size() > 1);
            scrolls.setMinSize(displayArea
                    .computeSize(SWT.DEFAULT, SWT.DEFAULT));
            for (Object listener : this.listeners.getListeners()) {
                ((IFilterListener) listener).added(entry);
            }
        }
    }

    /**
     * Remove a filter entry from this manager
     * 
     * @param entry
     */
    public void remove(FilterEntry entry) {
        if (entry != null) {
            if (filters.remove(entry)) {
                entry.removeListener(this.listener);
                if (filters.size() == 1) {
                    filters.get(0).setRemoveEnabled(false);
                }
                scrolls.setMinSize(displayArea.computeSize(SWT.DEFAULT,
                        SWT.DEFAULT));
                for (Object listener : this.listeners.getListeners()) {
                    ((IFilterListener) listener).removed(entry);
                }
                checkQuery();
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
