/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import com.perforce.team.ui.decorator.PerforceDecorator;

import org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecorationContext;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class P4DecoratedLabelProvider extends DecoratingStyledCellLabelProvider
        implements ILabelProvider {

    /**
     * Create a new p4 decorated label provider
     * 
     * @param labelProvider
     * @param decorator
     * 
     * @return decorating styled cell label provider
     */
    public static P4DecoratedLabelProvider create(ILabelProvider labelProvider,
            ILabelDecorator decorator) {
        IStyledLabelProvider styledProvider = new StyledLabelProvider(
                labelProvider);
        return new P4DecoratedLabelProvider(labelProvider, styledProvider,
                decorator, null);
    }

    /**
     * Create a new p4 decorated label provider
     * 
     * @param labelProvider
     * 
     * @return decorating styled cell label provider
     */
    public static P4DecoratedLabelProvider create(ILabelProvider labelProvider) {
        return create(labelProvider, new PerforceDecorator(true){
        	@Override
        	public String getName() {
        		return P4DecoratedLabelProvider.class.getSimpleName()+":"+PerforceDecorator.class.getSimpleName();
        	}
        });
    }

    /**
     * Create a new p4 decorated label provider
     * 
     * @return decorating styled cell label provider
     */
    public static P4DecoratedLabelProvider create() {
        return create(new PerforceLabelProvider(true, false, false));
    }

    private boolean addDecorations = true;
    private ILabelProvider base;

    /**
     * @param baseLabelProvider
     * @param labelProvider
     * @param decorator
     */
    public P4DecoratedLabelProvider(ILabelProvider baseLabelProvider,
            IStyledLabelProvider labelProvider, ILabelDecorator decorator) {
        this(baseLabelProvider, labelProvider, decorator, null);
    }

    /**
     * @param baseLabelProvider
     * @param labelProvider
     * @param decorator
     * @param decorationContext
     */
    public P4DecoratedLabelProvider(ILabelProvider baseLabelProvider,
            IStyledLabelProvider labelProvider, ILabelDecorator decorator,
            IDecorationContext decorationContext) {
        super(labelProvider, decorator, decorationContext);
        this.base = baseLabelProvider;
    }

    /**
     * @return the addDecorations
     */
    public boolean isAddDecorations() {
        return this.addDecorations;
    }

    /**
     * @param addDecorations
     *            the addDecorations to set
     */
    public void setAddDecorations(boolean addDecorations) {
        this.addDecorations = addDecorations;
    }

    /**
     * @see org.eclipse.jface.viewers.DecoratingStyledCellLabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        return addDecorations
                ? super.getImage(element)
                : getStyledStringProvider().getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return this.base.getText(element);
    }

}
