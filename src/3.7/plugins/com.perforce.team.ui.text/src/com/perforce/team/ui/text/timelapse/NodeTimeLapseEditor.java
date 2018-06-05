/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.IPerforceUIConstants;
import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.PerforceUIPlugin;
import com.perforce.team.ui.text.PerforceUiTextPlugin;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.progress.UIJob;

/**
 * Text time lapse editor that contains filtering by nodes within the document
 * that will focus the editor on that region when selected.
 * 
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public abstract class NodeTimeLapseEditor extends TextTimeLapseEditor {

    /**
     * NONE_SELECTED
     */
    public static final String NONE_SELECTED = "<none selected>"; //$NON-NLS-1$

    /**
     * SHOW_FOLDING
     */
    public static final String SHOW_FOLDING = "com.perforce.team.ui.text.timelapse.SHOW_FOLDING"; //$NON-NLS-1$

    /**
     * LINK_EDITOR
     */
    public static final String LINK_EDITOR = "com.perforce.team.ui.text.timelapse.LINK_EDITOR"; //$NON-NLS-1$

    private ITextViewerExtension5 viewer5 = null;

    /**
     * Simple range class containing an offset and length
     */
    protected static class Range {

        int offset;
        int length;

        /**
         * Create new range with offset and length
         * 
         * @param offset
         * @param length
         */
        public Range(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * Node filter combo box
     */
    protected Combo filters = null;

    /**
     * Current filter key
     */
    private String filterKey = null;

    private boolean folding = false;
    private boolean linking = false;

    /**
     * Get current filter
     * 
     * @return - filter
     */
    protected String getFilter() {
        return this.filterKey;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#createHeader(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createHeader(Composite parent) {
        super.createHeader(parent);
        createCombo(parent);
    }

    /**
     * Configure a tool item for linking with an outside node selection
     * provider, typically an outline page.
     * 
     * @param toolbar
     * @param tooltip
     */
    protected void configureLinking(ToolBar toolbar, String tooltip) {
        final ToolItem linkItem = new ToolItem(toolbar, SWT.CHECK);
        if (tooltip != null) {
            linkItem.setToolTipText(tooltip);
        }
        Image linkImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_LINK).createImage();
        P4UIUtils.registerDisposal(linkItem, linkImage);
        linkItem.setImage(linkImage);
        linkItem.setSelection(isLinking());
        linkItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                linking = linkItem.getSelection();
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(LINK_EDITOR, linking);
            }

        });
    }

    /**
     * Is this editor in link mode?
     * 
     * @return - true if in link mode, false otherwise
     */
    protected boolean isLinking() {
        return this.linking;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#fillToolbar(org.eclipse.swt.widgets.ToolBar)
     */
    @Override
    protected void fillToolbar(ToolBar toolbar) {
        super.fillToolbar(toolbar);
        final ToolItem projectionItem = new ToolItem(toolbar, SWT.CHECK);
        Image foldingImage = PerforceUiTextPlugin.getImageDescriptor(
                PerforceUiTextPlugin.IMG_FOLDING).createImage();
        P4UIUtils.registerDisposal(projectionItem, foldingImage);
        projectionItem.setImage(foldingImage);
        projectionItem
                .setToolTipText(Messages.NodeTimeLapseEditor_ToggleFolding);
        projectionItem.setSelection(this.folding);
        projectionItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                folding = projectionItem.getSelection();
                updateRedrawStatus();
                if (folding) {
                    scheduleProjection();
                } else {
                    scheduleNoProjection();
                }
                redrawRulers();
                PerforceUiTextPlugin.getDefault().getPreferenceStore()
                        .setValue(SHOW_FOLDING, folding);
            }

        });
    }

    /**
     * Get collection of string entries to display in the filter box
     * 
     * @return - collection of string filters
     */
    protected abstract Collection<String> getFilters();

    /**
     * Get label to display in front of filter combo
     * 
     * @return - non-null string label
     */
    protected abstract String getFilterLabel();

    /**
     * Get the key for the specified filter text
     * 
     * @param filterText
     * @return - filter key
     */
    protected abstract String getKey(String filterText);

    /**
     * Get the range of the specified filter key or null if no range in the
     * current document is available for the specified filter key.
     * 
     * @param filterKey
     * @return - range
     */
    protected abstract Range getRange(String filterKey);

    /**
     * Clear the currently cached tree and build a new one from the specified
     * editor input.
     * 
     * @param input
     */
    protected abstract void buildTree(IEditorInput input);

    /**
     * Build a document based on the editor input
     * 
     * @param input
     */
    protected abstract void buildDocument(IEditorInput input);

    /**
     * Enable projection on the viewer if it is a {@link ProjectionViewer}.
     * Subclasses may override this method.
     */
    protected void enableProjection() {
        ITextViewer viewer = getViewer();
        if (viewer instanceof ProjectionViewer) {
            ((ProjectionViewer) viewer).enableProjection();
        }
    }

    /**
     * Disable projection on the viewer if it is a {@link ProjectionViewer}.
     * Subclasses may override this method.
     */
    protected void disableProjection() {
        ITextViewer viewer = getViewer();
        if (viewer instanceof ProjectionViewer) {
            ((ProjectionViewer) viewer).disableProjection();
        }
    }

    /**
     * Schedule projection of this editor
     */
    protected void scheduleProjection() {
        if (folding && getFilter() == null) {
            UIJob job = new UIJob(Messages.NodeTimeLapseEditor_EnablingFolding) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    enableFolding();
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        }
    }

    /**
     * Disable folding
     */
    protected void disableFolding() {
        ITextViewer viewer = getViewer();
        if (viewer instanceof ProjectionViewer) {
            ((ProjectionViewer) viewer)
                    .doOperation(ProjectionViewer.EXPAND_ALL);
        }
        disableProjection();
    }

    /**
     * Enable folding
     */
    protected void enableFolding() {
        enableProjection();
    }

    /**
     * Schedules no projection of this editor
     */
    protected void scheduleNoProjection() {
        if (getFilter() == null) {
            UIJob job = new UIJob(Messages.NodeTimeLapseEditor_DisablingFolding) {

                @Override
                public IStatus runInUIThread(IProgressMonitor monitor) {
                    disableFolding();
                    return Status.OK_STATUS;
                }
            };
            job.schedule();
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#refresh(org.eclipse.ui.IEditorInput)
     */
    @Override
    protected void refresh(IEditorInput input) {
        buildTree(input);
        buildDocument(input);
        updateFilterEntries();
        filter(false);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#convertToModelLine(int)
     */
    @Override
    protected int convertToModelLine(int widgetLine) {
        if (viewer5 != null) {
            widgetLine = viewer5.widgetLine2ModelLine(widgetLine);
        }
        return widgetLine;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#convertToWidgetLine(int)
     */
    @Override
    protected int convertToWidgetLine(int modelLine) {
        if (viewer5 != null) {
            modelLine = viewer5.modelLine2WidgetLine(modelLine);
        }
        return modelLine;
    }

    /**
     * @see com.perforce.team.ui.timelapse.TimeLapseEditor#updateEditor()
     */
    @Override
    protected void updateEditor() {
        super.updateEditor();
        addFilterEntries();
    }

    /**
     * Add the filter entries to the combo box
     */
    private void addFilterEntries() {
        if (this.filters != null) {
            String previous = this.filters.getText();
            this.filters.removeAll();
            Collection<String> keys = getFilters();
            String[] entries = keys.toArray(new String[keys.size()]);
            filters.add(NONE_SELECTED);
            for (String entry : entries) {
                this.filters.add(entry);
            }
            if (previous.length() > 0) {
                this.filters.setText(previous);
            } else {
                this.filters.select(0);
            }
        }
    }

    /**
     * Re-load the filter entries in the filter combo box
     */
    protected void updateFilterEntries() {
        addFilterEntries();
    }

    /**
     * Show the entire document in the viewer
     */
    protected void showEntireDocument() {
        ITextViewer viewer = getViewer();
        viewer.resetVisibleRegion();
        if (folding) {
            enableProjection();
        } else {
            disableProjection();
        }
    }

    /**
     * Filter the document
     * 
     * @param newFilter
     */
    protected void filter(boolean newFilter) {
        ITextViewer viewer = getViewer();
        if (filterKey != null) {
            Range range = getRange(filterKey);
            if (range != null) {
                viewer.setVisibleRegion(range.offset, range.length);
            } else {
                viewer.setVisibleRegion(0, 0);
            }
        } else {
            showEntireDocument();
        }
        viewer.setSelectedRange(0, 0);
        redrawRulers();
    }

    /**
     * Is this editor filtering?
     * 
     * @return - true if filtering, false otherwise
     */
    protected boolean isFiltering() {
        return filterKey != null;
    }

    /**
     * Clear filter and show entire document
     */
    protected void clearFilter() {
        filterKey = null;
        filters.select(0);
        showEntireDocument();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#updateVisibleRange()
     */
    @Override
    protected void updateVisibleRange() {
        // Don't update visible range when in filter mode since this can jump
        // from the top of a node to somewhere in a node when scrolling has not
        // been done
        if (!isFiltering()) {
            super.updateVisibleRange();
        } else {
            getViewer().getTextWidget().setTopIndex(0);
        }
    }

    /**
     * Show a range
     * 
     * @param range
     */
    protected void showRange(Range range) {
        if (isFiltering()) {
            clearFilter();
        }
        getViewer().getTextWidget().setRedraw(false);
        getViewer().revealRange(range.offset, range.length);
        getViewer().getTextWidget().setRedraw(true);
        redrawRulers();
    }

    /**
     * Select a range
     * 
     * @param range
     */
    protected void selectRange(Range range) {
        setRedraw(false);
        getViewer().setSelectedRange(range.offset, range.length);
        setRedraw(true);
    }

    private void updateRedrawStatus() {
        if (P4CoreUtils.isWindows() || P4CoreUtils.isLinux()) {
            redraw = this.folding;
        } else {
            redraw = false;
        }
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#init(org.eclipse.ui.IEditorSite,
     *      org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        IPreferenceStore store = PerforceUiTextPlugin.getDefault()
                .getPreferenceStore();
        this.linking = store.getBoolean(LINK_EDITOR);
        this.folding = store.getBoolean(SHOW_FOLDING);
        updateRedrawStatus();
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.TextTimeLapseEditor#configureViewer(java.lang.String)
     */
    @Override
    protected void configureViewer(String fontPrefName) {
        super.configureViewer(fontPrefName);
        ITextViewer viewer = getViewer();
        if (viewer instanceof ITextViewerExtension5) {
            this.viewer5 = (ITextViewerExtension5) viewer;
            if (folding) {
                scheduleProjection();
            } else {
                scheduleNoProjection();
            }
        }
    }

    /**
     * Create the node filter combo box
     * 
     * @param parent
     */
    protected void createCombo(Composite parent) {
        Composite filterArea = new Composite(parent, SWT.NONE);
        GridLayout faLayout = new GridLayout(3, false);
        filterArea.setLayout(faLayout);
        filterArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

        Label methodsLabel = new Label(filterArea, SWT.NONE);
        methodsLabel.setText(getFilterLabel());

        final SelectionAdapter selectionAdapter = new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = filters.getSelectionIndex();
                if (index >= 0) {
                    boolean filter = false;
                    if (index == 0) {
                        filter = filterKey != null;
                        filterKey = null;
                    } else {
                        String newFilterKey = getKey(filters.getText());
                        filter = newFilterKey != null
                                && !newFilterKey.equals(filterKey);
                        filterKey = newFilterKey;
                    }
                    if (filter) {
                        filter(true);
                    }
                }
            }

        };

        filters = new Combo(filterArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        filters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        filters.addSelectionListener(selectionAdapter);

        ToolBar filterBar = new ToolBar(filterArea, SWT.FLAT);
        ToolItem clearItem = new ToolItem(filterBar, SWT.PUSH);
        Image clearImage = PerforceUIPlugin.getPlugin()
                .getImageDescriptor(IPerforceUIConstants.IMG_CLEAR)
                .createImage();
        P4UIUtils.registerDisposal(clearItem, clearImage);
        clearItem.setImage(clearImage);
        clearItem.setToolTipText(Messages.NodeTimeLapseEditor_ClearFilter);
        clearItem.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                filters.select(0);
                selectionAdapter.widgetSelected(null);
            }

        });
    }

}
