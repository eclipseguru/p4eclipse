/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.charts.annotate;

import com.perforce.team.ui.text.timelapse.TextTimeLapseEditor;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class TimeLapseStatsView extends PageBookView {

    /**
     * ID
     */
    public static final String ID = "com.perforce.team.ui.charts.TimeLapseStatsView"; //$NON-NLS-1$

    /**
     * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
     */
    @Override
    protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        page.createControl(getPageBook());
        page.setMessage(Messages.TimeLapseStatsView_ViewMessage);
        return page;
    }

    /**
     * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        TimeLapseStatsPage page = new TimeLapseStatsPage();
        if (part instanceof TextTimeLapseEditor) {
            page.setEditor((TextTimeLapseEditor) part);
        }
        initPage(page);
        page.createControl(getPageBook());
        PageRec rec = new PageRec(part, page);
        return rec;
    }

    /**
     * @see org.eclipse.ui.part.PageBookView#doDestroyPage(org.eclipse.ui.IWorkbenchPart,
     *      org.eclipse.ui.part.PageBookView.PageRec)
     */
    @Override
    protected void doDestroyPage(IWorkbenchPart part, PageRec pageRecord) {
        IPage page = pageRecord.page;
        page.dispose();
        pageRecord.dispose();
    }

    /**
     * @see org.eclipse.ui.part.PageBookView#getBootstrapPart()
     */
    @Override
    protected IWorkbenchPart getBootstrapPart() {
        IWorkbenchPage page = getSite().getPage();
        return page != null ? page.getActiveEditor() : null;
    }

    /**
     * @see org.eclipse.ui.part.PageBookView#isImportant(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    protected boolean isImportant(IWorkbenchPart part) {
        return part instanceof TextTimeLapseEditor;
    }

}
