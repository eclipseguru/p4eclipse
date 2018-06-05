package com.perforce.p4eclipse.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Perspective implements IPerspectiveFactory {

    /**
     * @see org.eclipse.ui.IPerspectiveFactory#createInitialLayout(org.eclipse.ui.IPageLayout)
     */
    public void createInitialLayout(IPageLayout layout) {
        String editorArea = layout.getEditorArea();
        layout.setEditorAreaVisible(true);
        layout.addView("com.perforce.team.ui.DepotView", IPageLayout.LEFT,
                0.25f, editorArea);

        IFolderLayout bottomLeft = layout.createFolder("bottomLeft",
                IPageLayout.BOTTOM, 0.5f, "com.perforce.team.ui.DepotView");
        bottomLeft.addView("com.perforce.team.ui.PendingChangelistView");
        bottomLeft.addView("org.eclipse.ui.views.ResourceNavigator");

        IFolderLayout bottomRight = layout.createFolder(
                "bottomRight", IPageLayout.BOTTOM, 0.75f, editorArea); //$NON-NLS-1$
        bottomRight.addView("com.perforce.team.ui.mergequest.tasks");
        bottomRight.addView("com.perforce.team.ui.charts.AnnotateChartView");
        bottomRight.addView("com.perforce.team.ui.charts.TimeLapseStatsView");

        bottomRight.addView("com.perforce.team.ui.SubmittedChangelistView");
        bottomRight.addView("com.perforce.team.ui.ShelveView");
        bottomRight.addView("org.eclipse.team.sync.views.SynchronizeView");
        bottomRight.addView("org.eclipse.team.ui.GenericHistoryView");
        bottomRight.addView("com.perforce.team.ui.ConsoleView");
        bottomRight.addPlaceholder("com.perforce.team.ui.LabelsView");
        bottomRight.addPlaceholder("com.perforce.team.ui.BranchesView");

        IFolderLayout topRight = layout.createFolder(
                "topRight", IPageLayout.RIGHT, 0.60f, editorArea); //$NON-NLS-1$
        topRight.addView("org.eclipse.mylyn.tasks.ui.views.tasks");
        topRight.addView("org.eclipse.mylyn.tasks.ui.views.repositories");
        topRight.addView("org.eclipse.ui.views.ContentOutline");
    }
}
