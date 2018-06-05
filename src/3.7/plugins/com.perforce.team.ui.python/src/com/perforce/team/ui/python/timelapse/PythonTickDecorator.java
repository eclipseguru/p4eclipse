/**
 * Copyright (c) 2009 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.python.timelapse;

import com.perforce.team.ui.text.timelapse.INodeModel;
import com.perforce.team.ui.text.timelapse.NodeTickDecorator;

import org.eclipse.dltk.core.IMethod;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 * 
 */
public class PythonTickDecorator extends NodeTickDecorator {

    private ILabelProvider provider;

    /**
     * Create a python tick decorator
     * 
     * @param model
     */
    public PythonTickDecorator(INodeModel model) {
        super(model);
        provider = PythonUtils.createLabelProvider();
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
