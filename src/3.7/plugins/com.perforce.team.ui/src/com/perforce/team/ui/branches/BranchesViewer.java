/**
 * Copyright (c) 2008 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.branches;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.p4java.IP4Branch;
import com.perforce.team.core.p4java.IP4Connection;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnPixelData;
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
import org.eclipse.osgi.util.NLS;
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

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class BranchesViewer extends FilterViewer implements
        ISelectionChangedListener, IPropertyChangeListener {

    /**
     * OWNER_HISTORY
     */
    public static final String OWNER_HISTORY = "com.perforce.team.ui.branches.OWNER_HISTORY"; //$NON-NLS-1$

    /**
     * NAME_HISTORY
     */
    public static final String NAME_HISTORY = "com.perforce.team.ui.branches.NAME_HISTORY"; //$NON-NLS-1$

    /**
     * BRANCH_COLUMN
     */
    public static final String BRANCH_COLUMN = Messages.BranchesViewer_Branch;

    /**
     * DESCRIPTION_COLUMN
     */
    public static final String DESCRIPTION_COLUMN = Messages.BranchesViewer_Description;

    /**
     * ACCESS_COLUMN
     */
    public static final String ACCESS_COLUMN = Messages.BranchesViewer_AccessTime;

    /**
     * UPDATE_COLUMN
     */
    public static final String UPDATE_COLUMN = Messages.BranchesViewer_UpdateTime;

    /**
     * OWNER_COLUMN
     */
    public static final String OWNER_COLUMN = Messages.BranchesViewer_Owner;

    /**
     * ELLIPSIS
     */
    public static final String ELLIPSIS = "..."; //$NON-NLS-1$

    // View sorter
    private BranchSorter sorter;

    // Branch list pane
    private TableViewer branchesList;

    // Current connection
    private IP4Connection connection;
    private IP4Branch[] branches;

    // Composite containing view controls
    private Composite viewComposite;

    private Link showMore;
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
    private BranchWidget branchView;

    private int retrieveCount = 0;
    private boolean isLoading = false;
    private boolean autoSelectFirstEntry = false;

    private IP4Branch selectedBranch = null;
    private Object loading = new Object();
    private Image loadingImage;
    private Image clearImage;

    /**
     * COLUMN_SIZES - preference key for column sizes
     */
    public static final String COLUMN_SIZES = "com.perforce.team.ui.branchcolumns"; //$NON-NLS-1$

    /**
     * Label provider for the branches list
     */
    private class BranchesLabelProvider extends LabelProvider implements
            ITableLabelProvider {

        // The branch image
        private Image branchImage = null;

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
                if (branchImage == null) {
                    branchImage = createBranchImage();
                }
                return branchImage;
            } else {
                return null;
            }
        }

        /**
         * @see org.eclipse.jface.viewers.BaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            if (branchImage != null && !branchImage.isDisposed()) {
                branchImage.dispose();
                branchImage = null;
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

        private Image createBranchImage() {
            PerforceUIPlugin plugin = PerforceUIPlugin.getPlugin();
            return plugin.getImageDescriptor(IPerforceUIConstants.IMG_BRANCH)
                    .createImage();
        }

        /**
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex) {
            if (element == loading && columnIndex == 0) {
                return Messages.BranchesViewer_Loading;
            } else if (element instanceof IP4Branch) {
                String value = null;
                IP4Branch branch = (IP4Branch) element;
                switch (columnIndex) {
                case 0:
                    value = branch.getName();
                    break;
                case 1:
                    value = branch.getOwner();
                    break;
                case 2:
                    value = P4UIUtils.formatLabelDate(branch.getAccessTime());
                    break;
                case 3:
                    value = P4UIUtils.formatLabelDate(branch.getUpdateTime());
                    break;
                case 4:
                    value = branch.getDescription();
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
     * Add a branch and select it to the viewer
     * 
     * @param branch
     */
    public void addBranch(IP4Branch branch) {
        if (branch != null) {
            if (branches != null) {
                IP4Branch[] newJobs = new IP4Branch[branches.length + 1];
                newJobs[0] = branch;
                System.arraycopy(branches, 0, newJobs, 1, branches.length);
                branches = newJobs;
            } else {
                branches = new IP4Branch[] { branch };
            }
            this.branchesList.insert(branch, 0);
            this.branchesList.setSelection(new StructuredSelection(branch),
                    true);
        }
    }

    /**
     * Shows the next amount of branches set to be retrieved
     */
    public void showMore() {
        if (retrieveCount != -1) {
            retrieveCount += getMaxBranches();
        }
        loadBranches();
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
     * Capture changes to branch preferences
     * 
     * @param event
     *            the property change event
     */
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getProperty();
        if (IPreferenceConstants.NUM_BRANCHES_RETRIEVE.equals(property)) {
            retrieveCount = getMaxBranches();
            updateMoreButton();
            loadBranches();
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

        branchesList.addSelectionChangedListener(this);

        updateSash(displayDetails);

        loadBranches();

        return sash;
    }

    private void updateMoreButton(boolean layout) {
        if (showMore != null && !showMore.isDisposed()) {
            int max = getMaxBranches();
            if (max == -1) {
                showMore.setText(Messages.BranchesViewer_ShowMore);
                showMore.setEnabled(false);
            } else {
                showMore.setText(NLS.bind(Messages.BranchesViewer_ShowMaxMore,
                        max));
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
                        loadBranches();
                    }
                });

        clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();

        ownerLabel = new Label(filterComposite, SWT.LEFT);
        ownerLabel.setText(Messages.BranchesViewer_OwnerLabel);
        ownerLabel
                .setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
        ownerCombo = new Combo(filterComposite, SWT.DROP_DOWN);
        String[] keywords = PerforceProjectView.getItems(OWNER_HISTORY);
        for (String keyword : keywords) {
            ownerCombo.add(keyword);
        }
        ownerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        ownerCombo.addSelectionListener(comboAdapter);
        ownerCombo.setText(connection.getParameters().getUserNoNull());

        ownerBar = new ToolBar(filterComposite, SWT.FLAT);
        ownerClearItem = new ToolItem(ownerBar, SWT.PUSH);
        ownerClearItem.setToolTipText(Messages.BranchesViewer_ClearOwnerFilter);
        ownerClearItem.setImage(clearImage);
        ownerClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                ownerCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        nameLabel = new Label(filterComposite, SWT.LEFT);
        nameLabel.setText(Messages.BranchesViewer_NameContains);
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
        nameClearItem.setToolTipText(Messages.BranchesViewer_ClearNameFilter);
        nameClearItem.setImage(clearImage);
        nameClearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                nameCombo.setText(""); //$NON-NLS-1$
                comboAdapter.widgetSelected(null);
            }

        });

        if (connection.getIntVersion() < IP4Connection.NAME_FILTER_SERVER_VERSION) {
            nameLabel.setVisible(false);
            nameCombo.setVisible(false);
            nameBar.setVisible(false);
        }
    }

    private IP4Branch getSelectedBranch(ISelection selection) {
        if (selection == null || !(selection instanceof IStructuredSelection)) {
            return null;
        }
        IStructuredSelection ss = (IStructuredSelection) selection;
        if (ss.size() != 1) {
            return null;
        }
        if (ss.getFirstElement() instanceof IP4Branch) {
            return (IP4Branch) ss.getFirstElement();
        } else {
            return null;
        }
    }

    /**
     * Gets the selected p4 branches
     * 
     * @return - array of p4 branches
     */
    public IP4Branch[] getSelectedBranches() {
        ISelection selected = branchesList.getSelection();
        List<IP4Branch> selectedBranches = new ArrayList<IP4Branch>();
        if (selected instanceof IStructuredSelection) {
            Object[] selectedItem = ((IStructuredSelection) selected).toArray();
            for (int i = 0; i < selectedItem.length; i++) {
                if (selectedItem[i] instanceof IP4Branch) {
                    selectedBranches.add((IP4Branch) selectedItem[i]);
                }
            }
        }
        return selectedBranches.toArray(new IP4Branch[selectedBranches.size()]);
    }

    /**
     * Gets the table control
     * 
     * @return - table
     */
    public Table getTableControl() {
        if (branchesList != null) {
            return branchesList.getTable();
        }
        return null;
    }

    /**
     * Gets the table viewer
     * 
     * @return - table viewer
     */
    public TableViewer getViewer() {
        return this.branchesList;
    }

    private Map<String, Integer> loadColumnSizes() {
        return SessionManager.loadColumnSizes(COLUMN_SIZES);
    }

    /**
     * Save the current column sizes
     */
    private void saveColumnSizes() {
        SessionManager.saveColumnPreferences(branchesList.getTable(),
                COLUMN_SIZES);
    }

    /**
     * Create the table showing the branches list
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
                        .removePropertyChangeListener(BranchesViewer.this);
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
                branchesList.refresh();
            }
        };

        branchesList = new TableViewer(table);
        branchesList.setContentProvider(new IStructuredContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                if (inputElement == loading) {
                    return new Object[] { loading };
                }
                if (branches != null) {
                    return branches;
                }
                return new Object[0];
            }
        });
        branchesList.setLabelProvider(new BranchesLabelProvider());

        // Add all columns to table
        TableColumn branchColumn = addColumn(branchesList.getTable(),
                BRANCH_COLUMN);
        addColumn(branchesList.getTable(), OWNER_COLUMN);
        addColumn(branchesList.getTable(), ACCESS_COLUMN);
        addColumn(branchesList.getTable(), UPDATE_COLUMN);
        addColumn(branchesList.getTable(), DESCRIPTION_COLUMN);

        for (TableColumn column : branchesList.getTable().getColumns()) {
            column.addSelectionListener(headerListener);
        }

        table.setSortColumn(branchColumn);
        table.setSortDirection(SWT.UP);

        Map<String, Integer> columnSizes = loadColumnSizes();

        for (TableColumn column : branchesList.getTable().getColumns()) {
            int width = 100;
            if (columnSizes.containsKey(column.getText())) {
                int size = columnSizes.get(column.getText()).intValue();
                if (size > 0) {
                    width = size;
                }
            }
            layout.addColumnData(new ColumnPixelData(width, true));
        }

        sorter = new BranchSorter(this.branchesList.getTable(),
                branchColumn.getText());
        sorter.setAscending();
        branchesList.setSorter(sorter);

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
     * Refreshes the branches viewer
     */
    public void refresh() {
        if (connection != null) {
            refreshRetrieveCount();
            loadBranches();
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUIPlugin.getPlugin().getPreferenceStore();
    }

    private int getMaxBranches() {
        return getPreferenceStore().getInt(
                IPreferenceConstants.NUM_BRANCHES_RETRIEVE);
    }

    private void updateDetailsPanel(final IP4Branch branch) {
        branchView.clear();
        if (branch != null && branch.needsRefresh()) {
            final String name = branch.getName();
            P4Runner.schedule(new P4Runnable() {

                @Override
                public void run(IProgressMonitor monitor) {
                    branch.refresh();
                    if (!branch.needsRefresh()) {
                        UIJob job = new UIJob(NLS.bind(
                                Messages.BranchesViewer_UpdatingBranch, name)) {

                            @Override
                            public IStatus runInUIThread(
                                    IProgressMonitor monitor) {
                                if (okToUse() && branch == selectedBranch) {
                                    updateDetailsPanel(branch);
                                }
                                return Status.OK_STATUS;
                            }

                        };
                        job.schedule();
                    }
                }

                @Override
                public String getTitle() {
                    return NLS
                            .bind(Messages.BranchesViewer_LoadingBranch, name);
                }

            });
        } else {
            branchView.update(branch);
            updateScrollPanel();
        }
    }

    /**
     * Refresh a branch
     * 
     * @param branch
     */
    public void refreshDetails(IP4Branch branch) {
        if (branch != null && branch.equals(selectedBranch)) {
            branchView.update(branch);
            updateScrollPanel();
        }
    }

    private void clearDetailsPanel() {
        branchView.clear();
        updateScrollPanel();
    }

    private void updateScrollPanel() {
        // Update size of scroll panel
        detailPanel.setMinSize(branchView.getControl().computeSize(SWT.DEFAULT,
                SWT.DEFAULT, true));
    }

    private ScrolledComposite createDetailPanel(Composite parent,
            Color background) {
        final ScrolledComposite panel = new FixedWidthScrolledComposite(parent,
                SWT.V_SCROLL | SWT.BORDER);
        panel.setLayout(new FillLayout());

        branchView = new BranchWidget(panel, 10, 10, false);

        panel.setContent(branchView.getControl());
        panel.setExpandHorizontal(true);
        panel.setExpandVertical(true);
        panel.setMinSize(branchView.getControl().computeSize(SWT.DEFAULT,
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
            IP4Branch branch = getSelectedBranch(branchesList.getSelection());
            if (branch == null) {
                clearDetailsPanel();
            } else {
                updateDetailsPanel(branch);
            }
        } else {
            sash.setMaximizedControl(viewComposite);
        }
    }

    private void enableFilters(boolean enabled) {
        ownerCombo.setEnabled(enabled);
        nameCombo.setEnabled(enabled);
    }

    private boolean checkFilter(String user) {
        if (user == null) {
            user = ""; //$NON-NLS-1$
        }
        return user.equals(ownerCombo.getText().trim());
    }

    private void updateHistory(String owner, String name) {
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
        return branchesList != null && branchesList.getTable() != null
                && !branchesList.getTable().isDisposed();
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

    private void loadBranches() {
        isLoading = true;
        if (viewComposite == null || viewComposite.isDisposed()) {
            return;
        }
        clearDetailsPanel();
        final Object newLoading = new Object();
        synchronized (this) {
            this.loading = newLoading;
            branchesList.setInput(this.loading);
        }
        String owner = ownerCombo.getText().trim();
        if (owner.length() == 0) {
            owner = null;
        }
        String nameContains = convertNameContains(nameCombo.getText().trim());
        if (nameContains.length() == 0) {
            nameContains = null;
        }
        final String finalOwner = owner;
        final String finalNameContains = nameContains;
        enableFilters(false);
        updateHistory(owner, nameContains);
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return Messages.BranchesViewer_LoadingBranches;
            }

            @Override
            public void run(IProgressMonitor monitor) {
                IP4Connection branchConnection = connection;
                if (branchConnection == null) {
                    isLoading = false;
                    return;
                }

                final IP4Branch[] newBranches = branchConnection.getBranches(
                        finalOwner, retrieveCount, finalNameContains);
                if (newLoading == loading) {
                    UIJob job = new UIJob(
                            Messages.BranchesViewer_UpdatingBranchesView) {

                        @Override
                        public IStatus runInUIThread(IProgressMonitor monitor) {
                            if (okToUse() && newLoading == loading
                                    && checkFilter(finalOwner)) {
                                branches = newBranches;
                                branchesList.setInput(branches);
                                if (autoSelectFirstEntry
                                        && newBranches.length > 0) {
                                    branchesList
                                            .setSelection(new StructuredSelection(
                                                    newBranches[0]));
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
        if (branchesList == null || branchesList.getTable().isDisposed()) {
            return;
        }
        IP4Branch branch = getSelectedBranch(event.getSelection());
        if (branch != null) {
            this.selectedBranch = branch;
            updateDetailsPanel(branch);
        }
    }

    /**
     * Is the branches viewer loading?
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
        retrieveCount = getMaxBranches();
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
    public BranchWidget getDetails() {
        return this.branchView;
    }

}