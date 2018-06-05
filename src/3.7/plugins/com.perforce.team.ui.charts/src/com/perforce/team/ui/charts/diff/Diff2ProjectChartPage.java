/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.ui.changelists.Folder.Type;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;
import com.perforce.team.ui.folder.diff.model.GroupedDiffContainer.GroupFolder;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Diff2ProjectChartPage extends Diff2ChartPage {

    /**
     * @param editor
     * @param diffPage
     */
    public Diff2ProjectChartPage(FormEditor editor, FolderDiffPage diffPage) {
        super(
                editor,
                "diff2ProjectChartPage", Messages.Diff2ProjectChartPage_ChartTitle, diffPage); //$NON-NLS-1$
    }

    private void processProjects(Object[] folders, String projectName,
            ChartData diff, ChartData unique) {
        for (Object entry : folders) {
            if (entry instanceof GroupFolder) {
                GroupFolder folder = (GroupFolder) entry;
                if (folder.isProject()) {
                    processProjects(folder.getChildren(folder),
                            folder.getLabel(folder), diff, unique);
                } else {
                    processProjects(folder.getChildren(folder), projectName,
                            diff, unique);
                }
            } else if (entry instanceof IP4DiffFile) {
                IP4DiffFile file = (IP4DiffFile) entry;
                Status status = file.getDiff().getStatus();
                if (projectName != null) {
                    if (Status.CONTENT == status && file.isFile1()) {
                        diff.get(projectName).increment();
                        unique.get(projectName);
                    } else if (Status.LEFT_ONLY == status
                            || Status.RIGHT_ONLY == status) {
                        diff.get(projectName);
                        unique.get(projectName).increment();
                    }
                }

            }
        }
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#generateChartData(com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData,
     *      com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData)
     */
    @Override
    protected void generateChartData(ChartData diffChartData,
            ChartData uniqueChartData) {
        Object[] folders = container.getContainer(Status.LEFT_ONLY)
                .getElements(Type.TREE);

        processProjects(folders, Messages.Diff2ChartPage_NoProject,
                diffChartData, uniqueChartData);

        folders = container.getContainer(Status.RIGHT_ONLY).getElements(
                Type.TREE);

        processProjects(folders, Messages.Diff2ChartPage_NoProject,
                diffChartData, uniqueChartData);
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#updateSectionDescriptions(org.eclipse.ui.forms.widgets.Section,
     *      org.eclipse.ui.forms.widgets.Section)
     */
    @Override
    protected void updateSectionDescriptions(Section diffSection,
            Section uniqueSection) {
        diffSection
                .setDescription(Messages.Diff2ProjectChartPage_ContentDescription);
        uniqueSection
                .setDescription(Messages.Diff2ProjectChartPage_UniqueDescription);
    }

}
