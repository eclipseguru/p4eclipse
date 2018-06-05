/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class StyledLabelProvider extends LabelProviderAdapter implements
        IStyledLabelProvider {

    /**
     * Label provider
     */
    protected ILabelProvider labelProvider = null;
    private IFontProvider fontProvider = null;

    /**
     * Create a styled label provider that simply wraps the specified non-null
     * label provider
     * 
     * @param labelProvider
     */
    public StyledLabelProvider(ILabelProvider labelProvider) {
        this.labelProvider = labelProvider;
        if (this.labelProvider instanceof IFontProvider) {
            fontProvider = (IFontProvider) this.labelProvider;
        }
    }

    /**
     * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider#getImage(java.lang.Object)
     */
    @Override
    public Image getImage(Object element) {
        return this.labelProvider.getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    @Override
    public void dispose() {
        super.dispose();
        labelProvider.dispose();
    }

    /**
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return this.labelProvider.getText(element);
    }

    /**
     * @see com.perforce.team.ui.LabelProviderAdapter#getFont(java.lang.Object)
     */
    @Override
    public Font getFont(Object element) {
        if (this.fontProvider != null) {
            return this.fontProvider.getFont(element);
        }
        return super.getFont(element);
    }

}
