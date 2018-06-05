/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.java.timelapse;

import com.perforce.team.ui.text.timelapse.INodeModel;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class JavaTickDecorator extends NodeTickDecorator {

    private JavaElementLabelProvider provider;

    /**
     * Create a java tick decorator
     * 
     * @param model
     */
    public JavaTickDecorator(INodeModel model) {
        super(model);
        provider = new JavaElementLabelProvider(
                JavaElementLabelProvider.SHOW_SMALL_ICONS
                        | JavaElementLabelProvider.SHOW_OVERLAY_ICONS);
    }

    /**
     * @see com.perforce.team.ui.text.timelapse.NodeTickDecorator#getBaseImage(java.lang.Object)
     */
    @Override
    protected Image getBaseImage(Object node) {
        if (node instanceof IMethod) {
            return provider.getImage(node);
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
