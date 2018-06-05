/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.DecorationLabel;
import com.perforce.team.ui.P4FormUIUtils;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.changelists.Folder;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.PerforceUiFolderPlugin;
import com.perforce.team.ui.folder.diff.editor.FolderDiffFilterArea;
import com.perforce.team.ui.folder.diff.editor.SubmittedChangelistWidget;
import com.perforce.team.ui.folder.diff.editor.input.IDiffConfiguration;
import com.perforce.team.ui.folder.diff.editor.input.IFolderDiffInput;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer;
import com.perforce.team.ui.folder.preferences.IPreferenceConstants;

import java.text.MessageFormat;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class DiffArea {

    private Section section;
    private Composite area;
    private FolderDiffFilterArea filterArea;
    private DiffViewer viewer;
    private SubmittedChangelistWidget changelist;
    private CLabel headerLabel;
    private DecorationLabel uniqueCountLabel;
    private DiffArea pair = null;
    private boolean settingSelection = false;
    private int treeOffset = -1;

    private FileDiffContainer container = null;

    /**
     * Create a new diff area
     */
    public DiffArea() {
    }

    /**
     * Create diff viewer
     * 
     * @return diff viewer
     */
    protected abstract DiffViewer createViewer();

    /**
     * Create area layout
     * 
     * @return grid layout
     */
    protected abstract GridLayout createAreaLayout();

    /**
     * Get area
     * 
     * @return main area composite
     */
    protected Composite getArea() {
        return this.area;
    }

    /**
     * Create control
     * 
     * @param parent
     * @param toolkit
     * @param input
     * @param config
     */
    public void createControl(Composite parent, final FormToolkit toolkit,
            IFolderDiffInput input, IDiffConfiguration config) {
        filterArea = new FolderDiffFilterArea(config.getOptions(),
                input.getConnection());
        filterArea.createControl(parent, toolkit);

        section = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR
                | ExpandableComposite.LEFT_TEXT_CLIENT_ALIGNMENT);
        section.titleBarTextMarginWidth = 1;
        section.marginWidth = 0;
        section.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createHeaderLabel(section, toolkit);
        updateTitle(config);

        area = toolkit.createComposite(section, SWT.BORDER);
        section.setClient(area);
        area.setLayout(createAreaLayout());
        area.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        P4FormUIUtils.registerExpansionSpaceGrabber(section, parent);

        this.viewer = createViewer();
        this.viewer.createControl(area, toolkit);
        this.viewer.showContentPairs(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_CONTENT));
        this.viewer.showUniquePairs(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_UNIQUE));
        this.viewer.showIdenticalPairs(getPreferenceStore().getBoolean(
                IPreferenceConstants.SHOW_IDENTICAL));
        this.viewer
                .getViewer()
                .getTree()
                .setLayoutData(
                        GridDataFactory.fillDefaults().grab(true, true)
                                .create());

        hookSelection();

        createChangelistControl(parent, toolkit);
    }

    /**
     * Create changelist control
     * 
     * @param parent
     * @param toolkit
     */
    public void createChangelistControl(Composite parent, FormToolkit toolkit) {
        this.changelist = createChangelistArea(parent, toolkit);
    }

    /**
     * Get diff viewer
     * 
     * @return diff viewer
     */
    public DiffViewer getViewer() {
        return this.viewer;
    }

    /**
     * Get filter area
     * 
     * @return filter area
     */
    public FolderDiffFilterArea getFilter() {
        return this.filterArea;
    }

    /**
     * Get unique count
     * 
     * @param container
     * @return unique fill count
     */
    protected abstract int getUniqueCount(FileDiffContainer container);

    /**
     * Get group container
     * 
     * @param container
     * @return grouped diff container
     */
    protected abstract GroupedDiffContainer getGroupContainer(
            FileDiffContainer container);

    /**
     * Set file diff container
     * 
     * @param container
     */
    public void setContainer(FileDiffContainer container) {
        this.container = container;
        int unique = getUniqueCount(this.container);
        if (unique == 1) {
            this.uniqueCountLabel.setText(Messages.DiffArea_SingleUniqueFile);
        } else {
            this.uniqueCountLabel.setText(MessageFormat.format(
                    Messages.DiffArea_MultipleUniqueFiles, unique));
        }
        ((GridData) this.uniqueCountLabel.getControl().getLayoutData()).widthHint = this.uniqueCountLabel
                .getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
        GroupedDiffContainer groupContainer = null;
        if (this.container != null) {
            groupContainer = getGroupContainer(container);
        }
        this.viewer.setInput(this.container, groupContainer);
        this.section.layout(true);
    }

    /**
     * Set diff area pair
     * 
     * @param pair
     */
    public void setPair(DiffArea pair) {
        this.pair = pair;
        if (this.pair != null) {
            this.pair.getFilter().getSection()
                    .addExpansionListener(new ExpansionAdapter() {

                        @Override
                        public void expansionStateChanging(ExpansionEvent e) {
                            getFilter().setExpanded(e.getState());
                        }

                    });
        }
    }

    /**
     * Set filter expanded
     * 
     * @param expanded
     */
    public void setFilterExpanded(boolean expanded) {
        getFilter().setExpanded(expanded);
        refreshTreeOffset();
        if (this.pair != null) {
            this.pair.refreshTreeOffset();
        }
    }

    /**
     * Select diff file pair
     * 
     * @param file
     */
    protected void selectPair(IP4DiffFile file) {
        IP4DiffFile pair = null;
        if (file != null) {
            pair = file.getPair();
        }
        setPairSelection(pair);
    }

    /**
     * Set pair selection
     * 
     * @param element
     */
    protected void setPairSelection(Object element) {
        if (element != null) {
            this.pair.getViewer().getViewer()
                    .setSelection(new StructuredSelection(element), true);
        } else {
            this.pair.getViewer().getViewer()
                    .setSelection(StructuredSelection.EMPTY, true);
        }
    }

    private void hookSelection() {
        this.viewer.getViewer().addSelectionChangedListener(
                new ISelectionChangedListener() {

                    public void selectionChanged(SelectionChangedEvent event) {
                        settingSelection = true;
                        Object first = getFirstElement(viewer);
                        try {
                            if (pair != null && !pair.isSettingSelection()) {
                                if (first instanceof IP4DiffFile) {
                                    selectPair((IP4DiffFile) first);
                                } else if (first instanceof FileDiffElement) {
                                    setPairSelection(first);
                                } else if (first instanceof Folder) {
                                    setPairSelection(first);
                                } else {
                                    selectPair(null);
                                }
                            }
                        } finally {
                            settingSelection = false;
                        }
                        loadChangelist(first, changelist);
                    }
                });
    }

    /**
     * @return true if setting selection, false otherwise
     */
    protected boolean isSettingSelection() {
        return this.settingSelection;
    }

    /**
     * Load changelist for selected element
     * 
     * @param element
     * @param changelistArea
     */
    protected void loadChangelist(Object element,
            final SubmittedChangelistWidget changelistArea) {
        IP4Revision revision = P4CoreUtils.convert(element, IP4Revision.class);
        if (P4UIUtils.okToUse(section)) {
            changelistArea.setRevision(revision);
        }
    }

    private Object getFirstElement(DiffViewer viewer) {
        return ((IStructuredSelection) viewer.getViewer().getSelection())
                .getFirstElement();
    }

    private Composite createHeaderLabel(Section section, FormToolkit toolkit) {
        Composite header = P4FormUIUtils.createSectionTextClient(toolkit,
                section, 2, SWT.WRAP);
        headerLabel = new CLabel(header, SWT.NONE);
        headerLabel.setFont(section.getFont());
        headerLabel.setForeground(section.getTitleBarForeground());
        headerLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, true,
                true));
        uniqueCountLabel = new DecorationLabel(header);
        uniqueCountLabel.setForeground(section.getTitleBarForeground());
        uniqueCountLabel.getControl().setLayoutData(
                new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
        return header;
    }

    private IPreferenceStore getPreferenceStore() {
        return PerforceUiFolderPlugin.getDefault().getPreferenceStore();
    }

    private SubmittedChangelistWidget createChangelistArea(Composite parent,
            FormToolkit toolkit) {
        Section changelist = toolkit.createSection(parent,
                ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE
                        | ExpandableComposite.EXPANDED);
        changelist.setText(Messages.DiffArea_SubmittedChangelistDetails);
        changelist.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Composite changelistArea = toolkit
                .createComposite(changelist, SWT.NONE);
        changelist.setClient(changelistArea);
        changelistArea.setLayout(GridLayoutFactory.swtDefaults().margins(0, 0)
                .create());
        changelistArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true));

        SubmittedChangelistWidget clWidget = new SubmittedChangelistWidget(
                changelistArea, toolkit);

        P4FormUIUtils.registerExpansionSpaceGrabber(changelist, parent);

        return clWidget;
    }

    /**
     * Refresh offset returned from {@link #getTreeOffset()}
     */
    public void refreshTreeOffset() {
        int offset = 0;
        Control base = getViewer().getViewer().getTree().getParent();
        while (base != section) {
            if (base == null) {
                break;
            }
            offset += base.getBounds().y;
            base = base.getParent();
        }
        offset += section.getBounds().y;
        treeOffset = offset;
    }

    /**
     * Get tree y offset
     * 
     * @return y offset of tree widget
     */
    public int getTreeOffset() {
        if (treeOffset == -1) {
            refreshTreeOffset();
        }
        return treeOffset;
    }

    /**
     * @param type
     */
    public void setType(Type type) {
        this.viewer.setType(type);
    }

    /**
     * Update title
     * 
     * @param config
     */
    public void updateTitle(IDiffConfiguration config) {
        headerLabel.setText(config.getLabel(config));
        ImageDescriptor descriptor = config.getImageDescriptor(config);
        if (descriptor != null) {
            Image image = descriptor.createImage();
            P4UIUtils.registerDisposal(headerLabel, image);
            headerLabel.setImage(image);
        } else {
            headerLabel.setImage(null);
        }
        section.layout(true, true);
    }

}
