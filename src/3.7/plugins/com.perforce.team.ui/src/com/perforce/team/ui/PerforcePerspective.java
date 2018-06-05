package com.perforce.team.ui;

/*
 * Copyright (c) 2003 Perforce Software.  All rights reserved.
 *
 */

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

import com.perforce.team.ui.branches.BranchesView;
import com.perforce.team.ui.labels.LabelsView;
import com.perforce.team.ui.shelve.ShelveView;
import com.perforce.team.ui.streams.StreamsView;
import com.perforce.team.ui.views.ConsoleView;
import com.perforce.team.ui.views.DepotView;
import com.perforce.team.ui.views.JobView;
import com.perforce.team.ui.views.PendingView;
import com.perforce.team.ui.views.SubmittedView;

/**
 * Perforce perspective
 */
public class PerforcePerspective implements IPerspectiveFactory {

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
        bottomLeft.addView(PendingView.VIEW_ID);

        IFolderLayout bottomRight = layout.createFolder(
                "bottomRight", IPageLayout.BOTTOM, 0.66f, editorArea); //$NON-NLS-1$
        bottomRight.addView(ConsoleView.VIEW_ID);
        bottomRight.addView(JobView.VIEW_ID);
        bottomRight.addView("org.eclipse.team.ui.GenericHistoryView"); //$NON-NLS-1$
        bottomRight.addView(SubmittedView.VIEW_ID);
        bottomRight.addView(LabelsView.VIEW_ID);
        bottomRight.addView(BranchesView.VIEW_ID);
        bottomRight.addView(ShelveView.VIEW_ID);
        bottomRight.addView(StreamsView.VIEW_ID);
    }
}
