/**
 * Copyright (c) 2010 Perforce Software.  All rights reserved.
 */
package com.perforce.team.ui.mergequest.views.tasks;

import com.perforce.team.ui.P4UIUtils;
import com.perforce.team.ui.mergequest.editor.IBranchGraphEditor;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class IntegrateTaskView extends PageBookView {

    /**
     * View id
     */
    public static final String ID = "com.perforce.team.ui.mergequest.tasks"; //$NON-NLS-1$

    /**
     * @see org.eclipse.ui.part.PageBookView#createDefaultPage(org.eclipse.ui.part.PageBook)
     */
    @Override
    protected IPage createDefaultPage(PageBook book) {
        MessagePage page = new MessagePage();
        page.createControl(getPageBook());
        page.setMessage(Messages.IntegrateTaskView_DefaultMessage);
        return page;
    }

    /**
     * @see org.eclipse.ui.part.PageBookView#doCreatePage(org.eclipse.ui.IWorkbenchPart)
     */
    @Override
    protected PageRec doCreatePage(IWorkbenchPart part) {
        if (P4UIUtils.okToUse(getPageBook())) {
            IntegrateTaskPage page = new IntegrateTaskPage(
                    (IBranchGraphEditor) part);
            initPage(page);
            page.createControl(getPageBook());
            PageRec rec = new PageRec(part, page);
            return rec;
        } else {
            return null;
        }
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
        return part instanceof IBranchGraphEditor;
    }

}
