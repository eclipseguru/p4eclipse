/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public final class P4FormUIUtils {

    /**
     * Register expansion space grabbing for the specified section
     * 
     * @param section
     * @param body
     */
    public static void registerExpansionSpaceGrabber(final Section section,
            final Composite body) {
        registerExpansionSpaceGrabber(section, body, true, false);
    }

    /**
     * Register expansion space grabbing for the specified section
     * 
     * @param section
     * @param body
     * @param vertical
     * @param horizontal
     */
    public static void registerExpansionSpaceGrabber(final Section section,
            final Composite body, final boolean vertical,
            final boolean horizontal) {
        if (section != null && body != null) {
            section.addExpansionListener(new ExpansionAdapter() {

                @Override
                public void expansionStateChanged(ExpansionEvent e) {
                    GridData data = (GridData) section.getLayoutData();
                    if (vertical) {
                        data.grabExcessVerticalSpace = section.isExpanded();
                    }
                    if (horizontal) {
                        data.grabExcessHorizontalSpace = section.isExpanded();
                    }
                    body.layout(true, true);
                }
            });
        }
    }

    /**
     * Create text client for specified section
     * 
     * @param toolkit
     * @param section
     * @param numColumns
     * @param style
     * @return - composite
     */
    public static Composite createSectionTextClient(FormToolkit toolkit,
            Section section, int numColumns, int style) {
        Composite area = null;
        if (toolkit != null && section != null) {
            area = toolkit.createComposite(section, style);
            area.setBackground(null);
            GridLayout aLayout = new GridLayout(numColumns, false);
            aLayout.marginWidth = 0;
            aLayout.marginHeight = 0;
            area.setLayout(aLayout);
            section.setTextClient(area);
        }
        return area;
    }

    /**
     * Create text client for specified section
     * 
     * @param toolkit
     * @param section
     * @param numColumns
     * @param style
     * @return - composite
     */
    public static Composite createSectionTextClient(FormToolkit toolkit,
            Section section, int numColumns) {
        return createSectionTextClient(toolkit, section, numColumns, SWT.NONE);
    }

    /**
     * Create text client for specified section
     * 
     * @param toolkit
     * @param section
     * @return - composite
     */
    public static Composite createSectionTextClient(FormToolkit toolkit,
            Section section) {
        return createSectionTextClient(toolkit, section, 1);
    }

    /**
     * Create toolbar on specified section
     * 
     * @param toolkit
     * @param section
     * @return - created toolbar
     */
    public static ToolBar createSectionToolbar(FormToolkit toolkit,
            Section section) {
        ToolBar toolbar = null;
        if (toolkit != null && section != null) {
            Composite toolbarArea = createSectionTextClient(toolkit, section);
            toolbar = new ToolBar(toolbarArea, SWT.FLAT);
        }
        return toolbar;
    }

}
