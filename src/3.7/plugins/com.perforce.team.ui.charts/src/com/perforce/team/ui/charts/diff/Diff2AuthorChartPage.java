/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.p4java.core.IFileDiff.Status;
import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.core.folder.IP4DiffFile;
import com.perforce.team.core.p4java.IP4Revision;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;
import com.perforce.team.ui.folder.diff.model.FileEntry;
import com.perforce.team.ui.folder.diff.model.IFolderDiffListener;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Diff2AuthorChartPage extends Diff2ChartPage implements
        IFolderDiffListener {

    /**
     * @param editor
     * @param diffPage
     */
    public Diff2AuthorChartPage(FormEditor editor, FolderDiffPage diffPage) {
        super(
                editor,
                "diff2AuthorChartPage", Messages.Diff2AuthorChartPage_ChartTitle, diffPage); //$NON-NLS-1$
    }

    private String getAuthor(Object adaptable) {
        IP4Revision revision = P4CoreUtils
                .convert(adaptable, IP4Revision.class);
        if (revision != null) {
            String author = revision.getAuthor();
            if (author != null) {
                return author;
            }
        }
        return null;
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#generateChartData(com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData,
     *      com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData)
     */
    @Override
    public void generateChartData(ChartData diffChartData,
            ChartData uniqueChartData) {

        for (FileEntry entry : container.getEntries()) {
            IP4DiffFile file = entry.getFile();
            Status status = file.getDiff().getStatus();
            if (status == Status.CONTENT && file.isFile1()) {
                String author = getAuthor(file);
                if (author != null) {
                    diffChartData.get(author).increment();
                    uniqueChartData.get(author);
                }
                author = getAuthor(file.getPair());
                if (author != null) {
                    diffChartData.get(author).increment();
                    uniqueChartData.get(author);
                }
            } else if (status == Status.RIGHT_ONLY
                    || status == Status.LEFT_ONLY) {
                String author = getAuthor(file);
                if (author != null) {
                    uniqueChartData.get(author).increment();
                    diffChartData.get(author);
                }
            }
        }

    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#updateSectionDescriptions(org.eclipse.ui.forms.widgets.Section,
     *      org.eclipse.ui.forms.widgets.Section)
     */
    @Override
    protected void updateSectionDescriptions(Section diffSection,
            Section uniqueSection) {
        diffSection
                .setDescription(Messages.Diff2AuthorChartPage_ContentDescription);
        uniqueSection
                .setDescription(Messages.Diff2AuthorChartPage_UniqueDescription);
    }

}
