package com.perforce.p4eclipse.rcp;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor;
import org.eclipse.ui.internal.ide.application.IDEWorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends
        IDEWorkbenchWindowAdvisor {

    /**
     * 
     * @param wbAdvisor
     * @param configurer
     */
    public ApplicationWorkbenchWindowAdvisor(IDEWorkbenchAdvisor wbAdvisor,
            IWorkbenchWindowConfigurer configurer) {
        super(wbAdvisor, configurer);
    }

    /**
     * @see org.eclipse.ui.internal.ide.application.IDEWorkbenchWindowAdvisor#preWindowOpen()
     */
    @Override
    public void preWindowOpen() {
        super.preWindowOpen();
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        Display display = Display.getCurrent();
        if (display != null) {
            int height = display.getBounds().height;
            int width = display.getBounds().width;
            if (height > 0 && width > 0) {
                configurer.setInitialSize(new Point(width, height));
            }
        }
        configurer.setShowPerspectiveBar(true);
        configurer.setShowStatusLine(true);
        configurer.setShowCoolBar(true);
        configurer.setShowProgressIndicator(true);
    }
}
