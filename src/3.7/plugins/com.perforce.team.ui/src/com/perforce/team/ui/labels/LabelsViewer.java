/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.labels;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.progress.UIJob;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Label;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.dialogs.DialogUtils;
import com.perforce.team.ui.dialogs.FixedWidthScrolledComposite;
import com.perforce.team.ui.preferences.IPreferenceConstants;
import com.perforce.team.ui.viewer.FilterViewer;
import com.perforce.team.ui.views.PerforceProjectView;
import com.perforce.team.ui.views.SessionManager;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class LabelsViewer extends FilterViewer implements
        ISelectionChangedListener, IPropertyChangeListener {

    /**
     * FILE_FOLDER_HISTORY
     */
    public static final String FILE_FOLDER_HISTORY = "com.perforce.team.ui.labels.FILE_FOLDER_HISTORY"; //$NON-NLS-1$

    /**
     * OWNER_HISTORY
     */
    public static final String OWNER_HISTORY = "com.perforce.team.ui.labels.OWNER_HISTORY"; //$NON-NLS-1$

    /**
     * NAME_HISTORY
     */
    public static final String NAME_HISTORY = "com.perforce.team.ui.labels.NAME_HISTORY"; //$NON-NLS-1$

    /**
     * LABEL_COLUMN
     */
    public static final String LABEL_COLUMN = Messages.LabelsViewer_Label;

    /**
     * DESCRIPTION_COLUMN
     */
    public static final String DESCRIPTION_COLUMN = Messages.LabelsViewer_Description;

    /**
     * ACCESS_COLUMN
     */
    public static final String ACCESS_COLUMN = Messages.LabelsViewer_AccessTime;

    /**
     * OWNER_COLUMN
     */
    public static final String OWNER_COLUMN = Messages.LabelsViewer_Owner;

    /**
     * ELLIPSIS
     */
    public static final String ELLIPSIS = "..."; //$NON-NLS-1$

    // View sorter
    private LabelSorter sorter;

    // Label list pane
    private TableViewer labelsList;

    // Current connection
    private IP4Connection connection;

    // Composite containing view controls
    private Composite viewComposite;

    private Link showMore;
    private Label folderFileLabel;
    private Combo folderFileCombo;
    private ToolBar folderFileBar;
    private ToolItem folderFileClearItem;
    private Label ownerLabel;
    private Combo ownerCombo;
    private ToolBar ownerBar;
    private ToolItem ownerClearItem;
    private Label nameLabel;
    private Combo nameCombo;
    private ToolBar nameBar;
    private ToolItem nameClearItem;

    private SashForm sash;

    // Detail panel
    private ScrolledComposite detailPanel;
    private LabelWidget labelView;

    private int retrieveCount = 0;
    private boolean isLoading = false;
    private boolean autoSelectFirstEntry = false;

    private IP4Label selectedLabel = null;
    private Object loading = new Object();
    private Image loadingImage;
    private Image clearImage;

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.labelcolumns"; //$NON-NLS-1$

    /**
     * Label provider for the labels list
     */
    private class LabelsLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        // The label image
        private Image labelImage = null;

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
                if (labelImage == null) {
                    labelImage = createLabelImage();
                }
                return labelImage;
            } else {
                return null;
            }
        }

        /**
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            if (labelImage != null && !labelImage.isDisposed()) {
                labelImage.dispose();
                labelImage = null;
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

        private Image createLabelImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_LABEL)
                    .createImage();
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element == loading && columnIndex == 0) {
                return Messages.LabelsViewer_Loading;
            } else if (element instanceof IP4Label) {
                String value = null;
                IP4Label label = (IP4Label) element;
                switch (columnIndex) {
                case 0:
                    value = label.getName();
                    break;
                case 1:
                    value = label.getOwner();
                    break;
                case 2:
                    value = P4UIUtils.formatLabelDate(label.getAccessTime());
                    break;
                case 3:
                    value = label.getDescription();
                    break;
                default:
                    break;
                }
                if (value != null) {
                    return P4CoreUtils.removeWhitespace(value);
                }
            }
            return ""; //$NON-NLS-1$
        }
    }

    /**
     * Shows the next amount of labels set to be retrieved
     */
    public void showMore() {
        if (retrieveCount != -1) {
            retrieveCount += getMaxLabels();
        }
        loadLabels();
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

        if (connection.getIntVersion() < IP4Connection.MAX_FILTER_SERVER_VERSION) {
            showMore.setVisible(false);
        }
    }

    /**
     * Capture changes to label preferences
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IPreferenceConstants.NUM_LABELS_RETRIEVE.equals(property)) {
            retrieveCount = getMaxLabels();
            updateMoreButton();
            loadLabels();
        }
    }

    /**
     * Create dialog controls
     * 
     * @param parent
     *            the parent window
     * @param con
     * @param multiSelect
     * @param displayDetails
     * @return the main composite control
     */
    public Composite createControl(Composite parent, final IP4Connection con,
            boolean multiSelect, boolean displayDetails) {
        this.connection = con;

        if (sash != null && !sash.isDisposed()) {
            sash.dispose();
        }

        // Add listener after disposing sash since dispose event on table causes
        // pref listener to be removed
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

        labelsList.addSelectionChangedListener(this);

        updateSash(displayDetails);

        loadLabels();

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
            loadLabels();
        }
    }

    private void updateMoreButton(boolean layout) {
        if (showMore != null && !showMore.isDisposed()) {
            int max = getMaxLabels();
            if (max == -1) {
                showMore.setText(Messages.LabelsViewer_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(MessageFormat.format(
                        Messages.LabelsViewer_ShowNumMore, max));
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
        GridLayout fcLayout = new GridLayout(6, false);
        filterComposite.setLayout(fcLayout);
        filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                false));

        final SelectionListener comboAdapter = P4UIUtils
                .createComboSelectionListener(new Runnable() {

                    public void run() {
                        loadLabels();
                    }
                });

        clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();

        folderFileLabel = new Label(filterComposite, SWT.LEFT);
        folderFileLabel.setText(Messages.LabelsViewer_FolderFile);
        folderFileCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] folders = PerforceProjectView.getItems(FILE_FOLDER_HISTORY);
        for (String folder : folders) {
            folderFileCombo.add(folder);
        }
        folderFileCombo.addSelectionListener(comboAdapter);
        GridData ffcData = new GridData(SWT.FILL, SWT.FILL, true, false);
        ffcData.horizontalSpan = 4;
        folderFileCombo.setLayoutData(ffcData);

        folderFileBar = new ToolBar(filterComposite, SWT.FLAT);
        folderFileClearItem = new ToolItem(folderFileBar, SWT.PUSH);
        folderFileClearItem
                .setToolTipText(Messages.LabelsViewer_ClearFolderFile);
        folderFileClearItem.setImage(clearImage);
        folderFileClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                folderFileCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        ownerLabel = new Label(filterComposite, SWT.LEFT);
        ownerLabel.setText(Messages.LabelsViewer_OwnerLabel);
        ownerLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        ownerCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] keywords = PerforceProjectView.getItems(OWNER_HISTORY);
        for (String keyword : keywords) {
            ownerCombo.add(keyword);
        }
        ownerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ownerCombo.addSelectionListener(comboAdapter);

        ownerBar = new ToolBar(filterComposite, SWT.FLAT);
        ownerClearItem = new ToolItem(ownerBar, SWT.PUSH);
        ownerClearItem.setToolTipText(Messages.LabelsViewer_ClearOwnerFilter);
        ownerClearItem.setImage(clearImage);
        ownerClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ownerCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        nameLabel = new Label(filterComposite, SWT.LEFT);
        nameLabel.setText(Messages.LabelsViewer_NameContains);
        nameLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        nameCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] names = PerforceProjectView.getItems(NAME_HISTORY);
        for (String name : names) {
            nameCombo.add(name);
        }
        nameCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        nameCombo.addSelectionListener(comboAdapter);

        nameBar = new ToolBar(filterComposite, SWT.FLAT);
        nameClearItem = new ToolItem(nameBar, SWT.PUSH);
        nameClearItem.setToolTipText(Messages.LabelsViewer_ClearNameFilter);
        nameClearItem.setImage(clearImage);
        nameClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                nameCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(e);
            }

        });

        if (connection.getIntVersion() < IP4Connection.NAME_FILTER_SERVER_VERSION) {
            nameLabel.setVisible(false);
            nameCombo.setVisible(false);
            nameBar.setVisible(false);
        }
    }

    private IP4Label getSelectedLabel(ISelection selection) {
        if (selection == null || !(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection ss = (IStructuredSelection) selection;
        if (ss.size() != 1) {
            return null;
        }
        if (ss.getFirstElement() instanceof IP4Label) {
            return (IP4Label) ss.getFirstElement();
        } else {
            return null;
        }
    }

    /**
     * Gets the selected p4 labels
     * 
     * @return - array of p4 labels
     */
    public IP4Label[] getSelectedLabels() {
        ISelection selected = labelsList.getSelection();
        List<IP4Label> selectedLabels = new ArrayList<IP4Label>();
        if (selected instanceof IStructuredSelection) {
            Object[] selectedItem = ((IStructuredSelection) selected).toArray();
            for (int i = 0; i < selectedItem.length; i++) {
                if (selectedItem[i] instanceof IP4Label) {
                    selectedLabels.add((IP4Label) selectedItem[i]);
                }
            }
        }
        return selectedLabels.toArray(new IP4Label[selectedLabels.size()]);
    }

    /**
     * Gets the table control
     * 
     * @return - table
     */
    public Table getTableControl() {
        if (labelsList != null) {
            return labelsList.getTable();
        }
        return null;
    }

    /**
     * Gets the table viewer
     * 
     * @return - table viewer
     */
    public TableViewer getViewer() {
        return this.labelsList;
    }

    private Map<String, Integer> loadColumnSizes() {
        return SessionManager.loadColumnSizes(COLUMN_SIZES);
    }

    /**
     * Save the current column sizes
     */
    private void saveColumnSizes() {
        SessionManager.saveColumnPreferences(labelsList.getTable(),
                COLUMN_SIZES);
    }

    /**
     * Create the table showing the labels list
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
                        .removePropertyChangeListener(LabelsViewer.this);
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

        SelectionListener headerListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                // column selected - need to sort
                TableColumn column = (TableColumn) e.widget;
                sorter.setSortColumn(column.getText());
                labelsList.refresh();
            }
        };

        labelsList = new TableViewer(table);
        labelsList.setContentProvider(new ArrayContentProvider());
        labelsList.setLabelProvider(new LabelsLabelProvider());

        // Add all columns to table
        TableColumn labelColumn = addColumn(labelsList.getTable(), LABEL_COLUMN);
        addColumn(labelsList.getTable(), OWNER_COLUMN);
        addColumn(labelsList.getTable(), ACCESS_COLUMN);
        addColumn(labelsList.getTable(), DESCRIPTION_COLUMN);

        for (TableColumn column : labelsList.getTable().getColumns()) {
            column.addSelectionListener(headerListener);
        }

        table.setSortColumn(labelColumn);
        table.setSortDirection(SWT.UP);

        Map<String, Integer> columnSizes = loadColumnSizes();

        for (TableColumn column : labelsList.getTable().getColumns()) {
            int width = 100;
            if (columnSizes.containsKey(column.getText())) {
                int size = columnSizes.get(column.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            layout.addColumnData(new ColumnPixelData(width, true));
        }

        sorter = new LabelSorter(this.labelsList.getTable(),
                labelColumn.getText());
        sorter.setAscending();
        labelsList.setSorter(sorter);

        return table;
    }

    /**
     * Add another column to the table
     */
    private TableColumn addColumn(Table table, String title) {
        TableColumn col = new TableColumn(table, SWT.NONE);
        col.setResizable(true);
        col.setText(title);
        return col;
    }

    /**
     * Refreshes the labels viewer
     */
    public void refresh() {
        if (connection != null) {
            refreshRetrieveCount();
            loadLabels();
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    private int getMaxLabels() {
        return getPreferenceStore().getInt(
                IPreferenceConstants.NUM_LABELS_RETRIEVE);
    }

    private void updateDetailsPanel(final IP4Label label) {
        labelView.clear();
        if (label != null && label.needsRefresh()) {
            final String name = label.getName();
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    label.refresh();
                    if (!label.needsRefresh()) {
                        UIJob job = new UIJob(
                                Messages.LabelsViewer_UpdatingLabel + name) {

                            @Override
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                if (okToUse() && label == selectedLabel) {
                                    updateDetailsPanel(label);
                                }
                                return Status.OK_STATUS;
                            }

                        };
                        job.schedule();
                    }
                }

                @Override
                public String getTitle() {
                    return Messages.LabelsViewer_LoadingLabel + name;
                }

            });
        } else {
            labelView.update(label);
            updateScrollPanel();
        }
    }

    private void clearDetailsPanel() {
        labelView.clear();
        updateScrollPanel();
    }

    private void updateScrollPanel() {
        // Update size of scroll panel
        detailPanel.setMinSize(labelView.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true));
    }

    private ScrolledComposite createDetailPanel(Composite parent,
            Color background) {
        final ScrolledComposite panel = new FixedWidthScrolledComposite(parent,
                SWT.V_SCROLL | SWT.BORDER);
        panel.setLayout(new FillLayout());

        labelView = new LabelWidget(panel, 10, 10, false);

        panel.setContent(labelView.getControl());
        panel.setExpandHorizontal(true);
        panel.setExpandVertical(true);
        panel.setMinSize(labelView.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT));

        return panel;
    }

    /**
     * Update the sash and optionally display the details
     * 
     * @param displayDetails
     */
    public void updateSash(boolean displayDetails) {
        if (displayDetails) {
            sash.setMaximizedControl(null);
            IP4Label label = getSelectedLabel(labelsList.getSelection());
            if (label == null) {
                clearDetailsPanel();
            } else {
                updateDetailsPanel(label);
            }
        } else {
            sash.setMaximizedControl(viewComposite);
        }
    }

    private void enableFilters(boolean enabled) {
        folderFileCombo.setEnabled(enabled);
        ownerCombo.setEnabled(enabled);
        nameCombo.setEnabled(enabled);
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
                    && keyword.equals(ownerCombo.getText().trim());
        }
        return false;
    }

    private void updateHistory(String path, String owner, String name) {
        if (path != null && path.length() > 0) {
            SessionManager.saveComboHistory(folderFileCombo, 10,
                    FILE_FOLDER_HISTORY);
        }
        if (owner != null && owner.length() > 0) {
            SessionManager.saveComboHistory(ownerCombo, 10, OWNER_HISTORY);
        }
        if (name != null && name.length() > 0) {
            SessionManager.saveComboHistory(nameCombo, 10, NAME_HISTORY);
        }
    }

    /**
     * Is this view's main control not disposed
     * 
     * @return - true if not disposed
     */
    public boolean okToUse() {
        return labelsList != null && labelsList.getTable() != null
                && !labelsList.getTable().isDisposed();
    }

    private String convertNameContains(String entered) {
        String converted = entered;
        if (converted.length() > 0) {
            converted = converted.replace("*", ELLIPSIS); //$NON-NLS-1$
            if (converted.indexOf("...") == -1) { //$NON-NLS-1$
                converted = ELLIPSIS + converted + ELLIPSIS;
            }
        }
        return converted;
    }

    private void loadLabels() {
        isLoading = true;
        if (viewComposite == null || viewComposite.isDisposed()) {
            return;
        }
        clearDetailsPanel();
        final Object newLoading = new Object();
        synchronized (this) {
            this.loading = newLoading;
            labelsList.setInput(new Object[] { this.loading });
        }
        String[] paths = null;
        String path = folderFileCombo.getText().trim();
        if (path.length() == 0) {
            paths = null;
        } else {
            paths = new String[] { path };
        }
        String owner = ownerCombo.getText().trim();
        if (owner.length() == 0) {
            owner = null;
        }
        String nameContains = convertNameContains(nameCombo.getText().trim());
        if (nameContains.length() == 0) {
            nameContains = null;
        }
        final String[] finalPaths = paths;
        final String finalOwner = owner;
        final String finalNameContains = nameContains;
        enableFilters(false);
        updateHistory(path, owner, nameContains);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.LabelsViewer_LoadingLabels;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                IP4Connection labelConnection = connection;
                if (labelConnection == null) {
                    isLoading = false;
                    return;
                }
                final IP4Label[] newLabels = labelConnection.getLabels(
                        finalOwner, finalPaths, retrieveCount,
                        finalNameContains);
                if (newLoading == loading) {
                    UIJob job = new UIJob(
                            Messages.LabelsViewer_UpdatingLabelsView) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (okToUse() && newLoading == loading
                                    && checkFilter(finalPaths, finalOwner)) {
                                labelsList.setInput(newLabels);
                                if (autoSelectFirstEntry
                                        && newLabels.length > 0) {
                                    labelsList
                                            .setSelection(new StructuredSelection(
                                                    newLabels[0]));
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
        if (labelsList == null || labelsList.getTable().isDisposed()) {
            return;
        }
        IP4Label label = getSelectedLabel(event.getSelection());
        if (label != null) {
            this.selectedLabel = label;
            updateDetailsPanel(label);
        }
    }

    /**
     * Is the labels viewer loading?
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
        retrieveCount = getMaxLabels();
    }

    /**
     * @param autoSelectFirstEntry
     *            the autoSelectFirstEntry to set
     */
    public void setAutoSelectFirstEntry(boolean autoSelectFirstEntry) {
        this.autoSelectFirstEntry = autoSelectFirstEntry;
    }

    /**
     * Get details widget
     * 
     * @return - details widget
     */
    public LabelWidget getDetails() {
        return this.labelView;
    }

}
