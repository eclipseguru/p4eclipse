/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.perspective;

import com.perforce.team.ui.branches.BranchesView;
import com.perforce.team.ui.mergequest.views.tasks.IntegrateTaskView;
import com.perforce.team.ui.views.ConsoleView;
import com.perforce.team.ui.views.DepotView;
import com.perforce.team.ui.views.PendingView;
import com.perforce.team.ui.views.SubmittedView;

import org.eclipse.gef.ui.views.palette.PaletteView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class BranchGraphPerspective implements IPerspectiveFactory {

    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        // Get the editor area.
        String editorArea = layout.getEditorArea();

        IFolderLayout topLeft = layout.createFolder(
                "topLeft", IPageLayout.LEFT, 0.25f, editorArea); //$NON-NLS-1$
        topLeft.addView(PaletteView.ID);

        IFolderLayout bottomLeft = layout.createFolder(
                "bottomLeft", IPageLayout.BOTTOM, 0.50f, "topLeft"); //$NON-NLS-1$ //$NON-NLS-2$
        bottomLeft.addView(DepotView.VIEW_ID);
        bottomLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);

        IFolderLayout bottom = layout.createFolder(
                "bottom", IPageLayout.BOTTOM, 0.66f, editorArea); //$NON-NLS-1$
        bottom.addView(IntegrateTaskView.ID);
        bottom.addView(ConsoleView.VIEW_ID);
        bottom.addView(BranchesView.VIEW_ID);
        bottom.addView(SubmittedView.VIEW_ID);
        bottom.addView(PendingView.VIEW_ID);

        IFolderLayout topRight = layout.createFolder(
                "topRight", IPageLayout.RIGHT, 0.70f, editorArea); //$NON-NLS-1$
        topRight.addView(IPageLayout.ID_OUTLINE);

        IFolderLayout lowerRight = layout.createFolder(
                "lowerRight", IPageLayout.BOTTOM, 0.50f, "topRight"); //$NON-NLS-1$ //$NON-NLS-2$
        lowerRight.addView(IPageLayout.ID_PROP_SHEET);

    }
}
