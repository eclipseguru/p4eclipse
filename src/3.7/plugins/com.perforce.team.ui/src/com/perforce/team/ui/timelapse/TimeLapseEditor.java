/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.timelapse;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.team.core.history.IFileRevision;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

import com.perforce.team.core.PerforceProviderPlugin;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.timelapse.IAnnotateModel.Type;
import com.perforce.team.ui.timelapse.TimeLapseSlider.IRevisionListener;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class TimeLapseEditor extends EditorPart implements
        IRevisionInputCache {

    /**
     * SHOW_CHANGELIST
     */
    public static final String SHOW_CHANGELIST = "com.perforce.team.ui.timelapse.SHOW_CHANGELIST"; //$NON-NLS-1$

    /**
     * SHOW_BRANCH_HISTORY
     */
    public static final String SHOW_BRANCH_HISTORY = "com.perforce.team.ui.timelapse.SHOW_BRANCH_HISTORY"; //$NON-NLS-1$

    /**
     * SHOW_FILE_ACTIONS
     */
    public static final String SHOW_FILE_ACTIONS = "com.perforce.team.ui.timelapse.SHOW_FILE_ACTIONS"; //$NON-NLS-1$

    private static final int[] SASH_WEIGHTS = new int[] { 80, 20, };

    /**
     * Display area
     */
    private SashForm displayArea;
    private Composite loadingArea;
    private Composite mainArea;
    private Composite outer;
    private TimeLapseSlider slider;
    private TimeLapseChangelistWidget changelistArea;
    private IP4Revision revision = null;
    private IP4Revision[] history;
    private Map<IP4Revision, IStorageEditorInput> inputCache;
    private boolean branch = false;
    private boolean showActions = false;
    private Type type = Type.REVISION;
    private boolean settingInput = false;
    private ITickDecorator decorator;
    private ListenerList listeners;

    /**
     * Create a new time lapse editor
     */
    public TimeLapseEditor() {
        this.inputCache = Collections
                .synchronizedMap(new HashMap<IP4Revision, IStorageEditorInput>());
        this.listeners = new ListenerList();
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
        if (input instanceof TimeLapseInput) {
            setSite(site);
            setInput(input);
            IPreferenceStore store = PerforceUIPlugin.getPlugin()
                    .getPreferenceStore();
            this.branch = store.getBoolean(SHOW_BRANCH_HISTORY);
            this.showActions = store.getBoolean(SHOW_FILE_ACTIONS);
        } else {
            throw new PartInitException(Messages.TimeLapseEditor_WrongInputType);
        }
    }

    private void startEditorLoad(final Runnable uiCallback) {
        final IP4File file = getFile();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public String getTitle() {
                return MessageFormat.format(
                        Messages.TimeLapseEditor_LoadingTimelapseViewFor,
                        file.getActionPath());
            }

            @Override
            public void run(IProgressMonitor monitor) {
                loadEditor(file, monitor);
                if (history != null && uiCallback != null) {
                    PerforceUIPlugin.syncExec(uiCallback);
                }
                notifyLoaded();
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

    /**
     * Should branch history be shown?
     * 
     * @return - true to show branch history
     */
    protected boolean showBranches() {
        return this.branch;
    }

    /**
     * Load any server data needed for the time lapse view. This will be done
     * async from the editor creation and all server commands needed to display
     * a time lapse editor should be run here. Sub-classes should override if
     * needed but always call super.loadEditor().
     * 
     * @param file
     * @param monitor
     */
    protected void loadEditor(IP4File file, IProgressMonitor monitor) {
        clear();
        IFileRevision[] allHistory = file.getCompleteHistory(showBranches(),
                new NullProgressMonitor());
        monitor.setTaskName(Messages.TimeLapseEditor_LoadingHistory);
        List<IP4Revision> depotRevisions = new ArrayList<IP4Revision>();
        for (IFileRevision rev : allHistory) {
            if (rev instanceof IP4Revision) {
                depotRevisions.add(0, (IP4Revision) rev);
            }
        }
        Collections.sort(depotRevisions);
        this.history = depotRevisions.toArray(new IP4Revision[depotRevisions
                .size()]);
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

        this.slider = createSlider(mainArea, getFile());
        configureSlider(this.slider);
        fillToolbar(this.slider.getToolbar());

        createHeader(mainArea);

        createViewer(mainArea);

        this.changelistArea = new TimeLapseChangelistWidget(this.displayArea);

        this.displayArea.setWeights(SASH_WEIGHTS);

        boolean showChangelist = PerforceUIPlugin.getPlugin()
                .getPreferenceStore().getBoolean(SHOW_CHANGELIST);

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

    private TimeLapseSlider createSlider(Composite parent, IP4File file) {
        TimeLapseSlider slider = new TimeLapseSlider(this.history);
        slider.setDrawDecorations(this.showActions);
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
        if (newRevision != this.revision) {
            this.settingInput = true;
            try {
                this.revision = newRevision;

                // Only update changelist area if being displayed
                if (this.displayArea.getMaximizedControl() == null) {
                    this.changelistArea.showRevision(this.revision);
                }

                IEditorInput input = getRevisionInput(this.revision);
                updateDocument(input);
            } finally {
                this.settingInput = false;
            }
        }
    }

    /**
     * Generate a storage editor input from the revision
     * 
     * @param revision
     * @return - storage editor input
     */
    protected abstract IStorageEditorInput generateInput(IP4Revision revision);

    /**
     * @see com.perforce.team.ui.timelapse.IRevisionInputCache#getRevisionInput(com.perforce.team.core.p4java.IP4Revision)
     */
    public IStorageEditorInput getRevisionInput(IP4Revision revision) {
        IStorageEditorInput input = null;
        Map<IP4Revision, IStorageEditorInput> cache = this.inputCache;
        if (cache != null) {
            input = cache.get(revision);
            if (input == null) {
                input = generateInput(revision);
                InputStream in=null;
                try {
                    in=input.getStorage().getContents();
                } catch (CoreException e) {
                    PerforceProviderPlugin.logError(e);
                } finally {
                	if(in!=null){
                		try {
							in.close();
						} catch (IOException e) {
							PerforceProviderPlugin.logError(e);
						}
                	}
                }
                cache.put(revision, input);
            }
        }
        return input;
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
     * @see com.perforce.team.ui.timelapse.IRevisionInputCache#clear()
     */
    public void clear() {
        if (this.inputCache != null) {
            for (IEditorInput input : this.inputCache.values()) {
                dispose(input);
            }
            this.inputCache.clear();
        }
    }

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        clear();
        this.inputCache = null;
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
        loadingLabel.setText(Messages.TimeLapseEditor_LoadingTimelapseView);
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
    protected abstract void createHeader(Composite parent);

    /**
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        setImage();
        setPartName(getFile().getName());

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
                } else {
                    clear();
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
        decorator = new ActionTickDecorator();
        slider.setDecorator(decorator);
    }

    /**
     * Fill the toolbar with any tool items
     * 
     * @param toolbar
     */
    protected void fillToolbar(ToolBar toolbar) {
        final ToolItem branchesItem = new ToolItem(toolbar, SWT.CHECK);
        Image branchesImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_BRANCHES)
                .createImage();
        P4UIUtils.registerDisposal(branchesItem, branchesImage);
        branchesItem
                .setToolTipText(Messages.TimeLapseEditor_ShowBranchingHistory);
        branchesItem.setImage(branchesImage);
        branchesItem.setSelection(this.branch);
        if (!((TimeLapseInput) getEditorInput()).enableBranchHistoy()) {
            branchesItem.setSelection(false);
            branchesItem.setEnabled(false);
        }
        branchesItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                branch = branchesItem.getSelection();
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_BRANCH_HISTORY, branch);
                preserveSelectionLoad();
            }

        });

        final ToolItem changelistItem = new ToolItem(toolbar, SWT.CHECK);
        Image submittedImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CHG_SUBMITTED)
                .createImage();
        P4UIUtils.registerDisposal(changelistItem, submittedImage);
        changelistItem
                .setToolTipText(Messages.TimeLapseEditor_DisplayRevisionDetails);
        changelistItem.setImage(submittedImage);
        changelistItem.setSelection(PerforceUIPlugin.getPlugin()
                .getPreferenceStore().getBoolean(SHOW_CHANGELIST));
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
                        .setValue(SHOW_CHANGELIST,
                                changelistItem.getSelection());
            }

        });

        final ToolItem actionsItem = new ToolItem(toolbar, SWT.CHECK);
        Image actionsImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_SHOW_FILE_ACTIONS)
                .createImage();
        P4UIUtils.registerDisposal(actionsItem, actionsImage);
        actionsItem.setToolTipText(Messages.TimeLapseEditor_DisplayActionIcons);
        actionsItem.setImage(actionsImage);
        actionsItem.setSelection(PerforceUIPlugin.getPlugin()
                .getPreferenceStore().getBoolean(SHOW_FILE_ACTIONS));
        actionsItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                showActions = actionsItem.getSelection();
                getSlider().setDrawDecorations(showActions);
                PerforceUIPlugin.getPlugin().getPreferenceStore()
                        .setValue(SHOW_FILE_ACTIONS, showActions);
            }

        });
    }

    /**
     * Update the shown document
     * 
     * @param wrappedInput
     */
    protected abstract void updateDocument(IEditorInput wrappedInput);

    /**
     * Create viewer needed
     * 
     * @param parent
     */
    protected abstract void createViewer(Composite parent);

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
                } else {
                    clear();
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

    /**
     * Get the file from the input
     * 
     * @return - p4 file
     */
    protected IP4File getFile() {
        return ((TimeLapseInput) getEditorInput()).getFile();
    }

    /**
     * Add time lapse listener to this editor
     * 
     * @param listener
     */
    public void addListener(ITimeLapseListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * Remove time lapse listener from this editor
     * 
     * @param listener
     */
    public void removeListener(ITimeLapseListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    private void notifyLoaded() {
        for (Object listener : this.listeners.getListeners()) {
            ((ITimeLapseListener) listener).loaded(this);
        }
    }

}
