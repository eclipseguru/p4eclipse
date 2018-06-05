/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.c.timelapse;

import com.perforce.team.ui.text.timelapse.INodeModel;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;

import org.eclipse.cdt.core.model.IFunctionDeclaration;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class CTickDecorator extends NodeTickDecorator {

    private CElementLabelProvider provider;

    /**
     * @param model
     */
    public CTickDecorator(INodeModel model) {
        super(model);
        provider = new CElementLabelProvider(
                CElementLabelProvider.SHOW_SMALL_ICONS
                        | CElementLabelProvider.SHOW_OVERLAY_ICONS);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#getBaseImage(java.lang.Object)
     */
    @Override
    protected Image getBaseImage(Object node) {
        if (node instanceof IFunctionDeclaration) {
            return this.provider.getImage(node);
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#dispose()
     */
    @Override
    public void dispose() {
        provider.dispose();
        super.dispose();
    }

}
