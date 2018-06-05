/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.viewer;

import com.perforce.team.ui.P4UIUtils;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public abstract class FilterViewer implements IFilterViewer {

    /**
     * Composite used by {@link #showFilters(boolean, boolean)}
     */
    protected Composite filterComposite = null;

    /**
     * @see com.perforce.team.ui.viewer.IFilterViewer#showFilters(boolean,
     *      boolean)
     */
    public void showFilters(boolean show, boolean layout) {
        if (P4UIUtils.okToUse(filterComposite)) {
            if (show != filterComposite.isVisible()) {
                filterComposite.setVisible(show);
                ((GridData) filterComposite.getLayoutData()).exclude = !show;
                if (layout) {
                    filterComposite.getParent().layout(true, true);
                }
                if(show)
                    filterComposite.setFocus();
            }
        }
    }

}
