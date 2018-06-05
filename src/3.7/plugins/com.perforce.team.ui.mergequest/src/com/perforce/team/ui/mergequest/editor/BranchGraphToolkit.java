/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.editor;

import com.perforce.team.ui.mergequest.P4BranchGraphPlugin;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphToolkit extends FormToolkit {

    /**
     * INCLUDE_SCROLLS
     */
    public static final String INCLUDE_SCROLLS = P4BranchGraphPlugin.PLUGIN_ID
            + ".NO_SCROLLS"; //$NON-NLS-1$

    /**
     * @param display
     */
    public BranchGraphToolkit(Display display) {
        super(display);
    }

    /**
     * @see org.eclipse.ui.forms.widgets.FormToolkit#createScrolledForm(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public ScrolledForm createScrolledForm(Composite parent) {
        ScrolledForm form = null;
        if (Boolean.TRUE.equals(parent.getData(INCLUDE_SCROLLS))) {
            form = super.createScrolledForm(parent);
        } else {
            form = new ScrolledForm(parent, getOrientation());
            form.setExpandHorizontal(true);
            form.setExpandVertical(true);
            form.setBackground(getColors().getBackground());
            form.setForeground(getColors().getColor(IFormColors.TITLE));
            form.setFont(JFaceResources.getHeaderFont());
        }
        return form;
    }
}
