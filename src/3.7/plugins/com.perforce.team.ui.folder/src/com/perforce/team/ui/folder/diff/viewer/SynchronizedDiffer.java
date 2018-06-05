/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.folder.diff.model.FileDiffElement;

import org.eclipse.compare.structuremergeviewer.ICompareInput;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class SynchronizedDiffer {

    /**
     * Diff element filter
     */
    public static class DiffFilter extends ViewerFilter {

        private boolean source = true;

        /**
         * Create diff filter
         * 
         * @param source
         */
        public DiffFilter(boolean source) {
            this.source = source;
        }

        /**
         * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
         *      java.lang.Object, java.lang.Object)
         */
        @Override
        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            if (element instanceof FileDiffElement) {
                ICompareInput input = P4CoreUtils.convert(element,
                        ICompareInput.class);
                if (source) {
                    return input.getLeft() != null;
                } else {
                    return input.getRight() != null;
                }
            }
            return true;
        }

    }

    /**
     * Expansion listener
     */
    public static class ExpansionListener implements ITreeViewerListener {

        private TreeViewer viewer;

        /**
         * Create expansion listener
         * 
         * @param viewer
         */
        public ExpansionListener(TreeViewer viewer) {
            this.viewer = viewer;
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeViewerListener#treeCollapsed(org.eclipse.jface.viewers.TreeExpansionEvent)
         */
        public void treeCollapsed(TreeExpansionEvent event) {
            if (event.getElement() instanceof IP4DiffFile) {
                IP4DiffFile pair = ((IP4DiffFile) event.getElement()).getPair();
                if (pair != null) {
                    viewer.collapseToLevel(pair, 1);
                }
            }
        }

        /**
         * @see org.eclipse.jface.viewers.ITreeViewerListener#treeExpanded(org.eclipse.jface.viewers.TreeExpansionEvent)
         */
        public void treeExpanded(TreeExpansionEvent event) {
            if (event.getElement() instanceof IP4DiffFile) {
                IP4DiffFile pair = ((IP4DiffFile) event.getElement()).getPair();
                if (pair != null) {
                    viewer.expandToLevel(pair, TreeViewer.ALL_LEVELS);
                }
            }
        }

    }

    private TreeViewer sourceViewer;
    private TreeViewer targetViewer;

    /**
     * @param sourceViewer
     * @param targetViewer
     */
    public SynchronizedDiffer(TreeViewer sourceViewer, TreeViewer targetViewer) {
        this.sourceViewer = sourceViewer;
        this.targetViewer = targetViewer;
        configure();
    }

    private void configure() {
        sourceViewer.addTreeListener(new ExpansionListener(targetViewer));
        targetViewer.addTreeListener(new ExpansionListener(sourceViewer));
        sourceViewer.addFilter(new DiffFilter(true));
        targetViewer.addFilter(new DiffFilter(false));
    }
}
