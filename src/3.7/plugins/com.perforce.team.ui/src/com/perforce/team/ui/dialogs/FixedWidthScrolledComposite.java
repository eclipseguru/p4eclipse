package com.perforce.team.ui.dialogs;

/*
 * Copyright (c) 2003 - 2005 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;

public class FixedWidthScrolledComposite extends ScrolledComposite {

    private boolean inSetBounds = false;

    public FixedWidthScrolledComposite(Composite parent, int style) {
        super(parent, style);
    }

    @Override
    public void layout(boolean changed) {
        super.layout(changed);
        if (!inSetBounds) {
            Rectangle bounds = getContent().getBounds();
            Rectangle hostRect = getClientArea();
            bounds.width = hostRect.width;
            // Avoid infinite recursion on Linux
            inSetBounds = true;
            getContent().setBounds(bounds);
            inSetBounds = false;
        }
    }
}
