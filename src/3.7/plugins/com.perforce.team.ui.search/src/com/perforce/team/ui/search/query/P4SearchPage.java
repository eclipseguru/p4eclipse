/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.search.query;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.search.query.QueryOptions;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4ConnectionManager;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.search.P4UiSearchPlugin;
import com.perforce.team.ui.search.preferences.IPreferenceConstants;
import com.perforce.team.ui.views.SessionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.StackLayout;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4SearchPage extends DialogPage implements ISearchPage {

    /**
     * PAGE_NAME
     */
    public static final String PAGE_NAME = "p4grepSearchPage"; //$NON-NLS-1$

    /**
     * SELECTED_CONNECTION
     */
    public static final String SELECTED_CONNECTION = "SELECTED_CONNECTION"; //$NON-NLS-1$

    private Composite rootArea;
    private StackLayout raLayout;
    private Composite displayArea;
    private Composite loadingArea;
    private SearchOptionsArea optionsArea;

    private String pattern = ""; //$NON-NLS-1$
    private List<IP4Connection> connections = new ArrayList<IP4Connection>();
    private Combo connectionCombo;
    private Combo grepText;
    private CheckboxTreeViewer projectsViewer;
    private CheckboxTreeViewer depotViewer;
    private TabFolder scope;
    private TabItem projectTab;
    private TabItem depotTab;

    private Map<IP4Connection, P4SearchPageSettings> settings = new HashMap<IP4Connection, P4SearchPageSettings>();
    private P4SearchPageSettings currentSettings = null;

    private ISearchPageContainer container;
    private boolean firstLayout = true;

    /**
     * @see org.eclipse.search.ui.ISearchPage#performAction()
     */
    public boolean performAction() {
        IP4Connection connection = getConnection();
        if (connection != null) {
            NewSearchUI.runQueryInBackground(new P4SearchQuery(getConnection(),
                    createOptions(connection)));
            return true;
        }
        return false;
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#dispose()
     */
    @Override
    public void dispose() {
        SessionManager.saveComboHistory(grepText, 10,
                IPreferenceConstants.RECENT_SEARCH_PATTERNS, P4UiSearchPlugin
                        .getDefault().getPreferenceStore());
        saveSettings();
        super.dispose();
    }

    private String getResourcePath(IResource resource) {
        return P4CoreUtils.getResourceActionPath(resource);
    }

    private QueryOptions createOptions(IP4Connection connection) {
        final QueryOptions options = new QueryOptions(pattern);
        optionsArea.fillOptions(options);
        TabItem[] selection = scope.getSelection();
        if (selection.length == 1) {
            if (projectTab == selection[0]) {
                for (IProject project : currentSettings.getSelectedProjects()) {
                    String path = getResourcePath(project);
                    if (path != null) {
                        options.addPath(path.toString());
                        try {
                            project.accept(new IResourceProxyVisitor() {

                                public boolean visit(IResourceProxy proxy)
                                        throws CoreException {
                                    if (proxy.isLinked()) {
                                        options.addPath(getResourcePath(proxy
                                                .requestResource()));
                                    }
                                    return true;
                                }
                            }, IResource.DEPTH_INFINITE);
                        } catch (CoreException e) {
                            PerforceProviderPlugin.logError(e);
                        }
                    }
                }
            } else if (depotTab == selection[0]) {
                for (DepotPath path : currentSettings.getSelectedPaths()) {
                    options.addPath(path.getPath());
                }
            }
        }
        return options;
    }

    private IP4Connection getConnection() {
        IP4Connection connection = null;
        int index = connectionCombo.getSelectionIndex();
        if (index >= 0) {
            connection = this.connections.get(index);
        }
        return connection;
    }

    private void validate() {
        if (this.container != null) {
            boolean enabled = true;

            if (enabled) {
                enabled = getConnection() != null;
            }

            if (enabled) {
                enabled = pattern.length() > 0;
            }

            if (enabled && this.currentSettings != null) {
                TabItem[] selection = scope.getSelection();
                if (selection.length == 1) {
                    if (enabled && projectTab == selection[0]) {
                        enabled = currentSettings.getSelectedProjects().length > 0;
                    }
                    if (enabled && depotTab == selection[0]) {
                        enabled = currentSettings.getSelectedPaths().length > 0;
                    }
                }
            }

            this.container.setPerformActionEnabled(enabled);
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (firstLayout) {
                firstLayout = false;
                rootArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                        true));
                rootArea.getParent().layout(true, true);
            }
            validate();
        }
    }

    /**
     * @see org.eclipse.search.ui.ISearchPage#setContainer(org.eclipse.search.ui.ISearchPageContainer)
     */
    public void setContainer(ISearchPageContainer container) {
        this.container = container;
    }

    private void createSearchArea(Composite parent) {
        Composite searchArea = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        searchArea.setLayout(layout);
        searchArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label connectionLabel = new Label(searchArea, SWT.NONE);
        connectionLabel.setText(Messages.P4SearchPage_Connection);

        connectionCombo = new Combo(searchArea, SWT.READ_ONLY | SWT.DROP_DOWN);
        connectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
                false));
        connectionCombo.addSelectionListener(P4UIUtils
                .createComboSelectionListener(new Runnable() {

                    public void run() {
                        int index = connectionCombo.getSelectionIndex();
                        if (index > -1) {
                            loadSettings(connections.get(index));
                        }
                        validate();
                    }
                }));

        Label grepLabel = new Label(searchArea, SWT.NONE);
        grepLabel.setText(Messages.P4SearchPage_SearchPattern);

        grepText = new Combo(searchArea, SWT.SINGLE | SWT.DROP_DOWN);
        grepText.setLayoutData(GridDataFactory.fillDefaults().grab(true, false)
                .hint(convertWidthInCharsToPixels(50), SWT.DEFAULT).create());
        SessionManager.loadComboHistory(grepText,
                IPreferenceConstants.RECENT_SEARCH_PATTERNS, P4UiSearchPlugin
                        .getDefault().getPreferenceStore());
        grepText.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                pattern = grepText.getText();
                validate();
            }
        });
    }

    private void createLoadingArea(Composite parent) {
        this.loadingArea = new Composite(parent, SWT.NONE);
        GridLayout laLayout = new GridLayout(1, true);
        this.loadingArea.setLayout(laLayout);
        this.loadingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        CLabel loadingLabel = new CLabel(loadingArea, SWT.NONE);
        loadingLabel
                .setText(Messages.P4SearchPage_LoadingSearchableConnections);
        Image loadingImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_LOADING).createImage();
        P4UIUtils.registerDisposal(loadingLabel, loadingImage);
        loadingLabel.setImage(loadingImage);
    }

    private void createOptionsArea(Composite parent) {
        Group options = new Group(parent, SWT.NONE);
        options.setText(Messages.P4SearchPage_SearchOptions);
        GridLayout oLayout = new GridLayout(1, true);
        options.setLayout(oLayout);
        GridData oData = new GridData(SWT.FILL, SWT.FILL, true, false);
        options.setLayoutData(oData);

        optionsArea = new SearchOptionsArea();
        optionsArea.createControl(options);
    }

    private void addCheckItems(ToolBar parent, final CheckboxTreeViewer viewer,
            final Runnable callback) {
        ToolItem check = new ToolItem(parent, SWT.PUSH);
        Image checkImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_CHECK).createImage();
        P4UIUtils.registerDisposal(check, checkImage);
        check.setImage(checkImage);
        check.setToolTipText(Messages.P4SearchPage_CheckAll);
        check.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(true);
                callback.run();
                validate();
            }

        });

        ToolItem uncheck = new ToolItem(parent, SWT.PUSH);
        Image uncheckImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_UNCHECK).createImage();
        P4UIUtils.registerDisposal(uncheck, uncheckImage);
        uncheck.setImage(uncheckImage);
        uncheck.setToolTipText(Messages.P4SearchPage_UncheckAll);
        uncheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setAllChecked(false);
                callback.run();
                validate();
            }

        });
    }

    private Control createProjectsArea(Composite parent) {
        Composite projectArea = new Composite(parent, SWT.NONE);
        projectArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(2)
                .equalWidth(false).margins(0, 0).create());
        projectArea.setLayoutData(GridDataFactory.fillDefaults().grab(true,
                true));

        ToolBar options = new ToolBar(projectArea, SWT.FLAT | SWT.VERTICAL);
        options.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));

        projectsViewer = new CheckboxTreeViewer(projectArea, SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData pvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        pvData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        projectsViewer.getTree().setLayoutData(pvData);
        projectsViewer.setLabelProvider(new WorkbenchLabelProvider());
        projectsViewer.setContentProvider(new ITreeContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                return currentSettings.getProjects();
            }

            public boolean hasChildren(Object element) {
                return false;
            }

            public Object getParent(Object element) {
                return null;
            }

            public Object[] getChildren(Object parentElement) {
                return PerforceContentProvider.EMPTY;
            }
        });
        projectsViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCheckedProjects();
                validate();
            }
        });

        addCheckItems(options, projectsViewer, new Runnable() {

            public void run() {
                updateCheckedProjects();
            }
        });

        return projectArea;
    }

    private void updateCheckedProjects() {
        Object[] elements = this.projectsViewer.getCheckedElements();
        IProject[] projects = new IProject[elements.length];
        System.arraycopy(elements, 0, projects, 0, projects.length);
        this.currentSettings.setSelectedProjects(projects);
    }

    private void updateCheckedPaths() {
        Object[] elements = this.depotViewer.getCheckedElements();
        DepotPath[] paths = new DepotPath[elements.length];
        System.arraycopy(elements, 0, paths, 0, paths.length);
        this.currentSettings.setSelectedPaths(paths);
    }

    private Control createDepotPathArea(Composite parent) {
        Composite depotArea = new Composite(parent, SWT.NONE);
        GridLayout daLayout = new GridLayout(2, false);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        depotArea.setLayout(daLayout);
        depotArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        ToolBar toolbar = new ToolBar(depotArea, SWT.FLAT | SWT.VERTICAL);
        GridData tData = new GridData(SWT.FILL, SWT.BEGINNING, false, true);
        toolbar.setLayoutData(tData);

        depotViewer = new CheckboxTreeViewer(depotArea, SWT.MULTI
                | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        GridData dvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dvData.heightHint = P4UIUtils.VIEWER_HEIGHT_HINT;
        depotViewer.setContentProvider(new ITreeContentProvider() {

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {

            }

            public void dispose() {

            }

            public Object[] getElements(Object inputElement) {
                return currentSettings.getPaths();
            }

            public boolean hasChildren(Object element) {
                return false;
            }

            public Object getParent(Object element) {
                return null;
            }

            public Object[] getChildren(Object parentElement) {
                return PerforceContentProvider.EMPTY;
            }
        });
        depotViewer.setLabelProvider(new WorkbenchLabelProvider());
        depotViewer.getTree().setLayoutData(dvData);

        final ToolItem addItem = new ToolItem(toolbar, SWT.PUSH);
        Image addImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_ADD).createImage();
        P4UIUtils.registerDisposal(addItem, addImage);
        addItem.setImage(addImage);
        addItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                IP4Connection connection = getConnection();
                if (connection != null) {
                    DepotPathDialog pathDialog = new DepotPathDialog(
                            displayArea.getShell(), connection);
                    if (DepotPathDialog.OK == pathDialog.open()) {
                        DepotPath path = new DepotPath(pathDialog.getPath());
                        currentSettings.addPath(path);
                        depotViewer.refresh();
                        depotViewer.setChecked(path, true);
                        updateCheckedPaths();
                    }
                    validate();
                }
            }

        });

        final ToolItem removeItem = new ToolItem(toolbar, SWT.PUSH);
        Image removeImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_DELETE).createImage();
        P4UIUtils.registerDisposal(removeItem, removeImage);
        removeItem.setImage(removeImage);
        removeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Object selected : ((IStructuredSelection) depotViewer
                        .getSelection()).toArray()) {
                    currentSettings.removePath((DepotPath) selected);
                }
                depotViewer.refresh();
                updateCheckedPaths();
                validate();
            }

        });

        new ToolItem(toolbar, SWT.SEPARATOR);
        addCheckItems(toolbar, depotViewer, new Runnable() {

            public void run() {
                updateCheckedPaths();
            }
        });

        depotViewer.addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                updateCheckedPaths();
                validate();
            }
        });

        return depotArea;
    }

    private void createResourcesArea(Composite parent) {
        scope = new TabFolder(parent, SWT.NONE);
        scope.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        scope.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentSettings != null) {
                    currentSettings.setSelectedTab(scope.getSelectionIndex());
                }
                validate();
            }

        });

        projectTab = new TabItem(scope, SWT.NONE);
        projectTab.setText(Messages.P4SearchPage_SearchProjects);

        projectTab.setControl(createProjectsArea(scope));

        depotTab = new TabItem(scope, SWT.NONE);
        depotTab.setText(Messages.P4SearchPage_SearchDepotPaths);

        depotTab.setControl(createDepotPathArea(scope));

        scope.setSelection(projectTab);
    }

    private void loadConnections() {
        final List<IP4Connection> connectRequired = new ArrayList<IP4Connection>();
        final Set<IP4Connection> searchableConnections = new TreeSet<IP4Connection>(
                new Comparator<IP4Connection>() {

                    public int compare(IP4Connection o1, IP4Connection o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
        IP4Connection[] allConnections = P4ConnectionManager.getManager()
                .getConnections();
        for (IP4Connection connection : allConnections) {
            if (!connection.isOffline()) {
                if (connection.isConnected()) {
                    if (connection.isSearchSupported()) {
                        searchableConnections.add(connection);
                    }
                } else {
                    connectRequired.add(connection);
                }
            }
        }
        final Runnable runnable = new Runnable() {

            public void run() {
                if (!P4UIUtils.okToUse(rootArea)) {
                    return;
                }
                raLayout.topControl = displayArea;
                P4SearchPage.this.connections.addAll(searchableConnections);
                for (IP4Connection connection : P4SearchPage.this.connections) {
                    connectionCombo.add(connection.getName());
                }
                if (connectionCombo.getItemCount() > 0) {
                    connectionCombo.select(getInitialConnection());
                    loadSettings(getConnection());
                    if (currentSettings != null) {
                        scope.setSelection(currentSettings.getSelectedTab());
                    }
                } else {
                    scope.setEnabled(false);
                    optionsArea.setEnabled(false);
                    grepText.setEnabled(false);
                    depotViewer.getTree().setEnabled(false);
                    projectsViewer.getTree().setEnabled(false);
                }
            }
        };
        if (!connectRequired.isEmpty()) {
            P4Runner.schedule(new P4Runnable() {

                @Override
                public String getTitle() {
                    return Messages.P4SearchPage_LoadingSearchableConnections2;
                }

                @Override
                public void run(IProgressMonitor monitor) {
                    monitor.beginTask(getTitle(), connectRequired.size());

                    for (IP4Connection connection : connectRequired) {
                        monitor.subTask(connection.getName());
                        if (!connection.isOffline()) {
                            if (!connection.isConnected()) {
                                connection.connect();
                            }
                            if (connection.isSearchSupported()) {
                                searchableConnections.add(connection);
                            }
                        }
                        monitor.worked(1);
                    }
                    monitor.done();
                    PerforceUIPlugin.syncExec(new Runnable() {

                        public void run() {
                            runnable.run();
                            if (P4UIUtils.okToUse(rootArea)) {
                                rootArea.layout(true, true);
                            }
                        }
                    });
                }

            });
        } else {
            runnable.run();
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        this.rootArea = new Composite(parent, SWT.NONE);
        this.raLayout = new StackLayout();
        this.rootArea.setLayout(this.raLayout);

        displayArea = new Composite(rootArea, SWT.NONE);
        GridLayout daLayout = new GridLayout(1, true);
        displayArea.setLayout(daLayout);
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createLoadingArea(rootArea);
        createSearchArea(displayArea);
        createOptionsArea(displayArea);
        createResourcesArea(displayArea);

        raLayout.topControl = loadingArea;
        loadConnections();

        grepText.setFocus();

        setControl(this.rootArea);
        Dialog.applyDialogFont(this.rootArea);
    }

    private void loadSettings(IP4Connection connection) {
        if (connection == null) {
            return;
        }
        IDialogSettings settings = getSettings();
        if (settings != null) {
            P4SearchPageSettings storedSettings = this.settings.get(connection);
            if (storedSettings == null) {
                storedSettings = new P4SearchPageSettings(connection, settings);
                storedSettings.load();
                this.settings.put(connection, storedSettings);
            }
            currentSettings = storedSettings;
            projectsViewer.setInput(currentSettings);
            projectsViewer.setCheckedElements(currentSettings
                    .getSelectedProjects());
            depotViewer.setInput(currentSettings);
            depotViewer.setCheckedElements(currentSettings.getSelectedPaths());
        }
    }

    private void saveSettings() {
        for (P4SearchPageSettings setting : this.settings.values()) {
            setting.save();
        }
        IDialogSettings settings = getSettings();
        if (settings != null) {
            IP4Connection connection = getConnection();
            if (connection != null) {
                settings.put(SELECTED_CONNECTION, connection.getName());
            }
        }
    }

    private int getInitialConnection() {
        int connection = 0;
        IDialogSettings settings = getSettings();
        if (settings != null) {
            String name = settings.get(SELECTED_CONNECTION);
            if (name != null && name.length() > 0) {
                for (int i = 0; i < connections.size(); i++) {
                    if (name.equals(connections.get(i).getName())) {
                        connection = i;
                        break;
                    }
                }
            }
        }
        return connection;
    }

    private IDialogSettings getSettings() {
        IDialogSettings searchSettings = null;
        IDialogSettings settings = P4UiSearchPlugin.getDefault()
                .getDialogSettings();
        if (settings != null) {
            searchSettings = settings.getSection(PAGE_NAME);
            if (searchSettings == null) {
                searchSettings = settings.addNewSection(PAGE_NAME);
            }
        }
        return searchSettings;

    }
}
