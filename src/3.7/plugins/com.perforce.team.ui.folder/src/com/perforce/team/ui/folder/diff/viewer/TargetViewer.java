/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.viewer;

import com.perforce.team.ui.folder.diff.editor.FileDiffContentProvider;
import com.perforce.team.ui.folder.diff.editor.ProxyFileDiffContentProvider;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TargetViewer extends DiffViewer {

    /**
     * @see com.perforce.team.ui.folder.diff.viewer.DiffViewer#createContentProvider(org.eclipse.jface.viewers.TreeViewer)
     */
    @Override
    protected FileDiffContentProvider createContentProvider(TreeViewer viewer) {
        return new ProxyFileDiffContentProvider(viewer, true);
    }

}
