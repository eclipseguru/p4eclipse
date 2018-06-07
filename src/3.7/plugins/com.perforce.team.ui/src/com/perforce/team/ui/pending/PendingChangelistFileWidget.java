/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.pending;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;

import com.perforce.team.core.Tracing;
import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.changelists.ChangelistFileWidget;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.changelists.FolderFileLabelProvider;
import com.perforce.team.ui.diff.DiffContentProvider;
import com.perforce.team.ui.diff.IFileDiffer;
import com.perforce.team.ui.preferences.IPreferenceConstants;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 *
 */
public class PendingChangelistFileWidget extends ChangelistFileWidget implements
        ICheckable {

    /**
     * Pending label provider
     */
    public class PendingLabelProvider extends FolderFileLabelProvider {

        /**
         * Pending label provider
         */
        public PendingLabelProvider() {

        }

        /**
         * @see com.perforce.team.ui.changelists.FolderFileLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IP4File) {
                String path = null;
                if (getType() == Type.FLAT) {
                    path = ((IP4File) element)
                            .getActionPath(IP4Resource.Type.REMOTE);
                } else {
                    path = ((IP4File) element).getName();
                }
                String decorated = decorator.getLabelDecorator().decorateText(
                        path, element);
                if (decorated != null) {
                    path = decorated;
                }
                return path;
            }
            return super.getColumnText(element, columnIndex);
        }

    }

    // Default dimensions
    private static final int DEFAULT_WIDTH = 500;
    private static final int DEFAULT_HEIGHT = 150;

    private boolean small = false;
    private boolean checkedOnly = false;
    private boolean settingInput = false;

    private int startingSize;

    // Configuration options
    private boolean includeFilesLabel;

    private Label filesLabel;
	private IP4Resource[] origChecked;

    /**
     * @param small
     *            true if control should be half height
     */
    public PendingChangelistFileWidget(boolean small) {
        this(small, false, false);
    }

    /**
     *
     * @param small
     * @param includeFilesLabel
     * @param includeSelectButtons
     */
    public PendingChangelistFileWidget(boolean small,
            boolean includeFilesLabel, boolean includeSelectButtons) {
        this.small = small;
        this.includeFilesLabel = includeFilesLabel;
        this.checkedOnly = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.CHANGELIST_SHOW_CHECKED_ONLY);
    }

    /**
     * Select all files in this widget
     */
    public void selectAll() {
        if (checkedOnly) {
            checkedOnly = false;
            refresh();
            checkedOnly = true;
        }
        setAllChecked(true);
    }

    private void updateCheckedCount() {
        int checked = 0;
        for (Object check : getCheckedElements()) {
            if (check instanceof IP4File) {
                checked++;
            }
        }
        filesLabel.setText(MessageFormat.format(
                Messages.PendingChangelistFileWidget_FilesNumSelected, checked,
                startingSize));
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createControl(Composite parent) {
        this.createControl(parent, new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement,
                    Object element) {
                if (checkedOnly){
                	if(!settingInput) {
	                    if (element instanceof IP4Resource) {
	                        return getChecked(element);
	                    }
	                }else{
	                	if(element instanceof IP4Resource){
	                		for(IP4Resource file: origChecked){
	                			if(element==file)
	                				return true;
	                		}
	                	}
	                }
                	return false;
                }
                return true;
            }
        });
        if (this.includeFilesLabel) {
            updateCheckedCount();
            getCheckViewer().addCheckStateListener(new ICheckStateListener() {

                public void checkStateChanged(CheckStateChangedEvent event) {
                    updateCheckedCount();
                }
            });
        }
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#configureViewer(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected void configureViewer(TreeViewer viewer) {
        super.configureViewer(viewer);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.widthHint = DEFAULT_WIDTH;
        if (small) {
            data.heightHint = DEFAULT_HEIGHT / 2;
        } else {
            data.heightHint = DEFAULT_HEIGHT;
        }
        viewer.getTree().setLayoutData(data);
        addCheckStateListener(new ICheckStateListener() {

            public void checkStateChanged(CheckStateChangedEvent event) {
                Object checked = event.getElement();
                if (checked instanceof IP4Resource) {
                    IP4Resource resource = (IP4Resource) checked;
                    DiffContentProvider provider = (DiffContentProvider) getViewer()
                            .getContentProvider();
                    IFileDiffer differ = provider.getDiffer(resource);
                    if (differ != null && differ.diffGenerated(resource)) {
                        getCheckViewer().setSubtreeChecked(checked,
                                event.getChecked());
                    }
                } else {
                    getCheckViewer().setChecked(checked, !event.getChecked());
                }
            }
        });
        P4UIUtils.trackMovedFiles(getCheckViewer());
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        createExpandOptions(toolbar);
        final ToolItem showCheckedOnly = new ToolItem(toolbar, SWT.CHECK);
        showCheckedOnly
                .setToolTipText(Messages.PendingChangelistFileWidget_DisplayCheckedFilesOnly);
        checkedOnly = PerforceUIPlugin.getPlugin().getPreferenceStore()
                .getBoolean(IPreferenceConstants.CHANGELIST_SHOW_CHECKED_ONLY);
        showCheckedOnly.setSelection(checkedOnly);
        showCheckedOnly.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkedOnly = showCheckedOnly.getSelection();
                refresh();
                PerforceUIPlugin
                        .getPlugin()
                        .getPreferenceStore()
                        .setValue(
                                IPreferenceConstants.CHANGELIST_SHOW_CHECKED_ONLY,
                                showCheckedOnly.getSelection());
            }

        });

        final Image checkedImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_CHECKED).createImage();
        P4UIUtils.registerDisposal(showCheckedOnly, checkedImage);
        showCheckedOnly.setImage(checkedImage);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#createViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected TreeViewer createViewer(Composite parent) { // VIRTUAL for CheckboxTreeViewer does not work
        return new CheckboxTreeViewer(parent, SWT.BORDER | SWT.MULTI
                | SWT.V_SCROLL | SWT.H_SCROLL);
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#createContentProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected ITreeContentProvider createContentProvider(TreeViewer viewer) {
        return new PendingFileContentProvider(viewer, true, this) {

            @Override
            protected void updateResource(IP4Resource resource) {
                super.updateResource(resource);
                // Sync check state between resource and children
                getCheckViewer().setSubtreeChecked(resource,
                        getChecked(resource));

            }

            @Override
            public boolean hasChildren(Object element) {
            	if(element instanceof IP4File)
            		return false;
            	return super.hasChildren(element);
            }

        };
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#createLabelProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected FolderFileLabelProvider createLabelProvider(TreeViewer viewer) {
        return new PendingLabelProvider();
    }

    private CheckboxTreeViewer getCheckViewer() {
        return (CheckboxTreeViewer) getViewer();
    }

    /**
     * Set all elements checked or unchecked
     *
     * @param checked
     */
    public void setAllChecked(boolean checked) {
    	for(TreeItem item:getCheckViewer().getTree().getItems())
    		item.setChecked(checked);
//        getCheckViewer().setAllChecked(checked);
        syncMovedFiles();
        updateCheckedCount();
    }

    private void syncMovedFiles() {
        for (Object element : getCheckViewer().getCheckedElements()) {
            P4UIUtils.syncMovedFile(this, element, true);
        }
    }

    /**
     * Set the checked elements
     *
     * @param elements
     */
    public void setCheckedElements(final Object[] elements) {
		Tracing.printExecTime(() -> {
			// DO NOT use this, it slows down for large set (e.g.,5000) of elements.
            // getCheckViewer().setCheckedElements(elements);
			TreeItem[] items = getCheckViewer().getTree().getItems();
			for (TreeItem item : items) {
				for (Object obj : elements)
					if (item.getData() == obj) {
						item.setChecked(true);
						break;
					}
			}

		}, "SUBMIT", getClass().getSimpleName() + ":setCheckedElement()");
        syncMovedFiles();
        updateCheckedCount();
    }

    /**
     * Get checked files
     *
     * @return - non-null array of p4 files
     */
    public IP4File[] getCheckedFiles() {
        List<IP4File> files = new ArrayList<IP4File>();
        for (Object checked : getCheckedElements()) {
            if (checked instanceof IP4File) {
                files.add((IP4File) checked);
            }
        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Get unchecked files
     *
     * @return - non-null array of p4 files
     */
    public IP4File[] getUncheckedFiles() {
//    	TreeItem[] items = getCheckViewer().getTree().getItems();
        List<IP4File> files = new ArrayList<IP4File>();
        for(TreeItem item:getCheckViewer().getTree().getItems()){
            if (!item.getChecked() && item.getData() instanceof IP4File) {
                files.add((IP4File) item.getData());
            }
        }
//        for (IP4Resource resource : getFiles()) {
//            if (resource instanceof IP4File && !getChecked(resource)) {
//                files.add((IP4File) resource);
//            }
//        }
        return files.toArray(new IP4File[files.size()]);
    }

    /**
     * Get checked elements in tree
     *
     * @return - checked elements
     */
    public Object[] getCheckedElements() {
        return getCheckViewer().getCheckedElements();
    }

    /**
     * @see org.eclipse.jface.viewers.ICheckable#addCheckStateListener(org.eclipse.jface.viewers.ICheckStateListener)
     */
    public void addCheckStateListener(ICheckStateListener listener) {
        getCheckViewer().addCheckStateListener(listener);
    }

    /**
     * @see org.eclipse.jface.viewers.ICheckable#getChecked(java.lang.Object)
     */
    public boolean getChecked(Object element) {
        return getCheckViewer().getChecked(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ICheckable#removeCheckStateListener(org.eclipse.jface.viewers.ICheckStateListener)
     */
    public void removeCheckStateListener(ICheckStateListener listener) {
        getCheckViewer().removeCheckStateListener(listener);
    }

    /**
     * @see org.eclipse.jface.viewers.ICheckable#setChecked(java.lang.Object,
     *      boolean)
     */
    public boolean setChecked(Object element, boolean state) {
        return getCheckViewer().setChecked(element, state);
    }

    /**
     * Set input of changelist file widget
     *
     * @param files
     * @param checked
     */
    public void setInput(IP4Resource[] files, IP4Resource[] checked) {
        this.settingInput = true;
        origChecked=checked;
        setFiles(files);
        refreshInput();
        this.settingInput = false;
        setCheckedElements(checked);
//        refresh();
    }

    /**
     * @see com.perforce.team.ui.changelis)ts.ChangelistFileWidget#setFiles(com.perforce.team.core.p4java.IP4Resource[])
     */
    @Override
    public void setFiles(IP4Resource[] files) {
        this.startingSize = files.length;
        super.setFiles(files);
        updateCheckedCount();
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#refreshInput()
     */
    @Override
    public void refreshInput() {
        if (!this.settingInput) {
            Object[] checked = getCheckedElements();
            this.settingInput = true;
            super.refreshInput();
            this.settingInput = false;
            setCheckedElements(checked);
            refresh();
        } else {
            super.refreshInput();
        }
    }

    /**
     * Create a toolbar
     *
     * @param parent
     */
    @Override
    protected void createToolbar(Composite parent) {
        if (this.includeFilesLabel) {
            Composite barArea = new Composite(parent, SWT.NONE);
            GridLayout baLayout = new GridLayout(2, false);
            baLayout.marginHeight = 0;
            baLayout.marginWidth = 0;
            barArea.setLayout(baLayout);
            barArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

            this.filesLabel = new Label(barArea, SWT.NONE);
            this.filesLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
                    true, false));

            super.createToolbar(barArea);
        } else {
            super.createToolbar(parent);
        }
    }

    /**
     * @see com.perforce.team.ui.changelists.ChangelistFileWidget#getTypePreference()
     */
    @Override
    public Type getTypePreference() {
        return Type.FLAT;
    }

}
