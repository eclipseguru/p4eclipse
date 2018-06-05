/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import com.perforce.team.ui.StyledLabelProvider;

import org.eclipse.jface.viewers.ILabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class StyledDiffLabelProvider extends StyledLabelProvider {

    /**
     * @param labelProvider
     */
    public StyledDiffLabelProvider(ILabelProvider labelProvider) {
        super(labelProvider);
    }

}
