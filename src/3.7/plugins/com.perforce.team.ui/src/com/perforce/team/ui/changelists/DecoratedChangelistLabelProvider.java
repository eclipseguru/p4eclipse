/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.changelists;

import com.perforce.team.ui.P4DecoratedLabelProvider;

import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class DecoratedChangelistLabelProvider extends P4DecoratedLabelProvider {

    /**
     * Create a pending label provider
     * 
     * @return pending label provider
     */
    public static DecoratedChangelistLabelProvider create() {
        ChangelistDecorator decorator = new ChangelistDecorator(true);
        ChangelistLabelProvider labelProvider = new ChangelistLabelProvider(
                true);
        StyledChangelistLabelProvider styledLabelProvider = new StyledChangelistLabelProvider(
                labelProvider);
        DecoratedChangelistLabelProvider decorated = new DecoratedChangelistLabelProvider(
                labelProvider, styledLabelProvider, decorator);
        decorated.setAddDecorations(false);
        return decorated;
    }

    /**
     * @param baseLabelProvider
     * @param labelProvider
     * @param decorator
     */
    public DecoratedChangelistLabelProvider(ILabelProvider baseLabelProvider,
            IStyledLabelProvider labelProvider, ILabelDecorator decorator) {
        super(baseLabelProvider, labelProvider, decorator);
    }

    /**
     * @param baseLabelProvider
     * @param labelProvider
     * @param decorator
     * @param context
     */
    public DecoratedChangelistLabelProvider(ILabelProvider baseLabelProvider,
            IStyledLabelProvider labelProvider, ILabelDecorator decorator,
            IDecorationContext context) {
        super(baseLabelProvider, labelProvider, decorator, context);
    }

}
