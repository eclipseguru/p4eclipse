/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider;
import com.perforce.team.ui.folder.diff.editor.ProxyFileDiffContentProvider;
import com.perforce.team.ui.folder.diff.model.FileDiffContainer;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.layout.GridLayout;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SourceArea extends DiffArea {

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#createViewer()
     */
    @Override
    protected DiffViewer createViewer() {
        return new SourceViewer();
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#createAreaLayout()
     */
    @Override
    protected GridLayout createAreaLayout() {
        return GridLayoutFactory.swtDefaults().numColumns(1).margins(0, 0)
                .spacing(0, 5).create();
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#getUniqueCount(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    protected int getUniqueCount(FileDiffContainer container) {
        return container != null ? container.getLeftUniqueCount() : 0;
    }

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffArea#getGroupContainer(com.perforce.team.ui.folder.diff.model.FileDiffContainer)
     */
    @Override
    protected GroupedDiffContainer getGroupContainer(FileDiffContainer container) {
        return container != null
                ? container.getContainer(Status.LEFT_ONLY)
                : null;
    }

    /**
     * Set diff area pair
     * 
     * @param pair
     */
    @Override
    public void setPair(DiffArea pair) {
        super.setPair(pair);
        if (pair != null) {
            TreeViewer diffViewer = getViewer().getViewer();
            TreeViewer pairViewer = pair.getViewer().getViewer();
            ((ProxyFileDiffContentProvider) pairViewer.getContentProvider())
                    .setContentProvider((FileDiffContentProvider) diffViewer
                            .getContentProvider());
        }
    }

}
