/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.shelve;

import com.perforce.team.core.p4java.IP4File;
import com.perforce.team.core.p4java.IP4PendingChangelist;
import com.perforce.team.core.p4java.IP4Resource;
import com.perforce.team.core.p4java.IP4ShelveFile;
import com.perforce.team.core.p4java.P4ShelveFile;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceLabelProvider;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.decorator.OverlayIcon;
import com.perforce.team.ui.dialogs.P4StatusDialog;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class UpdateShelveDialog extends P4StatusDialog {

    /**
     * SECTION_NAME
     */
    public static final String SECTION_NAME = "UPDATE_SHELVE_DIALOG"; //$NON-NLS-1$

    /**
     * Shelve options
     */
    public enum Option {

        /**
         * UPDATE
         */
        UPDATE,

        /**
         * ADD
         */
        ADD,

        /**
         * DELETE
         */
        DELETE,

        /**
         * UNCHANGED
         */
        UNCHANGED,

        /**
         * REPLACE
         */
        REPLACE,
    }

    /**
     * Change to shelf
     */
    public static class ShelveChange {

        /**
         * File
         */
        public IP4File file;

        /**
         * Option
         */
        public Option option = Option.UNCHANGED;
    }

    /**
     * Shelve label provider
     */
    private class ShelveLabelProvider extends PerforceLabelProvider {

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof P4ShelveFile) {
                element = ((P4ShelveFile) element).getFile();
            }
            if (element instanceof IP4Resource) {
                String path = ((IP4Resource) element)
                        .getActionPath(IP4Resource.Type.REMOTE);
                String decorated = decorator.getLabelDecorator().decorateText(
                        path, element);
                if (decorated != null) {
                    path = decorated;
                }
                return path;
            }
            return super.getColumnText(element, columnIndex);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof P4ShelveFile) {
                element = ((P4ShelveFile) element).getFile();
            }
            return super.getColumnImage(element, columnIndex);
        }

    }

    /**
     * Shelve label provider
     */
    private class ShelveChangeLabelProvider extends ShelveLabelProvider {

        private Map<OverlayIcon, Image> changeImages = new HashMap<OverlayIcon, Image>();

        private Image decorateChange(Image base, String descriptorPath) {
            Image decorated = null;
            Rectangle imageBounds = base.getBounds();
            ImageDescriptor[] descriptor = new ImageDescriptor[] { PerforceUIPlugin
                    .getDescriptor(descriptorPath), };
            ImageData descData = descriptor[0].getImageData();
            int width = imageBounds.width + descData.width;
            int height = Math.max(imageBounds.height, descData.height);
            OverlayIcon icon = new OverlayIcon(base, descriptor,
                    new int[] { IPerforceUIConstants.ICON_TOP_LEFT }, width,
                    height, descData.width, 0);
            decorated = changeImages.get(icon);
            if (decorated == null) {
                decorated = icon.createImage();
                changeImages.put(icon, decorated);
            }
            return decorated;
        }

        @Override
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof ShelveChange) {
                element = ((ShelveChange) element).file;
            }
            return super.getColumnText(element, columnIndex);
        }

        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            if (element instanceof ShelveChange) {
                ShelveChange change = (ShelveChange) element;
                Image image = super.getColumnImage(change.file, columnIndex);
                switch (change.option) {
                case UPDATE:
                    image = decorateChange(image,
                            IPerforceUIConstants.IMG_SHELVE_UPDATE);
                    break;
                case ADD:
                    image = decorateChange(image,
                            IPerforceUIConstants.IMG_SHELVE_ADD);
                    break;
                case DELETE:
                    image = decorateChange(image,
                            IPerforceUIConstants.IMG_SHELVE_DELETE);
                    break;
                case UNCHANGED:
                    image = decorateChange(image,
                            IPerforceUIConstants.IMG_EMPTY);
                    break;
                default:
                    break;
                }
                return image;
            }
            return null;
        }

        /**
         * @see com.perforce.team.ui.PerforceLabelProvider#dispose()
         */
        @Override
        public void dispose() {
            super.dispose();
            for (Image image : changeImages.values()) {
                image.dispose();
            }
        }

    }

    private ViewerSorter sorter = new ViewerSorter() {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            String path1 = null;
            String path2 = null;
            if (e1 instanceof IP4Resource && e2 instanceof IP4Resource) {
                path1 = ((IP4Resource) e1).getRemotePath();
                path2 = ((IP4Resource) e2).getRemotePath();
            } else if (e1 instanceof ShelveChange && e2 instanceof ShelveChange) {
                ShelveChange sc1 = (ShelveChange) e1;
                ShelveChange sc2 = (ShelveChange) e2;
                if (sc1.file != null) {
                    path1 = sc1.file.getRemotePath();
                }
                if (sc2.file != null) {
                    path2 = sc2.file.getRemotePath();
                }
            }
            if (path1 != null && path2 != null) {
                return path1.compareTo(path2);
            } else {
                return super.compare(viewer, e1, e2);
            }
        }

    };

    private IP4Resource[] shelved;
    private IP4Resource[] pending;
    private IP4Resource[] selected;
    private ShelveChange[] changes = new ShelveChange[0];
    private Option initialOption;

    private Composite displayArea;

    private Button replaceButton;
    private Button updateButton;
    private Button deleteButton;

    private Image checkImage;
    private Image uncheckImage;

    private Label previewLabel;
    private TableViewer previewViewer;

    private TableViewer pendingViewer;
    private TableViewer deleteViewer;

    private List<IP4Resource> pendingChecked = new ArrayList<IP4Resource>();
    private List<IP4Resource> deleteChecked = new ArrayList<IP4Resource>();

    /**
     * @param parent
     * @param list
     * @param selected
     * @param shelvedFiles
     * @param pendingFiles
     * @param option
     */
    public UpdateShelveDialog(Shell parent, IP4PendingChangelist list,
            IP4Resource[] selected, IP4Resource[] shelvedFiles,
            IP4Resource[] pendingFiles, Option option) {
        super(parent);
        setTitle(MessageFormat.format(
                Messages.UpdateShelveDialog_ShelveChangelist, list.getId()));
        setModalResizeStyle();
        this.selected = getFiles(selected);
        this.shelved = getFiles(shelvedFiles);
        this.pending = getFiles(pendingFiles);
        this.initialOption = option;
    }

    /**
     * @see com.perforce.team.ui.dialogs.P4StatusDialog#getSectionName()
     */
    @Override
    protected String getSectionName() {
        return SECTION_NAME;
    }

    private void updatePreview() {
        Map<String, ShelveChange> shelveChanges = new HashMap<String, ShelveChange>();
        boolean valid = false;
        if (replaceButton.getSelection()) {
            valid = true;
            for (IP4Resource resource : this.shelved) {
                ShelveChange change = new ShelveChange();
                change.option = Option.DELETE;
                change.file = ((IP4ShelveFile) resource).getFile();
                shelveChanges.put(resource.getRemotePath(), change);
            }
            for (IP4Resource resource : this.pending) {
                ShelveChange change = shelveChanges.get(resource
                        .getRemotePath());
                if (change == null) {
                    change = new ShelveChange();
                    change.file = (IP4File) resource;
                    change.option = Option.ADD;
                    shelveChanges.put(resource.getRemotePath(), change);
                } else {
                    change.option = Option.UPDATE;
                }
                change.file = (IP4File) resource;
            }
        } else if (updateButton.getSelection()) {
            for (IP4Resource resource : shelved) {
                ShelveChange change = new ShelveChange();
                change.file = ((IP4ShelveFile) resource).getFile();
                shelveChanges.put(resource.getRemotePath(), change);
            }
            Object[] updates = pendingChecked.toArray();
            valid = updates.length > 0;
            for (Object update : updates) {
                IP4File file = (IP4File) update;
                ShelveChange change = shelveChanges.get(file.getRemotePath());
                if (change == null) {
                    change = new ShelveChange();
                    change.file = file;
                    change.option = Option.ADD;
                    shelveChanges.put(file.getRemotePath(), change);
                } else {
                    change.option = Option.UPDATE;
                }
                change.file = file;
            }
        } else if (deleteButton.getSelection()) {
            Object[] deletes = this.deleteChecked.toArray();
            valid = deletes.length > 0;
            for (IP4Resource resource : this.shelved) {
                ShelveChange change = new ShelveChange();
                change.file = ((IP4ShelveFile) resource).getFile();
                shelveChanges.put(resource.getRemotePath(), change);
            }
            for (Object delete : deletes) {
                IP4File file = ((IP4ShelveFile) delete).getFile();
                ShelveChange change = shelveChanges.get(file.getRemotePath());
                if (change == null) {
                    change = new ShelveChange();
                    change.file = file;
                    change.option = Option.DELETE;
                    shelveChanges.put(file.getRemotePath(), change);
                } else {
                    change.option = Option.DELETE;
                }
            }
        }
        int nonDeletes = 0;
        this.changes = shelveChanges.values().toArray(
                new ShelveChange[shelveChanges.size()]);
        for (ShelveChange change : this.changes) {
            if (change.option != Option.DELETE) {
                nonDeletes++;
            }
        }
        previewLabel.setText(MessageFormat.format(
                Messages.UpdateShelveDialog_ShelvedFilesUpdatePreview,
                nonDeletes));
        previewViewer.setInput(this.changes);
        if (!valid) {
            if (deleteButton.getSelection()) {
                setErrorMessage(Messages.UpdateShelveDialog_SelecteAtLeastOneFileDelete);
            } else if (updateButton.getSelection()) {
                setErrorMessage(Messages.UpdateShelveDialog_SelectAtLeastOneFileShelve);
            }
        } else {
            setErrorMessage(null);
        }
    }

    /**
     * Update the changes with the latest selection
     */
    public void updateChanges() {
        if (this.replaceButton.getSelection()) {
            ShelveChange replaceChange = new ShelveChange();
            replaceChange.option = Option.REPLACE;
            this.changes = new ShelveChange[] { replaceChange };
        } else if (deleteButton.getSelection()) {
            List<ShelveChange> finalChanges = new ArrayList<ShelveChange>();
            for (ShelveChange change : this.changes) {
                if (change.option == Option.DELETE) {
                    finalChanges.add(change);
                }
            }
            this.changes = finalChanges.toArray(new ShelveChange[finalChanges
                    .size()]);
        } else if (updateButton.getSelection()) {
            List<ShelveChange> finalChanges = new ArrayList<ShelveChange>();
            for (ShelveChange change : this.changes) {
                if (change.option == Option.ADD
                        || change.option == Option.UPDATE) {
                    finalChanges.add(change);
                }
            }
            this.changes = finalChanges.toArray(new ShelveChange[finalChanges
                    .size()]);
        } else {
            this.changes = new ShelveChange[0];
        }
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#okPressed()
     */
    @Override
    protected void okPressed() {
        updateChanges();
        super.okPressed();
    }

    /**
     * Get the selected shelve changes
     * 
     * @return - non-null array of shelve changes
     */
    public ShelveChange[] getChanges() {
        return this.changes;
    }

    private void syncMovedFiles(TableViewer viewer, Object element,
            List<IP4Resource> checkedList, boolean isChecked) {
        if (viewer != null && element != null) {
            IP4Resource pair = null;
            if (element instanceof IP4File) {
                IP4File file = (IP4File) element;
                String moved = file.getMovedFile();
                if (moved != null) {
                    pair = file.getConnection().getFile(moved);
                }
            } else if (element instanceof IP4ShelveFile) {
                IP4ShelveFile file = (IP4ShelveFile) element;
                String moved = file.getFile().getMovedFile();
                if (moved != null) {
                    for (IP4Resource shelve : shelved) {
                        if (moved.equals(shelve.getRemotePath())) {
                            pair = shelve;
                            break;
                        }
                    }
                }
            }
            if (pair != null) {
                if (isChecked) {
                    checkedList.add(pair);
                } else {
                    checkedList.remove(pair);
                }
                viewer.refresh(pair);
            }
        }
    }

    private void createCheckColumn(final TableViewer viewer,
            final List<IP4Resource> checkedList, final IP4Resource[] model) {
        final TableColumn checkColumn = new TableColumn(viewer.getTable(),
                SWT.NONE);
        checkColumn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                checkedList.clear();
                if (checkColumn.getImage() == uncheckImage) {
                    checkedList.addAll(Arrays.asList(model));
                    checkColumn.setImage(checkImage);
                } else {
                    checkColumn.setImage(uncheckImage);
                }
                viewer.refresh();
                updatePreview();
            }

        });
        checkColumn.setImage(uncheckImage);

        viewer.getTable().addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.x >= 0 && e.x < checkColumn.getWidth()) {
                    IStructuredSelection selection = (IStructuredSelection) viewer
                            .getSelection();
                    Object selected = selection.getFirstElement();
                    if (selected instanceof IP4Resource) {
                        boolean isChecked = !checkedList.contains(selected);
                        if (isChecked) {
                            checkedList.add((IP4Resource) selected);
                        } else {
                            checkedList.remove(selected);
                        }
                        syncMovedFiles(viewer, selected, checkedList, isChecked);
                        viewer.refresh(selected);
                        updatePreview();
                    }
                }
            }

        });
    }

    private int computeMaxLength(Table table, IP4Resource[] resources) {
        ShelveLabelProvider provider = new ShelveLabelProvider();
        int length = 200;
        GC gc = new GC(table.getDisplay());
        try {
            gc.setFont(table.getFont());
            for (IP4Resource resource : resources) {
                String text = provider.getColumnText(resource, 1);
                if (text != null) {
                    length = Math.max(length, gc.stringExtent(text).x);
                }
            }
        } finally {
            gc.dispose();
        }
        return length + 25;
    }

    private void resetDeleteChecked() {
        deleteChecked.clear();
        for (IP4Resource file : this.shelved) {
            for (IP4Resource select : this.selected) {
                if (file.getRemotePath().equals(select.getRemotePath())) {
                    deleteChecked.add(file);
                    break;
                }
            }
        }
    }

    private void resetPendingChecked() {
        this.pendingChecked.clear();
        for (IP4Resource file : this.pending) {
            for (IP4Resource select : this.selected) {
                if (file.getRemotePath().equals(select.getRemotePath())) {
                    this.pendingChecked.add(file);
                    break;
                }
            }
        }
    }

    private void createShelveViewer(Composite parent) {
        deleteViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData dvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        dvData.heightHint = P4UIUtils.computePixelHeight(deleteViewer
                .getTable().getFont(), 5);
        dvData.widthHint = 300;
        deleteViewer.getTable().setLayoutData(dvData);
        deleteViewer.getTable().setHeaderVisible(true);
        deleteViewer.setContentProvider(new ArrayContentProvider());
        deleteViewer.setLabelProvider(new ShelveLabelProvider() {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    return ""; //$NON-NLS-1$
                }
                return super.getColumnText(element, 0);
            }

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    if (deleteChecked.contains(element)) {
                        return checkImage;
                    } else {
                        return uncheckImage;
                    }
                }
                return super.getColumnImage(element, 0);
            }
        });

        int width = computeMaxLength(deleteViewer.getTable(), this.shelved);

        resetDeleteChecked();

        deleteViewer.setInput(this.shelved);

        createCheckColumn(deleteViewer, deleteChecked, this.shelved);

        TableColumn deleteColumn = new TableColumn(deleteViewer.getTable(),
                SWT.NONE);
        deleteColumn.setText(MessageFormat.format(
                Messages.UpdateShelveDialog_ShelvedFiles, this.shelved.length));

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnPixelData(30));
        tableLayout.addColumnData(new ColumnPixelData(width));
        deleteViewer.getTable().setLayout(tableLayout);

        deleteViewer.setSorter(this.sorter);
    }

    private void createPendingViewer(Composite parent) {
        pendingViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE
                | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData pvData = new GridData(SWT.FILL, SWT.FILL, true, true);
        pvData.heightHint = P4UIUtils.computePixelHeight(pendingViewer
                .getTable().getFont(), 5);
        pvData.widthHint = 300;
        pendingViewer.getTable().setLayoutData(pvData);
        pendingViewer.getTable().setHeaderVisible(true);

        pendingViewer.setContentProvider(new ArrayContentProvider());
        pendingViewer.setLabelProvider(new ShelveLabelProvider() {

            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    return ""; //$NON-NLS-1$
                }
                return super.getColumnText(element, 0);
            }

            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                if (columnIndex == 0) {
                    if (pendingChecked.contains(element)) {
                        return checkImage;
                    } else {
                        return uncheckImage;
                    }
                }
                return super.getColumnImage(element, 0);
            }
        });

        int width = computeMaxLength(pendingViewer.getTable(), this.pending);

        resetPendingChecked();

        pendingViewer.setInput(this.pending);

        createCheckColumn(pendingViewer, pendingChecked, pending);

        final TableColumn pendingColumn = new TableColumn(
                pendingViewer.getTable(), SWT.NONE);
        pendingColumn.setText(MessageFormat.format(
                Messages.UpdateShelveDialog_PendingFiles, this.pending.length));

        TableLayout tableLayout = new TableLayout();
        tableLayout.addColumnData(new ColumnPixelData(30));
        tableLayout.addColumnData(new ColumnPixelData(width));
        pendingViewer.getTable().setLayout(tableLayout);

        pendingViewer.setSorter(this.sorter);
    }

    private void createPreviewViewer(Composite parent) {
        GridData plData = new GridData(SWT.FILL, SWT.FILL, true, false);
        plData.verticalIndent = 15;

        previewLabel = new Label(parent, SWT.NONE);
        previewLabel.setLayoutData(plData);

        previewViewer = new TableViewer(parent, SWT.SINGLE | SWT.BORDER
                | SWT.FULL_SELECTION);
        GridData vData = new GridData(SWT.FILL, SWT.FILL, true, true);
        vData.heightHint = P4UIUtils.computePixelHeight(previewViewer
                .getTable().getFont(), 5);
        previewViewer.setContentProvider(new ArrayContentProvider());
        previewViewer.setLabelProvider(new ShelveChangeLabelProvider());
        previewViewer.getTable().setLayoutData(vData);
        previewViewer.setSorter(this.sorter);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite c = (Composite) super.createDialogArea(parent);
        displayArea = new Composite(c, SWT.NONE);
        displayArea.setLayout(new GridLayout(1, true));
        displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        checkImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_CHECK).createImage();
        P4UIUtils.registerDisposal(displayArea, checkImage);
        uncheckImage = PerforceUIPlugin.getDescriptor(
                IPerforceUIConstants.IMG_UNCHECK).createImage();
        P4UIUtils.registerDisposal(displayArea, uncheckImage);

        SelectionListener optionListener = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                pendingViewer.getTable()
                        .setEnabled(updateButton.getSelection());
                deleteViewer.getTable().setEnabled(deleteButton.getSelection());
                updatePreview();
            }

        };

        Composite topArea = new Composite(displayArea, SWT.NONE);
        GridLayout taLayout = new GridLayout(2, true);
        taLayout.verticalSpacing = 2;
        taLayout.marginHeight = 0;
        taLayout.marginWidth = 0;
        topArea.setLayout(taLayout);
        topArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createPendingViewer(topArea);
        createShelveViewer(topArea);

        updateButton = new Button(displayArea, SWT.RADIO);
        updateButton.setText(Messages.UpdateShelveDialog_ShelveSelectedFiles);
        updateButton.addSelectionListener(optionListener);

        replaceButton = new Button(displayArea, SWT.RADIO);
        replaceButton.setText(Messages.UpdateShelveDialog_ReplaceShelvedFiles);
        replaceButton.addSelectionListener(optionListener);
        replaceButton.setEnabled(this.pending.length > 0);

        deleteButton = new Button(displayArea, SWT.RADIO);
        deleteButton
                .setText(Messages.UpdateShelveDialog_DeleteSelectedShelvedFiles);
        deleteButton.addSelectionListener(optionListener);

        if (this.initialOption != null) {
            switch (this.initialOption) {
            case ADD:
            case UPDATE:
            case UNCHANGED:
                updateButton.setSelection(true);
                break;
            case DELETE:
                deleteButton.setSelection(true);
                break;
            case REPLACE:
                if (replaceButton.isEnabled()) {
                    replaceButton.setSelection(true);
                }
                break;

            default:
                break;
            }
        } else {
            updateButton.setSelection(true);
        }

        if (!updateButton.getSelection()) {
            pendingViewer.getTable().setEnabled(false);
        }
        if (!deleteButton.getSelection()) {
            deleteViewer.getTable().setEnabled(false);
        }

        Composite bottomArea = new Composite(displayArea, SWT.NONE);

        GridLayout baLayout = new GridLayout(1, true);
        baLayout.marginHeight = 0;
        baLayout.marginWidth = 0;
        baLayout.verticalSpacing = 2;
        bottomArea.setLayout(baLayout);
        bottomArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        createPreviewViewer(bottomArea);

        updatePreview();
        return c;
    }

    /**
     * @see org.eclipse.jface.dialogs.StatusDialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        okStatusButton = createButton(parent, IDialogConstants.OK_ID,
                Messages.UpdateShelveDialog_Update, true);
        createButton(parent, IDialogConstants.CANCEL_ID,
                IDialogConstants.CANCEL_LABEL, false);
    }

}
