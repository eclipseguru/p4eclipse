/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.P4Runnable;
import com.perforce.team.core.p4java.P4Runner;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.IP4FolderUiConstants;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.actions.DiffCompareAction;
import com.perforce.team.ui.folder.diff.actions.DiffOpenAction;
import com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider.IDiffListener;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.IFileDiffContainerProvider;
import com.perforce.team.ui.folder.diff.model.IFolderDiffListener;
import com.perforce.team.ui.folder.diff.viewer.DiffViewer;
import com.perforce.team.ui.folder.diff.viewer.SourceArea;
import com.perforce.team.ui.folder.diff.viewer.SynchronizedDiffer;
import com.perforce.team.ui.folder.diff.viewer.TargetArea;
import com.perforce.team.ui.folder.preferences.FolderDiffPreferencePage;
import com.perforce.team.ui.folder.preferences.IPreferenceConstants;
import com.perforce.team.ui.p4java.actions.P4Action;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ControlContribution;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class FolderDiffPage extends FormPage implements
        IFileDiffContainerProvider {

    private Composite body;
    private Text descriptionText;
    private SourceArea leftArea;
    private LinkCanvas canvas;
    private TargetArea rightArea;
    private Type type = Type.FLAT;
    private FileDiffContainer container;

    // Actions
    private IAction flatMode;
    private IAction treeMode;
    private IAction compressedMode;
    private IAction showIdenticalPairs;
    private IAction showContentPairs;
    private IAction showUniquePairs;
    private IAction compareMode;
    private IAction refresh;
    private IAction prefPage;

    private ListenerList listeners;
    private ISchedulingRule rule = P4Runner.createRule();

    /**
     * @param editor
     */
    public FolderDiffPage(FormEditor editor) {
        super(editor, "folderDiffPage", Messages.FolderDiffPage_Title); //$NON-NLS-1$
        this.listeners = new ListenerList();
        createActions();
    }

    /**
     * @see com.perforce.team.ui.folder.diff.model.IFileDiffContainerProvider#getContainer()
     */
    public FileDiffContainer getContainer() {
        return this.container;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.model.IFileDiffContainerProvider#addListener(com.perforce.team.ui.folder.diff.model.IFolderDiffListener)
     */
    public void addListener(IFolderDiffListener listener) {
        if (listener != null) {
            this.listeners.add(listener);
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.model.IFileDiffContainerProvider#removeListener(com.perforce.team.ui.folder.diff.model.IFolderDiffListener)
     */
    public void removeListener(IFolderDiffListener listener) {
        if (listener != null) {
            this.listeners.remove(listener);
        }
    }

    /**
     * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        body = managedForm.getForm().getBody();
        body.setLayout(GridLayoutFactory.swtDefaults().numColumns(1)
                .equalWidth(false).margins(2, 2).spacing(0, 2).create());
        FormToolkit toolkit = managedForm.getToolkit();

        descriptionText = new Text(body, SWT.READ_ONLY | SWT.WRAP);
        toolkit.adapt(descriptionText, false, false);
        descriptionText.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, false).create());

        Composite displayArea = toolkit.createComposite(body);
        displayArea.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, true).create());
        displayArea.setLayout(GridLayoutFactory.swtDefaults().numColumns(3)
                .margins(0, 0).spacing(0, 0).create());

        Composite left = toolkit.createComposite(displayArea);
        left.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());
        left.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, true).create());

        canvas = new LinkCanvas(displayArea);
        toolkit.adapt(canvas.getControl(), false, false);

        Composite right = toolkit.createComposite(displayArea);
        right.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0).create());
        right.setLayoutData(GridDataFactory.swtDefaults()
                .align(SWT.FILL, SWT.FILL).grab(true, true).create());

        new Resizer(left, canvas.getControl(), right);

        IFolderDiffInput input = getDiffInput();
        this.leftArea = new SourceArea();
        this.leftArea.createControl(left, toolkit, input,
                input.getLeftConfiguration());

        this.rightArea = new TargetArea();
        this.rightArea.createControl(right, toolkit, input,
                input.getRightConfiguration());

        this.leftArea.setPair(this.rightArea);
        this.rightArea.setPair(this.leftArea);

        IExpansionListener expansionListener = new ExpansionAdapter() {

            @Override
            public void expansionStateChanged(ExpansionEvent e) {
                leftArea.refreshTreeOffset();
                rightArea.refreshTreeOffset();
                canvas.redraw();
            }

        };
        this.leftArea.getFilter().getSection()
                .addExpansionListener(expansionListener);
        this.rightArea.getFilter().getSection()
                .addExpansionListener(expansionListener);

        this.rightArea.getSlider().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                canvas.redraw();
            }

        });
        IDiffListener listener = new IDiffListener() {

            public void update(IP4DiffFile file) {
                canvas.refresh();
                rightArea.getSlider().refresh();
            }

            public void load(IP4DiffFile file) {

            }
        };
        ((FileDiffContentProvider) this.leftArea.getViewer().getViewer()
                .getContentProvider()).addDiffListener(listener);

        new SynchronizedDiffer(this.leftArea.getViewer().getViewer(),
                this.rightArea.getViewer().getViewer());

        this.rightArea.getSlider().addScrollListener(this.canvas.getControl());

        addMenu(this.leftArea.getViewer());
        hookDoubleClick(this.leftArea.getViewer());

        addMenu(this.rightArea.getViewer());
        hookDoubleClick(this.rightArea.getViewer());

        Type initialType = null;
        try {
            initialType = Type.valueOf(PerforceUiFolderPlugin.getDefault()
                    .getPreferenceStore()
                    .getString(IPreferenceConstants.COMPARE_DISPLAY_MODE));
        } catch (Exception e) {
            initialType = null;
        }
        setType(initialType);

        switch (getType()) {
        case FLAT:
            flatMode.setChecked(true);
            break;
        case TREE:
            treeMode.setChecked(true);
            break;
        case COMPRESSED:
            compressedMode.setChecked(true);
            break;
        default:
            break;
        }

        refreshInput();
    }

    /**
     * Generate and display the differences for the current configurations. This
     * method must be called from the UI-thread.
     */
    public void loadDifferences() {
        this.canvas.setEnabled(false);
        this.canvas.redraw();
        this.leftArea.getViewer().showLoading();
        this.rightArea.getViewer().showLoading();
        final String leftFilterSpec = this.leftArea.getFilter().getFilterSpec();
        final String rightFilterSpec = this.rightArea.getFilter()
                .getFilterSpec();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                IFolderDiffInput input = getDiffInput();
                container = input.generateDiffs(leftFilterSpec,
                        rightFilterSpec, monitor);
                int identical = container.getIdenticalCount();
                int different = container.getContentCount();

                String differing = different == 1
                        ? Messages.FolderDiffPage_SingleDifferingFile
                        : MessageFormat.format(
                                Messages.FolderDiffPage_MultipleDifferingFile,
                                different);
                String same = identical == 1
                        ? Messages.FolderDiffPage_SingleIdenticalFile
                        : MessageFormat.format(
                                Messages.FolderDiffPage_MultipleIdenticalFiles,
                                identical);
                final String description = differing + " " + same; //$NON-NLS-1$
                PerforceUIPlugin.asyncExec(new Runnable() {

                    public void run() {
                        if (P4UIUtils.okToUse(getPartControl())) {
                            descriptionText.setText(description);
                            leftArea.setContainer(container);
                            rightArea.setContainer(container);
                            canvas.setContainer(container);
                            leftArea.setFilterExpanded(false);
                            rightArea.setFilterExpanded(false);
                            canvas.setEnabled(true);
                            canvas.setDiffAreas(leftArea, rightArea);
                            rightArea.getSlider().scrollToTop();
                            resetSlider();
                        }
                    }
                });
                for (Object listener : listeners.getListeners()) {
                    ((IFolderDiffListener) listener)
                            .diffsGenerated(FolderDiffPage.this.container);
                }
            }

            @Override
            public String toString() {
                return Messages.FolderDiffPage_LoadingDiffs;
            }

        }, rule);
    }

    private IFolderDiffInput getDiffInput() {
        return (IFolderDiffInput) getEditorInput();
    }

    private void addMenu(DiffViewer viewer) {
        MenuManager manager = new MenuManager();
        TreeViewer tViewer = viewer.getViewer();

        P4UIUtils.addStandardPerforceMenus(manager);

        Menu menu = manager.createContextMenu(tViewer.getTree());
        tViewer.getTree().setMenu(menu);
        getSite().registerContextMenu(manager, tViewer);
    }

    private void hookDoubleClick(final DiffViewer viewer) {
        viewer.getViewer().addDoubleClickListener(new IDoubleClickListener() {

            public void doubleClick(DoubleClickEvent event) {
                IP4DiffFile diff = getSelectedDiff(viewer);
                if (diff != null) {
                    P4Action action = null;
                    if (compareMode.isChecked()
                            && diff.getDiff().getStatus() == Status.CONTENT) {
                        action = new DiffCompareAction();
                    } else {
                        action = new DiffOpenAction();
                    }
                    action.selectionChanged(null, new StructuredSelection(diff));
                    action.run(null);
                }
            }
        });
    }

    private IP4DiffFile getSelectedDiff(DiffViewer viewer) {
        Object first = ((IStructuredSelection) viewer.getViewer()
                .getSelection()).getFirstElement();
        return P4CoreUtils.convert(first, IP4DiffFile.class);
    }

    /**
     * Set display type
     * 
     * @param type
     */
    public void setType(Type type) {
        if (type != this.type) {
            this.type = type;
            PerforceUiFolderPlugin
                    .getDefault()
                    .getPreferenceStore()
                    .setValue(IPreferenceConstants.COMPARE_DISPLAY_MODE,
                            this.type.toString());
            this.leftArea.setType(this.type);
            this.rightArea.setType(this.type);
            this.canvas.refresh();
        }
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUiFolderPlugin.getDefault().getPreferenceStore();
    }

    private void resetSlider() {
        rightArea.resetSlider();
        canvas.refresh();
    }

    private void createActions() {
        refresh = new Action(Messages.FolderDiffPage_Refresh,
                PerforceUIPlugin.getDescriptor(IPerforceUIConstants.IMG_DIFF)) {

            @Override
            public void run() {
                loadDifferences();
            }

        };

        showContentPairs = new Action(Messages.FolderDiffPage_ShowContentPair,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                leftArea.getViewer().showContentPairs(checked);
                rightArea.getViewer().showContentPairs(checked);
                resetSlider();
                getPreferenceStore().setValue(
                        IPreferenceConstants.SHOW_CONTENT, checked);
            }
        };
        showContentPairs.setChecked(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_CONTENT));
        showContentPairs.setImageDescriptor(PerforceUiFolderPlugin
                .getDescriptor(IP4FolderUiConstants.FILTER_CONTENT));

        showUniquePairs = new Action(Messages.FolderDiffPage_ShowUniqueFiles,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                leftArea.getViewer().showUniquePairs(checked);
                rightArea.getViewer().showUniquePairs(checked);
                resetSlider();
                getPreferenceStore().setValue(IPreferenceConstants.SHOW_UNIQUE,
                        checked);
            }
        };
        showUniquePairs.setChecked(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_UNIQUE));
        showUniquePairs.setImageDescriptor(PerforceUiFolderPlugin
                .getDescriptor(IP4FolderUiConstants.FILTER_UNIQUE));

        showIdenticalPairs = new Action(Messages.FolderDiffPage_ShowIdentical,
                IAction.AS_CHECK_BOX) {

            @Override
            public void run() {
                boolean checked = isChecked();
                leftArea.getViewer().showIdenticalPairs(checked);
                rightArea.getViewer().showIdenticalPairs(checked);
                getPreferenceStore().setValue(
                        IPreferenceConstants.SHOW_IDENTICAL, checked);
            }
        };
        showIdenticalPairs.setChecked(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_IDENTICAL));
        showIdenticalPairs.setImageDescriptor(PerforceUiFolderPlugin
                .getDescriptor(IP4FolderUiConstants.FILTER_IDENTICAL));

        flatMode = new Action(Messages.FolderDiffPage_FlatMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                setType(Type.FLAT);
            }
        };
        flatMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_FLAT_LAYOUT));

        treeMode = new Action(Messages.FolderDiffPage_TreeMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                setType(Type.TREE);
            }
        };
        treeMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_TREE_LAYOUT));

        compressedMode = new Action(Messages.FolderDiffPage_CompressedMode,
                IAction.AS_RADIO_BUTTON) {

            @Override
            public void run() {
                setType(Type.COMPRESSED);
            }
        };
        compressedMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_COMPRESSED_LAYOUT));

        compareMode = new Action(Messages.FolderDiffPage_CompareMode,
                Action.AS_CHECK_BOX) {

            @Override
            public void run() {
                PerforceUiFolderPlugin
                        .getDefault()
                        .getPreferenceStore()
                        .setValue(IPreferenceConstants.COMPARE_SELECT_MODE,
                                isChecked());
            }
        };
        compareMode.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_COMPARE));
        compareMode.setChecked(PerforceUiFolderPlugin.getDefault()
                .getPreferenceStore()
                .getBoolean(IPreferenceConstants.COMPARE_SELECT_MODE));

        prefPage = new Action(Messages.FolderDiffPage_OpenPreferences,
                Action.AS_PUSH_BUTTON) {

            @Override
            public void run() {
                P4UIUtils.openPreferencePage(FolderDiffPreferencePage.ID);
            }
        };
        prefPage.setImageDescriptor(PerforceUIPlugin
                .getDescriptor(IPerforceUIConstants.IMG_PREFERENCES));
    }

    /**
     * @return type
     */
    public Type getType() {
        return this.type;
    }

    private void refreshInput() {
        final IFolderDiffInput input = getDiffInput();
        P4Runner.schedule(new P4Runnable() {

            @Override
            public void run(IProgressMonitor monitor) {
                input.refreshInput(monitor);
                PerforceUIPlugin.syncExec(new Runnable() {

                    public void run() {
                        leftArea.updateTitle(input.getLeftConfiguration());
                        rightArea.updateTitle(input.getRightConfiguration());
                    }
                });
            }

            @Override
            public String getTitle() {
                return Messages.FolderDiffPage_RefreshingInput;
            }

        }, rule);
    }

    /**
     * Fill the toolbar
     * 
     * @param toolbar
     */
    public void fillToolbar(IToolBarManager toolbar) {
        // toolbar.add(showIdenticalPairs);
        toolbar.add(showUniquePairs);
        toolbar.add(showContentPairs);
        toolbar.add(new Separator());
        toolbar.add(flatMode);
        toolbar.add(treeMode);
        toolbar.add(compressedMode);
        toolbar.add(new Separator());
        toolbar.add(compareMode);
        toolbar.add(prefPage);
        toolbar.add(new Separator());
        toolbar.add(new ControlContribution(
                "com.perforce.team.ui.folder.diff.editor.refresh") { //$NON-NLS-1$

            @Override
            protected Control createControl(Composite parent) {
                Button button = new Button(parent, SWT.FLAT);
                Image image = refresh.getImageDescriptor().createImage();
                P4UIUtils.registerDisposal(button, image);
                button.setImage(image);
                button.setToolTipText(refresh.getText());
                button.setText(Messages.FolderDiffPage_GenerateDiffs);
                button.setLayoutData(GridDataFactory.swtDefaults()
                        .grab(true, true)
                        .minSize(SWT.DEFAULT, image.getBounds().y).create());
                button.addSelectionListener(new SelectionAdapter() {

                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        refresh.run();
                    }

                });
                return button;
            }
        });
    }
}
