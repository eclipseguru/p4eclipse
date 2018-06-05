package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.perforce.p4java.core.IJobSpec;
import com.perforce.p4java.core.IJobSpec.IJobSpecField;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Job;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.viewer.FilterViewer;
import com.perforce.team.ui.views.JobSorter;
import com.perforce.team.ui.views.PerforceProjectView;

/**
 * Jobs dialog
 */
public class JobsDialog extends FilterViewer implements
        ISelectionChangedListener, IPropertyChangeListener {

    /**
     * FILE_FOLDER_HISTORY
     */
    public static final String FILE_FOLDER_HISTORY = "com.perforce.team.ui.jobs.FILE_FOLDER_HISTORY"; //$NON-NLS-1$

    /**
     * KEYWORD_HISTORY
     */
    public static final String KEYWORD_HISTORY = "com.perforce.team.ui.jobs.KEYWORD_HISTORY"; //$NON-NLS-1$

    // View sorter
    private JobSorter sorter;

    // Job list pane
    private TableViewer jobsList;

    // Current connection
    private IP4Connection connection;

    private String[] displayColumns;

    // Composite containing view controls
    private Composite viewComposite;

    private Link showMore;
    private Label folderFileLabel;
    private Combo folderFileCombo;
    private ToolBar folderFileBar;
    private ToolItem folderFileClearItem;
    private Label keywordLabel;
    private Combo keywordCombo;
    private ToolBar keywordBar;
    private ToolItem keywordClearItem;

    private IJobDoubleClickListener doubleListener = null;

    private boolean forceKeywordFocus = false;

    private SashForm sash;

    private Label longestLabel;
    private Text[] detailFields;
    private ScrolledComposite detailPanel;

    private int retrieveCount = 0;
    private boolean isLoading = false;
    private boolean autoSelectFirstEntry = false;

    private IP4Job selectedJob = null;
    private IP4Job[] jobs = null;
    private Object loading = new Object();
    private Image loadingImage;
    private Image clearImage;

    /**
     * COLUMNS_PREFIX - memento key for columns shown
     */
    public static final String COLUMNS_PREFIX = "P4ECLIPSE-Columns-"; //$NON-NLS-1$

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.jobcolumns"; //$NON-NLS-1$

    /**
     * Label provider for the job list
     */
    private class JobLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        // The job image
        private Image jobImage = null;

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex) {
            if (columnIndex == 0) {
                if (element == loading) {
                    if (loadingImage == null) {
                        loadingImage = getLoadingImage();
                    }
                    return loadingImage;
                }
                if (jobImage == null) {
                    jobImage = getJobImage();
                }
                return jobImage;
            } else {
                return null;
            }
        }

        /**
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            if (jobImage != null && !jobImage.isDisposed()) {
                jobImage.dispose();
                jobImage = null;
            }
            if (loadingImage != null && !loadingImage.isDisposed()) {
                loadingImage.dispose();
                loadingImage = null;
            }
            if (clearImage != null && !clearImage.isDisposed()) {
                clearImage.dispose();
                clearImage = null;
            }
        }

        private Image getLoadingImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_LOADING)
                    .createImage();
        }

        private Image getJobImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_CHG_JOB)
                    .createImage();
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element == loading && columnIndex == 0) {
                return Messages.JobsDialog_Loading;
            } else if (element instanceof IP4Job) {
                IP4Job job = (IP4Job) element;
                String[] columns = displayColumns;
                if (columnIndex < columns.length) {
                    String name = columns[columnIndex];
                    Object value = job.getField(name);
                    if (value != null) {
                        return P4CoreUtils.removeWhitespace(value.toString());
                    }
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Adds a job to the dialog, must be called from ui-thread.
     * 
     * @param job
     */
    public void addJob(IP4Job job) {
        if (job != null) {
            if (jobs != null) {
                IP4Job[] newJobs = new IP4Job[jobs.length + 1];
                newJobs[0] = job;
                System.arraycopy(jobs, 0, newJobs, 1, jobs.length);
                jobs = newJobs;
            } else {
                jobs = new IP4Job[] { job };
            }
            this.jobsList.insert(job, 0);
            this.jobsList.setSelection(new StructuredSelection(job), true);
        }
    }

    /**
     * Shows the need amount of jobs set to be retrieved
     */
    public void showMore() {
        if (retrieveCount != -1) {
            retrieveCount += getMaxJobs();
        }
        loadJobs();
    }

    private void createMoreLink(Composite parent) {
        showMore = new Link(parent, SWT.PUSH);
        showMore.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showMore();
            }

        });
        showMore.setLayoutData(new GridData(SWT.END, SWT.FILL, true, false));
        refreshRetrieveCount();
        updateMoreButton(false);
    }

    /**
     * Capture changes to jobs preferences
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS.equals(property)) {
            retrieveCount = getMaxJobs();
            updateMoreButton();
            loadJobs();
        }
    }

    /**
     * Create job table from connection
     * 
     * @param connection
     */
    public void createJobTable(IP4Connection connection) {
        this.connection = connection;

        Table table = jobsList.getTable();

        // Dispose of old columns and re-build table
        saveColumnSizes();

        table.setRedraw(false);
        try {
            for (TableColumn column : table.getColumns()) {
                column.dispose();
            }

            TableLayout layout = new TableLayout();
            table.setLayout(layout);

            initDisplayColumns();
            Map<String, Integer> columnSizes = loadColumnSizes();
            String[] columns = displayColumns;

            SelectionListener headerListener = new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent e) {
                    // column selected - need to sort
                    TableColumn column = (TableColumn) e.widget;
                    sorter.setSortColumn(column.getText());
                    jobsList.refresh();
                }
            };

            for (int i = 0; i < columns.length; i++) {
                TableColumn col = new TableColumn(table, SWT.NONE);
                col.setResizable(true);
                col.setText(columns[i]);
                int width = 100;
                if (columnSizes.containsKey(columns[i])) {
                    int size = columnSizes.get(columns[i]).intValue();
                    if (size > 0) {
                        width = size;
                    }
                }
                layout.addColumnData(new ColumnPixelData(width, true));
                col.addSelectionListener(headerListener);
            }

            if (table.getColumnCount() > 0) {
                TableColumn first = table.getColumn(0);
                table.setSortColumn(first);
                table.setSortDirection(SWT.DOWN);
            }

            sorter = new JobSorter(this.jobsList.getTable(), columns.length > 0
                    ? columns[0]
                    : ""); //$NON-NLS-1$
            jobsList.setSorter(sorter);

            if (detailPanel != null && detailPanel.getContent() != null) {
                detailPanel.getContent().dispose();
                createDetails(detailPanel, table.getBackground());
            }
        } finally {
            table.setRedraw(true);
        }
        loadJobs();
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     *            the parent window
     * @param connection
     * @param multiSelect
     * @param displayDetails
     * @return the main composite control
     */
    public Composite createControl(Composite parent, IP4Connection connection,
            boolean multiSelect, boolean displayDetails) {
        Composite composite = createControl(parent, multiSelect, displayDetails);
        createJobTable(connection);
        return composite;
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     *            the parent window
     * @param multiSelect
     * @param displayDetails
     * @return the main composite control
     */
    public Composite createControl(Composite parent, boolean multiSelect,
            boolean displayDetails) {

        PerforceUIPlugin.getPlugin().getPreferenceStore()
                .addPropertyChangeListener(this);

        sash = DialogUtils.createSash(parent);

        viewComposite = new Composite(sash, SWT.NONE);
        GridLayout gl = new GridLayout();
        gl.marginHeight = 0;
        gl.marginWidth = 0;
        gl.verticalSpacing = 0;
        viewComposite.setLayout(gl);

        createFilterArea(viewComposite);

        createMoreLink(viewComposite);

        Table table = createTable(viewComposite, multiSelect);

        detailPanel = createDetailPanel(sash, table.getBackground());

        updateSash(displayDetails);

        return sash;
    }

    /**
     * Sets the path for the folder/file filter
     * 
     * @param folderFilePath
     */
    public void setPath(String folderFilePath) {
        if (okToUse()) {
            if (folderFilePath == null) {
                folderFilePath = ""; //$NON-NLS-1$
            }
            folderFileCombo.setText(folderFilePath);
            loadJobs();
        }
    }

    private void updateMoreButton(boolean layout) {
        if (showMore != null && !showMore.isDisposed()) {
            int max = getMaxJobs();
            if (max == -1) {
                showMore.setText(Messages.JobsDialog_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(MessageFormat.format(
                        Messages.JobsDialog_ShowNumMore, max));
                showMore.setEnabled(true);
            }
            if (layout) {
                showMore.getParent().layout(new Control[] { showMore });
            }
        }
    }

    private void updateMoreButton() {
        updateMoreButton(true);
    }

    private void createFilterArea(Composite parent) {
        filterComposite = new Composite(parent, SWT.NONE);
        GridLayout fcLayout = new GridLayout(3, false);
        filterComposite.setLayout(fcLayout);
        filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        final SelectionListener comboAdapter = P4UIUtils
                .createComboSelectionListener(new Runnable() {

                    public void run() {
                        loadJobs();
                    }
                });

        keywordLabel = new Label(filterComposite, SWT.LEFT);
        keywordLabel.setText(Messages.JobsDialog_Keywords);
        keywordLabel.setLayoutData(new GridData(SWT.END, SWT.CENTER, false,
                false));
        keywordCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] keywords = PerforceProjectView.getItems(KEYWORD_HISTORY);
        for (String keyword : keywords) {
            keywordCombo.add(keyword);
        }
        keywordCombo
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        keywordCombo.addSelectionListener(comboAdapter);

        clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();

        keywordBar = new ToolBar(filterComposite, SWT.FLAT);
        keywordClearItem = new ToolItem(keywordBar, SWT.PUSH);
        keywordClearItem.setToolTipText(Messages.JobsDialog_ClearKeywordFilter);
        keywordClearItem.setImage(clearImage);
        keywordClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                keywordCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(e);
            }

        });

        folderFileLabel = new Label(filterComposite, SWT.LEFT);
        folderFileLabel.setText(Messages.JobsDialog_FolderFile);
        folderFileCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] folders = PerforceProjectView.getItems(FILE_FOLDER_HISTORY);
        for (String folder : folders) {
            folderFileCombo.add(folder);
        }
        folderFileCombo.addSelectionListener(comboAdapter);
        folderFileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        folderFileBar = new ToolBar(filterComposite, SWT.FLAT);
        folderFileClearItem = new ToolItem(folderFileBar, SWT.PUSH);
        folderFileClearItem.setToolTipText(Messages.JobsDialog_ClearFolderFile);
        folderFileClearItem.setImage(clearImage);
        folderFileClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                folderFileCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(e);
            }

        });
    }

    /**
     * Set keyword combo as having focus
     */
    public void setKeywordFocus() {
        this.forceKeywordFocus = true;
    }

    private IP4Job getSelectedJob(ISelection selection) {
        if (selection == null || !(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection ss = (IStructuredSelection) selection;
        if (ss.size() != 1) {
            return null;
        }
        if (ss.getFirstElement() instanceof IP4Job) {
            return (IP4Job) ss.getFirstElement();
        } else {
            return null;
        }
    }

    /**
     * Gets the column names current being displayed
     * 
     * @return - array of displayed column names
     */
    public String[] getDisplayColumns() {
        return displayColumns;
    }

    /**
     * Gets the job field names
     * 
     * @return - string array of field names
     */
    public String[] getFieldNames() {
        String[] fields = new String[0];
        if (connection != null) {
            fields = connection.getJobFields();
        }
        return fields;
    }

    /**
     * Gets the selected p4 jobs
     * 
     * @return - array of p4 jobs
     */
    public IP4Job[] getSelectedJobs() {
        ISelection selected = jobsList.getSelection();
        List<IP4Job> selectedJobs = new ArrayList<IP4Job>();
        if (selected instanceof IStructuredSelection) {
            Object[] selectedItem = ((IStructuredSelection) selected).toArray();
            for (int i = 0; i < selectedItem.length; i++) {
                if (selectedItem[i] instanceof IP4Job) {
                    selectedJobs.add((IP4Job) selectedItem[i]);
                }
            }
        }
        return selectedJobs.toArray(new IP4Job[selectedJobs.size()]);
    }

    /**
     * Gets the table control
     * 
     * @return - table
     */
    public Table getTableControl() {
        if (jobsList != null) {
            return jobsList.getTable();
        }
        return null;
    }

    /**
     * Gets the table viewer
     * 
     * @return - table viewer
     */
    public TableViewer getViewer() {
        return this.jobsList;
    }

    private void initDisplayColumns() {
        String port = this.connection.getParameters().getPortNoNull();
        String columnsLine = getPreferenceStore().getString(
                COLUMNS_PREFIX + port);
        if (columnsLine == null || columnsLine.length() == 0) {
            List<String> defaultCols = new ArrayList<String>();
            String[] fields = this.connection.getJobFields();
            for (int i = 0; i < fields.length; i++) {
                defaultCols.add(fields[i]);
                // Just get first 5 fields
                if (i == 4) {
                    break;
                }
            }
            displayColumns = defaultCols
                    .toArray(new String[defaultCols.size()]);
        } else {
            displayColumns = decodeLines(columnsLine);
        }
    }

    private Map<String, Integer> loadColumnSizes() {
        Map<String, Integer> columnSizes = new HashMap<String, Integer>();
        String columns = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getString(COLUMN_SIZES);
        if (columns.length() > 0) {
            String[] pairs = columns.split(PerforceProjectView.SIZE_DELIMITER);
            for (String pair : pairs) {
                String[] nameValue = pair.split("="); //$NON-NLS-1$
                if (nameValue.length == 2) {
                    try {
                        Integer value = Integer.parseInt(nameValue[1]);
                        columnSizes.put(nameValue[0], value);
                    } catch (NumberFormatException nfe) {
                        PerforceProviderPlugin.logError(nfe);
                    }
                }
            }
        }
        return columnSizes;
    }

    /**
     * Save the current column sizes
     */
    public void saveColumnSizes() {
        TableColumn[] columns = jobsList.getTable().getColumns();
        if (columns.length > 0) {
            StringBuilder buffer = new StringBuilder();
            for (TableColumn column : columns) {
                buffer.append(column.getText() + "=" + column.getWidth() //$NON-NLS-1$
                        + PerforceProjectView.SIZE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(COLUMN_SIZES, buffer.toString());
        }
    }

    /**
     * Create the table showing the jobs list
     */
    private Table createTable(Composite parent, boolean multiSelect) {
        int flags = SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION;
        if (multiSelect) {
            flags |= SWT.MULTI;
        }
        final Table table = new Table(parent, flags);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        table.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                saveColumnSizes();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .removePropertyChangeListener(JobsDialog.this);
            }

        });

        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        table.setLayoutData(gd);

        TableLayout layout = new TableLayout();
        table.setLayout(layout);

        jobsList = new TableViewer(table);
        jobsList.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement == loading) {
                    return new Object[] { loading };
                }
                if (jobs != null) {
                    return jobs;
                }
                return new Object[0];
            }
        });
        jobsList.setLabelProvider(new JobLabelProvider());
        jobsList.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                if (doubleListener != null) {
                    doubleListener.doubleClick();
                }
            }
        });

        // Make sure we display the job clicked on no matter what the current
        // selection
        // This is for multiple selection.
        jobsList.getControl().addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                TableItem item = jobsList.getTable().getItem(
                        new Point(e.x, e.y));
                if (item != null) {
                    IP4Job job = (IP4Job) item.getData();
                    updateDetailsPanel(job);
                }
            }
        });

        jobsList.addSelectionChangedListener(this);

        PlatformUI.getWorkbench().getHelpSystem()
                .setHelp(table, IHelpContextIds.JOB_VIEW_JOB_LIST);

        return table;
    }

    /**
     * Refreshes the jobs dialog
     */
    public void refresh() {
        if (connection != null) {
            refreshRetrieveCount();
            loadJobs();
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    private int getMaxJobs() {
        return getPreferenceStore().getInt(
                IPerforceUIConstants.PREF_RETRIEVE_NUM_JOBS);
    }

    private String encodeLines(String[] lines) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            buffer.append(lines[i]);
            buffer.append('\n');
        }
        return buffer.toString();
    }

    private String[] decodeLines(String line) {
        StringTokenizer tokenizer = new StringTokenizer(line, "\n"); //$NON-NLS-1$
        List<String> lines = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            lines.add(tokenizer.nextToken());
        }
        return lines.toArray(new String[lines.size()]);
    }

    /**
     * Save the display columns to the preference store
     * 
     * @param columns
     */
    public void saveDisplayColumns(String[] columns) {
        String port = connection.getParameters().getPortNoNull();
        getPreferenceStore().putValue(COLUMNS_PREFIX + port,
                encodeLines(columns));
    }

    /**
     * Refresh details panel if the specified job is the currently selected job
     * 
     * @param job
     */
    public void refreshDetailsPanel(IP4Job job) {
        if (job == this.selectedJob) {
            updateDetailsPanel(job);
        }
    }

    private void updateDetailsPanel(IP4Job job) {
        for (int i = 0; i < detailFields.length; i++) {
            Object data = detailFields[i].getData();
            if (data != null) {
                Object value = job.getField(data.toString());
                if (value != null) {
                    detailFields[i].setText(value.toString());
                } else {
                    detailFields[i].setText(""); //$NON-NLS-1$
                }
            } else {
                detailFields[i].setText(""); //$NON-NLS-1$
            }
        }
        updateScrollPanel();
    }

    private void clearDetailsPanel() {
        for (int i = 0; i < detailFields.length; i++) {
            detailFields[i].setText(""); //$NON-NLS-1$
        }
        updateScrollPanel();
    }

    private void updateScrollPanel() {
        if (detailFields.length > 0) {
            // Update size of scroll panel
            Composite composite = detailFields[0].getParent();
            detailPanel.setMinSize(composite.computeSize(SWT.DEFAULT,
                    SWT.DEFAULT, true));
        }
    }

    /**
     * Set the double click listener
     * 
     * @param listener
     */
    public void setDoubleListener(IJobDoubleClickListener listener) {
        this.doubleListener = listener;
    }

    private Composite createDetails(ScrolledComposite parent, Color background) {
        final Composite composite = new Composite(parent, SWT.NONE);
        parent.setContent(composite);

        final GridLayout gl = new GridLayout();
        gl.numColumns = 2;
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        composite.setLayout(gl);

        if (this.connection != null) {
            String name = null;
            String status = null;
            String desc = null;
            String date = null;
            String user = null;
            List<String> fields = new ArrayList<String>();
            String[] flds = this.connection.getJobFields();
            fields.addAll(Arrays.asList(flds));
            IJobSpec spec = this.connection.getJobSpec();
            if (spec != null) {
                for (IJobSpecField field : spec.getFields()) {
                    switch (field.getCode()) {
                    case IP4Job.JOB_NAME_CODE:
                        name = field.getName();
                        fields.remove(name);
                        break;
                    case IP4Job.JOB_STATUS_CODE:
                        status = field.getName();
                        fields.remove(status);
                        break;
                    case IP4Job.JOB_USER_CODE:
                        user = field.getName();
                        fields.remove(user);
                        break;
                    case IP4Job.JOB_DATE_CODE:
                        date = field.getName();
                        fields.remove(date);
                        break;
                    case IP4Job.JOB_DESCRIPTION_CODE:
                        desc = field.getName();
                        fields.remove(desc);
                        break;
                    default:
                        break;
                    }
                    if (name != null && status != null && desc != null
                            && date != null && user != null) {
                        break;
                    }
                }
            }
            detailFields = new Text[flds.length];
            int index = 0;
            if (name != null) {
                detailFields[index] = createDetailField(composite, name, false,
                        false, background, false);
                index++;
            }
            if (status != null) {
                detailFields[index] = createDetailField(composite, status,
                        false, false, background, false);
                index++;
            }
            if (user != null) {
                detailFields[index] = createDetailField(composite, user, false,
                        false, background, false);
                index++;
            }
            if (desc != null) {
                detailFields[index] = createDetailField(composite, desc, true,
                        true, background, true);
                index++;
            }
            if (date != null) {
                detailFields[index] = createDetailField(composite, date, false,
                        false, background, false);
                index++;
            }

            for (String field : fields) {
                detailFields[index] = createDetailField(composite, field, true,
                        true, background, false);
                index++;
            }
        } else {
            detailFields = new Text[0];
        }

        parent.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return composite;
    }

    private ScrolledComposite createDetailPanel(Composite parent,
            Color background) {
        final ScrolledComposite panel = new FixedWidthScrolledComposite(parent,
                SWT.V_SCROLL | SWT.BORDER);
        panel.setLayout(new FillLayout());
        createDetails(panel, background);
        panel.setExpandHorizontal(true);
        panel.setExpandVertical(true);
        return panel;
    }

    private Text createDetailField(Composite parent, String name,
            boolean multi, boolean wrap, Color background, boolean grabVertical) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(name + ":"); //$NON-NLS-1$
        GridData lData = new GridData();
        lData.verticalAlignment = SWT.BEGINNING;
        label.setLayoutData(lData);

        // Keep the longest label
        if (longestLabel == null || longestLabel.isDisposed()
                || longestLabel.getText().length() < name.length()) {
            longestLabel = label;
        }

        int flags = SWT.READ_ONLY;
        if (multi) {
            flags |= SWT.MULTI;
            if (wrap) {
                flags |= SWT.WRAP;
            }
        } else {
            flags |= SWT.SINGLE;
        }
        Text text = new Text(parent, flags);
        text.setData(name);
        text.setBackground(background);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        if (grabVertical) {
            gd.verticalAlignment = SWT.FILL;
            gd.grabExcessVerticalSpace = grabVertical;
        }
        text.setLayoutData(gd);
        return text;
    }

    /**
     * Update the sash and optionally display the details
     * 
     * @param displayDetails
     */
    public void updateSash(boolean displayDetails) {
        if (displayDetails) {
            sash.setMaximizedControl(null);
            IP4Job job = getSelectedJob(jobsList.getSelection());
            if (job == null) {
                clearDetailsPanel();
            } else {
                updateDetailsPanel(job);
            }
        } else {
            sash.setMaximizedControl(viewComposite);
        }
    }

    private void enableFilters(boolean enabled) {
        folderFileCombo.setEnabled(enabled);
        keywordCombo.setEnabled(enabled);
        if (enabled && forceKeywordFocus) {
            forceKeywordFocus = false;
            keywordCombo.setFocus();
        }
    }

    /**
     * Clear the filters
     */
    public void clearFilters() {
        folderFileCombo.setText(""); //$NON-NLS-1$
        keywordCombo.setText(""); //$NON-NLS-1$
    }

    private boolean checkFilter(String[] paths, String keyword) {
        if (keyword == null) {
            keyword = ""; //$NON-NLS-1$
        }
        if (paths == null) {
            paths = new String[] { "" }; //$NON-NLS-1$
        }
        if (paths.length == 1 && paths[0] != null) {
            return paths[0].equals(folderFileCombo.getText().trim())
                    && keyword.equals(keywordCombo.getText().trim());
        }
        return false;
    }

    private void updateHistory(String path, String keyword) {
        if (path != null && path.length() > 0) {
            List<String> paths = new ArrayList<String>();
            paths.add(path);
            for (String item : folderFileCombo.getItems()) {
                if (!paths.contains(item)) {
                    paths.add(item);
                }
                if (paths.size() == 10) {
                    break;
                }
            }
            folderFileCombo.removeAll();
            StringBuffer folders = new StringBuffer();
            for (String item : paths) {
                folderFileCombo.add(item, folderFileCombo.getItemCount());
                folders.append(item).append(PerforceProjectView.SIZE_DELIMITER);
            }
            folderFileCombo.select(0);
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(FILE_FOLDER_HISTORY, folders.toString());
        }

        if (keyword != null && keyword.length() > 0) {
            List<String> keywords = new ArrayList<String>();
            keywords.add(keyword);
            for (String item : keywordCombo.getItems()) {
                if (!keywords.contains(item)) {
                    keywords.add(item);
                }
                if (keywords.size() == 10) {
                    break;
                }
            }
            keywordCombo.removeAll();
            StringBuffer usersBuffer = new StringBuffer();
            for (String item : keywords) {
                keywordCombo.add(item, keywordCombo.getItemCount());
                usersBuffer.append(item).append(
                        PerforceProjectView.SIZE_DELIMITER);
            }
            PerforceUIPlugin.getPlugin().getPreferenceStore()
                    .setValue(KEYWORD_HISTORY, usersBuffer.toString());
            keywordCombo.select(0);
        }
    }

    /**
     * Is this view's main control not disposed
     * 
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return jobsList != null && jobsList.getTable() != null
                && !jobsList.getTable().isDisposed();
    }

    private void loadJobs() {
        final IP4Connection currConnection = connection;
        isLoading = true;
        if (viewComposite == null || viewComposite.isDisposed()) {
            return;
        }
        final Object newLoading = new Object();
        synchronized (this) {
            this.loading = newLoading;
            jobsList.setInput(this.loading);
        }
        String[] paths = null;
        String path = folderFileCombo.getText().trim();
        if (path.length() == 0) {
            paths = null;
        } else {
            paths = new String[] { path };
        }
        String keyword = keywordCombo.getText().trim();
        if (keyword.length() == 0) {
            keyword = null;
        }
        final String[] finalPaths = paths;
        final String finalKeyword = keyword;
        enableFilters(false);
        updateHistory(path, keyword);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.JobsDialog_LoadingJobs;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                IP4Connection jobConnection = connection;
                if (jobConnection == null || jobConnection != currConnection) {
                    isLoading = false;
                    return;
                }
                final IP4Job[] newJobs = jobConnection.getJobs(finalPaths,
                        retrieveCount, finalKeyword);
                if (newLoading == loading) {
                    UIJob job = new UIJob(Messages.JobsDialog_UpdatingJobsView) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (currConnection != connection) {
                                return Status.CANCEL_STATUS;
                            }
                            if (okToUse() && newLoading == loading
                                    && checkFilter(finalPaths, finalKeyword)) {
                                jobs = newJobs;
                                jobsList.setInput(jobs);
                                if (autoSelectFirstEntry && newJobs.length > 0) {
                                    jobsList.setSelection(new StructuredSelection(
                                            newJobs[0]));
                                }
                                enableFilters(true);
                                isLoading = false;
                            }
                            return Status.OK_STATUS;
                        }

                    };
                    job.schedule();
                }
            }
        });
    }

    /**
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (jobsList == null || jobsList.getTable().isDisposed()) {
            return;
        }
        this.selectedJob = getSelectedJob(event.getSelection());
        if (this.selectedJob != null) {
            updateDetailsPanel(this.selectedJob);
        }
    }

    /**
     * Is the jobs dialog loading?
     * 
     * @return - true if loading
     */
    public boolean isLoading() {
        return this.isLoading;
    }

    /**
     * Refresh the retrieve count with the latest value from the pref store
     */
    public void refreshRetrieveCount() {
        retrieveCount = getMaxJobs();
    }

    /**
     * @param autoSelectFirstEntry
     *            the autoSelectFirstEntry to set
     */
    public void setAutoSelectFirstEntry(boolean autoSelectFirstEntry) {
        this.autoSelectFirstEntry = autoSelectFirstEntry;
    }

}
