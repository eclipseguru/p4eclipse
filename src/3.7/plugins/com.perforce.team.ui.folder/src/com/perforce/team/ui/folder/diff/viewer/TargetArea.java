/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.diff.editor.SubmittedChangelistWidget;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridLayout;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TargetArea extends DiffArea {

    private SynchronizedSlider slider = null;

    /**
     */
    public TargetArea() {
    }

    /**
     * Load changelist for selected element
     * 
     * @param element
     * @param changelistArea
     */
    @Override
    protected void loadChangelist(Object element,
            final SubmittedChangelistWidget changelistArea) {
        if (element instanceof FileDiffElement) {
            // Use pair file since file diff elements are parented in the source
            IP4DiffFile file = P4CoreUtils.convert(element, IP4DiffFile.class);
            if (file != null) {
                element = file.getPair();
            }
        }
        super.loadChangelist(element, changelistArea);
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#createViewer()
     */
    @Override
    protected DiffViewer createViewer() {
        return new TargetViewer();
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#createAreaLayout()
     */
    @Override
    protected GridLayout createAreaLayout() {
        return GridLayoutFactory.swtDefaults().numColumns(2).margins(0, 0)
                .spacing(0, 5).create();
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#getUniqueCount(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    protected int getUniqueCount(FileDiffContainer container) {
        return container != null ? container.getRightUniqueCount() : 0;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#getGroupContainer(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    protected GroupedDiffContainer getGroupContainer(FileDiffContainer container) {
        return container != null
                ? container.getContainer(Status.RIGHT_ONLY)
                : null;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#setContainer(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    public void setContainer(FileDiffContainer container) {
        super.setContainer(container);
        if (this.slider != null) {
            this.slider.refresh();
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#setPair(com.perforce.team.ui.folder.diff.viewer.DiffArea)
     */
    @Override
    public void setPair(DiffArea pair) {
        super.setPair(pair);
        if (pair != null) {
            TreeViewer diffViewer = getViewer().getViewer();
            TreeViewer pairViewer = pair.getViewer().getViewer();
            this.slider = new SynchronizedSlider(getArea());
            this.slider.setTrees(pairViewer.getTree(), diffViewer.getTree());
        }
    }

    /**
     * Get slider
     * 
     * @return slider
     */
    public SynchronizedSlider getSlider() {
        return this.slider;
    }

    /**
     * Reset slider
     */
    public void resetSlider() {
        if (this.slider != null) {
            this.slider.scrollToTop();
            this.slider.refresh();
        }
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#setType(com.perforce.team.ui.changelists.Folder.Type)
     */
    @Override
    public void setType(Type type) {
        super.setType(type);
        resetSlider();
    }

}
