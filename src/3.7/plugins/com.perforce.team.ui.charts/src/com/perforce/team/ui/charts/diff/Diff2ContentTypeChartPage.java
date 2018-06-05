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

import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Diff2ContentTypeChartPage extends Diff2ChartPage implements
        IFolderDiffListener {

    /**
     * @param editor
     * @param diffPage
     */
    public Diff2ContentTypeChartPage(FormEditor editor, FolderDiffPage diffPage) {
        super(
                editor,
                "diff2ContentTypeChartPage", Messages.Diff2ContentTypeChartPage_ChartTitle, diffPage); //$NON-NLS-1$
    }

    /**
     * @see com.perforce.team.ui.charts.diff.Diff2ChartPage#generateChartData(com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData,
     *      com.perforce.team.ui.charts.diff.Diff2ChartPage.ChartData)
     */
    @Override
    protected void generateChartData(ChartData diffChartData,
            ChartData uniqueChartData) {
        IContentTypeManager contentTypes = Platform.getContentTypeManager();
        for (FileEntry entry : container.getEntries()) {
            IP4DiffFile file = entry.getFile();
            Status status = file.getDiff().getStatus();
            if (status != Status.IDENTICAL) {
                String typeName = null;
                String fileName = file.getName();
                IContentType type = contentTypes.findContentTypeFor(fileName);
                if (type != null) {
                    typeName = type.getName();
                }
                if (typeName == null && fileName != null) {
                    int suffixStart = fileName.lastIndexOf('.');
                    if (suffixStart != -1) {
                        if (suffixStart + 1 < fileName.length()) {
                            typeName = MessageFormat
                                    .format(Messages.Diff2ContentTypeChartPage_ExtensionFile,
                                            fileName.substring(suffixStart + 1,
                                                    suffixStart + 2)
                                                    .toUpperCase(), fileName
                                                    .substring(suffixStart + 2));
                        } else {
                            typeName = MessageFormat
                                    .format(Messages.Diff2ContentTypeChartPage_FileType,
                                            fileName.substring(suffixStart));
                        }
                    }
                }
                if (typeName == null) {
                    IP4Revision revision = P4CoreUtils.convert(file,
                            IP4Revision.class);
                    if (revision != null) {
                        String p4Type = revision.getType();
                        if (p4Type != null) {
                            if (p4Type.length() > 1) {
                                p4Type = p4Type.substring(0, 1).toUpperCase()
                                        + p4Type.substring(1);
                            }
                            typeName = MessageFormat
                                    .format(Messages.Diff2ContentTypeChartPage_FileType,
                                            p4Type);
                        }
                    }
                }
                if (typeName == null) {
                    typeName = Messages.Diff2ChartPage_Unknown;
                }
                if (status == Status.CONTENT && file.isFile1()) {
                    diffChartData.get(typeName).increment();
                    uniqueChartData.get(typeName);
                } else if (status == Status.RIGHT_ONLY
                        || status == Status.LEFT_ONLY) {
                    diffChartData.get(typeName);
                    uniqueChartData.get(typeName).increment();
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
                .setDescription(Messages.Diff2ContentTypeChartPage_ContentDescription);
        uniqueSection
                .setDescription(Messages.Diff2ContentTypeChartPage_UniqueDescription);
    }

}
