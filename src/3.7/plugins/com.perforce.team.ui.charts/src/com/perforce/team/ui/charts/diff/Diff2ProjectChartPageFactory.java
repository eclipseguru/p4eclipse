/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.diff;

import com.perforce.team.core.P4CoreUtils;
import com.perforce.team.ui.editor.P4PageFactoryAdapter;
import com.perforce.team.ui.folder.diff.editor.FolderDiffPage;

import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class Diff2ProjectChartPageFactory extends P4PageFactoryAdapter {

    /**
     * @see com.perforce.team.ui.editor.P4PageFactoryAdapter#createPage(org.eclipse.ui.forms.editor.FormEditor)
     */
    @Override
    public IFormPage createPage(FormEditor parent) {
        IFormPage page = null;
        FolderDiffPage diffPage = P4CoreUtils.convert(parent,
                FolderDiffPage.class);
        if (diffPage != null) {
            page = new Diff2ProjectChartPage(parent, diffPage);
        }
        return page;
    }

}