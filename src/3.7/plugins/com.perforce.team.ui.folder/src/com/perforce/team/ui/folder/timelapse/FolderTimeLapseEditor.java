/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.timelapse;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.perforce.p4java.client.IClient;
import com.perforce.p4java.core.file.IFileRevisionData;
import com.perforce.p4java.core.file.IFileSpec;
import com.perforce.p4java.exception.P4JavaError;
import com.perforce.p4java.exception.P4JavaException;
import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4ChangelistRevision;
import com.perforce.team.core.p4java.IP4Connection;
import com.perforce.team.core.p4java.IP4Folder;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4File;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.core.p4java.builder.P4FileSpecBuilder;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceContentProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.text.PerforceUiTextPlugin;
import com.perforce.team.ui.text.timelapse.NodeModelTimeLapseEditor;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;
import com.perforce.team.ui.timelapse.TimeLapseChangelistWidget;
import com.perforce.team.ui.timelapse.TimeLapseEditor;
import com.perforce.team.ui.timelapse.TimeLapseSlider;
import com.perforce.team.ui.timelapse.TimeLapseSlider.IRevisionListener;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class FolderTimeLapseEditor extends EditorPart {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.folder.timelapse.FolderTimeLapseEditor"; //$NON-NLS-1$

    /**
     * SHOW_INCREMENTAL
     */
    public static final String SHOW_INCREMENTAL = "com.perforce.team.ui.text.timelapse.folder.SHOW_INCREMENTAL"; //$NON-NLS-1$

    private static final int[] SASH_WEIGHTS = new int[] { 80, 20, };

    /**
     * Display area
     */
    private SashForm displayArea;
    private Composite loadingArea;
    private Composite mainArea;
    private Composite outer;
    private SashForm sash;
    private Composite viewerArea1;
    private Label viewerLabel1;
    private TreeViewer viewer;
    private Composite viewerArea2;
    private Label viewerLabel2;
    private TreeViewer viewer2;
    private FolderLabelDecorator folderDecorator1;
    private FolderLabelDecorator folderDecorator2;
    private FolderLabelProvider styleProvider;
    private FolderLabelProvider styleProvider2;
    private TimeLapseSlider slider;

    private ToolItem upItem;
    private ToolItem homeItem;

    private SashForm changelistSash;
    private TimeLapseChangelistWidget changelistArea;
    private TimeLapseChangelistWidget changelistArea2;

    private FolderTickDecorator decorator;
    private FolderTickFormatter formatter;
    private Color formatterBg = null;
    private IP4Revision revision = null;
    private IP4Revision revision2 = null;
    private IP4Revision[] history;
    private Type type = Type.REVISION;
    private boolean showActions = false;
    private boolean settingInput = false;
    private boolean settingSelection1 = false;
    private boolean settingSelection2 = false;
    private boolean showIncremental = false;
    private RootEntry rootFolder = null;
    private FolderEntry currentFolder = null;
    private Map<String, FileEntry> allFiles;
    private Set<Integer> aheads;
    private String root;

    /**
     * Create a new time lapse editor
     */
    public FolderTimeLapseEditor() {
    }

    /**
     * Get main control of editor
     * 
     * @return - composite
     */
    public Composite getControl() {
        return this.displayArea;
    }

    /**
     * Get current revision
     * 
     * @return - current revision
     */
    protected IP4Revision getRevision() {
        return this.revision;
    }

    /**
     * Get all revision
     * 
     * @return - all revisions
     */
    protected IP4Revision[] getRevisions() {
        return this.history;
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor) {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs() {

    }

    /**
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        if (input instanceof FolderTimeLapseInput) {
            setSite(site);
            setInput(input);
            IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                    .getPreferenceStore();
            this.showActions = store
                    .getBoolean(TimeLapseEditor.SHOW_FILE_ACTIONS);
            this.showIncremental = store.getBoolean(SHOW_INCREMENTAL);
            this.formatterBg = new Color(P4UIUtils.getDisplay(),
                    PreferenceConverter.getColor(store,
                            NodeModelTimeLapseEditor.TICK_CHANGE_COLOR));
            P4UIUtils.registerDisposal(getControl(), this.formatterBg);
        } else {
            throw new PartInitException(
                    "Wrong input type, must be TimeLapseInput");
        }
    }

    private IP4Folder getFolder() {
        return ((FolderTimeLapseInput) getEditorInput()).getFolder();
    }

    private void startEditorLoad(final Runnable uiCallback) {
        final IP4Folder folder = getFolder();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(
                        Messages.FolderTimeLapseEditor_LoadingTimelapseViewFor,
                        folder.getActionPath());
            }

            @Override
            public void run(IProgressMonitor monitor) {
                loadEditor(folder, monitor);
                if (history != null && uiCallback != null) {
                    PerforceUIPlugin.syncExec(uiCallback);
                }
            }

        });
    }

    /**
     * Is the time lapse editor loading?
     * 
     * @return - true if the loading area is showing, false otherwise
     */
    public boolean isLoading() {
        return this.loadingArea == ((StackLayout) outer.getLayout()).topControl;
    }

    private IP4ChangelistRevision[] getHistory(IP4Folder folder) {
    	if(folder==null)
    		return new IP4ChangelistRevision[0];
        return folder.getCompleteHistory(new NullProgressMonitor());
    }

    /**
     * Load any server data needed for the time lapse view. This will be done
     * async from the editor creation and all server commands needed to display
     * a time lapse editor should be run here. Sub-classes should override if
     * needed but always call super.loadEditor().
     * 
     * @param folder
     * @param monitor
     */
    protected void loadEditor(IP4Folder folder, IProgressMonitor monitor) {
        this.root = folder.getRemotePath();
        if (this.root == null) {
            this.root = folder.getFirstWhereRemotePath();
            if (this.root != null) {
                folder = folder.getConnection().getFolder(this.root, true);
            }
        }
        IP4ChangelistRevision[] allHistory = getHistory(folder);
        monitor.setTaskName(Messages.FolderTimeLapseEditor_LoadingHistory);
        Arrays.sort(allHistory);

        Map<String, Integer> haveMap = new HashMap<String, Integer>();

        if(folder!=null){
	        try {
	        	IP4Connection conn = folder.getConnection();
	        	if(conn!=null){
	        		IClient client = conn.getClient();
	        		if(client!=null){
			            List<IFileSpec> haves = client.haveList(
			                            P4FileSpecBuilder.makeFileSpecList(folder
			                                    .getActionPath()));
			            if (haves != null) {
			                for (IFileSpec spec : haves) {
			                    int have = spec.getEndRevision();
			                    haveMap.put(spec.getDepotPathString(), have);
			                }
			            }
	        		}
	        	}
	        } catch (P4JavaException e) {
	            PerforceProviderPlugin.logError(e);
	        } catch (P4JavaError e) {
	            PerforceProviderPlugin.logError(e);
	        }
        }

        this.allFiles = new HashMap<String, FileEntry>();
        this.aheads = new HashSet<Integer>();

        this.history = allHistory;

        this.rootFolder = new RootEntry(this.history);
        this.currentFolder = this.rootFolder;
        Map<String, FolderEntry> folders = new HashMap<String, FolderEntry>();

        for (IP4ChangelistRevision revision : allHistory) {
            IFileRevisionData[] revisions = revision.getRevisions();

            for (IFileRevisionData file : revisions) {
                String path = file.getDepotFileName();
                if (root!=null && path.startsWith(root)) {
                    String shortened = path.substring(root.length() + 1);
                    FileEntry fileToAdd = null;
                    if (!this.allFiles.containsKey(path)) {
                        if (shortened.lastIndexOf('/') == -1) {
                            fileToAdd = new FileEntry(file, this.rootFolder,
                                    this.rootFolder);
                        } else {
                            String parentPath = shortened.substring(0,
                                    shortened.lastIndexOf('/'));
                            FolderEntry parent = loadFolder(parentPath, folders);
                            fileToAdd = new FileEntry(file, parent,
                                    this.rootFolder);
                        }
                        this.allFiles.put(path, fileToAdd);
                    } else {
                        fileToAdd = this.allFiles.get(path);
                    }
                    fileToAdd.add(file);

                    if (haveMap.containsKey(path)) {
                        int have = haveMap.get(path);
                        fileToAdd.setHaveRevision(have);
                        if (have < file.getRevision()) {
                            this.aheads.add(file.getChangelistId());
                        }
                    }
                }
            }
        }
        this.rootFolder.complete();
    }

    private FolderEntry loadFolder(String path, Map<String, FolderEntry> folders) {
        FolderEntry folder = null;
        if (folders.containsKey(path)) {
            return folders.get(path);
        } else if (path.indexOf('/') == -1) {
            if (folders.containsKey(path)) {
                folder = folders.get(path);
            } else {
                folder = new FolderEntry(path, this.rootFolder);
                folder.setRoot(this.rootFolder);
                folders.put(path, folder);
            }
        } else {
            String parentPath = path.substring(0, path.lastIndexOf('/'));
            FolderEntry parent = null;
            if (folders.containsKey(parentPath)) {
                parent = folders.get(parentPath);
            } else {
                parent = loadFolder(parentPath, folders);
            }
            String name = path.substring(parentPath.length() + 1);
            folder = new FolderEntry(name, parent);
            folder.setRoot(this.rootFolder);
            folders.put(path, folder);
        }
        return folder;
    }

    private void displayLoading() {
        ((StackLayout) outer.getLayout()).topControl = loadingArea;
        outer.layout(true, true);
    }

    private void displayEditor() {
        GridLayout maLayout = new GridLayout(1, true);
        maLayout.marginHeight = 0;
        maLayout.marginWidth = 0;
        maLayout.verticalSpacing = 2;
        mainArea = new Composite(this.displayArea, SWT.NONE);
        mainArea.setLayout(maLayout);
        mainArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        this.slider = createSlider(mainArea, getFolder());
        configureSlider(this.slider);
        fillToolbar(this.slider.getToolbar());

        createHeader(mainArea);

        createViewer(mainArea);

        this.changelistSash = new SashForm(this.displayArea, SWT.HORIZONTAL);
        GridLayout csLayout = new GridLayout(1, true);
        csLayout.marginHeight = 0;
        csLayout.marginWidth = 0;
        this.changelistSash.setLayout(csLayout);
        this.changelistSash.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                true, false));
        this.changelistArea = new TimeLapseChangelistWidget(this.changelistSash);
        this.changelistArea2 = new TimeLapseChangelistWidget(
                this.changelistSash);

        this.displayArea.setWeights(SASH_WEIGHTS);

        updateSplit();

        boolean showChangelist = PerforceUIPlugin.getPlugin()
                .getPreferenceStore()
                .getBoolean(TimeLapseEditor.SHOW_CHANGELIST);

        if (showChangelist) {
            this.displayArea.setMaximizedControl(null);
        } else {
            this.displayArea.setMaximizedControl(mainArea);
        }
        showEditor();
    }

    private void showEditor() {
        ((StackLayout) outer.getLayout()).topControl = displayArea;
        outer.layout(true, true);
    }

    /**
     * Get time lapse slider
     * 
     * @return - time lapse slider or null if not created yet
     */
    protected TimeLapseSlider getSlider() {
        return this.slider;
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty() {
        return false;
    }

    /**
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    private TimeLapseSlider createSlider(Composite parent, IP4Folder folder) {
        TimeLapseSlider slider = new TimeLapseSlider(this.history, new Type[] {
                Type.CHANGELIST, Type.DATE, });
        slider.setDrawDecorations(true);
        slider.createControl(parent);
        slider.setListener(new IRevisionListener() {

            public void revisionChanged(IP4Revision revision) {
                showRevision(revision);
            }

            public void displayChanged(Type type) {
                updateDisplay(type);
            }

        });
        return slider;
    }

    /**
     * Display type changed
     * 
     * @param type
     */
    protected void updateDisplay(Type type) {
        this.type = type;
    }

    /**
     * Get display type
     * 
     * @return - type
     */
    protected Type getDisplayType() {
        return this.type;
    }

    /**
     * @return the settingInput
     */
    public boolean isSettingInput() {
        return this.settingInput;
    }

    private void showRevision(IP4Revision newRevision) {
        if (newRevision != this.revision2) {
            this.settingInput = true;
            try {
                this.revision2 = newRevision;
                int index = Arrays.binarySearch(this.history, this.revision2) - 1;
                if (index > -1) {
                    this.revision = this.history[index];
                } else {
                    this.revision = newRevision;
                }

                viewerLabel1.setText(MessageFormat.format(
                        Messages.FolderTimeLapseEditor_ChangelistNum,
                        this.revision.getChangelist()));
                viewerLabel2.setText(MessageFormat.format(
                        Messages.FolderTimeLapseEditor_ChangelistNum,
                        this.revision2.getChangelist()));

                // Only update changelist area if being displayed
                if (this.displayArea.getMaximizedControl() == null) {
                    this.changelistArea.showRevision(this.revision);
                    this.changelistArea2.showRevision(this.revision2);
                }

                this.styleProvider.setRevision(this.revision, this.root);
                this.folderDecorator1.setRevision(this.revision);
                this.styleProvider2.setRevision(this.revision2, this.root);
                this.folderDecorator2.setRevision(this.revision2);
                ISelection selection = this.viewer.getSelection();
                setCurrentFolder(this.currentFolder);
                if (!selection.isEmpty()
                        && !selection.equals(this.viewer.getSelection())) {
                    formatter.setFile(null);
                    decorator.setFile(null);
                    getSlider().redraw();
                    getSlider().update();
                }
            } finally {
                this.settingInput = false;
            }
        }
    }

    /**
     * Dispose callback for each input in the cache called when
     * {@link #dispose()} is called.
     * 
     * @param input
     */
    protected void dispose(IEditorInput input) {
        // Does nothing, subclasses should override
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        if (this.decorator != null) {
            this.decorator.dispose();
        }
    }

    private void setImage() {
        ImageDescriptor descriptor = getEditorInput().getImageDescriptor();
        if (descriptor != null) {
            Image editorImage = descriptor.createImage();
            setTitleImage(editorImage);
            P4UIUtils.registerDisposal(displayArea, editorImage);
        }
    }

    private void createLoadingArea(Composite parent) {
        loadingArea = new Composite(parent, SWT.NONE);
        ((StackLayout) parent.getLayout()).topControl = loadingArea;

        loadingArea.setLayout(new GridLayout(1, true));
        loadingArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Label loadingLabel = new Label(loadingArea, SWT.NONE);
        loadingLabel
                .setText(Messages.FolderTimeLapseEditor_LoadingTimelapseView);
        ProgressBar loadingBar = new ProgressBar(loadingArea, SWT.INDETERMINATE
                | SWT.HORIZONTAL);
        loadingBar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        loadingBar.setEnabled(true);
    }

    /**
     * Create any header widgets for this editor
     * 
     * @param parent
     */
    protected void createHeader(Composite parent) {
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        setImage();
        setPartName(((FolderTimeLapseInput) getEditorInput()).getName());

        outer = new Composite(parent, SWT.NONE);
        StackLayout oLayout = new StackLayout();
        oLayout.marginHeight = 0;
        oLayout.marginWidth = 0;
        outer.setLayout(oLayout);
        outer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createLoadingArea(outer);

        this.displayArea = new SashForm(outer, SWT.VERTICAL);
        this.displayArea.setBackground(this.displayArea.getDisplay()
                .getSystemColor(SWT.COLOR_DARK_GRAY));
        GridLayout daLayout = new GridLayout(1, true);
        daLayout.marginHeight = 0;
        daLayout.marginWidth = 0;
        this.displayArea.setLayout(daLayout);
        this.displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        startEditorLoad(new Runnable() {

            public void run() {
                if (!displayArea.isDisposed()) {
                    preUpdateEditor();
                    displayEditor();
                    updateEditor();
                    showRevision(history[history.length - 1]);
                }
            }
        });
    }

    private IP4Revision findRevision(IP4Revision revision) {
        if (revision != null) {
            IP4Revision[] revisions = getRevisions();
            for (int i = 0; i < revisions.length; i++) {
                if (revision.getContentIdentifier().equals(
                        revisions[i].getContentIdentifier())) {
                    return revisions[i];
                }
            }
        }
        return null;
    }

    /**
     * Configure time lapse slider
     * 
     * @param slider
     */
    protected void configureSlider(TimeLapseSlider slider) {
        if (decorator != null) {
            decorator.dispose();
        }
        decorator = new FolderTickDecorator();
        formatter = new FolderTickFormatter();
        formatter.setColor(this.formatterBg);
        formatter.setAheads(this.aheads);
        slider.setFormatter(formatter);
        slider.setDecorator(decorator);
        // slider.setPositioner(positioner);
    }

    /**
     * Fill the toolbar with any tool items
     * 
     * @param toolbar
     */
    protected void fillToolbar(ToolBar toolbar) {
        final ToolItem changelistItem = new ToolItem(toolbar, SWT.CHECK);
        Image submittedImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CHG_SUBMITTED)
                .createImage();
        P4UIUtils.registerDisposal(changelistItem, submittedImage);
        changelistItem
                .setToolTipText(Messages.FolderTimeLapseEditor_DisplayRevisionDetails);
        changelistItem.setImage(submittedImage);
        changelistItem.setSelection(PerforceUIPlugin.getPlugin()
                .getPreferenceStore()
                .getBoolean(TimeLapseEditor.SHOW_CHANGELIST));
        changelistItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (changelistItem.getSelection()) {
                    changelistArea.showRevision(getRevision());
                    displayArea.setMaximizedControl(null);
                } else {
                    displayArea.setMaximizedControl(mainArea);
                }
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(TimeLapseEditor.SHOW_CHANGELIST,
                                changelistItem.getSelection());
            }

        });

        final ToolItem actionsItem = new ToolItem(toolbar, SWT.CHECK);
        Image actionsImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SHOW_FILE_ACTIONS)
                .createImage();
        P4UIUtils.registerDisposal(actionsItem, actionsImage);
        actionsItem
                .setToolTipText(Messages.FolderTimeLapseEditor_DisplayActionIcons);
        actionsItem.setImage(actionsImage);
        actionsItem.setSelection(PerforceUIPlugin.getPlugin()
                .getPreferenceStore()
                .getBoolean(TimeLapseEditor.SHOW_FILE_ACTIONS));
        actionsItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showActions = actionsItem.getSelection();
                getSlider().setDrawDecorations(showActions);
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(TimeLapseEditor.SHOW_FILE_ACTIONS,
                                showActions);
            }

        });

        final ToolItem clearItem = new ToolItem(toolbar, SWT.PUSH);
        Image clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();
        P4UIUtils.registerDisposal(clearItem, clearImage);
        clearItem.setToolTipText(Messages.FolderTimeLapseEditor_ClearSelection);
        clearItem.setImage(clearImage);
        clearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                viewer.setSelection(StructuredSelection.EMPTY);
                decorator.setFile(null);
                formatter.setFile(null);
                getSlider().redraw();
                getSlider().update();
            }

        });

        this.homeItem = new ToolItem(toolbar, SWT.PUSH);
        Image homeImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_HOME).createImage();
        P4UIUtils.registerDisposal(homeItem, homeImage);
        homeItem.setToolTipText(Messages.FolderTimeLapseEditor_GoToRootFolder);
        homeItem.setImage(homeImage);
        homeItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                setCurrentFolder(rootFolder);
            }

        });

        this.upItem = new ToolItem(toolbar, SWT.PUSH);
        Image upImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_UP).createImage();
        P4UIUtils.registerDisposal(upItem, upImage);
        upItem.setToolTipText(Messages.FolderTimeLapseEditor_GoUpOneLevel);
        upItem.setImage(upImage);
        upItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (currentFolder != rootFolder) {
                    Object parent = currentFolder.getParent(currentFolder);
                    if (parent != null) {
                        setCurrentFolder((FolderEntry) parent);
                    }
                }
            }

        });

        final ToolItem splitItem = new ToolItem(toolbar, SWT.CHECK);
        Image splitImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_SPLIT).createImage();
        P4UIUtils.registerDisposal(splitItem, splitImage);
        splitItem
                .setToolTipText(Messages.FolderTimeLapseEditor_IncrementalFolderComparison);
        splitItem.setImage(splitImage);
        splitItem.setSelection(this.showIncremental);
        splitItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showIncremental = splitItem.getSelection();
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(SHOW_INCREMENTAL, showIncremental);
                updateSplit();
            }

        });

    }

    private void updateSplit() {
        ((GridData) viewerLabel1.getLayoutData()).exclude = !showIncremental;
        viewerLabel1.setVisible(showIncremental);
        ((GridData) viewerLabel2.getLayoutData()).exclude = !showIncremental;
        viewerLabel2.setVisible(showIncremental);
        if (showIncremental) {
            sash.setMaximizedControl(null);
            changelistSash.setMaximizedControl(null);
        } else {
            sash.setMaximizedControl(viewerArea2);
            changelistSash.setMaximizedControl(changelistArea2.getControl());
        }
        sash.layout(true, true);
    }

    private void setCurrentFolder(FolderEntry folder) {
        this.currentFolder = folder;
        homeItem.setEnabled(this.currentFolder != rootFolder);
        upItem.setEnabled(currentFolder.getParent(currentFolder) != null);
        Object[] children = this.currentFolder.getChildren(folder);
        this.viewer.setInput(children);
        this.viewer2.setInput(children);
    }

    /**
     * Create viewer needed
     * 
     * @param parent
     */
    protected void createViewer(Composite parent) {
        sash = new SashForm(parent, SWT.HORIZONTAL);
        GridLayout sLayout = new GridLayout(1, true);
        sLayout.marginHeight = 0;
        sLayout.marginWidth = 0;
        sash.setLayout(sLayout);
        sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewerArea1 = new Composite(sash, SWT.NONE);
        GridLayout va1Layout = new GridLayout(1, true);
        va1Layout.marginHeight = 0;
        va1Layout.marginWidth = 0;
        va1Layout.verticalSpacing = 0;
        viewerArea1.setLayout(va1Layout);
        viewerArea1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewerLabel1 = new Label(viewerArea1, SWT.NONE);
        viewerLabel1
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        viewerLabel1.setText(Messages.FolderTimeLapseEditor_Changelist);

        viewer = new TreeViewer(viewerArea1, SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER);
        viewer.setContentProvider(new PerforceContentProvider(viewer, true));
        styleProvider = new FolderLabelProvider();
        folderDecorator1 = new FolderLabelDecorator();
        viewer.setLabelProvider(new DecoratingStyledCellLabelProvider(
                styleProvider, folderDecorator1, null));
        viewer.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                settingSelection1 = true;
                try {
                    IStructuredSelection selection = (IStructuredSelection) viewer
                            .getSelection();
                    if (!settingSelection2) {
                        viewer2.setSelection(selection);
                    }
                    if (selection.size() == 1) {
                        Object first = selection.getFirstElement();
                        if (first instanceof FileEntry) {
                            decorator.setFile((FileEntry) first);
                            formatter.setFile((FileEntry) first);
                        } else {
                            decorator.setFile(null);
                            formatter.setFile(null);
                        }
                        getSlider().redraw();
                        getSlider().update();
                    }
                } finally {
                    settingSelection1 = false;
                }
            }
        });
        viewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IEntry) {
                    IEntry entry = (IEntry) element;
                    if (entry.getFirst() > revision.getChangelist()) {
                        return false;
                    } else if (entry instanceof FileEntry) {
                        IFileRevisionData data = ((FileEntry) entry)
                                .getData(revision.getChangelist());
                        if (data != null) {
                            // Don't display files where the latest action based
                            // on the selected revision is a delete
                            if (P4File.isActionDelete(data.getAction())
                                    && data.getChangelistId() < revision
                                            .getChangelist()) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        });
        viewer.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer
                        .getSelection();
                if (selection.size() == 1) {
                    Object first = selection.getFirstElement();
                    if (first instanceof FolderEntry) {
                        setCurrentFolder((FolderEntry) first);
                    }
                }
            }
        });

        viewerArea2 = new Composite(sash, SWT.NONE);
        GridLayout va2Layout = new GridLayout(1, true);
        va2Layout.marginHeight = 0;
        va2Layout.marginWidth = 0;
        va2Layout.verticalSpacing = 0;
        viewerArea2.setLayout(va2Layout);
        viewerArea2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewerLabel2 = new Label(viewerArea2, SWT.NONE);
        viewerLabel2
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        viewerLabel2.setText(Messages.FolderTimeLapseEditor_Changelist);

        viewer2 = new TreeViewer(viewerArea2, SWT.SINGLE | SWT.V_SCROLL
                | SWT.H_SCROLL | SWT.BORDER);
        viewer2.setContentProvider(new PerforceContentProvider(viewer2, true));
        styleProvider2 = new FolderLabelProvider();
        folderDecorator2 = new FolderLabelDecorator();
        viewer2.setLabelProvider(new DecoratingStyledCellLabelProvider(
                styleProvider2, folderDecorator2, null));
        viewer2.getTree().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));
        viewer2.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        viewer2.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {
                settingSelection2 = true;
                try {
                    IStructuredSelection selection = (IStructuredSelection) viewer2
                            .getSelection();
                    if (!settingSelection1) {
                        viewer.setSelection(selection);
                    }
                    if (selection.size() == 1) {
                        Object first = selection.getFirstElement();
                        if (first instanceof FileEntry) {
                            decorator.setFile((FileEntry) first);
                            formatter.setFile((FileEntry) first);
                        } else {
                            decorator.setFile(null);
                            formatter.setFile(null);
                        }
                        getSlider().redraw();
                        getSlider().update();
                    }
                } finally {
                    settingSelection2 = false;
                }
            }
        });
        viewer2.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (element instanceof IEntry) {
                    IEntry entry = (IEntry) element;
                    if (entry.getFirst() > revision2.getChangelist()) {
                        return false;
                    } else if (entry instanceof FileEntry) {
                        IFileRevisionData data = ((FileEntry) entry)
                                .getData(revision2.getChangelist());
                        if (data != null) {
                            // Don't display files where the latest action based
                            // on the selected revision is a delete
                            if (P4File.isActionDelete(data.getAction())
                                    && data.getChangelistId() < revision2
                                            .getChangelist()) {
                                return false;
                            }
                        }
                    }
                }
                return true;
            }
        });
        viewer2.addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IStructuredSelection selection = (IStructuredSelection) viewer2
                        .getSelection();
                if (selection.size() == 1) {
                    Object first = selection.getFirstElement();
                    if (first instanceof FolderEntry) {
                        setCurrentFolder((FolderEntry) first);
                    }
                }
            }
        });

        sash.setWeights(new int[] { 50, 50, });
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        this.displayArea.setFocus();
    }

    /**
     * Re-load the editor while preserving selection of current tick
     */
    protected void preserveSelectionLoad() {
        displayLoading();
        // Reload editor model with new branch options
        startEditorLoad(new Runnable() {

            public void run() {
                if (!displayArea.isDisposed()) {
                    IP4Revision newRevision = findRevision(getRevision());
                    preUpdateEditor();
                    configureSlider(slider);
                    slider.resetRevisions(history, newRevision);
                    if (newRevision != null) {
                        showRevision(newRevision);
                    } else {
                        showRevision(history[history.length - 1]);
                    }
                    showEditor();
                    updateEditor();
                }
            }
        });
    }

    /**
     * Update any editor ui after a load finishes but before the revision is
     * set. Sub-classes should override but call super.preUpdateEditor().
     */
    protected void preUpdateEditor() {

    }

    /**
     * Update any editor ui after a load finishes. Sub-classes should override
     * but call super.updateEditor().
     */
    protected void updateEditor() {

    }

}
