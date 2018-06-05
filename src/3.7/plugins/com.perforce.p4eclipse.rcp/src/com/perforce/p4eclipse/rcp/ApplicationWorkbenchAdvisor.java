package com.perforce.p4eclipse.rcp;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;

/**
 * @author Kevin Sawicki (ksawicki@perforce.com)
 */
public class ApplicationWorkbenchAdvisor extends IDEWorkbenchAdvisor {

    private static final String PERSPECTIVE_ID = "com.perforce.p4eclipse.rcp.perspective"; //$NON-NLS-1$

    /**
     * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
     */
    @Override
    public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
            IWorkbenchWindowConfigurer configurer) {
        return new ApplicationWorkbenchWindowAdvisor(this, configurer);
    }

    /**
     * @see org.eclipse.ui.application.WorkbenchAdvisor#getInitialWindowPerspectiveId()
     */
    @Override
    public String getInitialWindowPerspectiveId() {
        return PERSPECTIVE_ID;
    }
}
