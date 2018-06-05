/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.folder.diff.editor;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DecoratingDiffLabelProvider extends
        DecoratingStyledCellLabelProvider implements ILabelProvider,
        IBaseLabelProvider {

    private ILabelProvider provider = null;

    /**
     * @param labelProvider
     * @param decorator
     * @param decorationContext
     */
    public DecoratingDiffLabelProvider(ILabelProvider labelProvider,
            ILabelDecorator decorator, IDecorationContext decorationContext) {
        super(new StyledDiffLabelProvider(labelProvider), decorator,
                decorationContext);
        provider = labelProvider;
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return this.provider.getText(element);
    }
}
