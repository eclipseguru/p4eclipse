/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.text.timelapse.perspective;

import com.perforce.team.ui.shelve.ShelveView;
import com.perforce.team.ui.views.ConsoleView;
import com.perforce.team.ui.views.DepotView;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TimeLapsePerspective implements IPerspectiveFactory {

    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        // Get the editor area.
        String editorArea = layout.getEditorArea();

        IFolderLayout topLeft = layout.createFolder(
                "topLeft", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
        topLeft.addView(DepotView.VIEW_ID);

        IFolderLayout bottomLeft = layout.createFolder(
                "bottomLeft", IPageLayout.BOTTOM, 0.50f, "topLeft"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

        IFolderLayout topRight = layout.createFolder(
                "topRight", IPageLayout.RIGHT, 0.70f, editorArea); //$NON-NLS-1$
        topRight.addView(IPageLayout.ID_OUTLINE);

        IFolderLayout bottomRight = layout.createFolder(
                "bottomRight", IPageLayout.BOTTOM, 0.66f, editorArea); //$NON-NLS-1$
        bottomRight.addView("org.eclipse.team.ui.GenericHistoryView"); //$NON-NLS-1$
        bottomRight.addView(ConsoleView.VIEW_ID);
        bottomRight.addView(SubmittedView.VIEW_ID);
        bottomRight.addView(ShelveView.VIEW_ID);
    }

}
